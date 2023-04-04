package model;

public class Bug {
    //to be defined, still have to decide on the metrics
    private String key;
    private Version fv;
    private Version ov;
    private Version iv;

    public Bug(String key, Version fv, Version ov, Version iv) {
        this.key = key;
        this.fv = fv;
        this.ov = ov;
        this.iv = iv;
    }


}
