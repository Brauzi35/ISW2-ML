package model;

/*
0-Size (LOC)
1-NRevisions (per release)
2-NAuth (per release)
3-LocAdded
4-MaxLocAdded
5-AverageLocAdded
6-Churn
7-MaxChurn
8-AverageChurn
9-Warmth -mia
 */
public class FinalInstance {

    private JavaFile javafile;
    private String version;
    private String name;
    private int size;
    private int nr;
    private int locAdded;
    private int maxLocAdded;
    private double avgLocAdded;
    private int churn;
    private int maxChurn;
    private double avgChurn;
    private int nAuthors;
    private String buggyness;

    public FinalInstance(JavaFile javafile, String version, String name, int size, int nr, int locAdded, int maxLocAdded, double avgLocAdded, int churn, int maxChurn, double avgChurn, int nAuthors) {
        this.javafile = javafile;
        this.version = javafile.getVersion().getName();
        this.name = javafile.getFilename();
        this.size = size;
        this.nr = javafile.getCommitList().size(); //don't know if it is correct
        this.locAdded = locAdded;
        this.maxLocAdded = maxLocAdded;
        this.avgLocAdded = avgLocAdded;
        this.churn = churn;
        this.maxChurn = maxChurn;
        this.avgChurn = avgChurn;
        this.nAuthors = nAuthors;
        this.buggyness = "No";
    }

    public FinalInstance(JavaFile javafile){
        this.javafile = javafile;
        this.version = javafile.getVersion().getName();
        this.name = javafile.getFilename();
        this.nr = javafile.getCommitList().size();
        this.buggyness = "No";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public JavaFile getJavafile() {
        return javafile;
    }

    public void setJavafile(JavaFile javafile) {
        this.javafile = javafile;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public int getLocAdded() {
        return locAdded;
    }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getMaxLocAdded() {
        return maxLocAdded;
    }

    public void setMaxLocAdded(int maxLocAdded) {
        this.maxLocAdded = maxLocAdded;
    }

    public double getAvgLocAdded() {
        return avgLocAdded;
    }

    public void setAvgLocAdded(double avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }

    public int getChurn() {
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public int getMaxChurn() {
        return maxChurn;
    }

    public void setMaxChurn(int maxChurn) {
        this.maxChurn = maxChurn;
    }

    public double getAvgChurn() {
        return avgChurn;
    }

    public void setAvgChurn(double avgChurn) {
        this.avgChurn = avgChurn;
    }

    public int getnAuthors() {
        return nAuthors;
    }

    public void setnAuthors(int nAuthors) {
        this.nAuthors = nAuthors;
    }

    public String getBuggyness() {
        return buggyness;
    }

    public void setBuggyness(String buggyness) {
        this.buggyness = buggyness;
    }
}
