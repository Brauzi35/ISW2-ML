package control;

import model.Bug;
import model.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ProportionController {
    List<Bug> iterativeProportion(List<Bug> bugs, List<Version> versions) throws IOException {

        List<Bug> toDoBugs = new ArrayList<>();
        List<Bug> completeBugs = new ArrayList<>();

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


        for(Bug b : toDoBugs){
            List<Bug> previousBugs = new ArrayList<>();
            List<Float> pinc =new ArrayList<>();
            float retP = 0;
            float totP = 0;
            for(Bug a : completeBugs){
                if(a.getFv().getIndex()<b.getOv().getIndex()) previousBugs.add(a); //building a list of all complete bugs that were closed before the toDoBug considered was opened
            }


            if(!previousBugs.isEmpty()){

                for(Bug pb : previousBugs){
                    float p = pCalculator(pb.getIv().getIndex(), pb.getOv().getIndex(), pb.getFv().getIndex());
                    pinc.add(p);
                    totP += p;

                }

                retP = totP/pinc.size();
                System.err.println("bug id: " + b.getKey() + " p is: " + retP);
            }
            if(retP!=0) b.setIv(ivCalculator(b.getFv().getIndex(), b.getOv().getIndex(), retP ,versions));
        }
        float median = coldStart();
        for(Bug b : toDoBugs){
            if(b.getIv()==null){

                Version iv = ivCalculator(b.getFv().getIndex(), b.getOv().getIndex(), median ,versions);
                System.err.println("sto calcolando iv del bug: " + b.getKey() + " che dovrebbe essere: "+ iv.getIndex());
                b.setIv(iv);
            }
        }

        List<Bug> retBugs = bugs;
        for(Bug b : retBugs){
            for(Bug db : toDoBugs){
                if(db.getKey().equals(b.getKey())){
                    Collections.replaceAll(retBugs, b, db);
                }
            }
        }
        //return toDoBugs;
        return retBugs;
    }
    //gets indexes and computes (FV - IV) / (FV - OV)
    public float pCalculator(int iv, int ov, int fv){
        float p;
        float sub = (fv - iv);
        float den = (fv - ov);
        if(den == 0.0) den =1;
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
        //se non appartiene a nessuna versione lo forzo a 1
        if(res<1){
            iv = versions.get(0);
        }
        return iv;
    }

    public float coldStart() throws IOException {
        String projname = "TAJO";
        float p;
        ArrayList<Float> allp = new ArrayList<>();
        JiraController jc = new JiraController(projname);

        List<Version> vers = jc.getAllVersions();
        List<Bug> bgs = jc.getBugs(vers);
        for(Bug b : bgs){
            if(b.getIv()!=null && b.getOv().getIndex()!=b.getFv().getIndex()){

                    p = pCalculator(b.getIv().getIndex(), b.getOv().getIndex(), b.getFv().getIndex());
                    allp.add(p);

            }
        }

        //Collections.sort(all_p);
        float avg = 0;
        for(float f : allp){
            avg+= f;
        }

        avg = avg/ allp.size();
        return avg;
    }

}
