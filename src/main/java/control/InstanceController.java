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
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstanceController {
    private static final Logger logger = Logger.getLogger(InstanceController.class.getName());


    private String localPathBk;
    private Git git;

    private Repository repository;

    public InstanceController(String projName) throws IOException {
        this.localPathBk = "C:\\Users\\vlrbr\\Desktop\\Testing\\" + projName;
        this.git = Git.open(new File(this.localPathBk));
        this.repository = this.git.getRepository();
    }






    public int nAuthCounter(FinalInstance finalInstance) {
        int ret = 0;
        List<String> authors = new ArrayList<>();
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
        try (RevWalk walk = new RevWalk(this.repository)) {
            RevTree tree = walk.parseCommit(commit.getId()).getTree();
            try (TreeWalk treeWalk = new TreeWalk(this.repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(filePath));

                if (!treeWalk.next()) {
                    throw new IllegalStateException("Did not find expected file " + filePath);
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                try (ObjectReader reader = this.repository.newObjectReader()) {
                    ObjectLoader loader = reader.open(objectId);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    loader.copyTo(byteArrayOutputStream);

                    String content = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);

                    int linesOfCode = 0;
                    try (Scanner scanner = new Scanner(content)) {
                        while (scanner.hasNextLine()) {
                            String res = scanner.nextLine().trim();
                            if(linesOfCode%1000 == 0){
                                logger.info(res);
                            }

                            linesOfCode++;

                        }
                    }
                    return linesOfCode;
                }
            }
        } catch(MissingObjectException mob){
            Logger loggerj = Logger.getLogger(JiraController.class.getName());
            String out ="MissingObjectException";
            loggerj.log(Level.INFO, out);

        }
        return 0;
    }

    public List<FinalInstance> locRepBis(List<List<FinalInstance>> instDividedByName, List<FinalInstance> finalInstanceList3) {
        for (List<FinalInstance> li : instDividedByName) {
            for (FinalInstance i : li) {
                if (shouldUpdateSize(i, li)) {
                    int ind = findInstanceIndex(finalInstanceList3, i);
                    int curr = li.indexOf(i);
                    if (ind != -1 && curr > 0) {
                        updateSize(finalInstanceList3, li, ind, curr);
                    }
                }
            }
        }
        return finalInstanceList3;
    }

    private boolean shouldUpdateSize(FinalInstance instance, List<FinalInstance> instanceList) {
        return instance.getSize() == 0 && instanceList.size() > 1;
    }

    private int findInstanceIndex(List<FinalInstance> finalInstanceList, FinalInstance instance) {
        for (FinalInstance j : finalInstanceList) {
            if (j.getName().equals(instance.getName()) && j.getVersion().equals(instance.getVersion())) {
                return finalInstanceList.indexOf(j);
            }
        }
        return -1;
    }

    private void updateSize(List<FinalInstance> finalInstanceList, List<FinalInstance> instanceList, int instanceIndex, int currentIndex) {
        int previousSize = instanceList.get(currentIndex - 1).getSize();
        finalInstanceList.get(instanceIndex).setSize(previousSize);
        instanceList.get(currentIndex).setSize(previousSize);
    }



    //files that have 0 commits will have loc = 0: we fix that by forcing loc of files x.y.z at loc x.y.z-1
    public List<FinalInstance> locRepairer(List<FinalInstance> finalInstanceList) {

        List<FinalInstance> finalInstanceList2 = finalInstanceList;
        List<FinalInstance> finalInstanceList3 = new ArrayList<>();
        finalInstanceList3.addAll(finalInstanceList);
        List<List<FinalInstance>> instDividedByName = new ArrayList<>();
        do {
            List<FinalInstance> temp = new ArrayList<>();
            FinalInstance curr = finalInstanceList.get(0);
            for (FinalInstance i : finalInstanceList) {
                if (i.getName().equals(curr.getName())) {
                    temp.add(i);
                }
            }
            instDividedByName.add(temp);
            finalInstanceList2.removeAll(temp);

        } while (!finalInstanceList2.isEmpty());


        return locRepBis(instDividedByName, finalInstanceList3);

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

            for(RevCommit rc : i.getJavafile().getCommitList()){
                for(Bug b : bugs){
                    if(rc.getShortMessage().contains(b.getKey()+":")  || rc.getShortMessage().contains(b.getKey()+" ")){ //jira tag = shortmessage
                        List<FinalInstance> temp = foo(finalInstances, b.getAv(), i.getName());
                        buggyFinalInstances.addAll(temp);
                    }
                }
            }
        }
        return buggyFinalInstances;
    }

    //we say that a class is buggy if is touched by a commit that reports a jira issue



