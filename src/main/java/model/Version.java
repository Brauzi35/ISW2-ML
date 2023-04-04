package model;

import java.time.LocalDateTime;

public class Version {
    private String name;
    private LocalDateTime releaseDate;
    private String versionId;

    public Version(String name, LocalDateTime releaseDate, String versionId) {
        this.name = name;
        this.releaseDate = releaseDate;
        this.versionId = versionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }
}
