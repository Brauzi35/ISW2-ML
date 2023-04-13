package control;

import model.Bug;
import model.Version;

import java.util.ArrayList;
import java.util.List;

public class ProportionController {
    List<Bug> iterativeProportion(List<Bug> bugs, List<Version> versions){

        List<Bug> toDoBugs = new ArrayList<>();
        List<Bug> completeBugs = new ArrayList<>();
        List<Bug> staticbugs = bugs;

        for(Bug b : bugs){
            if(b.getIv()==null) {
                toDoBugs.add(b);
            }

            else if(b.getIv()!= null && b.getOv().getIndex()!=b.getFv().getIndex()){
                completeBugs.add(b);
            }

        }



        for(Bug b : toDoBugs){
            List<Float> pinc =new ArrayList<>();
            float retP;
            float totP = 0;
            for(Bug bc : completeBugs){
                if(b.getFv().getReleaseDate().compareTo(bc.getFv().getReleaseDate())>=0){
                    float p = pCalculator(bc.getIv().getIndex(), bc.getOv().getIndex(), bc.getFv().getIndex());
                    pinc.add(p);
                    totP += p;
                }
            }
            retP = totP/pinc.size();
            //b.setIv(ivCalculator(b.getFv().getIndex(), b.getOv().getIndex(), retP ,versions));
            //System.out.println(retP);
        }

        return toDoBugs;

    }
    //gets indexes and computes (FV - IV) / (FV - OV)
    public float pCalculator(int iv, int ov, int fv){
        float p;
        int sub = fv - iv;
        int den = fv - ov;
        p = sub/den;
        return p;
    }
    //IV = FV - (FV - OV) * P
    /*public Version ivCalculator(int fv, int ov, float p, List<Version> versions){
        Version iv = null;
        int sub = fv - ov;
        float mult = sub*p;
        int res = fv - (int)mult;
        for(Version v : versions){
            if(v.getIndex()==res){
                iv = v;
            }
        }
        return iv;
    } */

}
