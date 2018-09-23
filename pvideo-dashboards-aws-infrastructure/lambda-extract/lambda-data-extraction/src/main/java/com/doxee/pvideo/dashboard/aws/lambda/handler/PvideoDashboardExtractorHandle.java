package com.doxee.pvideo.dashboard.aws.lambda.handler;

import java.io.IOException;
import java.lang.System;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import com.doxee.pvideo.dashboard.aws.lambda.handler.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.json.Jackson;
import com.doxee.pvideo.dashboard.aws.lambda.handler.exception.PvideoDashboardException;
import com.doxee.pvideo.dashboard.aws.lambda.handler.mapper.ConfigurationItem;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.Classifier;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.DataSet;
import com.doxee.pvideo.dashboard.commons.json.CampaignVersionDTO;
import com.doxee.pvideo.dashboard.commons.json.ConfigurationEvent;
import com.doxee.pvideo.dashboard.commons.json.EmailDTO;
import com.doxee.pvideo.dashboard.commons.json.ExtractionDTO;
import com.doxee.pvideo.dashboard.commons.json.VideoDTO;

public class PvideoDashboardExtractorHandle implements RequestHandler<Object, String> {

    private static Logger log = Logger.getLogger(PvideoDashboardExtractorHandle.class);

    private static final AmazonRedshift redshiftClient = new AmazonRedshift();

    private static final AmazonS3 s3Client = new AmazonS3();

    public String handleRequest(Object input, Context context) {
        log.info("|pvideo-dashboard| Received pvideo extraction request. Input: " + input);
        try {

            ConfigurationEvent configEvent = validateInputEvent(input);

            run(configEvent);

            log.info("|pvideo-dashboard| Extraction completed successfully");
            return "Success";

        } catch (RuntimeException e) {
            log.warn("Execution error handled properly. Propagate...");
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
            log.warn("Execution error not handled. Propagate a internal server error...");
            throw new PvideoDashboardException(Constants.INTERNAL_SERVER_ERROR, e);
        }
    }

