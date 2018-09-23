#!/usr/bin/env python
from troposphere import GetAtt
from troposphere import Template,ImportValue
from troposphere.cognito import IdentityPoolRoleAttachment, RoleMapping

t = Template()

t.add_version("2010-09-09")

t.add_description("AWS CloudFormation for the pVideoDashboard Infrastructure. (2 step)")


#### PARAMETER ####
provider_name="INSERT_PROVIDERNAME"#"cognito-idp.eu-central-1.amazonaws.com/eu-central-1_7DwuhZnSk"
user_pool_app_client="INSERT_APP_CLIENT"#"6j2uejv1gud0bl5otuuoj0sujp"


identity_pool_id = ImportValue("exportIdentityPool")

cognito_identity_role_mapping =  RoleMapping("pVideoDashbordIdentityRoleMapping",
                                             AmbiguousRoleResolution="Deny",#Role resolution
                                             Type="Token"
                                             )

#https://forums.aws.amazon.com/thread.jspa?messageID=783626#783626
#Va bene solo hard coded "cognito-idp.eu-central-1.amazonaws.com/eu-central-1_IhWxsvkjZ:6tlvtkshchbdv4kh4sq7qr2ln4"
# key= Join("", [GetAtt( "PvideoDashbordUserPool"+suffixcf , "ProviderName"), ":", Ref(cognito_user_pool_app_client)])
# roles = {"authenticated" : GetAtt("pVideoDashboardCognitoRole","Arn"), "unauthenticated" : GetAtt("pVideoDashboardCognitoRole","Arn")}

cognito_identity_role_attach = t.add_resource(IdentityPoolRoleAttachment(
    "pVideoDashbordIdentityPoolRoleAttach",
    IdentityPoolId=identity_pool_id,
    RoleMappings= { "%s:%s" % (provider_name ,user_pool_app_client) :  RoleMapping("pVideoDashbordIdentityRoleMapping",
                                              AmbiguousRoleResolution="Deny",#Role resolution
                                              Type="Token"
                                              )},
    Roles={}
))



print(t.to_json())