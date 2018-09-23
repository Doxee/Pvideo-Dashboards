package com.doxee.pvideo.dashboard.commons.json;

import java.util.List;
import java.util.Objects;

public class CampaingInfoDTO {

    private String client;

    private String procedure;

    private String application;

    private String environment;

    private String version;

    private String lastUpdate;

    private List<List<String>> versions;

    public CampaingInfoDTO() {
        this.client = "";
        this.procedure = "";
        this.application = "";
        this.environment = "";
        this.version = "";
        this.lastUpdate = "";
        this.versions = versions;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<List<String>> getVersions() {
        return versions;
    }

    public void setVersions(List<List<String>> versions) {
        this.versions = versions;
    }

    @Override
    public String toString() {
        return "CampaingInfoDTO{" + "client='" + client + '\'' + ", procedure='" + procedure + '\'' + ", application='" + application + '\'' + ", environment='" + environment + '\'' + ", version='"
                + version + '\'' + ", lastUpdate='" + lastUpdate + '\'' + ", versions=" + versions + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CampaingInfoDTO that = (CampaingInfoDTO) o;
        return Objects.equals(client, that.client) && Objects.equals(procedure, that.procedure) && Objects.equals(application, that.application) && Objects.equals(environment, that.environment)
                && Objects.equals(version, that.version) && Objects.equals(lastUpdate, that.lastUpdate) && Objects.equals(versions, that.versions);
    }

    @Override
    public int hashCode() {

        return Objects.hash(client, procedure, application, environment, version, lastUpdate, versions);
    }
}
