package com.doxee.pvideo.dashboard.commons.json;

public class CampaignTitleDTO {

    private String client;

    private String procedure;

    private boolean production;

    public CampaignTitleDTO(String client, String procedure, String scope) {
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

    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((client == null) ? 0 : client.hashCode());
        result = prime * result + ((procedure == null) ? 0 : procedure.hashCode());
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
        CampaignTitleDTO other = (CampaignTitleDTO) obj;
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
        return true;
    }

    @Override
    public String toString() {
        return "CampaignTitleDTO [client=" + client + ", procedure=" + procedure + "]";
    }

}
