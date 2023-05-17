package model;

import java.util.List;

public class FinalMetrics {
    private List<List<Double>> precision;
    private List<List<Double>> recall;
    private List<List<Double>> auc;
    private List<List<Double>> kappa;
    private List<List<Double>> tp;
    private List<List<Double>> tn;
    private List<List<Double>> fp;
    private List<List<Double>> fn;
    private String projname;

    public FinalMetrics() {
    }

    public List<List<Double>> getPrecision() {
        return precision;
    }

    public void setPrecision(List<List<Double>> precision) {
        this.precision = precision;
    }

    public List<List<Double>> getRecall() {
        return recall;
    }

    public void setRecall(List<List<Double>> recall) {
        this.recall = recall;
    }

    public List<List<Double>> getAuc() {
        return auc;
    }

    public void setAuc(List<List<Double>> auc) {
        this.auc = auc;
    }

    public List<List<Double>> getKappa() {
        return kappa;
    }

    public void setKappa(List<List<Double>> kappa) {
        this.kappa = kappa;
    }

    public List<List<Double>> getTp() {
        return tp;
    }

    public void setTp(List<List<Double>> tp) {
        this.tp = tp;
    }

    public List<List<Double>> getTn() {
        return tn;
    }

    public void setTn(List<List<Double>> tn) {
        this.tn = tn;
    }

    public List<List<Double>> getFp() {
        return fp;
    }

    public void setFp(List<List<Double>> fp) {
        this.fp = fp;
    }

    public List<List<Double>> getFn() {
        return fn;
    }

    public void setFn(List<List<Double>> fn) {
        this.fn = fn;
    }

    public String getProjname() {
        return projname;
    }

    public void setProjname(String projname) {
        this.projname = projname;
    }
}
