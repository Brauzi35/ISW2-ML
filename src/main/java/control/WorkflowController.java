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
        List<Version> versions = jc.getAllVersions();
        /*for(Version v : versions){
            System.out.println(v.getIndex() + " " + v.getName() + " date:" + v.getReleaseDate());
        }*/
        List<Bug> bugs = jc.getBugs(versions);
        for(Bug b : bugs){
            System.out.println("key  " + b.getKey() + "  opening version: " + b.getOv().getName() + "  fixed version:  " +  b.getFv().getName());
        }

    }
}
