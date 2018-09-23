package com.doxee.pvideo.dashboard.aws.lambda.handler.utils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.s3.model.Region;
import com.doxee.pvideo.dashboard.aws.lambda.handler.mapper.ConfigurationItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AmazonDynamoDB {

    private final static com.amazonaws.services.dynamodbv2.AmazonDynamoDB dynamoClient = AmazonDynamoDBClientBuilder.defaultClient();

    // new AmazonDynamoDBClient(new AWSCredentials() {
    //
    // @Override
    // public String getAWSSecretKey() {
    // return "sB+u3Ec11+xCCbiafLmfrFFryLvTcovcSsljQKcb";
    // }
    //
    // @Override
    // public String getAWSAccessKeyId() {
    // return "AKIAI4NLXYP6LWTZLMRQ";
    // }
    // });

    private static final Logger log = Logger.getLogger(AmazonDynamoDB.class);

    public static void updateItem(String procedureKey, Long timestamp, String tableName, List<String> currentTestVersions, List<String> currentProdVersions) {
        try {
            log.info("|pvideo-dashboard| update Item configuration : " + procedureKey + " with " + timestamp);
            // Save the item.
            DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig.Builder().withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName)).build();
            DynamoDBMapper mapper = new DynamoDBMapper(dynamoClient, mapperConfig);

            // Retrieve the item.
            ConfigurationItem itemRetrieved = mapper.load(ConfigurationItem.class, procedureKey);

            log.debug("|pvideo-dashboard| Item retrieved : " + itemRetrieved);

            String testNewVersions = mergeVersions(itemRetrieved.getTestVersions(), currentTestVersions);
            String prodNewVersions = mergeVersions(itemRetrieved.getProdVersions(), currentProdVersions);

            itemRetrieved.setTestVersions(testNewVersions);
            itemRetrieved.setProdVersions(prodNewVersions);

            // Update the item.
            itemRetrieved.setLastCreated(timestamp);
            mapper.save(itemRetrieved);
            log.debug("|pvideo-dashboard| Item updated : " + itemRetrieved.getId());

        } catch (AmazonServiceException ase) {
            log.warn("Caught an AmazonServiceException, which " + "means your request made it " + "to Amazon  DynamoDB, but was rejected with an error response" + " for some reason.");
            log.warn("Error Message:    " + ase.getMessage());
            log.warn("HTTP Status Code: " + ase.getStatusCode());
            log.warn("AWS Error Code:   " + ase.getErrorCode());
            log.warn("Error Type:       " + ase.getErrorType());
            log.warn("Request ID:       " + ase.getRequestId());
            throw new RuntimeException("Internal Server Error");
        } catch (AmazonClientException ace) {
            log.warn("Caught an AmazonClientException, which " + "means the client encountered " + "an internal error while trying to " + "communicate with DynamoDB, "
                    + "such as not being able to access the network.");
            log.warn("Error Message: " + ace.getMessage());
            throw new RuntimeException("Internal Server Error");
        }
    }

    private static String mergeVersions(String oldVersionString, List<String> currentVersions) {

        String[] oldVersions = oldVersionString.split(",");

        for (String cv : oldVersions) {
            if (StringUtils.isNotBlank(cv) && StringUtils.isNotEmpty(cv) && !currentVersions.contains(cv)) {
                currentVersions.add(cv);
            }
        }
        //
        // if (!currentVersions.contains("ALL")) {
        // currentVersions.add("ALL");
        // }

        String result = StringUtils.join(currentVersions, ',');

        return result;
    }

    public static List<ConfigurationItem> getConfigurationForAllCampaigns(String tableName) {
        try {
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
            // .withIndexName("configuration")
                    .withConsistentRead(false);

            DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig.Builder().withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName)).build();
            // dynamoClient.setRegion(com.amazonaws.regions.Region.getRegion(Regions.EU_CENTRAL_1)); //DA RIMUOVERE!!!
            DynamoDBMapper mapper = new DynamoDBMapper(dynamoClient, mapperConfig);
            List<ConfigurationItem> replies = mapper.scan(ConfigurationItem.class, scanExpression);

            return replies;

        } catch (AmazonServiceException ase) {
            log.warn("Caught an AmazonServiceException, which " + "means your request made it " + "to Amazon DynamoDB, but was rejected with an error response" + " for some reason.");
            log.warn("Error Message:    " + ase.getMessage());
            log.warn("HTTP Status Code: " + ase.getStatusCode());
            log.warn("AWS Error Code:   " + ase.getErrorCode());
            log.warn("Error Type:       " + ase.getErrorType());
            log.warn("Request ID:       " + ase.getRequestId());
            throw new RuntimeException("Internal Server Error");
        } catch (AmazonClientException ace) {
            log.warn("Caught an AmazonClientException, which " + "means the client encountered " + "an internal error while trying to " + "communicate with DynamoDB, "
                    + "such as not being able to access the network.");
            log.warn("Error Message: " + ace.getMessage());
            throw new RuntimeException("Internal Server Error");
        }
    }

    public static List<ConfigurationItem> getConfigurationForCampaigns(String tableName, List<String> campaigns) {
        List<ConfigurationItem> itemList = new ArrayList<>();
        try {

            DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig.Builder().withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName)).build();

            DynamoDBMapper mapper = new DynamoDBMapper(dynamoClient, mapperConfig);

            for (String campaignName : campaigns) {
                ConfigurationItem configurationItem = new ConfigurationItem();

                configurationItem.setId(campaignName);
                DynamoDBQueryExpression<ConfigurationItem> queryExpression = new DynamoDBQueryExpression<ConfigurationItem>().withHashKeyValues(configurationItem);
                itemList = mapper.query(ConfigurationItem.class, queryExpression);

                for (ConfigurationItem item : itemList) {
                    log.debug("|pvideo-dashboard| configuration for  Clients  " + item.getId());
                }
            }

            return itemList;

        } catch (AmazonServiceException ase) {
            log.warn("Caught an AmazonServiceException, which " + "means your request made it " + "to Amazon DynamoDB, but was rejected with an error response" + " for some reason.");
            log.warn("Error Message:    " + ase.getMessage());
            log.warn("HTTP Status Code: " + ase.getStatusCode());
            log.warn("AWS Error Code:   " + ase.getErrorCode());
            log.warn("Error Type:       " + ase.getErrorType());
            log.warn("Request ID:       " + ase.getRequestId());
            throw new RuntimeException("Internal Server Error");
        } catch (AmazonClientException ace) {
            log.warn("Caught an AmazonClientException, which " + "means the client encountered " + "an internal error while trying to " + "communicate with DynamoDB, "
                    + "such as not being able to access the network.");
            log.warn("Error Message: " + ace.getMessage());
            throw new RuntimeException("Internal Server Error");
        }

    }

}
