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
        classifyBugs(bugs, toDoBugs, completeBugs);

        calculateProportions(toDoBugs, completeBugs, versions);
        updateInitialVersions(toDoBugs, versions);

        return replaceBugs(bugs, toDoBugs);
    }

    private void classifyBugs(List<Bug> bugs, List<Bug> toDoBugs, List<Bug> completeBugs) {
        int count = bugs.size();
        for (Bug b : bugs) {
            b.setIndex(count);
            count--;
            if (b.getIv() == null) {
                toDoBugs.add(b);
            } else if (b.getIv() != null && b.getOv().getIndex() != b.getFv().getIndex()) {
                completeBugs.add(b);
            }
        }
    }

    private void calculateProportions(List<Bug> toDoBugs, List<Bug> completeBugs, List<Version> versions){
        for (Bug b : toDoBugs) {
            List<Bug> previousBugs = new ArrayList<>();
            float retP = calculateRetP(b, completeBugs, previousBugs);

            if (retP != 0) {
                b.setIv(ivCalculator(b.getFv().getIndex(), b.getOv().getIndex(), retP, versions));
            }
        }
    }

    private float calculateRetP(Bug b, List<Bug> completeBugs, List<Bug> previousBugs) {
        float totP = 0;
        List<Float> pinc = new ArrayList<>();

        for (Bug a : completeBugs) {
            if (a.getFv().getIndex() < b.getOv().getIndex()) {
                previousBugs.add(a);
            }
        }

        if (!previousBugs.isEmpty()) {
            for (Bug pb : previousBugs) {
                float p = pCalculator(pb.getIv().getIndex(), pb.getOv().getIndex(), pb.getFv().getIndex());
                pinc.add(p);
                totP += p;
            }

            return (totP / pinc.size());
        }
        return 0;
    }

    private void updateInitialVersions(List<Bug> toDoBugs, List<Version> versions) throws IOException {
        float median = coldStart();
        for (Bug b : toDoBugs) {
            if (b.getIv() == null) {
                Version iv = ivCalculator(b.getFv().getIndex(), b.getOv().getIndex(), median, versions);
                b.setIv(iv);
            }
        }
    }

    private List<Bug> replaceBugs(List<Bug> bugs, List<Bug> toDoBugs) {
        List<Bug> retBugs = new ArrayList<>(bugs);
        for (Bug db : toDoBugs) {
            for (int i = 0; i < retBugs.size(); i++) {
                if (retBugs.get(i).getKey().equals(db.getKey())) {
                    retBugs.set(i, db);
                }
            }
        }
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

    public float coldStartInside(String projname) throws IOException {
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

        float avg = 0;
        for(float f : allp){
            avg+= f;
        }

        avg = avg/ allp.size();
        return avg;
    }

    public float coldStart() throws IOException {
        ArrayList<Float> vals = new ArrayList<>();
        String[] projectNames = {
                "AVRO",
                "OPENJPA",
                "ZOOKEEPER",
                "SYNCOPE",
                "TAJO"
        };

        for (String name : projectNames) {
            vals.add(coldStartInside(name));
        }

        return calculateMedian(vals);
    }

    public static float calculateMedian(List<Float> list) {
        // Ordina la lista
        Collections.sort(list);

        int length = list.size();
        if (length % 2 == 0) {
            // Se il numero di elementi è pari, calcola la media dei due elementi centrali
            return (list.get(length / 2 - 1) + list.get(length / 2)) / 2.0f;
        } else {
            // Se il numero di elementi è dispari, prendi l'elemento centrale
            return list.get(length / 2);
        }
    }

}
