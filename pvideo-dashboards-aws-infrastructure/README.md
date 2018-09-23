# Project Description
This repository contains the pVideo Dashboard components required in order to create and initialize the [Amazon Web Services][AWS] infrastructure.
In order to achieve this scope, the build task produces all the files mandatory to deploy all the AWS resources necessary for the cloud analytics to work properly.

## Configurations
####Lambda Environment variable:  

CONFIGURATION_TABLE: name of dynamoDB table for campaign configuration
CLUSTER_ID_CA: cluster id of Cloud Analitics Redshift installation
BUCKET_NAME: S3 root bucket (default pvideodashboard)


  
####Lambda Trigger event: 
settings Lambda event input as Constant (JSON text):
 
example:

this configuration extract metrics for selected campaigns: 
```
{

  "campaigns": [
  
    "env=envPV_CLARO_00,bp=PV_CLARO_00,proc=JAPI,ou=Doxee,p=Doxee Platform",
    
    "env=batch-test,bp=Pvideo Validation,proc=Paas Validation,ou=Doxee,p=Doxee Platform"
  ]
  
}
```

The configuration shown below is related to the extraction of all the metrics of all campaigns configured on
DynamoDB configuration table.
```
{

  "campaigns": []
  
}
 ```
####DynamoDB configuration item:

id = (S) value of environmentDn of campaign (es. "env=batch-test,bp=Pvideo Validation,proc=Paas Validation,ou=Doxee,p=Doxee Platform")

lastCreated = (N) value of last extracted metrics data,  in milliseconds (default 0)

updateFrequencyMin = (N) value of min. frequency update (in minute) configured for metrics

## AWS Resources
The core AWS resources involved in the installation are:
- [AWS DynamoDB][DynamoDB] that implements the datatable configuration engine;
- [AWS Lambda][Lambda] that offers a serverless environment to run on-demand code;
- [AWS Cloudwatch][Cloudwatch] that enables scheduled events to invoke lambda;
- [AWS S3][S3] that is a _"Simple Storage Service"_ that allows remote data storage;
- [AWS VPC][Vpc] that allows networks isolation;
- [AWS Cloudformation][Cloudformation] that is used to deploy AWS resources for a given configuration YAML/JSON file;  


# How to use
This project is managed through maven. 

In order to build the artifacts, run:

```
 $ mvn clean package
``` 

## Requirements
In order to manage the project correctly it is required to have:
- Java 8 installed (click [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) to download the software);
- Maven  installed (click [here](https://maven.apache.org/download.cgi) to download the software) and configured to use Java 8;

## Artifacts
The artifacts produced during the packaging are:

 1. A jar file lambda-data-extraction-version.jar containing the lambda execution code;
 
 
## Style Guide
 
Before adding new classes or making any changes to the code, the developer should configure their own IDE to use the Google Code Style which can be found on the following repository:

- https://github.com/google/styleguide

For IntelliJ users, you can follow this guide : https://www.jetbrains.com/help/idea/2016.3/configuring-code-style.html.
For Eclipse user, you can refer to this guide : http://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.cdt.doc.user%2Freference%2Fcdt_u_c_code_style_pref.htm

 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
[AWS]:https://aws.amazon.com
[Redshift]:https://aws.amazon.com/redshift/
[Lambda]:https://aws.amazon.com/lambda/
[ApiG]:https://aws.amazon.com/api-gateway/
[Cloudwatch]:https://aws.amazon.com/cloudwatch/
[S3]:https://aws.amazon.com/s3/
[DynamoDB]:https://aws.amazon.com/en/dynamodb/
[Vpc]:https://aws.amazon.com/vpc/
[Cloudformation]:https://aws.amazon.com/cloudformation/