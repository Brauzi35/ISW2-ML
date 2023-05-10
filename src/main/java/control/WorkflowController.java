package control;

import model.FinalInstance;
import model.Version;
import model.Bug;
import weka.core.Instance;

import java.util.ArrayList;
import java.util.List;

public class WorkflowController {
    private static String projectName = "BOOKKEEPER";

    public static List<FinalInstance> instancesHalver(List<Version> versions, List<FinalInstance> instances) {
        versions = versions.subList(0, versions.size() / 2);
        List<FinalInstance> ret = new ArrayList<>();
        for (Version v : versions) {
            for (FinalInstance i : instances) {
                if (v.getName().equals(i.getVersion())){
                    ret.add(i);
                }

            }

        }
        return ret;
    }

    public static void main(String[] args) throws Exception {
        JiraController jc = new JiraController(projectName);
        BugController bc = new BugController();
        List<Version> versions = jc.getAllVersions();
        //get half versions
        //versions = versions.subList(0, versions.size()/2);
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
        List<FinalInstance> finalInstances = clc.instanceListBuilder("BOOKKEEPER", versions);

        InstanceController ic = new InstanceController();
        List<FinalInstance> buggyFinalInstances = ic.isBuggy2(finalInstances, av_bugs);

        System.out.println("size buggyIstances "+ buggyFinalInstances.size());
        for(FinalInstance i : finalInstances){


            if(buggyFinalInstances.contains(i)){
                i.setBuggyness("Yes");
            }



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
        /*
        for(Bug b : av_bugs){
            System.out.println("key  " + b.getKey() + "  opening version: " + b.getOv().getIndex() + "  fixed version:  " +  b.getFv().getIndex() + " size av: " + b.getAv().size());
            for(Version f : b.getAv()){
                System.out.println("av id: "+f.getIndex());
            }
            if(b.getIv()!=null){
                System.out.println("indice iv: " + b.getIv().getIndex());
            }
        }

         */
        List<FinalInstance> finalInstancesHalved = instancesHalver(versions, finalInstances);

        //System.out.println("ELEMENTI NUOVA LISTA: " + finalInstancesHalved.size());


        //System.out.println(av_bugs.size());
        CsvWriter csvw = new CsvWriter();
        csvw.csv_builder(finalInstancesHalved, "output.csv");

        ArffConverter ac = new ArffConverter();
        ac.csv2arff("C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\output.csv", "output.arff");

        WekaController wc = new WekaController();
        wc.walkForward(finalInstancesHalved, versions.subList(0, versions.size() / 2));
        }
    }