    private void run(ConfigurationEvent configEvent) {
        try (Connection connection = redshiftClient.getConnection(System.getenv(Constants.CLUSTER_ID_CA_ENV))) {
            List<String> campaigns = configEvent.getCampaigns();
            List<ConfigurationItem> campaignConfigurations;
            // Env variable
            String bucketName = System.getenv(Constants.BUCKET_NAME_ENV) != null ? System.getenv(Constants.BUCKET_NAME_ENV) : Constants.BUCKET_NAME;
            log.info("|pvideo-dashboard| bucket name " + bucketName);
            String tableName = System.getenv(Constants.CONFIGURATION_TABLE_ENV) != null ? System.getenv(Constants.CONFIGURATION_TABLE_ENV) : Constants.CONFIGURATION_TABLENAME;

            // Run the query to get configuration data from Dynamodb
            if (configEvent.getCampaigns() == null || configEvent.getCampaigns().isEmpty()) {
                log.info("|pvideo-dashboard| Received pvideo extraction request for all campaigns");
                campaignConfigurations = AmazonDynamoDB.getConfigurationForAllCampaigns(tableName);
            } else {
                log.info("|pvideo-dashboard| Received pvideo extraction request for campaign : " + campaigns.size());
                campaignConfigurations = AmazonDynamoDB.getConfigurationForCampaigns(tableName, campaigns);
            }

            List<ConfigurationItem> campaignConfigurationsOrder = new ArrayList<>();
            // sort in asc order
            campaignConfigurationsOrder.addAll(campaignConfigurations);
            Collections.sort(campaignConfigurationsOrder, new Comparator<ConfigurationItem>() {

                @Override
                public int compare(ConfigurationItem o1, ConfigurationItem o2) {
                    if (o1.getLastCreated() == null) {
                        return (o2.getLastCreated() == null) ? 0 : -1;
                    }
                    if (o2.getLastCreated() == null) {
                        return 1;
                    }
                    return o1.getLastCreated().compareTo(o2.getLastCreated());
                }
            });

            // Get Redshift connection
            String templateName = null;
            long lastCreatedCampaign = 0;
            long updateFrequencyMin = 0;
            // for every campaigns in json input configuration
            for (ConfigurationItem campaignConfiguration : campaignConfigurationsOrder) {

                try {
                    List<ExtractionDTO> procedureExtractionTest = new ArrayList<>();
                    List<ExtractionDTO> procedureExtractionProduction = new ArrayList<>();

                    String clientName = Utils.getClientNameFromProcKey(campaignConfiguration.getProcedureKey());
                    String procedureName = Utils.getProcedureNameFromProcKey(campaignConfiguration.getProcedureKey());

                    templateName = campaignConfiguration.getTemplateName() != null ? campaignConfiguration.getTemplateName() : PVideoTemplate.Default.name();
                    log.info("|pvideo-dashboard| templateName : " + templateName + " for campaign : " + campaignConfiguration.getId());

                    // starting query at....
                    long queryTime = System.currentTimeMillis();
                    lastCreatedCampaign = campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0;
                    updateFrequencyMin = campaignConfiguration.getUpdateFrequencyMin() != null ? campaignConfiguration.getUpdateFrequencyMin() : 0;
                    // skip if client cron
                    if (queryTime - lastCreatedCampaign < updateFrequencyMin * 60000) {
                        log.info("|pvideo-dashboard| skip extraction for campaign : " + campaignConfiguration.getId() + " lastExtraction: "
                                + new Date(campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0) + " freq mins. "
                                + campaignConfiguration.getUpdateFrequencyMin());
                        continue;
                    } else {
                        log.info("|pvideo-dashboard| Extraction metrics for campaign: " + campaignConfiguration.getProcedureKey() + " lastExtraction: "
                                + new Date(campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0));
                    }

                    // Qui estraggo la mappa Campagna-List<versions>
                    List<CampaignVersionDTO> campaignsVersions = extractVersionsForCampaign(campaignConfiguration, lastCreatedCampaign, connection, queryTime);

                    if (campaignsVersions.isEmpty()) {
                        log.info("|pvideo-dashboard| no versions for campaign   " + campaignConfiguration.getId());
                    }

                    // set sql folder for Campaign
                    String procedurePath = clientName.concat(AmazonS3.pathSeparator).concat(procedureName);

                    if (s3Client.checkExist(bucketName, procedurePath.concat(AmazonS3.pathSeparator).concat("sql/versions.sql"))) {
                        Query.getInstance().setLoadSQLfromS3Path(true);
                        Query.getInstance().setBucketName(bucketName);
                        Query.getInstance().setProcedurePath(procedurePath);
                        log.info("|pvideo-dashboard| Load sql from s3 : " + procedurePath);
                    } else {
                        Query.getInstance().setLoadSQLfromS3Path(false);
                        log.info("|pvideo-dashboard| Load sql from jar ");
                    }

                    Map<CampaignVersionDTO, ExtractionDTO> extractions = new HashMap<>();

                    // case no MD mail delivery
                    boolean noDeliveryEmails = templateName.equalsIgnoreCase(PVideoTemplate.TemplateNoEmails.name());
                    log.info("|pvideo-dashboard| case noDeliveryEmails ? " + noDeliveryEmails);

                    // extract produced purl
                    extractProducedPurl(extractions, connection, campaignConfiguration, queryTime, clientName, procedureName);

                    if (!noDeliveryEmails) {
                        extractMailMetrics(extractions, connection, campaignConfiguration, queryTime, clientName, procedureName);
                    }

                    extractVideoMetrics(extractions, connection, campaignConfiguration, queryTime, clientName, procedureName, templateName);

                    // extract video details - interactions and conversions
                    extractInteractionsAndConversions(extractions, connection, campaignConfiguration, queryTime, clientName, procedureName);

                    extractUserAgentMetrics(extractions, connection, campaignConfiguration, queryTime, clientName, procedureName);

                    extractEngagementMetrics(extractions, connection, campaignConfiguration, queryTime, clientName, procedureName);

                    // extract first day metrics -> subcase : 1) delivery with MD 2) delivery without MD
                    extractFirstDayMetrics(extractions, connection, campaignConfiguration, queryTime, clientName, procedureName, noDeliveryEmails);

                    // Ho una lista ExtractionDTO che è una lista di versioni. Siamo
                    // sempre nella stessa procedura quindi aggiorno il json sempre
                    // con lo stesso path

                    List<String> testVersions = new ArrayList<>();
                    List<String> prodVersions = new ArrayList<>();

                    for (ExtractionDTO extraction : extractions.values()) {

                        manageExtraction(bucketName, procedureExtractionTest, procedureExtractionProduction, procedurePath, testVersions, prodVersions, extraction, templateName);

                    }

                    // 1) Creo il file all
                    String filename = Utils.getJsonFileName(Constants.ALL_VERSION);
                    String s3ProdFilePath = Utils.getJsonFolder(bucketName, procedurePath, "prod");

                    List<String> versionsProd = new ArrayList<>();
                    ExtractionDTO allProd = new ExtractionDTO();
                    allProd.setLastUpdate(Utils.dateFormat.format(new Date(queryTime)));
                    allProd.setVersion(Constants.ALL_VERSION);
                    for (ExtractionDTO currentVersion : procedureExtractionProduction) {
                        if (!versionsProd.contains(currentVersion.getVersion())) {
                            versionsProd.add(currentVersion.getVersion());
                        }
                        JsonUtils.mergeContentData(currentVersion, allProd, templateName);

                    }
                    // Upload data: scope PROD
                    String dataProd = s3Client.get(s3ProdFilePath, filename);
                    if (!StringUtils.isEmpty(dataProd)) {
                        allProd = JsonUtils.mergeContentData(allProd, Jackson.fromJsonString(dataProd, ExtractionDTO.class), templateName);
                    }
                    s3Client.uploadFile(filename, s3ProdFilePath, Jackson.toJsonPrettyString(allProd));

                    String s3TestFilePath = Utils.getJsonFolder(bucketName, procedurePath, "test");
                    List<String> versionsTest = new ArrayList<>();
                    ExtractionDTO allTest = new ExtractionDTO();
                    allTest.setLastUpdate(Utils.dateFormat.format(new Date(queryTime)));
                    allTest.setVersion(Constants.ALL_VERSION);
                    for (ExtractionDTO currentVersion : procedureExtractionTest) {
                        if (!versionsTest.contains(currentVersion.getVersion())) {
                            versionsTest.add(currentVersion.getVersion());
                        }
                        JsonUtils.mergeContentData(currentVersion, allTest, templateName);
                    }
                    // Upload data: scope TEST
                    String dataTest = s3Client.get(s3TestFilePath, filename);
                    if (!StringUtils.isEmpty(dataTest)) {
                        allTest = JsonUtils.mergeContentData(allTest, Jackson.fromJsonString(dataTest, ExtractionDTO.class), templateName);
                    }
                    s3Client.uploadFile(filename, s3TestFilePath, Jackson.toJsonPrettyString(allTest));

                    // 2) Update su dynamo
                    if (!testVersions.contains(Constants.ALL_VERSION_LABEL)) {
                        testVersions.add(0, Constants.ALL_VERSION_LABEL);
                    }

                    if (!prodVersions.contains(Constants.ALL_VERSION_LABEL)) {
                        prodVersions.add(0, Constants.ALL_VERSION_LABEL);
                    }

                    AmazonDynamoDB.updateItem(campaignConfiguration.getProcedureKey(), queryTime, tableName, testVersions, prodVersions);

                } catch (Exception e) {
                    log.error("An error occurred during extraction metrics data! for " + campaignConfiguration.getProcedureKey());
                    log.error(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
            log.warn("An error occurred during extraction metrics data! ");
            throw new PvideoDashboardException(Constants.INTERNAL_SERVER_ERROR, e);
        }
    }

    private void manageExtraction(String bucketName, List<ExtractionDTO> procedureExtractionTest, List<ExtractionDTO> procedureExtractionProduction, String procedurePath, List<String> testVersions,
            List<String> prodVersions, ExtractionDTO extraction, String templateName) throws PvideoDashboardException {
        String filename = Utils.getJsonFileName(extraction.getVersion());

        String s3FilePath = Utils.getJsonFolder(bucketName, procedurePath, "PRODUCTION".equals(extraction.getScope()) ? "prod" : "test");

        log.info("|pvideo-dashboard| File path " + s3FilePath);

        if ("PRODUCTION".equals(extraction.getScope())) {
            procedureExtractionProduction.add(extraction);

            if (!prodVersions.contains(extraction.getVersion())) {
                prodVersions.add(extraction.getVersion());
            }

        } else {
            procedureExtractionTest.add(extraction);

            if (!testVersions.contains(extraction.getVersion())) {
                testVersions.add(extraction.getVersion());
            }
        }

        // Prendo il file data che è l'ultimo
        log.info("|pvideo-dashboard| get s3 file  " + s3FilePath + AmazonS3.pathSeparator + filename);
        String data = s3Client.get(s3FilePath, filename);

        if (!StringUtils.isEmpty(data)) {
            // Merge del file
            log.info("|pvideo-dashboard| merge partial with  " + s3FilePath + AmazonS3.pathSeparator + filename);
            ExtractionDTO merged = JsonUtils.mergeContentData(extraction, Jackson.fromJsonString(data, ExtractionDTO.class), templateName);
            s3Client.uploadFile(filename, s3FilePath, Jackson.toJsonPrettyString(merged));

        } else {
            log.info("|pvideo-dashboard|  s3 file  " + s3FilePath + AmazonS3.pathSeparator + filename + " does not exist, version is new ");
            s3Client.uploadFile(filename, s3FilePath, Jackson.toJsonPrettyString(extraction));
        }
    }

    private void extractProducedPurl(Map<CampaignVersionDTO, ExtractionDTO> extractions, Connection connection, ConfigurationItem campaignConfiguration, long queryTime, String clientName,
            String procedureName) throws SQLException, IOException {

        Map<String, String> queryMap = Query.getQueryPurlProdClicked();
        String query;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {

            for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                query = entry.getValue();
                log.info("|pvideo-dashboard| extract  ProducedClickedPurl " + entry.getKey() + Constants.LOGGING_CAMPAIGN + campaignConfiguration.getProcedureKey() + Constants.LOGGING_FROM_DATE
                        + new Date(campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0));

                long lastCreated = campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0;

                stmt = connection.prepareStatement(query);
                stmt.setTimestamp(1, new Timestamp(lastCreated));// data_inizio
                stmt.setTimestamp(2, new Timestamp(queryTime));// data_fine
                stmt.setString(3, clientName);// client_name
                stmt.setString(4, procedureName);// procedure_name
                stmt.setTimestamp(5, new Timestamp(lastCreated));// data_inizio
                stmt.setString(6, clientName);// client_name
                stmt.setString(7, procedureName);// procedure_name
                stmt.setString(8, clientName);// client_name
                stmt.setString(9, procedureName);// procedure_name

                rs = stmt.executeQuery();

                while (rs.next()) {
                    String version = rs.getString(Constants.QUERY_PARAMETER_VERSION);
                    if (version == null) {
                        continue;
                    }

                    int totalProd = rs.getInt(Constants.QUERY_PARAMETER_TOTAL_UNIQUE);
                    String scope = rs.getString(Constants.QUERY_PARAMETER_SCOPE);

                    // Creo il CampaignVersionDTO
                    CampaignVersionDTO cv = new CampaignVersionDTO(clientName, procedureName, scope, version);
                    ExtractionDTO current = getCurrentExtraction(extractions, queryTime, cv);

                    log.info("|pvideo-dashboard| extract Produced Purl  found version " + version + " type " + entry.getKey() + " totalProd " + totalProd);

                    if (current != null) {
                        current.getEmail().setPurlProduced(totalProd);
                    }
                }
            }

        } finally {
            // Finally block to close resources.
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                log.error(e.getMessage() + ExceptionUtils.getStackTrace(e));
            }
        }

    }

    private void extractMailMetrics(Map<CampaignVersionDTO, ExtractionDTO> extractions, Connection connection, ConfigurationItem campaignConfiguration, long queryTime, String clientName,
            String procedureName) throws SQLException, IOException {

        Map<String, String> queryMap = Query.getQueryMailMap();
        String query;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {

            for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                query = entry.getValue();
                log.info("|pvideo-dashboard| extract MailMetricsForClient " + entry.getKey() + Constants.LOGGING_CAMPAIGN + campaignConfiguration.getProcedureKey() + Constants.LOGGING_FROM_DATE
                        + new Date(campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0));

                stmt = createQueryAndSetParameters(connection, campaignConfiguration, queryTime, clientName, procedureName, query, entry.getKey());

                rs = stmt.executeQuery();

                while (rs.next()) {
                    String version = rs.getString(Constants.QUERY_PARAMETER_VERSION);

                    if (version == null) {
                        continue;
                    }

                    int totalUnique = rs.getInt(Constants.QUERY_PARAMETER_TOTAL_UNIQUE);
                    String scope = rs.getString(Constants.QUERY_PARAMETER_SCOPE);

                    // Creo il CampaignVersionDTO
                    CampaignVersionDTO cv = new CampaignVersionDTO(clientName, procedureName, scope, version);
                    ExtractionDTO current = getCurrentExtraction(extractions, queryTime, cv);

                    log.info("|pvideo-dashboard| extract MailMetricsForClient  found version " + version + " type " + entry.getKey() + " totalUnique " + totalUnique);
                    if (current != null) {
                        if (entry.getKey().equalsIgnoreCase(Constants.SENT))
                            current.getEmail().setSent(totalUnique);// email
                                                                    // spedite
                        else if (entry.getKey().equalsIgnoreCase(Constants.OPEN))
                            current.getEmail().setOpened(totalUnique);// mail
                                                                      // aperte
                        else if (entry.getKey().equalsIgnoreCase(Constants.CLICK))
                            current.getEmail().setClicks(totalUnique);// click
                                                                      // purl
                                                                      // nella
                                                                      // mail
                                                                      // univoci
                        else if (entry.getKey().equalsIgnoreCase(Constants.BOUNCE))
                            current.getEmail().setBounces(totalUnique);// mail
                                                                       // ko
                    }
                }
            }

            for (ExtractionDTO entry : extractions.values()) {
                EmailDTO emailDTO = entry.getEmail();
                emailDTO.setDelivered(emailDTO.getSent() - emailDTO.getBounces()); // spedite
                                                                                   // -
                                                                                   // bounce
                emailDTO.setOpenRate(Utils.rateTwoDecimalsPercentual(emailDTO.getOpened(), emailDTO.getSent()));// spedite/aperte
                emailDTO.setClickRate(Utils.rateTwoDecimalsPercentual(emailDTO.getClicks(), emailDTO.getOpened()));// click
                // purl
                // /
                // email
                // aperta
                emailDTO.setBounceRate(Utils.rateTwoDecimalsPercentual(emailDTO.getBounces(), emailDTO.getSent()));// mail
                // ko
                // /
                // mail
                // spedite
            }

        } finally {
            // Finally block to close resources.
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                log.error(e.getMessage() + ExceptionUtils.getStackTrace(e));
            }
        }

    }

    private ConfigurationEvent validateInputEvent(Object input) throws PvideoDashboardException {
        try {
            return Jackson.fromJsonString(Jackson.toJsonString(input), ConfigurationEvent.class);
        } catch (Exception e) {
            log.error("An error occurred parsing input json data! " + "\n" + ExceptionUtils.getStackTrace(e));
            throw new PvideoDashboardException("An error occurred parsing input json data! ");
        }

    }

    private List<CampaignVersionDTO> extractVersionsForCampaign(ConfigurationItem campaignConfiguration, long lastCreated, Connection connection, long queryTime) throws PvideoDashboardException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<CampaignVersionDTO> campaigns = new ArrayList<>();
        try {
            // client Name

            String clientName = Utils.getClientNameFromProcKey(campaignConfiguration.getProcedureKey());
            String procedureName = Utils.getProcedureNameFromProcKey(campaignConfiguration.getProcedureKey());

            String query = Query.getInstance().getVersions();
            log.info("|pvideo-dashboard| extract Versions For Campaign  " + campaignConfiguration.getProcedureKey() + " from time " + new Date(lastCreated));
            stmt = connection.prepareStatement(query);
            stmt.setString(1, clientName);
            stmt.setString(2, procedureName);
            stmt.setTimestamp(3, new Timestamp(lastCreated));
            stmt.setTimestamp(4, new Timestamp(queryTime));

            rs = stmt.executeQuery();

            CampaignVersionDTO campaignVersion = null;
            // foreach version
            while (rs.next()) {
                String version = rs.getString(Constants.QUERY_PARAMETER_VERSION);
                if (StringUtils.isEmpty(version)) {
                    continue;
                }
                String client = rs.getString("client_name");
                String procedure = rs.getString("procedure_name");
                String scope = rs.getString(Constants.QUERY_PARAMETER_SCOPE);
                log.info("|pvideo-dashboard| extract CampaignForClient  found version:" + version + " camp title " + client + "/" + procedure + " scope " + scope);
                campaignVersion = new CampaignVersionDTO(client, procedure, scope, version);
                campaigns.add(campaignVersion);

            }
        } catch (SQLException e) {
            log.error(e.getMessage() + " " + ExceptionUtils.getStackTrace(e));
            throw new PvideoDashboardException("|pvideo-dashboard| Error running query.");
        } catch (Exception e) {
            log.error(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
            throw new PvideoDashboardException("Internal Server Error");
        } finally {
            // Finally block to close resources.
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                log.error(e.getMessage() + "" + ExceptionUtils.getStackTrace(e));
            }
        }
        return campaigns;
    }

    private void extractInteractionsAndConversions(Map<CampaignVersionDTO, ExtractionDTO> extractions, Connection connection, ConfigurationItem campaignConfiguration, long queryTime,
            String clientName, String procedureName) throws SQLException, PvideoDashboardException, IOException {

        Map<String, String> queryMap = Query.getQueryConversionsAndInteractionsMap();
        String query = "";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {

            for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                query = entry.getValue();

                long lastCreated = campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0;
                log.info("|pvideo-dashboard| extract InteractionsAndConversionsForClient " + entry.getKey() + Constants.LOGGING_CAMPAIGN + campaignConfiguration.getId() + Constants.LOGGING_FROM_DATE
                        + new Date(lastCreated));

                stmt = connection.prepareStatement(query);
                stmt.setTimestamp(1, new Timestamp(lastCreated));// data_inizio
                stmt.setTimestamp(2, new Timestamp(queryTime));// data_fine
                stmt.setString(3, clientName);// client_name
                stmt.setString(4, procedureName);// procedure_name
                stmt.setTimestamp(5, new Timestamp(lastCreated));// data_inizio
                stmt.setString(6, clientName);// client_name
                stmt.setString(7, procedureName);// procedure_name
                stmt.setString(8, clientName);// client_name
                stmt.setString(9, procedureName);// procedure_name

                rs = stmt.executeQuery();

                String eventCode = "";
                while (rs.next()) {
                    String version = rs.getString(Constants.QUERY_PARAMETER_VERSION);
                    if (StringUtils.isEmpty(version)) {
                        continue;
                    }
                    String scope = rs.getString(Constants.QUERY_PARAMETER_SCOPE);

                    // Creo il CampaignVersionDTO
                    CampaignVersionDTO cv = new CampaignVersionDTO(clientName, procedureName, scope, version);
                    ExtractionDTO current = getCurrentExtraction(extractions, queryTime, cv);

                    List<String> list = new ArrayList<>();
                    eventCode = rs.getString("event_code");
                    int totalUnique = rs.getInt(Constants.QUERY_PARAMETER_TOTAL_UNIQUE);
                    int total = rs.getInt("total");
                    String eventCodeNormalized = Utils.getConversionOrInteractionName(eventCode);
                    list.add(eventCodeNormalized);
                    list.add(String.valueOf(totalUnique));
                    list.add(String.valueOf(Utils.rateTwoDecimalsPercentual(totalUnique, current.getVideo().getViewsUnique())));
                    log.info("|pvideo-dashboard| extract " + entry.getKey() + " ForClient  - version: " + version + " event " + eventCode + " total " + total + " total_unique " + totalUnique);
                    if (entry.getKey().equalsIgnoreCase(Constants.INTERACTIONS)) {
                        current.getInteractions().add(list);
                    } else if (entry.getKey().equalsIgnoreCase(Constants.CONVERSIONS)) {
                        current.getConversions().add(list);
                    }
                }
            }

            for (ExtractionDTO entry : extractions.values()) {
                manageExtractionConversionsInteractions(entry);
            }

        } finally {
            // Finally block to close resources.
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                log.error(e.getMessage() + "" + ExceptionUtils.getStackTrace(e));
            }
        }

    }

    private ExtractionDTO getCurrentExtraction(Map<CampaignVersionDTO, ExtractionDTO> extractions, long queryTime, CampaignVersionDTO cv) {
        ExtractionDTO current = extractions.get(cv);
        if (current == null) {
            current = new ExtractionDTO(cv);
            current.setLastUpdate(Utils.dateFormat.format(new Date(queryTime)));
            extractions.put(cv, current);
        }
        return current;
    }

    private void manageExtractionConversionsInteractions(ExtractionDTO entry) {
        long allInteractions = 0;// interaction for all version
        long allConversions = 0;// conversion for all version
        VideoDTO videoDTO = null;

        videoDTO = entry.getVideo();
        if (entry.getInteractions() != null) {
            for (List<String> interaction : entry.getInteractions()) {
                allInteractions = allInteractions + Long.parseLong(interaction.get(1));// titolo
                                                                                       // interactions,
                                                                                       // numero
                                                                                       // di
                                                                                       // eventi
                                                                                       // unici,
                                                                                       // totali,
                                                                                       // %rispetto
                                                                                       // eventi
                                                                                       // totali=
                                                                                       // eventi
                                                                                       // unici
                                                                                       // /
                                                                                       // eventi
                                                                                       // totali
            }
        }
        if (entry.getConversions() != null) {
            for (List<String> conversion : entry.getConversions()) {
                allConversions = allConversions + Long.parseLong(conversion.get(1));// titolo
                                                                                    // interactions,
                                                                                    // numero
                                                                                    // di
                                                                                    // eventi
                                                                                    // unici,
                                                                                    // totali,
                                                                                    // %rispetto
                                                                                    // eventi
                                                                                    // totali=
                                                                                    // eventi
                                                                                    // unici
                                                                                    // /
                                                                                    // eventi
                                                                                    // totali
            }
        }
        // somma conversions su versione
        videoDTO.setConversions(allConversions);// numero di click
                                                // univoci sulle
                                                // conversioni con
                                                // prefix
        // somma interaction su versione
        videoDTO.setInteractions(allInteractions);// numero di click
                                                  // sulle int prefix
        // conversion Rate (event unique)
        videoDTO.setConversionRate(Utils.rateTwoDecimalsPercentual(allConversions, videoDTO.getViewsUnique()));// conversioni
                                                                                                               // /
                                                                                                               // video
                                                                                                               // unique
                                                                                                               // views
        // interactions Rate (event unique)
        videoDTO.setInteractionRate(Utils.rateTwoDecimalsPercentual(allInteractions, videoDTO.getViewsUnique()));// conversioni
                                                                                                                 // /
                                                                                                                 // video
                                                                                                                 // unique
                                                                                                                 // views
    }

    private PreparedStatement createQueryAndSetParameters(Connection connection, ConfigurationItem campaignConfiguration, long queryTime, String clientName, String procedureName, String query,
            String key) throws SQLException {
        PreparedStatement stmt;

        long lastCreated = campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0;
        stmt = connection.prepareStatement(query);
        // key != null for mail event
        if (key != null && (key.equalsIgnoreCase(Constants.BOUNCE) || key.equalsIgnoreCase(Constants.SENT))) {
            stmt.setTimestamp(1, new Timestamp(lastCreated));// data_inizio
            stmt.setTimestamp(2, new Timestamp(queryTime));// data_fine
            stmt.setString(3, clientName);// client_name
            stmt.setString(4, procedureName);// procedure_name
            stmt.setTimestamp(5, new Timestamp(lastCreated));// data_inizio
            stmt.setTimestamp(6, new Timestamp(queryTime));// data_fine
            stmt.setString(7, clientName);// client_name
            stmt.setString(8, procedureName);// procedure_name
            stmt.setTimestamp(9, new Timestamp(lastCreated));// data_inizio
            stmt.setString(10, clientName);// client_name
            stmt.setString(11, procedureName);// procedure_name
            stmt.setTimestamp(12, new Timestamp(lastCreated));// data_inizio
            stmt.setString(13, clientName);// client_name
            stmt.setString(14, procedureName);// procedure_name
            stmt.setString(15, clientName);// client_name
            stmt.setString(16, procedureName);// procedure_name
        } else if (key != null && (key.equalsIgnoreCase(Constants.OPEN) || key.equalsIgnoreCase(Constants.CLICK))) {
            stmt.setTimestamp(1, new Timestamp(lastCreated));// data_inizio
            stmt.setTimestamp(2, new Timestamp(queryTime));// data_fine
            stmt.setString(3, clientName);// client_name
            stmt.setString(4, procedureName);// procedure_name
            stmt.setTimestamp(5, new Timestamp(lastCreated));// data_inizio
            stmt.setString(6, clientName);// client_name
            stmt.setString(7, procedureName);// procedure_name
            stmt.setString(8, clientName);// client_name
            stmt.setString(9, procedureName);// procedure_name
        } else {
            stmt.setTimestamp(1, new Timestamp(lastCreated));// load_date
            stmt.setTimestamp(2, new Timestamp(queryTime));
            stmt.setTimestamp(3, new Timestamp(lastCreated));
            stmt.setString(4, clientName);
            stmt.setString(5, procedureName);
        }

        return stmt;
    }

    private PreparedStatement createQueryAndSetParameters(Connection connection, ConfigurationItem campaignConfiguration, long queryTime, String clientName, String procedureName, String query)
            throws SQLException {

        return createQueryAndSetParameters(connection, campaignConfiguration, queryTime, clientName, procedureName, query, null);
    }

    private void extractVideoMetrics(Map<CampaignVersionDTO, ExtractionDTO> extractions, Connection connection, ConfigurationItem campaignConfiguration, long queryTime, String clientName,
            String procedureName, String templateName) throws SQLException, PvideoDashboardException, IOException {

        Map<String, String> queryMap = Query.getQueryVideoMailMap();
        String query = "";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                query = entry.getValue();

                long lastCreated = campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0;
                log.info("|pvideo-dashboard| extract VideoMetricsForClient " + entry.getKey() + Constants.LOGGING_CAMPAIGN + campaignConfiguration.getId() + Constants.LOGGING_FROM_DATE
                        + new Date(lastCreated));
                stmt = connection.prepareStatement(query);
                stmt.setTimestamp(1, new Timestamp(lastCreated));// data_inizio
                stmt.setTimestamp(2, new Timestamp(queryTime));// data_fine
                stmt.setString(3, clientName);// client_name
                stmt.setString(4, procedureName);// procedure_name
                stmt.setTimestamp(5, new Timestamp(lastCreated));// data_inizio
                stmt.setString(6, clientName);// client_name
                stmt.setString(7, procedureName);// procedure_name
                stmt.setString(8, clientName);// client_name
                stmt.setString(9, procedureName);// procedure_name

                rs = stmt.executeQuery();

                // foreach version
                while (rs.next()) {
                    String version = rs.getString(Constants.QUERY_PARAMETER_VERSION);
                    if (StringUtils.isEmpty(version)) {
                        continue;
                    }
                    String scope = rs.getString(Constants.QUERY_PARAMETER_SCOPE);
                    int totalUnique = rs.getInt(Constants.QUERY_PARAMETER_TOTAL_UNIQUE);
                    int total = rs.getInt("total");
                    // Creo il CampaignVersionDTO
                    CampaignVersionDTO cv = new CampaignVersionDTO(clientName, procedureName, scope, version);
                    ExtractionDTO extractionDTO = getCurrentExtraction(extractions, queryTime, cv);
                    log.info("|pvideo-dashboard| extract VideoMetricsForClient  - version: " + version + " total: " + total + " total_unique: " + totalUnique);
                    if (entry.getKey().equalsIgnoreCase(Constants.VIEWUNIQUE_TOTAL)) {
                        extractionDTO.getVideo().setViews(total);
                        extractionDTO.getVideo().setViewsUnique(totalUnique);
                    }

                }
            }

            VideoDTO videoDTO = null;
            // extraction map contains key = version and value = ExtractionData
            // for version
            for (ExtractionDTO entry : extractions.values()) {
                videoDTO = entry.getVideo();
                videoDTO.setViewsUser(Utils.rateTwoDecimals(videoDTO.getViews(), videoDTO.getViewsUnique()));// numero
                                                                                                             // visualizzazione
                                                                                                             // medio
                                                                                                             // per
                                                                                                             // utente
                                                                                                             // view
                                                                                                             // totali/
                                                                                                             // view
                                                                                                             // uniche
                if (templateName.equalsIgnoreCase(PVideoTemplate.TemplateNoEmails.name())) {
                    videoDTO.setViewsRate((Utils.rateTwoDecimalsPercentual(videoDTO.getViewsUnique(), entry.getEmail().getPurlProduced())));
                } else {
                    videoDTO.setViewsRate((Utils.rateTwoDecimalsPercentual(videoDTO.getViewsUnique(), entry.getEmail().getDelivered())));// click
                    // su
                    // play
                    // univoci
                    // /
                    // mail
                    // consegnate
                }

            }

        } finally {
            // Finally block to close resources.
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                log.error(e.getMessage() + "" + ExceptionUtils.getStackTrace(e));
            }
        }

    }

    private void extractUserAgentMetrics(Map<CampaignVersionDTO, ExtractionDTO> extractions, Connection connection, ConfigurationItem campaignConfiguration, long queryTime, String clientName,
            String procedureName) {

        String query = "";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            query = Query.getInstance().getUseragentView();

            long lastCreated = campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0;
            log.info("|pvideo-dashboard|  extract UserAgentMetricsForClient campaign " + campaignConfiguration.getId() + Constants.LOGGING_FROM_DATE + new Date(lastCreated));
            stmt = connection.prepareStatement(query);
            stmt.setTimestamp(1, new Timestamp(lastCreated));// data_inizio
            stmt.setTimestamp(2, new Timestamp(queryTime));// data_fine
            stmt.setString(3, clientName);// client_name
            stmt.setString(4, procedureName);// procedure_name
            stmt.setTimestamp(5, new Timestamp(lastCreated));// data_inizio
            stmt.setString(6, clientName);// client_name
            stmt.setString(7, procedureName);// procedure_name
            stmt.setString(8, clientName);// client_name
            stmt.setString(9, procedureName);// procedure_name
            stmt.setString(10, clientName);// client_name
            stmt.setString(11, procedureName);// procedure_name

            rs = stmt.executeQuery();

            while (rs.next()) {
                String version = rs.getString(Constants.QUERY_PARAMETER_VERSION);
                if (StringUtils.isEmpty(version)) {
                    continue;
                }
                String scope = rs.getString(Constants.QUERY_PARAMETER_SCOPE);
                CampaignVersionDTO cv = new CampaignVersionDTO(clientName, procedureName, scope, version);
                ExtractionDTO extractionDTO = getCurrentExtraction(extractions, queryTime, cv);
                // visualizzazioni totali
                long videoUnique = extractionDTO.getVideo().getViewsUnique();
                int totalUnique = rs.getInt(Constants.QUERY_PARAMETER_TOTAL_UNIQUE);
                String userAgent = rs.getString("user_agent");
                log.info("|pvideo-dashboard| extract UserAgentMetricsForClient  version: " + version + " totalUnique: " + totalUnique + " userAgent: " + userAgent);

                Map<String, String> r = null;
                try {
                    r = Classifier.parse(userAgent);
                } catch (Exception e) {
                    log.info("|pvideo-dashboard| Error  parsing userAgent : " + userAgent);
                }
                // => "pc", "smartphone", "mobilephone", "appliance", "crawler",
                // "misc", "unknown"
                String category = Constants.UNDEFINED;
                String os = Constants.UNDEFINED;
                if (r != null) {
                    category = r.get(DataSet.DATASET_KEY_CATEGORY);
                    os = r.get(DataSet.DATASET_TYPE_OS);
                }

                log.trace("|pvideo-dashboard| extract UserAgentMetricsForClient  category: " + category + " os: " + os);
                if (category.equalsIgnoreCase(DataSet.DATASET_CATEGORY_PC)) {
                    extractionDTO.getDeviceType().setDesktop(extractionDTO.getDeviceType().getDesktop() + totalUnique);
                } else if (category.equalsIgnoreCase(DataSet.DATASET_CATEGORY_SMARTPHONE)) {
                    if (os.equalsIgnoreCase("iPad")) {
                        extractionDTO.getDeviceType().setTablet(extractionDTO.getDeviceType().getTablet() + totalUnique);
                    } else {
                        extractionDTO.getDeviceType().setMobile(extractionDTO.getDeviceType().getMobile() + totalUnique);
                    }
                } else if (category.equalsIgnoreCase(DataSet.DATASET_CATEGORY_MOBILEPHONE)) {
                    extractionDTO.getDeviceType().setMobile(extractionDTO.getDeviceType().getMobile() + totalUnique);
                } else {
                    // not detected
                    extractionDTO.getDeviceType().setOther(extractionDTO.getDeviceType().getOther() + totalUnique);
                    // extractionDTO.getDeviceType().setOther(
                    // (int) videoUnique - (extractionDTO.getDeviceType().getDesktop() + extractionDTO.getDeviceType().getMobile() + extractionDTO.getDeviceType().getTablet()));
                }
                // => os from user-agent, or carrier name of mobile phones
                // ["Android", "iOS", "Windows", "MacOS","Linux", "Other"]
                List<Integer> listOs = extractionDTO.getOperatingSystem().getData();
                if (os.equalsIgnoreCase("Android")) {
                    listOs.set(0, listOs.get(0) + totalUnique);
                } else if (os.equalsIgnoreCase("iOS") || os.equalsIgnoreCase("iPad") || os.equalsIgnoreCase("iPhone")) {
                    listOs.set(1, listOs.get(1) + totalUnique);
                } else if (os.contains("Win")) {
                    listOs.set(2, listOs.get(2) + totalUnique);
                } else if (os.contains("Mac")) {
                    listOs.set(3, listOs.get(3) + totalUnique);
                } else if (os.contains("Linux")) {
                    listOs.set(4, listOs.get(4) + totalUnique);
                } else if (os.contains("Windows Phone")) {
                    listOs.set(5, listOs.get(5) + totalUnique);
                } else {
                    // not detected
                    listOs.set(6, listOs.get(6) + totalUnique);
                    // listOs.set(6, (int) videoUnique - (listOs.get(0) + listOs.get(1) + listOs.get(2) + listOs.get(3) + listOs.get(4) + listOs.get(5)));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
        } finally {
            // Finally block to close resources.
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                log.error(e.getMessage() + "" + ExceptionUtils.getStackTrace(e));
            }
        }

    }

    private void extractEngagementMetrics(Map<CampaignVersionDTO, ExtractionDTO> extractions, Connection connection, ConfigurationItem campaignConfiguration, long queryTime, String clientName,
            String procedureName) throws SQLException, PvideoDashboardException, IOException {

        String query = "";
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            query = Query.getInstance().getEngagement();
            long lastCreated = campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0;
            log.info("|pvideo-dashboard| extract EngagementMetricsForClient campaign " + campaignConfiguration.getId() + Constants.LOGGING_FROM_DATE + new Date(lastCreated));

            stmt = connection.prepareStatement(query);
            stmt.setTimestamp(1, new Timestamp(lastCreated));// data_inizio
            stmt.setTimestamp(2, new Timestamp(queryTime));// data_fine
            stmt.setString(3, clientName);// client_name
            stmt.setString(4, procedureName);// procedure_name
            stmt.setTimestamp(5, new Timestamp(lastCreated));// data_inizio
            stmt.setString(6, clientName);// client_name
            stmt.setString(7, procedureName);// procedure_name
            stmt.setString(8, clientName);// client_name
            stmt.setString(9, procedureName);// procedure_name

            rs = stmt.executeQuery();

            // Get the data from the result set.
            int totalUnique = 0;
            String progress = "";
            ExtractionDTO extractionDTO = null;
            int pos = 0;
            Integer val = null;
            long videoUnique = 0;
            while (rs.next()) {
                String version = rs.getString(Constants.QUERY_PARAMETER_VERSION);
                if (StringUtils.isEmpty(version)) {
                    continue;
                }
                String scope = rs.getString(Constants.QUERY_PARAMETER_SCOPE);
                CampaignVersionDTO cv = new CampaignVersionDTO(clientName, procedureName, scope, version);
                extractionDTO = extractions.get(cv);
                if (extractionDTO == null) {
                    extractionDTO = new ExtractionDTO(cv);
                    extractionDTO.setLastUpdate(Utils.dateFormat.format(new Date(queryTime)));
                    extractions.put(cv, extractionDTO);
                }
                totalUnique = rs.getInt(Constants.QUERY_PARAMETER_TOTAL_UNIQUE);
                progress = rs.getString("PROGRESS");
                log.info("|pvideo-dashboard| extract EngagementMetricsForClient  - version: " + version + " totalUnique " + totalUnique + " progress " + progress);
                pos = Integer.parseInt(progress) / 10;
                val = extractionDTO.getEngagement().getData().get(pos - 1);
                extractionDTO.getEngagement().getData().set(pos - 1, val + totalUnique);
                // visualizzazioni video unique
                videoUnique = extractionDTO.getVideo().getViewsUnique();
                if (videoUnique != 0) {
                    // percentuale delle conversioni
                    extractionDTO.getEngagement().getRate().set(pos - 1, new Double(Utils.rateTwoDecimals(val + totalUnique, videoUnique)));
                }

            }
        } finally {
            // Finally block to close resources.
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                log.error(e.getMessage() + "" + ExceptionUtils.getStackTrace(e));
            }
        }
    }

    private void extractFirstDayMetrics(Map<CampaignVersionDTO, ExtractionDTO> extractions, Connection connection, ConfigurationItem campaignConfiguration, long queryTime, String clientName,
            String procedureName, boolean noDeliveryEmails) throws SQLException, PvideoDashboardException, IOException {

        Map<String, String> queryMap = Query.getQueryFirstDayMap();// OPEN_FIRST-VIEW_FIRST-CONV_FIRST
        if (noDeliveryEmails) {
            queryMap = Query.getQueryFirstDayMapNoDeliveryMails();// only conversions and views
        }
        String query;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        long lastCreated = 0;
        try {

            for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                query = entry.getValue();
                lastCreated = campaignConfiguration.getLastCreated() != null ? campaignConfiguration.getLastCreated() : 0;
                log.info("|pvideo-dashboard| extract FirstDay Metrics " + entry.getKey() + Constants.LOGGING_CAMPAIGN + campaignConfiguration.getId() + Constants.LOGGING_FROM_DATE + lastCreated
                        + " case noDeliveryMails=" + noDeliveryEmails);

                stmt = connection.prepareStatement(query);
                if (noDeliveryEmails) {
                    stmt.setTimestamp(1, new Timestamp(lastCreated));// data_inizio
                    stmt.setTimestamp(2, new Timestamp(queryTime));// data_fine
                    stmt.setString(3, clientName);// client_name
                    stmt.setString(4, procedureName);// procedure_name
                    stmt.setTimestamp(5, new Timestamp(lastCreated));// data_inizio

                    stmt.setString(6, clientName);// client_name
                    stmt.setString(7, procedureName);// procedure_name
                    stmt.setString(8, clientName);// client_name
                    stmt.setString(9, procedureName);// procedure_name
                    stmt.setString(10, clientName);// client_name
                    stmt.setString(11, procedureName);// procedure_name
                    stmt.setString(12, clientName);// client_name
                    stmt.setString(13, procedureName);// procedure_name
                    stmt.setString(14, clientName);// client_name
                    stmt.setString(15, procedureName);// procedure_name
                    stmt.setString(16, clientName);// client_name
                    stmt.setString(17, procedureName);// procedure_name
                    stmt.setString(18, clientName);// client_name
                    stmt.setString(19, procedureName);// procedure_name
                    stmt.setString(20, clientName);// client_name
                    stmt.setString(21, procedureName);// procedure_name
                    stmt.setString(22, clientName);// client_name
                    stmt.setString(23, procedureName);// procedure_name

                } else {
                    stmt.setTimestamp(1, new Timestamp(lastCreated));// data_inizio
                    stmt.setTimestamp(2, new Timestamp(queryTime));// data_fine
                    stmt.setString(3, clientName);// client_name
                    stmt.setString(4, procedureName);// procedure_name

                    stmt.setString(5, clientName);// client_name
                    stmt.setString(6, procedureName);// procedure_name
                    stmt.setString(7, clientName);// client_name
                    stmt.setString(8, procedureName);// procedure_name

                    stmt.setTimestamp(9, new Timestamp(lastCreated));// data_inizio
                    stmt.setString(10, clientName);// client_name
                    stmt.setString(11, procedureName);// procedure_name
                    stmt.setString(12, clientName);// client_name
                    stmt.setString(13, procedureName);// procedure_name
                }

                rs = stmt.executeQuery();

                // Get the data from the result set.
                ExtractionDTO extractionDTO = null;
                int totalUnique = 0;
                int dateHH = 0;
                int pos = 0;
                while (rs.next()) {
                    String version = rs.getString(Constants.QUERY_PARAMETER_VERSION);
                    if (StringUtils.isEmpty(version)) {
                        continue;
                    }
                    String scope = rs.getString(Constants.QUERY_PARAMETER_SCOPE);
                    CampaignVersionDTO cv = new CampaignVersionDTO(clientName, procedureName, scope, version);
                    extractionDTO = extractions.get(cv);
                    if (extractionDTO == null) {
                        extractionDTO = new ExtractionDTO(cv);
                        extractionDTO.setLastUpdate(Utils.dateFormat.format(new Date(queryTime)));
                        extractions.put(cv, extractionDTO);
                    }
                    dateHH = rs.getInt("date_hh");
                    totalUnique = rs.getInt(Constants.QUERY_PARAMETER_TOTAL_UNIQUE);
                    log.info("|pvideo-dashboard| extract " + entry.getKey() + " ForClient  - version: " + version + " scope " + scope + " dateHH " + dateHH + " total_unique " + totalUnique);
                    if (dateHH < 24 && extractionDTO != null && dateHH >= 0) {
                        pos = dateHH;
                        // primo array percentuali, Secondo array numeriche
                        // Terzo totali ovvero : email consegnate
                        // totali(rispetto a tutta la versione) * 100
                        if (entry.getKey().equalsIgnoreCase(Constants.OPEN_FIRST)) {
                            int current = extractionDTO.getFirstDay().getOpened().get(dateHH);
                            int newValue = current + totalUnique;
                            extractionDTO.getFirstDay().getOpened().set(dateHH, newValue);

                        } else if (entry.getKey().equalsIgnoreCase(Constants.VIEW_FIRST)) {
                            int current = extractionDTO.getFirstDay().getViews().get(dateHH);
                            int newValue = current + totalUnique;
                            extractionDTO.getFirstDay().getViews().set(dateHH, newValue);

                        } else if (entry.getKey().equalsIgnoreCase(Constants.CONV_FIRST)) {

                            int current = extractionDTO.getFirstDay().getConversions().get(dateHH);
                            int newValue = current + totalUnique;
                            extractionDTO.getFirstDay().getConversions().set(dateHH, newValue);
                        }
                    } else {
                        log.warn("|pvideo-dashboard| not found map item for version: " + version + "or date_HH >24");
                    }
                }
            }

        } finally {
            // Finally block to close resources.
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                log.error(e.getMessage() + "" + ExceptionUtils.getStackTrace(e));
            }
        }

    }

}
