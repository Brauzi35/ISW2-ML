package control;

import model.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.RawParseUtils;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InstanceController {

    static final String localPath = "C:\\Users\\vlrbr\\Desktop\\bookkeeper";
    static Git git;

    static {
        try {
            git = Git.open(new File(localPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Repository repository = git.getRepository();

    public InstanceController() throws IOException {
    }

    public int nAuthCounter(Instance instance) {
        int ret = 0;
        List<String> authors = new ArrayList<>();
        List<RevCommit> rcl = new ArrayList<>();
        for (RevCommit rc : instance.getJavafile().getCommitList()) {
            String author = rc.getAuthorIdent().getName();
            if (!authors.contains(author)) {
                authors.add(author);
            }
        }
        ret = authors.size();
        return ret;
    }

    //size calculator
    public int countLinesOfCode(RevCommit commit, String filePath) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            RevTree tree = walk.parseCommit(commit.getId()).getTree();
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(filePath));

                if (!treeWalk.next()) {
                    throw new IllegalStateException("Did not find expected file " + filePath);
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                try (ObjectReader reader = repository.newObjectReader()) {
                    ObjectLoader loader = reader.open(objectId);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    loader.copyTo(byteArrayOutputStream);

                    String content = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);

                    int linesOfCode = 0;
                    Scanner scanner = new Scanner(content);
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        //if (!line.isEmpty() && !line.startsWith("//")) {
                        linesOfCode++;
                        //}
                    }
                    return linesOfCode;
                }
            }
        }
    }


    //files that have 0 commits will have loc = 0: we fix that by forcing loc of files x.y.z at loc x.y.z-1
    public List<Instance> locRepairer(List<Instance> instanceList, List<Version> versions) {

        List<Instance> instanceList2 = instanceList;
        List<Instance> instanceList3 = new ArrayList<>();
        instanceList3.addAll(instanceList); //era questo il problema
        List<List<Instance>> instDividedByName = new ArrayList<>();
        do {
            List<Instance> temp = new ArrayList<>();
            Instance curr = instanceList.get(0);
            //temp.add(curr);
            for (Instance i : instanceList) {
                if (i.getName().equals(curr.getName())) {
                    temp.add(i);
                }
            }
            instDividedByName.add(temp);
            instanceList2.removeAll(temp);

        } while (!instanceList2.isEmpty());


            for (List<Instance> li : instDividedByName) {
                for (Instance i : li) {

                    if (i.getSize() == 0 && li.size() > 1) { //if loc = 0 and is not the first version
                        int ind = -1;

                        System.out.println(instanceList3.size());
                        for (Instance j : instanceList3) {


                            if (j.getName().equals(i.getName()) && j.getVersion().equals(i.getVersion())) {

                                ind = instanceList3.indexOf(j);
                            }
                        }
                        int curr = li.indexOf(i);
                        if (ind != -1) {

                            instanceList3.get(ind).setSize(li.get(curr - 1).getSize());
                            i.setSize(li.get(curr - 1).getSize());
                        }

                    }
                }
            }

        return instanceList3;

        }




    private int getAddedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {

        int addedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            addedLines += edit.getEndA() - edit.getBeginA();

        }
        return addedLines;

    }

    private int getDeletedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {

        int deletedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            deletedLines += edit.getEndB() - edit.getBeginB();

        }
        return deletedLines;

    }

    public List<Instance> foo(List<Instance> instances, List<Version> av, String filename){
        List<Instance> ret = new ArrayList<>();
        for(Instance i : instances){
            if(i.getJavafile().getFilename().equals(filename)){
               for(Version v : av){
                   if(v.getName().equals(i.getVersion())){
                       ret.add(i);
                   }
               }
            }
        }
        return ret;
    }

    public List<Instance> isBuggy2(List<Instance> instances, List<Bug> bugs){
        List<Instance> buggyInstances = new ArrayList<>();
        for(Instance i : instances){
            //System.out.println(i.getName());
            for(RevCommit rc : i.getJavafile().getCommitList()){
                for(Bug b : bugs){
                    if(rc.getShortMessage().contains(b.getKey()+":")  || rc.getShortMessage().contains(b.getKey()+" ")){ //jira tag = shortmessage
                        //System.out.println("bug key= " + b.getKey() + " short message: " + rc.getShortMessage());
                        List<Instance> temp = foo(instances, b.getAv(), i.getName());
                        buggyInstances.addAll(temp);
                    }
                }
            }
        }
        return buggyInstances;
    }

    //we say that a class is buggy if is touched by a commit that reports a jira issue
    public String isBuggy(Instance i, List<Bug> av_bugs){
        String yes = "Yes";
        String no = "No";

        for(RevCommit rc : i.getJavafile().getCommitList()){
            for(Bug b : av_bugs){
                boolean fveqov = false;
                if(b.getOv().equals(b.getFv())){
                    //i.getJavafile().getVersion().getIndex() < b.getFv().getIndex()
                    fveqov = true;
                }

                if(rc.getShortMessage().contains(b.getKey())){
                    System.out.println(""+ i.getName()+" entrato nel primo if, lo short message contiene " + b.getKey());
                    if(i.getJavafile().getVersion().getIndex() >= b.getIv().getIndex() && i.getJavafile().getVersion().getIndex() <= b.getOv().getIndex() && fveqov){

                        //System.out.println("entrato nel secondo if, versione beccata");
                        return yes;
                    } else if (i.getJavafile().getVersion().getIndex() >= b.getIv().getIndex() && i.getJavafile().getVersion().getIndex() <= b.getFv().getIndex() && !fveqov) {
                        return yes;
                    }
                    /*
                    for(Version c : b.getAv()){
                        System.out.println("version instance: " + i.getVersion() +", version av: " + c.getName());
                        if(i.getVersion().contains(c.getName())){
                            System.out.println("entrato nel secondo if, versione beccata");
                            return yes;
                        }
                    }

                     */
                }
            }
        }

        return no;

    }


    public LinesMetricCollector getLinesMetrics(Instance i) throws IOException{
            int removedLines = 0;
            int addedLines = 0; //addedLoc
            int maxLOC = 0;
            double avgLOC = 0;
            int churn = 0;
            int maxChurn = 0;
            double avgChurn = 0;

            List<Integer> counter = new ArrayList<>();

            //int counter = 0;


            for(RevCommit comm : i.getJavafile().getCommitList()) {
                try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

                    RevCommit parentComm = comm.getParent(0);

                    diffFormatter.setRepository(repository);
                    diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);

                    List<DiffEntry> diffs = diffFormatter.scan(parentComm.getTree(), comm.getTree());
                    System.out.println(i.getName() + " " +i.getVersion());
                    for(DiffEntry entry : diffs) {
                        if(entry.getNewPath().equals(i.getName())) {
                            int tempAdd = 0;
                            int tempRem = 0;
                            if(i.getVersion().equals("4.0.0") && i.getJavafile().getCommitList().indexOf(comm) == 0){
                                //cambiare
                                int tempcount = countLinesOfCode(comm, i.getName());
                                addedLines+= tempcount;
                                tempAdd = tempcount;
                                counter.add(tempcount);
                            }
                            else {
                                tempAdd = getAddedLines(diffFormatter, entry);
                                tempRem = getDeletedLines(diffFormatter, entry);
                                counter.add(tempAdd);

                                addedLines += tempAdd;
                                removedLines += tempRem;
                            }


                            int currentLOC = tempAdd;
                            int currentDiff = Math.abs(tempAdd - tempRem);


                            churn = churn + currentDiff;

                            if(currentLOC > maxLOC) {
                                maxLOC = currentLOC;
                            }
                            if(currentDiff > maxChurn) {
                                maxChurn = currentDiff;
                            }




                        }

                    }




                } catch(ArrayIndexOutOfBoundsException e) {
                    //commit has no parents: skip this commit, return an empty list and go on

                }
            }
        if(counter.size()!= 0) {
            avgLOC = addedLines/counter.size();
            avgChurn = churn/counter.size();

        }
            return new LinesMetricCollector(removedLines, addedLines, maxLOC, avgLOC, churn, maxChurn, avgChurn);
        }






}

