package control;

import model.Instance;
import model.LinesMetricCollector;
import model.Version;
import model.Bug;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class WorkflowController {
    private static String projectName = "BOOKKEEPER";

    public static void main(String[] args) throws IOException, GitAPIException {
        JiraController jc = new JiraController(projectName);
        BugController bc = new BugController();
        List<Version> versions = jc.getAllVersions();
        //get half versions
        versions = versions.subList(0, versions.size()/2);
        for(Version v : versions){
            System.out.println(v.getIndex() + " " + v.getName() + " date:" + v.getReleaseDate());
        }
        List<Bug> bugs = jc.getBugs(versions);
        for(Bug b : bugs){
            System.out.println("key  " + b.getKey() + "  opening version: " + b.getOv().getIndex() + "  fixed version:  " +  b.getFv().getIndex());
            if(b.getIv()!=null){
                System.out.println("indice iv: " + b.getIv().getIndex());
            }
        }
        System.out.println(bugs.size());
        ProportionController pc = new ProportionController();
        List<Bug> done = pc.iterativeProportion(bugs, versions);

        List<Bug> done1 = bc.bugTrimmer(done);



        List<Bug> av_bugs = bc.definitiveAvBuilder(done1, versions);


        CodeLineCounter clc = new CodeLineCounter();
        List<Instance> instances = clc.instanceListBuilder("BOOKKEEPER");

        InstanceController ic = new InstanceController();
        for(Instance i : instances){
            String buggy = ic.isBuggy(i, av_bugs);
            i.setBuggyness(buggy);

            System.out.println(i.getJavafile().getFilename() + " " + i.getVersion() +
                    "\n has this number of loc: " + i.getSize() +
                    "\n and this number of authors: " + i.getnAuthors() +
                    "\n and this number of commits: " + i.getNr() +
                    "\n and this locAdded: " + i.getLocAdded() +
                    "\n and this AvglocAdded: " + i.getAvgLocAdded() +
                    "\n and this MaxLoc: " + i.getMaxLocAdded() +
                    "\n and this churn: " + i.getChurn() +
                    "\n and this avgChurn: " + i.getAvgChurn() +
                    "\n and this maxChurn: " + i.getMaxChurn() +
                    "\n is this class buggy? " + i.getBuggyness());
        }
        for(Bug b : av_bugs){
            System.out.println("key  " + b.getKey() + "  opening version: " + b.getOv().getIndex() + "  fixed version:  " +  b.getFv().getIndex() + " size av: " + b.getAv().size());
            for(Version f : b.getAv()){
                System.out.println("av id: "+f.getIndex());
            }
            if(b.getIv()!=null){
                System.out.println("indice iv: " + b.getIv().getIndex());
            }
        }
        System.out.println(av_bugs.size());
        CsvWriter csvw = new CsvWriter();
        csvw.csv_builder(instances);
        }
    }

