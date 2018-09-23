package com.doxee.pvideo.dashboard.commons.json;

import java.util.ArrayList;
import java.util.List;

public class ExtractionDTO {

    private String campaignTitle;

    private String clientName;

    private String procedureName;

    private String scope;

    private String version;

    private String lastUpdate;

    private List<List<String>> versions;

    private EmailDTO email;

    private VideoDTO video;

    private FirstDayDTO firstDay;

    private List<List<String>> conversions; // titolo conversione, numero di eventi unici, NUMERO EVENTI TOTALI, %rispetto eventi totali= eventi unici / eventi totali

    private List<List<String>> interactions;

    private DeviceTypeDTO deviceType;

    private OperatingSystemDTO operatingSystem;

    private EngagementDTO engagement;

    public ExtractionDTO() {
        this.campaignTitle = "";
        this.clientName = "";
        this.version = "";
        this.lastUpdate = "";
        this.email = new EmailDTO();
        this.video = new VideoDTO();
        this.firstDay = new FirstDayDTO();
        // titolo conversione, numero di eventi unici, %rispetto eventi totali= eventi unici / eventi totali
        this.conversions = new ArrayList<>();
        this.interactions = new ArrayList<>();
        this.deviceType = new DeviceTypeDTO();
        this.operatingSystem = new OperatingSystemDTO();
        this.engagement = new EngagementDTO();
    }

    public ExtractionDTO(CampaignVersionDTO version) {
        this.campaignTitle = "";
        this.clientName = version.getClient();
        this.procedureName = version.getProcedure();
        this.scope = version.getScopeString();
        this.version = version.getVersion();
        this.lastUpdate = "";
        this.email = new EmailDTO();
        this.video = new VideoDTO();
        this.firstDay = new FirstDayDTO();
        // titolo conversione, numero di eventi unici, %rispetto eventi totali= eventi unici / eventi totali
        this.conversions = new ArrayList<>();
        this.interactions = new ArrayList<>();
        this.deviceType = new DeviceTypeDTO();
        this.operatingSystem = new OperatingSystemDTO();
        this.engagement = new EngagementDTO();
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCampaignTitle() {
        return campaignTitle;
    }

    public void setCampaignTitle(String campaignTitle) {
        this.campaignTitle = campaignTitle;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public List<List<String>> getVersions() {
        return versions;
    }

    public void setVersions(List<List<String>> versions) {
        this.versions = versions;
    }

    public EmailDTO getEmail() {
        return email;
    }

    public void setEmail(EmailDTO email) {
        this.email = email;
    }

    public VideoDTO getVideo() {
        return video;
    }

    public void setVideo(VideoDTO video) {
        this.video = video;
    }

    public FirstDayDTO getFirstDay() {
        return firstDay;
    }

    public void setFirstDay(FirstDayDTO firstDay) {
        this.firstDay = firstDay;
    }

    public List<List<String>> getConversions() {
        return conversions;
    }

    public void setConversions(List<List<String>> conversions) {
        this.conversions = conversions;
    }

    public List<List<String>> getInteractions() {
        return interactions;
    }

    public void setInteractions(List<List<String>> interactions) {
        this.interactions = interactions;
    }

    public DeviceTypeDTO getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceTypeDTO deviceType) {
        this.deviceType = deviceType;
    }

    public OperatingSystemDTO getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(OperatingSystemDTO operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public EngagementDTO getEngagement() {
        return engagement;
    }

    public void setEngagement(EngagementDTO engagement) {
        this.engagement = engagement;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "ExtractionDTO{" + "campaignTitle='" + campaignTitle + '\'' + ", clientName='" + clientName + '\'' + ", procedureName='" + procedureName + '\'' + ", scope='" + scope + '\''
                + ", version='" + version + '\'' + ", lastUpdate='" + lastUpdate + '\'' + ", versions=" + versions + ", email=" + email + ", video=" + video + ", firstDay=" + firstDay
                + ", conversions=" + conversions + ", interactions=" + interactions + ", deviceType=" + deviceType + ", operatingSystem=" + operatingSystem + ", engagement=" + engagement + '}';
    }
}
