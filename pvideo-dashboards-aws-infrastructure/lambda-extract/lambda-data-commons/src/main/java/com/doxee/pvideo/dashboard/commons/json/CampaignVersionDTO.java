package com.doxee.pvideo.dashboard.commons.json;

public class CampaignVersionDTO {

    private String client;

    private String procedure;

    private boolean production;

    private String version;

    public CampaignVersionDTO(String client, String procedure, String scope, String version) {
        this.procedure = procedure;
        this.client = client;

        if (scope == null) {
            this.production = false;
            return;
        }

        String scopeNormalized = scope.toLowerCase();
        if ("test".equals(scopeNormalized)) {
            this.production = false;
        } else {
            this.production = true;
        }

        this.version = version;
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

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getScopeString() {
        return production ? "PRODUCTION" : "TEST";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((client == null) ? 0 : client.hashCode());
        result = prime * result + ((procedure == null) ? 0 : procedure.hashCode());
        result = prime * result + (production ? 1231 : 1237);
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CampaignVersionDTO other = (CampaignVersionDTO) obj;
        if (client == null) {
            if (other.client != null)
                return false;
        } else if (!client.equals(other.client))
            return false;
        if (procedure == null) {
            if (other.procedure != null)
                return false;
        } else if (!procedure.equals(other.procedure))
            return false;
        if (production != other.production)
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

}
