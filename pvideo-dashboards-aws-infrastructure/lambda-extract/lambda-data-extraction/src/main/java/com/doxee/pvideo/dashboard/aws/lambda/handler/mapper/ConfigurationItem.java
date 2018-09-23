package com.doxee.pvideo.dashboard.aws.lambda.handler.mapper;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "PvideoDashboardCampaign")
public class ConfigurationItem {

    private String id;

    private String procedureKey;

    private Long updateFrequencyMin;

    private Long lastCreated;

    private String testVersions;

    private String prodVersions;

    private String templateName;

    // @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return procedureKey;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "updateFrequency")
    public Long getUpdateFrequencyMin() {
        return updateFrequencyMin;
    }

    public void setUpdateFrequencyMin(Long updateFrequencyMin) {
        this.updateFrequencyMin = updateFrequencyMin;
    }

    @DynamoDBAttribute(attributeName = "lastCreated")
    public Long getLastCreated() {
        return lastCreated;
    }

    public void setLastCreated(Long lastCreated) {
        this.lastCreated = lastCreated;
    }

    @DynamoDBHashKey(attributeName = "ProcedureKey")
    public String getProcedureKey() {
        return procedureKey;
    }

    public void setProcedureKey(String procedureKey) {
        this.procedureKey = procedureKey;
    }

    @DynamoDBAttribute(attributeName = "testVersions")
    public String getTestVersions() {
        return testVersions;
    }

    public void setTestVersions(String testVersions) {
        this.testVersions = testVersions;
    }

    @DynamoDBAttribute(attributeName = "prodVersions")
    public String getProdVersions() {
        return prodVersions;
    }

    public void setProdVersions(String prodVersions) {
        this.prodVersions = prodVersions;
    }

    @DynamoDBAttribute(attributeName = "templateName")
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public String toString() {
        return "ConfigurationItem{" + "id='" + id + '\'' + ", procedureKey='" + procedureKey + '\'' + ", updateFrequencyMin=" + updateFrequencyMin + ", lastCreated=" + lastCreated
                + ", testVersions='" + testVersions + '\'' + ", prodVersions='" + prodVersions + '\'' + ", templateName='" + templateName + '\'' + '}';
    }
}