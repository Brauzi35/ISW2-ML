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

public class CodeLineCounter {

   private String path;
   static String localPath;

    public CodeLineCounter(String localPathpass) {
        //static final String localPath = "C:\\Users\\vlrbr\\Desktop\\bookkeeper";
       localPath = localPathpass;
    }



    public static List<RevCommit> retrieveAllCommits() throws GitAPIException, RevisionSyntaxException, IOException {
        //String localPath = "C:\\Users\\vlrbr\\Desktop\\bookkeeper";
        File dir = new File(localPath);
        List<RevCommit> commitFinal = new ArrayList<>();

        try (Git git = Git.open(new File(localPath))) {

            //Iterable<RevCommit> commits = git.log().all().setRevFilter(RevFilter.NO_MERGES)
            //.setRevFilter(MessageRevFilter.create("BOOKKEEPER-")).call();
            Iterable<RevCommit> commits = git.log().all().call();
            //cosi prendo tutti i commit
            //Iterable<RevCommit> commits = git.log().all().setRevFilter(msgFilter).call();
            //Iterable<RevCommit> commits = git.log().setRevFilter(MessageRevFilter.create("BOOKKEEPER-"));//.call();

            for (RevCommit commit : commits) {
                //if(commit.getShortMessage().contains("BOOKKEEPER-")) {
                commitFinal.add(commit);
                //System.out.println("Commit: " + commit.getName() + " " + commit.getShortMessage());

                //}

            }
            int count = 0;
            for (RevCommit commit : commitFinal) {
            /*
                PersonIdent authorIdent = commit.getAuthorIdent();
                Date authorDate = authorIdent.getWhen();
                TimeZone authorTimeZone = authorIdent.getTimeZone();

                con queste LOC ottengo la data e l'ora del commit riferite alla timezone dell'autore
             */
                //System.out.println("Commit: " + commit.getName() + " " + commit.getShortMessage());
                count++;
            }

            System.out.println(count);


        } catch (GitAPIException e) {
            System.out.println("Exception occurred while cloning repository: " + e.getMessage());
        }
        return commitFinal;
    }


    public static List<List<RevCommit>> commitsDivider(List<RevCommit> commits, List<Version> versions) {
        //getCommitterIdent().getWhen()
        List<List<RevCommit>> returnList = new ArrayList<>();
        for (Version v : versions) {
            List<RevCommit> r = new ArrayList<>();
            for (RevCommit c : commits) {
                Instant instant = c.getCommitterIdent().getWhenAsInstant();
                //Instant instant = c.getAuthorIdent().getWhenAsInstant();
                LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                if (ldt.compareTo(v.getReleaseDate()) <= 0) {
                    r.add(c);
                }
            }
            commits.removeAll(r);
            returnList.add(r);
        }
        //commits not considered because committed after last release considered
        /*
        for(RevCommit rc : commits){
            System.out.println(rc.getShortMessage());
        }
        */
        //System.out.println("size of list of lists: " + returnList.size());
        int count = 0;
        for (List<RevCommit> r : returnList) {

            count += r.size();
            //System.out.println("commits in list(using count to difference lists):  " + count);
            /*
            for(RevCommit rr : r){
                System.out.println(rr.getShortMessage());
            }

             */

        }
        //System.out.println("tot commits divided: " + count);
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
        //return returnList;
    }


    public static void commitListOrderer(List<List<RevCommit>> llrc) {
        for (List<RevCommit> lrc : llrc) {
            Collections.sort(lrc, Comparator.comparingLong(RevCommit::getCommitTime));
        }
    }

