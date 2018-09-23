package com.doxee.pvideo.dashboard.aws.lambda.handler.utils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class AmazonS3 {

    private final static com.amazonaws.services.s3.AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    // new AmazonS3Client(new AWSCredentials() {
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
    private static final Logger log = Logger.getLogger(AmazonS3.class);

    public static final String pathSeparator = "/";

    public void uploadFile(String filename, String bucketName, String json) {
        // filename = filename.replaceAll(" ","_");
        // bucketName = bucketName.replaceAll(" ","_");

        log.info("|pvideo-dashboard| Try to upload the file: " + filename + " within the bucket: " + bucketName);
        try {

            File tempFile = File.createTempFile(filename, "");
            tempFile.deleteOnExit();
            FileUtils.writeStringToFile(tempFile, json, Charset.defaultCharset());
            // Create a put request
            s3Client.putObject(new PutObjectRequest(bucketName, filename, tempFile));

        } catch (IOException e) {
            log.error(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
        } catch (AmazonServiceException ase) {
            log.warn("Caught an AmazonServiceException, which " + "means your request made it " + "to Amazon S3, but was rejected with an error response" + " for some reason.");
            log.warn("Error Message:    " + ase.getMessage());
            log.warn("HTTP Status Code: " + ase.getStatusCode());
            log.warn("AWS Error Code:   " + ase.getErrorCode());
            log.warn("Error Type:       " + ase.getErrorType());
            log.warn("Request ID:       " + ase.getRequestId());
            throw new RuntimeException("Internal Server Error");
        } catch (AmazonClientException ace) {
            log.warn("Caught an AmazonClientException, which " + "means the client encountered " + "an internal error while trying to " + "communicate with S3, "
                    + "such as not being able to access the network.");
            log.warn("Error Message: " + ace.getMessage());
            throw new RuntimeException("Internal Server Error");
        }
    }

    public String get(String bucketPath, String objectName) {
        try {
            // s3Client.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
            // bucketPath = bucketPath.replaceAll(" ","_");
            // objectName = objectName.replaceAll(" ","_");

            GetObjectRequest request = new GetObjectRequest(bucketPath, objectName);
            S3Object s3Object = s3Client.getObject(request);
            return IOUtils.toString(s3Object.getObjectContent(), "UTF-8");

        } catch (AmazonServiceException ase) {
            if (ase.getErrorCode().startsWith("NoSuchKey")) {
                log.info("error : NoSuchKey return null object     ");
                return null;
            }
            log.info("Error Message:    " + ase.getMessage());
            log.info("HTTP Status Code: " + ase.getStatusCode());
            log.info("AWS Error Code:   " + ase.getErrorCode());
            log.info("Error Type:       " + ase.getErrorType());
            log.info("Request ID:       " + ase.getRequestId());
            throw new RuntimeException("Internal Server Error");
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered " + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ace.getMessage(), ace);
            throw new RuntimeException("Internal Server Error");
        } catch (Exception exc) {
            log.error("Error Message: " + exc.getMessage(), exc);
            throw new RuntimeException("Internal Server Error");
        }
    }

    public boolean checkExist(String bucketPath, String objectName) {
        try {
            // bucketPath = bucketPath.replaceAll(" ","_");
            // objectName = objectName.replaceAll(" ","_");
            s3Client.getObjectMetadata(bucketPath, objectName);
        } catch (AmazonServiceException ase) {
            if (ase.getErrorCode().startsWith("404")) {
                log.info("checkExist false");
                return false;
            }
            log.error("Caught an AmazonServiceException, which means your request made it " + "to Amazon S3, but was rejected with an error response for some reason.", ase);
            throw new RuntimeException("Internal Server Error");
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered " + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ace.getMessage(), ace);
            throw new RuntimeException("Internal Server Error");
        } catch (Exception exc) {
            log.error("Error Message: " + exc.getMessage(), exc);
            throw new RuntimeException("Internal Server Error");
        }
        return true;
    }

}
