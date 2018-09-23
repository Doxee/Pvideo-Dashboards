#!/usr/bin/env python
from troposphere import GetAtt, Join
from troposphere import Ref, Template
from troposphere.awslambda import Function, Code, VPCConfig, Environment
from troposphere.cloudwatch import Alarm, MetricDimension
from troposphere.dynamodb import Table, KeySchema, AttributeDefinition, ProvisionedThroughput
from troposphere.events import Rule, Target
from troposphere.iam import Role, Policy
from troposphere.logs import LogGroup
from troposphere.s3 import Bucket
from troposphere.sns import Subscription, Topic
from troposphere import GetAtt, Join, Split, Output, Sub, utils, logs, Ref, Export
from troposphere import Ref, Template,Parameter
from troposphere.cognito import UserPool, UserPoolClient, UserPoolGroup, UserPoolUser, \
    UserPoolUserToGroupAttachment, CognitoIdentityProvider, IdentityPool, IdentityPoolRoleAttachment, RoleMapping
from troposphere.iam import Role, Policy

t = Template()

t.add_version("2010-09-09")

t.add_description("AWS CloudFormation pVideoDashboard stack. (1 step)")

#### PARAMETER ####

# mettere Enable cloudWatch alarm
# mettere Enable status_trigger

suffixcf = "cf"
suffix_work = "-work"
log_group_name = "pVideoDashboardLogGroup"  # ansible_module.params['log_group_name']
s3_bucket_name = "pvideodashboard"  # ansible_module.params['s3_bucket_name']
ca_cluster_identifier = "ca-dev-frank-cloudanalyticsredshiftcluster-jlt2fskwsdso"
subnets_array_lambda = "subnet-00670b6b,subnet-8574d0f8"  # ansible_module.params['private_subnet_ids'].split(",")
security_group_lambda = "sg-17fd397a"  # ansible_module.params['security_group_lambda']
lambda_memory = 512  # ansible_module.params['lambda_memory'],
lambda_timeout = 300  # ansible_module.params['lambda_timeout'],
jar_version = "1.0.0.0-SNAPSHOT"  # ansible_module.params['version']
alarm_email_1 = "mdolla@consultants.doxee.com"  # ansible_module.params['alarm_email_1']
alarm_email_2 = "matteo.dolla@drafintech.it"  # ansible_module.params['alarm_email_2']
dynamo_table = "pVideoDashboard"  # ansible_module.params['dynamo_table']
enabled_alarm = False
lambda_trigger_status = "DISABLED"  # "ENABLED"
s3_cognito_allow_admin_path=s3_bucket_name

t = Template()

t.add_version("2010-09-09")

t.add_description("AWS CloudFormation for the pVideoDashboard Infrastructure.")

#####  LOG GROUP  #####

t.add_resource(LogGroup(
    'pVideoDashboardLogGroup' + suffixcf,
    LogGroupName=log_group_name + suffixcf
))

#####  S3  #####

s3bucket = t.add_resource(Bucket(
    "pVideoDashboardBucket" + suffixcf,
    BucketName=s3_bucket_name + suffixcf
))

s3bucket_work = t.add_resource(Bucket(
     "pVideoDashboardBucket"+suffix_work,
     BucketName=s3_bucket_name+suffix_work
 ))

###LAMBDA POLICIES ####

LambdaExecutionRole = t.add_resource(
    Role("pVideoDashboardLambdaRole" + suffixcf,
         RoleName="pVideoDashboardLambdaRole" + suffixcf,
         ManagedPolicyArns=["arn:aws:iam::aws:policy/service-role/AWSLambdaDynamoDBExecutionRole",
                            "arn:aws:iam::aws:policy/AmazonDynamoDBReadOnlyAccess",
                            "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"],
         Path="/",
         Policies=[Policy("pVideoDashboardPolicy" + suffixcf,
                          PolicyName="pVideoDashboardPolicy" + suffixcf,
                          PolicyDocument={"Version": "2012-10-17",
                                          "Statement": [
                                              {
                                                  "Action": ["s3:*"],
                                                  "Effect": "Allow",
                                                  "Resource": [Join("", ["arn:aws:s3:::", s3_bucket_name, "/*"]),Join("", ["arn:aws:s3:::", s3_bucket_name+suffix_work, "/*"])]
                                              },
                                              {
                                                  "Effect": "Allow",
                                                  "Action": [
                                                      "redshift:DescribeHsmConfigurations",
                                                      "redshift:DescribeClusterSecurityGroups",
                                                      "redshift:DescribeEventSubscriptions",
                                                      "redshift:DescribeOrderableClusterOptions",
                                                      "redshift:DescribeEvents",
                                                      "redshift:DescribeHsmClientCertificates",
                                                      "redshift:ViewQueriesInConsole",
                                                      "redshift:DescribeTags",
                                                      "redshift:DescribeClusterParameterGroups",
                                                      "redshift:DescribeDefaultClusterParameters",
                                                      "redshift:DescribeEventCategories",
                                                      "redshift:DescribeClusterSubnetGroups",
                                                      "redshift:DescribeReservedNodeOfferings",
                                                      "redshift:DescribeSnapshotCopyGrants",
                                                      "redshift:DescribeReservedNodes",
                                                      "redshift:DescribeClusterVersions",
                                                      "redshift:DescribeClusterSnapshots",
                                                      "redshift:DescribeClusters",
                                                      "redshift:DescribeResize",
                                                      "redshift:DescribeLoggingStatus",
                                                      "redshift:GetClusterCredentials",
                                                      "redshift:DescribeTableRestoreStatus",
                                                      "redshift:DescribeClusterParameters"

                                                  ],
                                                  "Resource": ["*"]
                                              },
                                              {
                                                  "Effect": "Allow",
                                                  "Action": ["ec2:CreateNetworkInterface",
                                                             "ec2:DescribeNetworkInterfaces",
                                                             "ec2:DeleteNetworkInterface"
                                                             ],
                                                  "Resource": ["*"]
                                              },
                                              {
                                                  "Effect": "Allow",
                                                  "Action": ["logs:*"],
                                                  "Resource": ["*"]
                                              },
                                              {
                                                  "Effect": "Allow",
                                                  "Action": [
                                                      "dynamodb:*"
                                                  ],
                                                  "Resource": [
                                                      Join("", ["", GetAtt("pVideoDashboard" + suffixcf, "Arn")])]
                                              }
                                          ]
                                          },
                          )],
         AssumeRolePolicyDocument={
             "Version": "2012-10-17",
             "Statement": [{
                 "Action": ["sts:AssumeRole"],
                 "Effect": "Allow",
                 "Principal": {
                     "Service": ["lambda.amazonaws.com"]
                 }
             }]
         },
         ))

