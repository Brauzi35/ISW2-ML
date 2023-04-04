package control;

import model.Version;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class WorkflowController {
    private static String projectName = "BOOKKEEPER";

    public static void main(String[] args) throws IOException {
        JiraController jc = new JiraController(projectName);
        List<Version> versions = jc.getAllVersions();
        for(Version v : versions){
            System.out.println(v.getIndex() + " " + v.getName() + " date:" + v.getReleaseDate());
        }
    }
}
