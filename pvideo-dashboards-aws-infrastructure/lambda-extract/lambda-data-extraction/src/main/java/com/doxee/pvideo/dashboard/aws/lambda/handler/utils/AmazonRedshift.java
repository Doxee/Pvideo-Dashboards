package com.doxee.pvideo.dashboard.aws.lambda.handler.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.redshift.AmazonRedshiftClient;
import com.amazonaws.services.redshift.AmazonRedshiftClientBuilder;
import com.amazonaws.services.redshift.model.DescribeClustersRequest;
import com.amazonaws.services.redshift.model.DescribeClustersResult;
import com.amazonaws.services.redshift.model.GetClusterCredentialsRequest;
import com.amazonaws.services.redshift.model.GetClusterCredentialsResult;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class AmazonRedshift {

    private final static com.amazonaws.services.redshift.AmazonRedshift redshift = AmazonRedshiftClientBuilder.defaultClient();

    // new AmazonRedshiftClient(new AWSCredentials() {
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
    private static Logger log = Logger.getLogger(AmazonRedshift.class);

    public Connection getConnection(String clusterIdentifier) throws RuntimeException {
        String redshiftUser, redshiftUrl, redshiftPass;
        int redshiftPort;
        // Get Credentials
        try {
            // redshift.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
            DescribeClustersResult result = redshift.describeClusters(new DescribeClustersRequest().withClusterIdentifier(clusterIdentifier));
            redshiftUser = result.getClusters().get(0).getMasterUsername();
            String dbName = result.getClusters().get(0).getDBName();
            redshiftUrl = result.getClusters().get(0).getEndpoint().getAddress();
            redshiftPort = result.getClusters().get(0).getEndpoint().getPort();
            redshiftUrl = "jdbc:redshift:iam://" + redshiftUrl + ":" + redshiftPort + "/" + dbName;
            // redshiftUrl = "jdbc:redshift://ca-dev-frank-cloudanalyticsredshiftcluster-jlt2fskwsdso.cmisobua5tbi.eu-central-1.redshift.amazonaws.com:5439/caddbdev";
            GetClusterCredentialsResult credentials = redshift.getClusterCredentials(new GetClusterCredentialsRequest().withDbUser(redshiftUser).withDbName(dbName)
                    .withClusterIdentifier(clusterIdentifier).withAutoCreate(false).withDurationSeconds(900));
            redshiftPass = credentials.getDbPassword();
            // redshiftPass = "devPlll01";
            redshift.describeClusters(new DescribeClustersRequest().withClusterIdentifier(""));
            // Connect
            try {
                log.info("|pvideo-dashboard| Connecting to " + redshiftUrl + ", user: " + redshiftUser + ",dbName: " + dbName);
                Class.forName("com.amazon.redshift.jdbc42.Driver");
                // Open a connection and define properties.
                Properties props = new Properties();

                // Uncomment the following line if using a keystore.
                // props.setProperty("ssl", "true");
                props.setProperty("user", redshiftUser);
                props.setProperty("password", redshiftPass);
                log.debug("|pvideo-dashboard| getConnection ....");
                return DriverManager.getConnection(redshiftUrl, props);
            } catch (Exception e) {
                log.error(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
                log.warn("|pvideo-dashboard| Error during DB connection.");
                throw new RuntimeException("Internal Server Error");

            }
        } catch (Exception e) {
            log.error(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
            log.warn("|pvideo-dashboard| Error getting Redshift credentials.");
            throw new RuntimeException("Internal Server Error");
        }
    }
}