public LinesMetricCollector getLinesMetrics(FinalInstance i, Version first) throws IOException {
    int removedLines = 0;
    int addedLines = 0; //addedLoc
    int maxLOC = 0;
    double avgLOC = 0;
    int churn = 0;
    int maxChurn = 0;
    double avgChurn = 0;

    List<Integer> counter = new ArrayList<>();

    for (RevCommit comm : i.getJavafile().getCommitList()) {
        if (comm.getParentCount() > 0) {
            RevCommit parentComm = comm.getParent(0);
            try (DiffFormatter diffFormatter = createDiffFormatter()) {
                List<DiffEntry> diffs = diffFormatter.scan(parentComm.getTree(), comm.getTree());
                for (DiffEntry entry : diffs) {
                    if (entry.getNewPath().equals(i.getName())) {
                        Metrics metrics = calculateMetrics(i, first, comm, diffFormatter, entry);
                        addedLines += metrics.addedLines;
                        removedLines += metrics.removedLines;
                        churn += metrics.churn;
                        counter.add(metrics.tempAdd);

                        maxLOC = Math.max(maxLOC, metrics.tempAdd);
                        maxChurn = Math.max(maxChurn, metrics.churn);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException | MissingObjectException e) {
                logException(e);
            }
        }
    }

    if (!counter.isEmpty()) {
        avgLOC = (double) addedLines / counter.size();
        avgChurn = (double) churn / counter.size();
    }

    return new LinesMetricCollector(removedLines, addedLines, maxLOC, avgLOC, churn, maxChurn, avgChurn);
}

    private DiffFormatter createDiffFormatter() {
        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(this.repository);
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
        return diffFormatter;
    }

    private Metrics calculateMetrics(FinalInstance i, Version first, RevCommit comm, DiffFormatter diffFormatter, DiffEntry entry) throws IOException {
        int tempAdd = 0;
        int tempRem = 0;
        int currentDiff = 0;

        if (isFirstCommitOfVersion(i, first, comm)) {
            tempAdd = countLinesOfCode(comm, i.getName());
        } else {
            tempAdd = getAddedLines(diffFormatter, entry);
            tempRem = getDeletedLines(diffFormatter, entry);
        }

        currentDiff = Math.abs(tempAdd - tempRem);

        return new Metrics(tempAdd, tempRem, currentDiff);
    }

    private boolean isFirstCommitOfVersion(FinalInstance i, Version first, RevCommit comm) {
        return i.getVersion().equals(first.getName()) && i.getJavafile().getCommitList().indexOf(comm) == 0;
    }

    private void logException(Exception e) {
        Logger loggerjj = Logger.getLogger(JiraController.class.getName());
        loggerjj.log(Level.INFO, e.getClass().getSimpleName(), e);
    }

    private static class Metrics {
        int addedLines;
        int removedLines;
        int churn;
        int tempAdd;

        Metrics(int tempAdd, int tempRem, int churn) {
            this.addedLines = tempAdd;
            this.removedLines = tempRem;
            this.churn = churn;
            this.tempAdd = tempAdd;
        }
    }



}