    public static List<JavaFile> getFilesNew(RevCommit commit) throws IOException {
        ObjectId treeId = commit.getTree().getId();
        Git git = Git.open(new File(localPath));
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

    public static void commitsFilePairer(List<List<JavaFile>> listAllFiles, List<List<RevCommit>> dividedCommits) throws IOException {
        int size = listAllFiles.size();
        Git git = Git.open(new File(localPath));
        //for cycle for working on listAllFiles[i] and dividedCommits[i]
        for (int i = 0; i < size; i++) {
            List<JavaFile> ijfl = listAllFiles.get(i);
            List<RevCommit> ircl = dividedCommits.get(i);
            for (RevCommit rc : ircl) {
                if (!rc.equals(dividedCommits.get(0).get(0)) && rc.getParentCount()>0) {
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
                            //System.out.println("Changed file: " + diff.getNewPath());
                            //System.out.println(formatter.toFileHeader(diff));
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
            }
        }
    }


    public static List<FinalInstance> instancesBuilder(List<List<JavaFile>> allfiles) {
        List<FinalInstance> retFinalInstances = new ArrayList<>();
        //Instance instance = new Instance()
        for (List<JavaFile> jfl : allfiles) {
            for (JavaFile jf : jfl) {
                FinalInstance finalInstance = new FinalInstance(jf);
                retFinalInstances.add(finalInstance);
            }
        }
        return retFinalInstances;
    }

    public List<FinalInstance> instanceListBuilder(String projName, List<Version> versionsHalved) throws IOException, GitAPIException {
        Git git = Git.open(new File(localPath));
        Repository repository = git.getRepository();

        List<RevCommit> commits = retrieveAllCommits();
        JiraController jc = new JiraController(projName);
        List<List<RevCommit>> dividedCommits = commitsDivider(commits, versionsHalved);
        commitListOrderer(dividedCommits);
        List<List<JavaFile>> listAllFiles = new ArrayList<>();

        for (List<RevCommit> lrc : dividedCommits) {
            if(lrc.size()>0) {
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
            if (i.getJavafile().getCommitList().size() != 0) {
                temp = ic.countLinesOfCode(i.getJavafile().getCommitList().get(i.getJavafile().getCommitList().size() - 1), i.getName());

                i.setSize(temp);
            } else {
                i.setSize(0);
            }
            i.setnAuthors(ic.nAuthCounter(i));
            LinesMetricCollector lmc = ic.getLinesMetrics(i);
            i.setLocAdded(lmc.getAddedLines());
            i.setChurn(lmc.getChurn());
            i.setAvgChurn(lmc.getAvgChurn());
            i.setMaxLocAdded(lmc.getMaxLOC());
            i.setMaxChurn(lmc.getMaxChurn());
            i.setAvgLocAdded(lmc.getAvgLOC());

        }
        List<FinalInstance> finalFinalInstances = ic.locRepairer(instancesList, versionsHalved); //before it was verions
        return finalFinalInstances;
    }
}

/*
    public static void main(String[] args) throws Exception {
        Git git = Git.open(new File(localPath));
        Repository repository = git.getRepository();

        List<RevCommit> commits = retrieveAllCommits();
        JiraController jc = new JiraController("BOOKKEEPER");
        List<Version> versions = jc.getAllVersions();
        List<Version> versionsHalved = versions.subList(0, versions.size()/2);
        List<List<RevCommit>> dividedCommits = commitsDivider(commits, versionsHalved);
        commitListOrderer(dividedCommits);
        List<List<JavaFile>> listAllFiles = new ArrayList<>();

        for(List<RevCommit> lrc : dividedCommits) {

            //System.out.println(lrc.get(lrc.size()-1).getCommitterIdent().getWhen()); //ultimo
            List<JavaFile> jfl = getFilesNew(lrc.get(lrc.size()-1));
            listAllFiles.add(jfl);
            //System.out.println("AOOO " +jfl.size());
            /*
            for(JavaFile jf : jfl){
                System.out.println(jf.getFilename());
            }
            */
    /*

        }


        fileListVersioner(listAllFiles, versionsHalved); //versioner
        commitsFilePairer(listAllFiles, dividedCommits);




        InstanceController ic = new InstanceController();


        List<Instance> instancesList = instancesBuilder(listAllFiles);

        for(Instance i : instancesList){

            int temp = 0;
            if(i.getJavafile().getCommitList().size() != 0) {
                temp = ic.countLinesOfCode(i.getJavafile().getCommitList().get(i.getJavafile().getCommitList().size() - 1), i.getName());

                i.setSize(temp);
            } else{
                i.setSize(0);
            }
            i.setnAuthors(ic.nAuthCounter(i));
            //i.setLocAdded(ic.getAddedLoc(i, repository));
            LinesMetricCollector lmc = ic.getLinesMetrics(i);
            i.setLocAdded(lmc.getAddedLines());
            i.setChurn(lmc.getChurn());
            i.setAvgChurn(lmc.getAvgChurn());
            i.setMaxLocAdded(lmc.getMaxLOC());
            i.setMaxChurn(lmc.getMaxChurn());
            i.setAvgLocAdded(lmc.getAvgLOC());

        }
        List<Instance> finalInstances = ic.locRepairer(instancesList, versions);
        for(Instance i : finalInstances){


            System.out.println(i.getJavafile().getFilename() + " " + i.getVersion() +
                    "\n has this number of loc: " + i.getSize() +
                    "\n and this number of authors: " + i.getnAuthors() +
                    "\n and this number of commits: " + i.getNr() +
                    "\n and this locAdded: " + i.getLocAdded() +
                    "\n and this AvglocAdded: " + i.getAvgLocAdded() +
                    "\n and this MaxLoc: " + i.getMaxLocAdded() +
                    "\n and this churn: " + i.getChurn() +
                    "\n and this avgChurn: " + i.getAvgChurn() +
                    "\n and this maxChurn: " + i.getMaxChurn());
        }




    }
}
*/

/*
* prendo tutti i commit -> faccio una lista di liste dove la i-esima lista contiene i commit relativi alla stessa
* release -> poi prendo tutti nomi dei file toccati dai commit in una release (quelli della stessa lista), eliminando
* i duplicati ->  abbiamo tutti i file con release di appartenenza, nome e lista di commit che li toccano-> possiamo
*  calcolare tutte le metriche*/