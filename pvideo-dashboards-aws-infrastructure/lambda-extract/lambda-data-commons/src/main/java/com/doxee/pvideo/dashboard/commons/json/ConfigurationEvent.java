package com.doxee.pvideo.dashboard.commons.json;

import java.util.List;

public class ConfigurationEvent {

    private List<String> campaigns;

    private boolean loadSQLfromS3;

    public List<String> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<String> campaigns) {
        this.campaigns = campaigns;
    }

    public boolean isLoadSQLfromS3() {
        return loadSQLfromS3;
    }

    public void setLoadSQLfromS3(boolean loadSQLfromS3) {
        this.loadSQLfromS3 = loadSQLfromS3;
    }

    @Override
    public String toString() {
        return "ConfigurationEvent{" + "campaigns=" + campaigns + ", loadSQLfromS3=" + loadSQLfromS3 + '}';
    }
}
