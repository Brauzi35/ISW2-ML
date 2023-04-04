package control;

import model.Version;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JiraController {
    //da jira voglio prendere issues e versioni
    private String projectName;

    public JiraController(String projectName) {
        this.projectName = projectName;
    }
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONArray(jsonText);
        }
    }
    public List<Version> getAllVersions() throws IOException {

        ArrayList<Version> versions = new ArrayList<>();

        String url = "https://issues.apache.org/jira/rest/api/2/project/"+this.projectName+"/versions";
        JSONArray json = readJsonArrayFromUrl(url);
        int tot =json.length();
        int i;
        int count = 0;
        for (i=0; i<tot; i++){
            String nameRelease = json.getJSONObject(i).get("name").toString();
            String released = json.getJSONObject(i).get("released").toString();
            String releaseId = json.getJSONObject(i).get("id").toString();
            if(released.equalsIgnoreCase("true")){
                try{
                    LocalDateTime dateRelease;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String dateReleaseStr = json.getJSONObject(i).get("releaseDate").toString();
                    dateRelease = LocalDateTime.parse(dateReleaseStr + "T00:00:00");
                    count++;
                    Version element = new Version(nameRelease,dateRelease, releaseId, count);
                    versions.add(element);
                }catch (JSONException e){
                    Logger logger = Logger.getLogger(JiraController.class.getName());
                    String out ="["+this.projectName+"] - una release non possiede la data di rilascio. Release saltata.";
                    logger.log(Level.INFO, out);
                }
            }
        }
        versions.sort(Comparator.comparing(Version::getReleaseDate)); //ordering version by release date (oldest to newest)
        return versions;
    }
}