lambda_name = "lambda-data-extraction-%s.jar" % jar_version

lambda_parameters_environment=Environment(
    Variables={
        "CLUSTER_ID_CA":  ca_cluster_identifier,
        "CONFIGURATION_TABLE": dynamo_table
    }
)

lambda_function = t.add_resource(Function(
    "pVideoDashboardMetricsExtract" + suffixcf,
    FunctionName="pVideoDashboardMetricsExtract" + suffixcf,
    Code=Code(
        S3Bucket=s3_bucket_name + suffix_work,  # pvideodashboardwork/lambda-data-extraction-1.0.0.0-SNAPSHOT.jar
        S3Key=lambda_name
    ),
    Handler="com.doxee.pvideo.dashboard.aws.lambda.handler.PvideoDashboardExtractorHandle",
    Role=GetAtt("pVideoDashboardLambdaRole" + suffixcf, "Arn"),
    Runtime="java8",
    MemorySize=lambda_memory,
    Timeout=lambda_timeout,
    Environment=lambda_parameters_environment,
    ReservedConcurrentExecutions=1,
    VpcConfig=VPCConfig(
        SecurityGroupIds=[security_group_lambda],
        SubnetIds=subnets_array_lambda.split(",")
    )
))

##### DYNAMODB #####

myDynamoDB = t.add_resource(Table(
    "pVideoDashboard" + suffixcf,
    TableName="pVideoDashboard" + suffixcf,
    AttributeDefinitions=[
        AttributeDefinition(
            AttributeName="id",
            AttributeType="S"
        ),
        AttributeDefinition(
            AttributeName="lastCreated",
            AttributeType="N"
        ),AttributeDefinition(
            AttributeName="updateFrequencyMin",  # Ref(tableIndexName),
            AttributeType="N"  # Ref(tableIndexDataType)
        )
    ],
    KeySchema=[
        KeySchema(
            AttributeName="id",  # Ref(tableIndexName),
            KeyType="HASH"
        )
    ],
    ProvisionedThroughput=ProvisionedThroughput(
        ReadCapacityUnits=10,
        WriteCapacityUnits=5
    )
))

#### CLOUDWATCH ####

# Create the Event Target
event_target = Target(
    "pVideoDashboardEventTarget" + suffixcf,
    Arn=GetAtt('pVideoDashboardMetricsExtract' + suffixcf, 'Arn'),
    Id="lambdaTarget",
    Input="{" +
          "  \"campaigns\": [] "
          + "\"}"
)


event_rule = t.add_resource(Rule("pVideoDashboardTrigger" + suffixcf,
                                 Name="pVideoDashboardTrigger" + suffixcf,
                                 ScheduleExpression="cron(0/5 8-17 ? * MON-FRI *)",
                                 # Run every 5 minutes Monday through Friday between 8:00 am and 19:55 pm (UTC)
                                 Description="cron for trigger extraction metrics by lambda (pVideoDashboardMetricsExtract)",
                                 State=lambda_trigger_status,
                                 Targets=[event_target]
                                 ))

#### CLOUDWATCH ALARM ####

alarm_topic = t.add_resource(
    Topic("pVideoDashboardAlarmTopic" + suffixcf,
          TopicName="pVideoDashboardAlarmTopic" + suffixcf,
          Subscription=[
              Subscription(
                  Endpoint=alarm_email_1,
                  Protocol="email"
              ), Subscription(
                  Endpoint=alarm_email_2,
                  Protocol="email"
              ),
          ],
          )
)

