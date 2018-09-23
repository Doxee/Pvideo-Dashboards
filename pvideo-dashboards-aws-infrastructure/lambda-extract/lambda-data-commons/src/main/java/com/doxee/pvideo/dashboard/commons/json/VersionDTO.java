package com.doxee.pvideo.dashboard.commons.json;

import java.util.List;

public class VersionDTO {

    private String versionName;

    private String versionData;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionData() {
        return versionData;
    }

    public void setVersionData(String versionData) {
        this.versionData = versionData;
    }

    @Override
    public String toString() {
        return "VersionDTO{" + "versionName='" + versionName + '\'' + ", versionData='" + versionData + '\'' + '}';
    }
}
