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
        List<Bug> bugs2 = bc.bugHalver(bugs);
        ProportionController pc = new ProportionController();
        List<Bug> done = pc.iterativeProportion(bugs2, versions);

        List<Bug> done1 = bc.bugTrimmer(done);

        for(Bug b : done1){
            System.out.println("key  " + b.getKey() + "  opening version: " + b.getOv().getIndex() + "  fixed version:  " +  b.getFv().getIndex());
            if(b.getIv()!=null){
                System.out.println("indice iv: " + b.getIv().getIndex());
            }
        }
        System.out.println(done1.size());


    }
}
