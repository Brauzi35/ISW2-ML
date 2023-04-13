package control;

import model.Bug;
import model.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ProportionController {
    List<Bug> iterativeProportion(List<Bug> bugs, List<Version> versions){

        List<Bug> toDoBugs = new ArrayList<>();
        List<Bug> completeBugs = new ArrayList<>();
        List<Bug> staticbugs = bugs;

        int count= bugs.size();


        for(Bug b : bugs){
            b.setIndex(count);
            count--;

            if(b.getIv()==null) {
                toDoBugs.add(b);
            }

            else if(b.getIv()!= null && b.getOv().getIndex()!=b.getFv().getIndex()){
                completeBugs.add(b);
            }

        }


        /*
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
            System.out.println("bug id: " + b.getKey() + " p is: " + retP);
        }
        */
        for(Bug b : toDoBugs){
            List<Bug> previousBugs = new ArrayList<>();
            List<Float> pinc =new ArrayList<>();
            float retP = 0;
            float totP = 0;
            for(Bug a : completeBugs){
                if(a.getIndex()<b.getIndex()) previousBugs.add(a); //building a list of all complete bugs that came before the toDoBug considered
            }

            if(previousBugs.isEmpty()) System.out.println("sul bug: " + b.getKey() + "devo fare cold start");

            else{

                for(Bug pb : previousBugs){
                    float p = pCalculator(pb.getIv().getIndex(), pb.getOv().getIndex(), pb.getFv().getIndex());
                    pinc.add(p);
                    totP += p;
                }
                retP = totP/pinc.size();
                System.out.println("bug id: " + b.getKey() + " p is: " + retP);
            }
            if(retP!=0) b.setIv(ivCalculator(b.getFv().getIndex(), b.getOv().getIndex(), retP ,versions));

        }

        return toDoBugs; //sbagliato ma per ora non mi serve

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
    public Version ivCalculator(int fv, int ov, float p, List<Version> versions){
        Version iv = null;
        int sub = fv - ov;
        if(sub == 0) sub = 1; //if
        float mult = sub*p;
        int res = fv - Math.round(mult);
        for(Version v : versions){

            if(v.getIndex()==res){
                iv = v;
            }
        }
        return iv;
    }



}
