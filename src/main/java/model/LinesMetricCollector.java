package model;

public class LinesMetricCollector {
    private int removedLines = 0;
    private int addedLines = 0; //addedLoc
    private int maxLOC = 0;
    private double avgLOC = 0;
    private int churn = 0;
    private int maxChurn = 0;
    private double avgChurn = 0;

    public LinesMetricCollector(int removedLines, int addedLines, int maxLOC, double avgLOC, int churn, int maxChurn, double avgChurn) {
        this.removedLines = removedLines;
        this.addedLines = addedLines;
        this.maxLOC = maxLOC;
        this.avgLOC = avgLOC;
        this.churn = churn;
        this.maxChurn = maxChurn;
        this.avgChurn = avgChurn;
    }

    public int getRemovedLines() {
        return removedLines;
    }

    public void setRemovedLines(int removedLines) {
        this.removedLines = removedLines;
    }

    public int getAddedLines() {
        return addedLines;
    }

    public void setAddedLines(int addedLines) {
        this.addedLines = addedLines;
    }

    public int getMaxLOC() {
        return maxLOC;
    }

    public void setMaxLOC(int maxLOC) {
        this.maxLOC = maxLOC;
    }

    public double getAvgLOC() {
        return avgLOC;
    }

    public void setAvgLOC(double avgLOC) {
        this.avgLOC = avgLOC;
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
}