queue_depthalarm = t.add_resource(
    Alarm(
        "pVideoDashboardAlarm" + suffixcf,
        AlarmName="pVideoDashboardAlarm" + suffixcf,
        AlarmDescription="pVideoDashboardAlarm if Error more than 2",
        Namespace="AWS/Lambda",  # The namespace of the metric that is associated with the alarm.
        MetricName="Errors",
        Dimensions=[  # The dimensions of the metric for the alarm.
            MetricDimension(
                Name="FunctionName",
                Value=Ref(lambda_function).getdata(lambda_function)
            ),
        ],
        Statistic="Sum",
        Period="3600",
        # The time over which the specified statistic is applied. Specify time in seconds, in multiples of 60.
        EvaluationPeriods="1",
        Threshold="2",
        ComparisonOperator="GreaterThanThreshold",
        AlarmActions=[Ref(alarm_topic), ],
        TreatMissingData="notBreaching",
        ActionsEnabled=enabled_alarm
    )
)

#### COGNITO ####


cognito_group_role = t.add_resource(
    Role("pVideoDashboardCognitoRole"+suffixcf,
         RoleName="pVideoDashboardCognitoRole"+suffixcf,
         Path="/",
         Policies=[Policy("pVideoDashboardPolicy",
                          PolicyName="pVideoDashboardPolicy",
                          PolicyDocument={"Version": "2012-10-17",
                                          "Statement": [
                                              {
                                                  "Action": ["s3:*"],
                                                  "Effect": "Allow",
                                                  "Resource": [Join("", ["arn:aws:s3:::", s3_cognito_allow_admin_path, "/*"])]
                                              }
                                          ]
                                          },
                          )],
         AssumeRolePolicyDocument={
             "Version": "2012-10-17",
             "Statement": [ {
                 "Effect": "Allow",
                 "Principal": {
                     "Service": "cognito-idp.amazonaws.com"
                 },
                 "Action": "sts:AssumeRoleWithWebIdentity"
             },
                 {
                     "Effect": "Allow",
                     "Principal": {
                         "Federated": "cognito-identity.amazonaws.com"
                     },
                     "Action": "sts:AssumeRoleWithWebIdentity"
                 }]
         },
         ))


cognito_user_pool = t.add_resource(UserPool(
    "PvideoDashbordUserPool"+suffixcf,
    UserPoolName="PvideoDashbordUserPool"+suffixcf
))

cognito_user_pool_app_client = t.add_resource(UserPoolClient(
    "UserPoolClient"+suffixcf,
    ClientName="web_app_client",
    UserPoolId=Ref(cognito_user_pool),
    RefreshTokenValidity=30,
    GenerateSecret=False
))

t.add_output([
    Output(
        "AppClient",
        Description="appClientId of the newly created appclient",#3ssechafljmesc0bt29pdigb9
        Value=Ref(cognito_user_pool_app_client),
        Export = Export("exportAppClient")
    )])

cognito_user_pool_group = t.add_resource(UserPoolGroup(
    "UserPoolGroup"+suffixcf,
    GroupName="UserPoolGroup"+suffixcf,
    UserPoolId=Ref(cognito_user_pool),
    RoleArn=GetAtt("pVideoDashboardCognitoRole"+suffixcf, "Arn")
))

cognito_user_pool_user = t.add_resource(UserPoolUser(
    "UserPoolUser"+suffixcf,
    UserPoolId=Ref(cognito_user_pool),
    Username="pvideo_admin"
))

cognito_user_pool_group = t.add_resource(UserPoolUserToGroupAttachment(
    "UserPoolUserToGroupAttachment"+suffixcf,
    UserPoolId=Ref(cognito_user_pool),
    GroupName=Ref(cognito_user_pool_group),
    Username=Ref(cognito_user_pool_user)
))


#### IDENTITY PROVIDER ####

t.add_output([
    Output(
        "ProviderName",
        Description="InstanceId of the newly created user pool",
        Value=GetAtt(cognito_user_pool,"ProviderName")#cognito-idp.eu-central-1.amazonaws.com/eu-central-1_YnXYL6LHi
    )])


cognito_identity_provider =   CognitoIdentityProvider(
    ClientId=Ref(cognito_user_pool_app_client),
    ProviderName=GetAtt(cognito_user_pool,"ProviderName")
)

cognito_identity_pool = t.add_resource(IdentityPool("pVideoDashbordIdentityPool"+suffixcf,
                                                    IdentityPoolName="pVideoDashbordIdentityPool"+suffixcf,
                                                    AllowUnauthenticatedIdentities=False,
                                                    CognitoIdentityProviders=[cognito_identity_provider]))


t.add_output([
    Output(
        "IdentityPool",
        Description="InstanceId of the newly created user pool",
        Value=Ref(cognito_identity_pool), #eu-central-1:4e35e9ef-9702-452b-a629-393e0d9a52a1
        Export = Export("exportIdentityPool")
    )])



print(t.to_json())
