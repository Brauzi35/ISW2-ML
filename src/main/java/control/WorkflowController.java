package control;

import model.Version;
import model.Bug;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class WorkflowController {
    private static String projectName = "BOOKKEEPER";

    public static void main(String[] args) throws IOException {
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
    }
}
