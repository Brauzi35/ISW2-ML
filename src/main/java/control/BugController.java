package control;

import model.Bug;
import model.Version;

import java.time.LocalDateTime;
import java.util.List;

public class BugController {
    /*given key, fv, version, releaseDate and creationDate this method aims to compute ov, iv and av
    * and return a valid entity */
    public Bug bugAssembler(List<Version> versions, ){

    }
    /* this method computes ov or fv given the list of all the versions and a date (resolution
    or creation) */
    public Version ov_fvCalculator(List<Version> versions, String stringDate){
        //is the most recent version that was submitted before creationDate
        Version vrs = null;
        LocalDateTime date = LocalDateTime.parse(stringDate + "T00:00:00");
        for(Version v : versions){
            if(date.compareTo(v.getReleaseDate())<0) {
                vrs = v;
                break;
            }
        }
        return vrs;
    }
}
