package model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

public class JavaFile {
    private String filename;
    private Version version;

    private List<RevCommit> commitList;

    public JavaFile(String filename, Version version, List<RevCommit> commitList) {
        this.filename = filename;
        this.version = version;
        this.commitList = commitList;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public List<RevCommit> getCommitList() {
        return commitList;
    }

    public void setCommitList(List<RevCommit> commitList) {
        this.commitList = commitList;
    }
}
