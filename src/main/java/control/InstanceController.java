package control;

import model.Instance;
import model.JavaFile;
import model.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
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
        //da finire
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


    }

