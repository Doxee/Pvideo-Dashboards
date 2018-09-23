package com.doxee.pvideo.dashboard.aws.lambda.handler.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.util.IOUtils;

public class Query {

    private static final String LOGGING_PVIDEO_DASHBOARD_BUCKET = "|pvideo-dashboard| bucket ";

    private static final Logger log = Logger.getLogger(Query.class);

    private final static AmazonS3 s3Client = new AmazonS3();

    private static Query instance = null;

    private boolean loadSQLfromS3Path = false;

    private String bucketName;

    private String procedurePath;

    protected Query() {
    }

    public static Query getInstance() {
        if (instance == null) {
            instance = new Query();
        }
        return instance;
    }

    public static Map<String, String> getQueryMailMap() throws IOException {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(Constants.SENT, getInstance().getMailSent().trim());
        queryMap.put(Constants.OPEN, getInstance().getMailOpen().trim());
        queryMap.put(Constants.CLICK, getInstance().getMailClick().trim());
        queryMap.put(Constants.BOUNCE, getInstance().getMailBounce().trim());
        return queryMap;
    }

    public static Map<String, String> getQueryVideoMailMap() throws IOException {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(Constants.VIEWUNIQUE_TOTAL, getInstance().getViewUniqueAndTotal().trim());
        return queryMap;
    }

    public static Map<String, String> getQueryConversionsAndInteractionsMap() throws IOException {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(Constants.INTERACTIONS, getInstance().getVideoInteractions().trim());
        queryMap.put(Constants.CONVERSIONS, getInstance().getVideoConversions().trim());
        return queryMap;
    }

    public static Map<String, String> getQueryFirstDayMap() throws IOException {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(Constants.OPEN_FIRST, getInstance().getOpenFirst().trim());
        queryMap.put(Constants.VIEW_FIRST, getInstance().getViewFirst().trim());
        queryMap.put(Constants.CONV_FIRST, getInstance().getConvFirst().trim());
        return queryMap;
    }

    public static Map<String, String> getQueryFirstDayMapNoDeliveryMails() throws IOException {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(Constants.VIEW_FIRST, getInstance().getViewFirstNoEmails().trim());
        queryMap.put(Constants.CONV_FIRST, getInstance().getConvFirstNoEmails().trim());
        return queryMap;
    }

    public static Map<String, String> getQueryPurlProdClicked() throws IOException {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(Constants.PURL_PROD_CLICKED, getInstance().getPurlProducedClicked().trim());
        return queryMap;
    }

    // Purl produced / clicked
    public String getPurlProducedClicked() throws IOException {
        String resource = "sql/q_purl_prod.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug(LOGGING_PVIDEO_DASHBOARD_BUCKET + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    // first day
    public String getOpenFirst() throws IOException {
        String resource = "sql/q_open_first.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug(LOGGING_PVIDEO_DASHBOARD_BUCKET + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    public String getViewFirst() throws IOException {
        String resource = "sql/q_view_first.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug(LOGGING_PVIDEO_DASHBOARD_BUCKET + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    public String getConvFirst() throws IOException {
        String resource = "sql/q_conv_first.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug(LOGGING_PVIDEO_DASHBOARD_BUCKET + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    // first day no Emails
    public String getViewFirstNoEmails() throws IOException {
        String resource = "sql/q_view_first_nomails.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug(LOGGING_PVIDEO_DASHBOARD_BUCKET + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    public String getConvFirstNoEmails() throws IOException {
        String resource = "sql/q_conv_first_nomails.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug(LOGGING_PVIDEO_DASHBOARD_BUCKET + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    // mail
    public String getMailSent() throws IOException {
        String resource = "sql/q_sent.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug(LOGGING_PVIDEO_DASHBOARD_BUCKET + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    public String getMailOpen() throws IOException {
        String resource = "sql/q_open.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug(LOGGING_PVIDEO_DASHBOARD_BUCKET + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    public String getMailClick() throws IOException {
        String resource = "sql/q_click.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug(LOGGING_PVIDEO_DASHBOARD_BUCKET + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    public String getMailBounce() throws IOException {
        String resource = "sql/q_bounce.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug(LOGGING_PVIDEO_DASHBOARD_BUCKET + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    public String getUseragentView() throws IOException {
        String resource = "sql/q_view_useragent.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug(LOGGING_PVIDEO_DASHBOARD_BUCKET + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    // video
    public String getViewUniqueAndTotal() throws IOException {
        String resource = "sql/q_view.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug("|pvideo-dashboard| bucketName " + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    public String getVideoConversions() throws IOException {
        String resource = "sql/q_conversion.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug("|pvideo-dashboard| bucketName " + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    public String getVideoInteractions() throws IOException {
        String resource = "sql/q_interaction.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug("|pvideo-dashboard| bucketName " + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    // versions
    public String getVersions() throws IOException {
        String resource = "sql/versions.sql";
        // if(loadSQLfromS3Path){
        // return s3Client.get(bucketName, resource);
        // }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    // engagement
    public String getEngagement() throws IOException {
        String resource = "sql/q_engagement.sql";
        if (loadSQLfromS3Path) {
            resource = procedurePath.concat(AmazonS3.pathSeparator).concat(resource);
            log.debug("|pvideo-dashboard| bucketName " + bucketName + " " + resource);
            return s3Client.get(bucketName, resource);
        }
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

    public boolean isLoadSQLfromS3Path() {
        return loadSQLfromS3Path;
    }

    public void setLoadSQLfromS3Path(boolean loadSQLfromS3Path) {
        this.loadSQLfromS3Path = loadSQLfromS3Path;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getProcedurePath() {
        return procedurePath;
    }

    public void setProcedurePath(String clientPath) {
        this.procedurePath = clientPath;
    }
}
