package control;

import model.JavaFile;
import model.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.MessageRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class CodeLineCounter {


    static final String localPath = "C:\\Users\\vlrbr\\Desktop\\bookkeeper";
    public static List<RevCommit> retrieveAllCommits() throws GitAPIException, RevisionSyntaxException, IOException {
        //String localPath = "C:\\Users\\vlrbr\\Desktop\\bookkeeper";
        File dir = new File(localPath);
        List<RevCommit> commitFinal = new ArrayList<>();

        try (Git git = Git.open(new File(localPath))){

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
            for (RevCommit commit : commitFinal){
            /*
                PersonIdent authorIdent = commit.getAuthorIdent();
                Date authorDate = authorIdent.getWhen();
                TimeZone authorTimeZone = authorIdent.getTimeZone();

                con queste LOC ottengo la data e l'ora del commit riferite alla timezone dell'autore
             */
                System.out.println("Commit: " + commit.getName() + " " + commit.getShortMessage());
                count++;
            }

            System.out.println(count);



        } catch (GitAPIException e) {
            System.out.println("Exception occurred while cloning repository: " + e.getMessage());
        }
        return commitFinal;
    }



    public static List<List<RevCommit>> commitsDivider(List<RevCommit> commits, List<Version> versions){
        //getCommitterIdent().getWhen()
        List<List<RevCommit>> returnList = new ArrayList<>();
        for(Version v : versions){
            List<RevCommit> r = new ArrayList<>();
            for(RevCommit c : commits){
                Instant instant = c.getCommitterIdent().getWhenAsInstant();
                LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                if(ldt.compareTo(v.getReleaseDate())<=0){
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
        System.out.println("size of list of lists: " + returnList.size());
        int count = 0;
        for(List<RevCommit> r : returnList){

            count+= r.size();
            System.out.println("commits in list(using count to difference lists):  " + count);

            for(RevCommit rr : r){
                System.out.println(rr.getShortMessage());
            }

        }
        System.out.println("tot commits divided: " + count);
        return returnList;
    }

    public static List<List<JavaFile>> getFiles(List<List<RevCommit>> divCommits) throws Exception {
        Git git = Git.open(new File(localPath));
        Repository repository = git.getRepository();
        List<List<JavaFile>> files = new ArrayList<>();
        int index = 0;
        for(List<RevCommit> lrc : divCommits){
            List<JavaFile> temp = new ArrayList<>();
            for(RevCommit singleRC : lrc){
                List<JavaFile> ret = getFilesInCommit(repository, singleRC);
                temp.addAll(ret);
            }
            files.add(temp);
        }

        System.out.println(files.size());
        int size = 0;
        for (List<JavaFile> jv : files){
            size+=jv.size();
        }
        System.out.println(size);
        return files;
    }

    public static List<JavaFile> getFilesInCommit(Repository repository, RevCommit revcommit) throws Exception {

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(ObjectId.fromString(revcommit.getName()));
            RevTree tree = commit.getTree();
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                List<String> files = new ArrayList<>();
                while (treeWalk.next()) {
                    if(treeWalk.getPathString().endsWith(".java")) {
                        files.add(treeWalk.getPathString());
                    }
                }

                List<JavaFile> javafiles = new ArrayList<>();
                //ci metto solo il nome e il commit
                for(String s : files){
                    List<RevCommit> rc = new ArrayList<>();
                    rc.add(revcommit);
                    JavaFile jv = new JavaFile(s, null, rc);
                    javafiles.add(jv);
                }
                return javafiles;
            }
        }
    }

    //need to merge all the files having same release and same name in a single file having all commits touching that file
    public static List<List<JavaFile>> doubleFilesMerger(List<List<JavaFile>> files){
        List<List<JavaFile>> filesReworked = new ArrayList<>();
        for(List<JavaFile> jfl : files){
            List<JavaFile> jfl_clone = jfl; //avoid concurrent mod
            List<JavaFile> javafiles = new ArrayList<>();
            while (!jfl_clone.isEmpty()){
                JavaFile jf = jfl.get(0);
                jf.setCommitList(new ArrayList<>()); //need empty list to start with
                List<JavaFile> removeArray = new ArrayList<>();
                for(JavaFile j : jfl_clone){
                    if(j.getFilename().equals(jf.getFilename())){
                        List<RevCommit> comm = jf.getCommitList();
                        comm.addAll(j.getCommitList());

                       // jfl_clone.remove(j); //concurrent mod
                        removeArray.add(j);
                    }
                }
                jfl_clone.removeAll(removeArray);
                javafiles.add(jf);
            }
            filesReworked.add(javafiles);

        }
        /*
        useful prints
        System.out.println("size of list of list of file: " + filesReworked.size());
        int size = 0;
        for (List<JavaFile> jfl : filesReworked){
            size+= jfl.size();
            for (JavaFile jf : jfl){
                System.out.println("il file: " + jf.getFilename() + " è toccato da :" + jf.getCommitList().size() + " commit");
            }
        }
        System.out.println("numero file in file reworked: " + size);

         */
        return filesReworked;
    }
    //each file needs a version: knowing that the first list in filesreworked represent the first version and so on
    //the versioning operation can be done as follows
    public static void fileListVersioner(List<List<JavaFile>> filesReworked, List<Version> versions){
        int count = 0;
        List<List<JavaFile>> returnList = filesReworked;
        for (List<JavaFile> jfl : returnList){
            for(JavaFile jf : jfl){
                jf.setVersion(versions.get(count));
            }
            count++;
        }
        //return returnList;
    }


    public static void commitListOrderer(List<List<RevCommit>> llrc){
        for(List<RevCommit> lrc : llrc) {
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
            }
            else {
                if (treeWalk.getPathString().endsWith(".java") && !treeWalk.getPathString().contains("/test/")) {
                    String className = treeWalk.getPathString() ;
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
                if (!rc.equals(dividedCommits.get(0).get(0))) {
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
                            System.out.println("Changed file: " + diff.getNewPath());
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
        }

        fileListVersioner(listAllFiles, versionsHalved); //versioner
        commitsFilePairer(listAllFiles, dividedCommits);
        //prints
        for(List<JavaFile> jfl : listAllFiles){
            for (JavaFile jf : jfl){
                System.out.println("file: " + jf.getFilename() + " at version "+ jf.getVersion().getName() +" is touched by " + jf.getCommitList().size() + " commits");
                for(RevCommit aaa : jf.getCommitList()){
                    System.out.println(aaa.getShortMessage());
                }
            }

        }

        /*
        System.out.println(commit.getShortMessage());
        System.out.println("");
        //file toccati dai commit
        DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        formatter.setRepository(git.getRepository());
        formatter.setDiffComparator(RawTextComparator.DEFAULT);
        formatter.setDetectRenames(true);

        ObjectId commitId = commit.getId();
        RevCommit parent = commit.getParent(0);
        if (parent != null) {
            ObjectId parentId = parent.getId();
            List<DiffEntry> diffs = formatter.scan(parentId, commitId);
            for (DiffEntry diff : diffs) {
                System.out.println("Changed file: " + diff.getNewPath());
                //System.out.println(formatter.toFileHeader(diff));
                int linesDeleted = 0;
                int linesAdded = 0;
                for (Edit edit : formatter.toFileHeader(diff).toEditList()) {
                    linesDeleted += edit.getEndA() - edit.getBeginA();
                    linesAdded += edit.getEndB() - edit.getBeginB();
                }
                System.out.println("linee cancellate: " + linesDeleted + "-------- linee aggiunte: " + linesAdded);

            }
        }


        /*
        List<List<JavaFile>> files = getFiles(dividedCommits);
        List<List<JavaFile>> filesReworked = doubleFilesMerger(files);
        List<List<JavaFile>> filesVersioned = fileListVersioner(filesReworked, versionsHalved);
        int size = 0;
        for (List<JavaFile> jfl : filesVersioned){
            size+= jfl.size();
            System.out.println("size release: " + jfl.size());
            for (JavaFile jf : jfl){
                System.out.println("il file: " + jf.getFilename() + " alla versione: "+ jf.getVersion().getName() + " è toccato da :" + jf.getCommitList().size() + " commit");

            }


        }
        System.out.println("dimensione finale: " + size);

     */
    }
}

/*
* prendo tutti i commit -> faccio una lista di liste dove la i-esima lista contiene i commit relativi alla stessa
* release -> poi prendo tutti nomi dei file toccati dai commit in una release (quelli della stessa lista), eliminando
* i duplicati ->  abbiamo tutti i file con release di appartenenza, nome e lista di commit che li toccano-> possiamo
*  calcolare tutte le metriche*/