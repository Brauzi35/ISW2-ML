package model;

import java.util.List;

public class Bug {
    //to be defined, still have to decide on the metrics
    private String key;
    private Version fv;
    private Version ov;
    private Version iv;
    private List<Version> av;
    private boolean valid;

    public Bug(String key, Version fv, Version ov, Version iv, List<Version> av) {
        this.key = key;
        this.fv = fv;
        this.ov = ov;
        this.iv = iv;
        this.av = av;
    }

    public Bug(String key, Version fv, Version ov, List<Version> av) {
        this.key = key;
        this.fv = fv;
        this.ov = ov;
        this.iv = null;
        this.av = av;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Version getFv() {
        return fv;
    }

    public void setFv(Version fv) {
        this.fv = fv;
    }

    public Version getOv() {
        return ov;
    }

    public void setOv(Version ov) {
        this.ov = ov;
    }

    public Version getIv() {
        return iv;
    }

    public void setIv(Version iv) {
        this.iv = iv;
    }

    public List<Version> getAv() {
        return av;
    }

    public void setAv(List<Version> av) {
        this.av = av;
    }


}
