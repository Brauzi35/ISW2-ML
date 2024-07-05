package control;

import model.Bug;
import model.FinalInstance;
import model.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkflowController {
    private static final Logger logger = Logger.getLogger(WekaController.class.getName());
    private static String projectName = "BOOKKEEPER"; //change to STORM or BOOKKEEPER depending on the project

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
        for(Version v : versions){

            String m = v.getIndex() + " " + v.getName() + " date:" + v.getReleaseDate();
            logger.log(Level.INFO,  m);
        }
        List<Bug> bugs = jc.getBugs(versions);
        for(Bug b : bugs){
            String m2 = "key  " + b.getKey() + "  opening version: " + b.getOv().getIndex() + "  fixed version:  " +  b.getFv().getIndex();
            logger.log(Level.INFO,  m2);
            if(b.getIv()!=null){

                String m3 = "indice iv: " + b.getIv().getIndex();
                logger.log(Level.INFO,  m3);
            }
        }

        ProportionController pc = new ProportionController();
        List<Bug> done = pc.iterativeProportion(bugs, versions);

        List<Bug> done1 = bc.bugTrimmer(done);
        List<Bug> avbugs = bc.definitiveAvBuilder(done1, versions);


        CodeLineCounter clc = new CodeLineCounter("C:\\Users\\vlrbr\\Desktop\\Testing\\" + projectName.toLowerCase());
        List<FinalInstance> finalInstances = clc.instanceListBuilder(projectName, versions);

        InstanceController ic = new InstanceController(projectName.toLowerCase());
        List<FinalInstance> buggyFinalInstances = ic.isBuggy2(finalInstances, avbugs);
        for(FinalInstance i : finalInstances){


            if(buggyFinalInstances.contains(i)){
                i.setBuggyness("Yes");
            }

            

            String m4 = i.getJavafile().getFilename() + " " + i.getVersion() +
                    "\n has this number of loc: " + i.getSize() +
                    "\n and this number of authors: " + i.getnAuthors() +
                    "\n and this number of commits: " + i.getNr() +
                    "\n and this locAdded: " + i.getLocAdded() +
                    "\n and this AvglocAdded: " + i.getAvgLocAdded() +
                    "\n and this MaxLoc: " + i.getMaxLocAdded() +
                    "\n and this churn: " + i.getChurn() +
                    "\n and this avgChurn: " + i.getAvgChurn() +
                    "\n and this maxChurn: " + i.getMaxChurn() +
                    "\n is this class buggy? " + i.getBuggyness();
            logger.log(Level.INFO,  m4);
        }


        List<FinalInstance> finalInstancesHalved = instancesHalver(versions, finalInstances);

        CsvWriter csvw = new CsvWriter();
        csvw.csvBuilder(finalInstancesHalved, "output.csv");

        ArffConverter ac = new ArffConverter();
        ac.csv2arff("C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\output.csv", "output.arff");

        WekaController wc = new WekaController(projectName.toLowerCase(), "C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\");
        wc.walkForward(finalInstancesHalved, versions.subList(0, versions.size() / 2));
        }
    }

