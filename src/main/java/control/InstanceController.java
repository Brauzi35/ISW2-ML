package control;

import model.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InstanceController {

    static String localPathBk;
    static Git git;

    static Repository repository;

    public InstanceController(String projName) throws IOException {
        localPathBk = "C:\\Users\\vlrbr\\Desktop\\" + projName;
        try {
            git = Git.open(new File(localPathBk));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        repository = git.getRepository();
    }






    public int nAuthCounter(FinalInstance finalInstance) {
        int ret = 0;
        List<String> authors = new ArrayList<>();
        List<RevCommit> rcl = new ArrayList<>();
        for (RevCommit rc : finalInstance.getJavafile().getCommitList()) {
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
                        linesOfCode++;

                    }
                    return linesOfCode;
                }
            }
        } catch(MissingObjectException mob){

        }
        return 0;
    }




    //files that have 0 commits will have loc = 0: we fix that by forcing loc of files x.y.z at loc x.y.z-1
    public List<FinalInstance> locRepairer(List<FinalInstance> finalInstanceList, List<Version> versions) {

        List<FinalInstance> finalInstanceList2 = finalInstanceList;
        List<FinalInstance> finalInstanceList3 = new ArrayList<>();
        finalInstanceList3.addAll(finalInstanceList);
        List<List<FinalInstance>> instDividedByName = new ArrayList<>();
        do {
            List<FinalInstance> temp = new ArrayList<>();
            FinalInstance curr = finalInstanceList.get(0);
            //temp.add(curr);
            for (FinalInstance i : finalInstanceList) {
                if (i.getName().equals(curr.getName())) {
                    temp.add(i);
                }
            }
            instDividedByName.add(temp);
            finalInstanceList2.removeAll(temp);

        } while (!finalInstanceList2.isEmpty());


            for (List<FinalInstance> li : instDividedByName) {
                for (FinalInstance i : li) {

                    if (i.getSize() == 0 && li.size() > 1) { //if loc = 0 and is not the first version
                        int ind = -1;
                        for (FinalInstance j : finalInstanceList3) {


                            if (j.getName().equals(i.getName()) && j.getVersion().equals(i.getVersion())) {

                                ind = finalInstanceList3.indexOf(j);
                            }
                        }
                        int curr = li.indexOf(i);
                        if (ind != -1 && curr > 0) {

                            finalInstanceList3.get(ind).setSize(li.get(curr - 1).getSize());
                            i.setSize(li.get(curr - 1).getSize());
                        }

                    }
                }
            }

        return finalInstanceList3;

        }




    private int getAddedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {

        int addedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            addedLines += edit.getEndB() - edit.getBeginB();

        }
        return addedLines;

    }

    private int getDeletedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {

        int deletedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            deletedLines += edit.getEndA() - edit.getBeginA();

        }
        return deletedLines;

    }

    public List<FinalInstance> foo(List<FinalInstance> finalInstances, List<Version> av, String filename){
        List<FinalInstance> ret = new ArrayList<>();
        for(FinalInstance i : finalInstances){
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

    public List<FinalInstance> isBuggy2(List<FinalInstance> finalInstances, List<Bug> bugs){
        List<FinalInstance> buggyFinalInstances = new ArrayList<>();
        for(FinalInstance i : finalInstances){
            //System.out.println(i.getName());
            for(RevCommit rc : i.getJavafile().getCommitList()){
                for(Bug b : bugs){
                    if(rc.getShortMessage().contains(b.getKey()+":")  || rc.getShortMessage().contains(b.getKey()+" ")){ //jira tag = shortmessage
                        //System.out.println("bug key= " + b.getKey() + " short message: " + rc.getShortMessage());
                        List<FinalInstance> temp = foo(finalInstances, b.getAv(), i.getName());
                        buggyFinalInstances.addAll(temp);
                    }
                }
            }
        }
        return buggyFinalInstances;
    }

    //we say that a class is buggy if is touched by a commit that reports a jira issue

    public LinesMetricCollector getLinesMetrics(FinalInstance i, Version first) throws IOException{
            int removedLines = 0;
            int addedLines = 0; //addedLoc
            int maxLOC = 0;
            double avgLOC = 0;
            int churn = 0;
            int maxChurn = 0;
            double avgChurn = 0;

            List<Integer> counter = new ArrayList<>();

            for(RevCommit comm : i.getJavafile().getCommitList()) {
                RevCommit parentComm = comm.getParent(0);
                if(comm.getParentCount()>0) {

                    try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {



                        diffFormatter.setRepository(repository);
                        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);

                        List<DiffEntry> diffs = diffFormatter.scan(parentComm.getTree(), comm.getTree());
                        System.out.println(i.getName() + " " + i.getVersion());
                        for (DiffEntry entry : diffs) {
                            if (entry.getNewPath().equals(i.getName())) {
                                int tempAdd = 0;
                                int tempRem = 0;
                                if (i.getVersion().equals(first.getName()) && i.getJavafile().getCommitList().indexOf(comm) == 0) {
                                    //cambiare
                                    int tempcount = countLinesOfCode(comm, i.getName());
                                    addedLines += tempcount;
                                    tempAdd = tempcount;
                                    counter.add(tempcount);
                                } else {
                                    tempAdd = getAddedLines(diffFormatter, entry);
                                    tempRem = getDeletedLines(diffFormatter, entry);
                                    counter.add(tempAdd);

                                    addedLines += tempAdd;
                                    removedLines += tempRem;
                                }


                                int currentLOC = tempAdd;
                                int currentDiff = Math.abs(tempAdd - tempRem);


                                churn = churn + currentDiff;
                                System.out.println(churn);

                                if (currentLOC > maxLOC) {
                                    maxLOC = currentLOC;
                                }
                                if (currentDiff > maxChurn) {
                                    maxChurn = currentDiff;
                                }


                            }

                        }

                        System.out.println(counter);


                    } catch (ArrayIndexOutOfBoundsException e) {
                        //commit has no parents: skip this commit, return an empty list and go on

                    } catch (MissingObjectException moe){
                        //commit has no parents
                    }
                }
            }
        if(counter.size()!= 0) {
            avgLOC = (double)addedLines/counter.size();
            avgChurn = (double)churn/counter.size();

        }
            return new LinesMetricCollector(removedLines, addedLines, maxLOC, avgLOC, churn, maxChurn, avgChurn);
        }






}

