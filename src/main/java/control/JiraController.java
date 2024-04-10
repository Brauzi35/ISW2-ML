package control;

import model.Bug;
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
    private static final String FIELDS = "fields";  // Compliant
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

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
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
        for (i=0; i<tot; i++){
            String nameRelease = json.getJSONObject(i).get("name").toString();
            String released = json.getJSONObject(i).get("released").toString();
            String releaseId = json.getJSONObject(i).get("id").toString();
            if(released.equalsIgnoreCase("true")){
                try{
                    LocalDateTime dateRelease;
                    String dateReleaseStr = json.getJSONObject(i).get("releaseDate").toString();
                    dateRelease = LocalDateTime.parse(dateReleaseStr + "T00:00:00");
                    Version element = new Version(nameRelease,dateRelease, releaseId, 0); //it was count
                    versions.add(element);
                }catch (JSONException e){
                    Logger logger = Logger.getLogger(JiraController.class.getName());
                    String out ="["+this.projectName+"] - una release non possiede la data di rilascio. Release saltata.";
                    logger.log(Level.INFO, out);
                }
            }
        }
        versions.sort(Comparator.comparing(Version::getReleaseDate)); //ordering version by release date (oldest to newest)
        int count = 1;
        for(Version v : versions){
            v.setIndex(count);
            count++;
        }
        return versions;
    }


    public List<Bug> getBugs(List<Version> versions) throws IOException {
        final int MaxDisplay = 1000;

        //List<Version> versions = this.getAllVersions(); //potrebbe esserci (quasi sicuro) un problema con project name
        List<Bug> bugs = new ArrayList<>();
        BugController bc = new BugController();
        int upperBound = 0;
        int lowerBound = 0;
        int total = 0;
        JSONArray jsonAv;

        do {
            upperBound = lowerBound + MaxDisplay;

            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project%20%3D%20" + this.projectName +
                    "%20AND%20issuetype%20%3D%20Bug%20AND%20(%22status%22%20%3D%22resolved%22%20OR%20%22status" +
                    "%22%20%3D%20%22closed%22)%20AND%20%20%22resolution%22%20%3D%20%22fixed%22%20&fields=key," +
                    "resolutiondate,versions,created,fixVersions&startAt=" + lowerBound + "&maxResults=" + upperBound;
            JSONObject json = readJsonFromUrl(url);
            JSONArray bugsList = json.getJSONArray("issues");
            total = json.getInt("total");

            for (; lowerBound < total && lowerBound < upperBound; lowerBound++) {

                JSONObject jsonTrunc = bugsList.getJSONObject(lowerBound%MaxDisplay); //get single jsonObject from jsonArray
                String key = jsonTrunc.get("key").toString();
                /*
                String version = jsonTrunc.getJSONObject(FIELDS).get("versions").toString();
                String fv = jsonTrunc.getJSONObject(FIELDS).get("fixVersions").toString();

                 */
                String rDate = jsonTrunc.getJSONObject(FIELDS).get("resolutiondate").toString();
                String cDate = jsonTrunc.getJSONObject(FIELDS).get("created").toString();
                //building JSONarray containing av for the considered issue
                jsonAv = bugsList.getJSONObject(lowerBound%MaxDisplay).getJSONObject(FIELDS).getJSONArray("versions");
                Bug bug = bc.bugAssembler(versions, cDate, rDate, jsonAv, key);
                bugs.add(bug);


            }
        } while(lowerBound<total);
        return bc.bugCleaner(bugs);
    }
}
