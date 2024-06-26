package control;

import model.Bug;
import model.Version;
import org.json.JSONArray;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BugController {
    /*given key, fv, version, releaseDate and creationDate this method aims to compute ov, iv and av
    * and return a valid entity */
    public Bug bugAssembler(List<Version> versions, String ovDate, String fvDate, JSONArray jsonAv, String key){
        Bug bug = null;
        Version ov = ovFvCalculator(versions, ovDate);
        Version fv = ovFvCalculator(versions, fvDate);
        List<Version> av = avBuilder(jsonAv, versions);
        //if last av > ov then av is wrong, so it should not be considered
        //need to sort av
        Version iv = null;
        if(!av.isEmpty()) {
            av.sort(Comparator.comparing(Version::getReleaseDate)); //ordering version by release date (oldest to newest)
            iv = av.get(0);
            bug = new Bug(key, fv, ov, iv, av);
        }
        else{
            bug = new Bug(key, fv, ov, av);
        }

        return bug;
    }
    /* this method computes ov or fv given the list of all the versions and a date (resolution
    or creation) */
    public Version ovFvCalculator(List<Version> versions, String stringDate){
        //is the most recent version that was submitted before creationDate
        Version vrs = null;
        String newDate = stringDate.substring(0, stringDate.length() - 9); //discard last 9 characters
        LocalDateTime date = LocalDateTime.parse(newDate);
        for(Version v : versions){
            if(date.compareTo(v.getReleaseDate())<0) {
                vrs = v;
                break;
            }
        }
        return vrs;
    }
    //this method builds a preliminary av, the correct and definitive av is built later
    public List<Version> avBuilder(JSONArray jsonAv, List<Version> versions){

        List<Version> av = new ArrayList<>();
        int jlen = jsonAv.length();
        boolean released;

        for(int i=0; i<jlen; i++){
            released = jsonAv.getJSONObject(i).getBoolean("released");
            if(released){
                String nameRelease = jsonAv.getJSONObject(i).get("name").toString();
                LocalDateTime releaseDate = null;
                int index = 0;
                for(Version v : versions){
                    if(v.getName().equals(nameRelease)){
                        releaseDate = v.getReleaseDate();
                        index = v.getIndex();
                    }
                }

                String releaseId = jsonAv.getJSONObject(i).get("id").toString();


                Version v = new Version(nameRelease, releaseDate, releaseId, index);
                if(releaseDate != null){
                    av.add(v);
                }

            }
        }
        return av;

    }

    public List<Bug> bugCleaner(List<Bug> bugs){
        List<Bug> cleanBugList = new ArrayList<>();
        for(Bug b : bugs){
            /* discard issues without FV or OV and issues with OV and FV inconsistent (OV>=FV)
             and issues pre-release (IV=OV=FV)*/

            boolean include = true;

            if ((b.getFv() == null || b.getOv() == null || b.getOv().getReleaseDate().compareTo(b.getFv().getReleaseDate()) > 0) ||
                    (b.getIv() != null && (b.getOv().getReleaseDate().compareTo(b.getFv().getReleaseDate()) == 0 && b.getIv().getReleaseDate().compareTo(b.getOv().getReleaseDate()) == 0)) ||
                    (b.getIv() != null && b.getOv().getReleaseDate().compareTo(b.getIv().getReleaseDate()) < 0)) {
                include = false;
            }


            if(include){
                cleanBugList.add(b);
            }

        }
        return cleanBugList;
    }

    //should not consider ov = 1 and fv = 1
    public List<Bug> bugTrimmer(List<Bug> bugs){
        List<Bug> retBugs = new ArrayList<>();
        for(Bug b : bugs){
            if(b.getFv().getIndex()== 1 && b.getOv().getIndex()== 1){
                continue;
            }
            retBugs.add(b);
        }
        return retBugs;
    }

    //only take bugs' first half
    //this method builds the final av post proportion
    public List<Bug> definitiveAvBuilder(List<Bug> bugs, List<Version> versions){
        for(Bug b : bugs){
            //the final av is [iv, fv)
            List<Version> av = new ArrayList<>();
            int ivIdx = b.getIv().getIndex();
            int fvIdx = b.getFv().getIndex();
            for(Version v : versions){
                if(v.getIndex()>=ivIdx && v.getIndex()<fvIdx){
                    av.add(v);
                }
            }
            b.setAv(av);
        }
        return bugs;
    }

}
