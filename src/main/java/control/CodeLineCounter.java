package control;

import model.FinalInstance;
import model.JavaFile;
import model.LinesMetricCollector;
import model.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CodeLineCounter {
   private String localPath;

    public CodeLineCounter(String localPathpass) {
       this.localPath = localPathpass;
    }



    public List<RevCommit> retrieveAllCommits() throws RevisionSyntaxException, IOException {

        List<RevCommit> commitFinal = new ArrayList<>();

        try (Git git = Git.open(new File(this.localPath))) {

            Iterable<RevCommit> commits = git.log().all().call();
            //cosi prendo tutti i commit

            for (RevCommit commit : commits) {

                commitFinal.add(commit);


            }
        } catch (GitAPIException e) {
            Logger logger = Logger.getLogger(CodeLineCounter.class.getName());
            String out ="GitAPIException";
            logger.log(Level.INFO, out);
        }
        return commitFinal;
    }


    public static List<List<RevCommit>> commitsDivider(List<RevCommit> commits, List<Version> versions) {

        List<List<RevCommit>> returnList = new ArrayList<>();
        for (Version v : versions) {
            List<RevCommit> r = new ArrayList<>();
            for (RevCommit c : commits) {
                Instant instant = c.getCommitterIdent().getWhenAsInstant();
                LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                if (ldt.compareTo(v.getReleaseDate()) <= 0) {
                    r.add(c);
                }
            }
            commits.removeAll(r);
            returnList.add(r);
        }
        //commits not considered if committed after last release considered
        return returnList;
    }


    //need to merge all the files having same release and same name in a single file having all commits touching that file

    //each file needs a version: knowing that the first list in filesreworked represent the first version and so on
    //the versioning operation can be done as follows
    public static void fileListVersioner(List<List<JavaFile>> filesReworked, List<Version> versions) {
        int count = 0;
        List<List<JavaFile>> returnList = filesReworked;
        for (List<JavaFile> jfl : returnList) {
            for (JavaFile jf : jfl) {
                jf.setVersion(versions.get(count));
            }
            count++;
        }

    }


    public static void commitListOrderer(List<List<RevCommit>> llrc) {
        for (List<RevCommit> lrc : llrc) {
            Collections.sort(lrc, Comparator.comparingLong(RevCommit::getCommitTime));
        }
    }

    public List<JavaFile> getFilesNew(RevCommit commit) throws IOException {
        ObjectId treeId = commit.getTree().getId();
        Git git = Git.open(new File(this.localPath));
        Repository repository = git.getRepository();
        TreeWalk treeWalk = new TreeWalk(repository);

        treeWalk.reset(treeId);
        treeWalk.setRecursive(false);

        List<JavaFile> jfl = new ArrayList<>();
        while (treeWalk.next()) {
            if (treeWalk.isSubtree()) {
                treeWalk.enterSubtree();
            } else {
                if (treeWalk.getPathString().endsWith(".java") && !treeWalk.getPathString().contains("/test/")) {
                    String className = treeWalk.getPathString();
                    JavaFile jv = new JavaFile(className, null, new ArrayList<>());
                    jfl.add(jv);

                }
            }
        }
        return jfl;
    }

    public void commFilePairerBis(RevCommit rc, Git git, List<List<RevCommit>> dividedCommits, List<JavaFile> ijfl) throws IOException {
        if (!rc.equals(dividedCommits.get(0).get(0)) && rc.getParentCount()>0) {

            commFilePairerBisUnsmell(rc, git, dividedCommits, ijfl); //prova riduzione complessità


        }
    }

    public void commFilePairerBisUnsmell(RevCommit rc, Git git, List<List<RevCommit>> dividedCommits, List<JavaFile> ijfl) throws IOException {
        DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        formatter.setRepository(git.getRepository());
        formatter.setDiffComparator(RawTextComparator.DEFAULT);
        formatter.setDetectRenames(true);

        ObjectId commitId = rc.getId();
        RevCommit parent = rc.getParent(0);
        if (parent != null) {
            ObjectId parentId = parent.getId();
            List<DiffEntry> diffs = formatter.scan(parentId, commitId);
            for (DiffEntry diff : diffs) {
                for (JavaFile jf : ijfl) {
                    if (jf.getFilename().equals(diff.getNewPath())) {
                        List<RevCommit> fileCommits = jf.getCommitList();
                        fileCommits.add(rc);
                        jf.setCommitList(fileCommits);
                        break;
                    }
                }

            }
        }
    }

    public void commitsFilePairer(List<List<JavaFile>> listAllFiles, List<List<RevCommit>> dividedCommits) throws IOException {
        int size = listAllFiles.size();
        Git git = Git.open(new File(this.localPath));
        //for cycle for working on listAllFiles[i] and dividedCommits[i]
        for (int i = 0; i < size; i++) {
            List<JavaFile> ijfl = listAllFiles.get(i);
            List<RevCommit> ircl = dividedCommits.get(i);
            for (RevCommit rc : ircl) {

                commFilePairerBis(rc, git, dividedCommits, ijfl);

            }
        }
    }


    public static List<FinalInstance> instancesBuilder(List<List<JavaFile>> allfiles) {
        List<FinalInstance> retFinalInstances = new ArrayList<>();
        for (List<JavaFile> jfl : allfiles) {
            for (JavaFile jf : jfl) {
                FinalInstance finalInstance = new FinalInstance(jf);
                retFinalInstances.add(finalInstance);
            }
        }
        return retFinalInstances;
    }

    public List<FinalInstance> instanceListBuilder(String projName, List<Version> versionsHalved) throws IOException {
        List<RevCommit> commits = retrieveAllCommits();
        List<List<RevCommit>> dividedCommits = commitsDivider(commits, versionsHalved);
        commitListOrderer(dividedCommits);
        List<List<JavaFile>> listAllFiles = new ArrayList<>();

        for (List<RevCommit> lrc : dividedCommits) {
            if(!lrc.isEmpty()) {
                List<JavaFile> jfl = getFilesNew(lrc.get(lrc.size() - 1));
                listAllFiles.add(jfl);
            }
        }


        fileListVersioner(listAllFiles, versionsHalved); //versioner
        commitsFilePairer(listAllFiles, dividedCommits);


        InstanceController ic = new InstanceController(projName);


        List<FinalInstance> instancesList = instancesBuilder(listAllFiles);

        for (FinalInstance i : instancesList) {

            int temp = 0;
            if (!i.getJavafile().getCommitList().isEmpty()) {
                temp = ic.countLinesOfCode(i.getJavafile().getCommitList().get(i.getJavafile().getCommitList().size() - 1), i.getName());

                i.setSize(temp);
            } else {
                i.setSize(0);
            }
            i.setnAuthors(ic.nAuthCounter(i));
            LinesMetricCollector lmc = ic.getLinesMetrics(i, versionsHalved.get(0));
            i.setLocAdded(lmc.getAddedLines());
            i.setChurn(lmc.getChurn());
            i.setAvgChurn(lmc.getAvgChurn());
            i.setMaxLocAdded(lmc.getMaxLOC());
            i.setMaxChurn(lmc.getMaxChurn());
            i.setAvgLocAdded(lmc.getAvgLOC());

        }
        return ic.locRepairer(instancesList);
    }
}


/*
* prendo tutti i commit -> faccio una lista di liste dove la i-esima lista contiene i commit relativi alla stessa
* release -> poi prendo tutti nomi dei file toccati dai commit in una release (quelli della stessa lista), eliminando
* i duplicati ->  abbiamo tutti i file con release di appartenenza, nome e lista di commit che li toccano-> possiamo
*  calcolare tutte le metriche*/