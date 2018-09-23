console.info("Lambda get campaigns function initialization.");
// Import modules
var AWS = require('aws-sdk')
    ,
    region = process.env.AWS_REGION
    ,
    dynamo_client = new AWS.DynamoDB()
    ,
    dynamo_auth_table_name = process.env.DynamoAuthorizationTableName
    ,
    dynamo_campaign_table_name = process.env.DynamoCampaignTableName;


exports.handler = function(event, context) {
    // If the function was called by a schedule event return immediately, it is invoked to avoid environment shut down
    if (event.source === "aws.events") {
        console.info("Warm-up scheduled event invocation.");
        context.succeed("Warm-up completed.");
    }
    else {

        console.log(event.CognitoGroups);

        if (event.CognitoGroups == undefined)
        {
            console.log("CognitoGroups is undefined, exit without values");
            context.succeed(JSON.stringify([]));
            return;
        }

        if (event.CognitoGroups.length == 0)
        {
            console.log("CognitoGroups is a blank string, exit without values");
            context.succeed(JSON.stringify([]));
            return;
        }

        console.log("Get Campaigns for user: "+ event.CognitoUser);


        let testAuth = [];
        var prodAuth = [];
        run(event)
            .then(function(result) {
                //Result è un array di promesse
                result.forEach(groupAuthsString => {
                    try{
                        if (groupAuthsString != null) {
                    var groupAuthsJson = JSON.parse(groupAuthsString);
                    groupAuthsJson.forEach(singoleAuth => {
                        try {
                            var data = {
                                client: singoleAuth.client,
                                procedure: singoleAuth.procedure,
                            }
                            var key = singoleAuth.client+":"+singoleAuth.procedure;
                    if (singoleAuth.test == true) {
                        testAuth.push(key);
                    }

                    if (singoleAuth.production == true) {
                        prodAuth.push(key);
                    }
                } catch (error) {

                    }

                })
                }
            } catch (error) {

                }
            });

                var resultAuthJson = [];
                let resultAuthJsonPromise=[];

                //Ciclo su prodAuth e intanto controllo se c'è anche in testAuth
                prodAuth.forEach(auth => {

                    var test = false;

                /*if (testAuth.indexOf(auth) > -1) {
                    test = true;
                }*/

                var values = auth.split(":");

                let dataPromise = get_versions_from_procedure_prod(values).then(res => {

                    let versions = [];
                    let templateName = "default";



                    if(res instanceof Array){
                        if (values != null && values != undefined) {
                            versions = res[0].split(",");
                        }
                        if (res[1] != null && res[1] != undefined) {
                            templateName = res[1];
                        }
                    }else{
                        if (values != null && values != undefined) {
                            versions = res.split(",");
                        }
                    }

                    return {
                        client : values[0],
                        procedure: values[1],
                        test: test,
                        production: true,
                        versions : versions,
                        templateName : templateName
                    };
            });


                resultAuthJsonPromise.push(dataPromise);

            });

                testAuth.forEach(auth => {

                    //if (prodAuth.indexOf(auth) == -1) {
                    var values = auth.split(":");

                let dataPromise = get_versions_from_procedure_test(values).then(res => {

                    let versions = [];

                    let templateName = "default";



                    if(res instanceof Array){

                        if (values != null && values != undefined) {
                            versions = res[0].split(",");
                        }
                        if (res[1] != null && res[1] != undefined) {
                            templateName = res[1];
                        }
                    }else{
                        if (values != null && values != undefined) {
                            versions = res.split(",");
                        }
                    }


                    return {
                        client : values[0],
                        procedure: values[1],
                        test: true,
                        production: false,
                        versions : versions,
                        templateName : templateName
                    };
            });


                resultAuthJsonPromise.push(dataPromise);
                // }


            });

                return Promise.all(resultAuthJsonPromise);


            }).then(resultAuthJson=>{

            let prod = {};
        let test = {};

        resultAuthJson.forEach(function(value){
            if (value.test == true) {

                var client = test[value.client];
                if (client == null) {
                    var procedure = {};
                    client = {};
                    test[value.client] = client;
                }

                var procedure = test[value.procedure];
                if (procedure == null) {
                    //client[value.procedure] = [];
                    var campaigns_template = {};
                    client[value.procedure] = campaigns_template;
                }
                //client[value.procedure] = value.versions;
                campaigns_template["campaigns"] = value.versions;
                campaigns_template["template"] = value.templateName;
            }

            if (value.production == true) {

                var client = prod[value.client];
                if (client == null) {
                    var procedure = {};
                    client = {};
                    prod[value.client] = client;
                }

                var procedure = prod[value.procedure];
                if (procedure == null) {
                    // client[value.procedure] = [];
                    var campaigns_template = {};
                    client[value.procedure] = campaigns_template;
                }
                // client[value.procedure] = value.versions;
                campaigns_template["campaigns"] = value.versions;
                campaigns_template["template"] = value.templateName;
            }
        });

        //console.log(JSON.stringify(prod));

        var finalJson = {};
        finalJson["prod"] = prod;
        finalJson["test"] = test;

        // console.log(JSON.stringify(finalJson));

        context.succeed(JSON.stringify(finalJson));
    })
    .catch(function(err) {
            console.error(err, err.stack);
            context.fail('{"code":"' + (err.code || 500) + '", "message": "' + (err.msg || 'Internal Server Error.') + ', "name": "' + err.name + '"}');
        });
    }
};

function get_versions_from_procedure_test(auth) {

    var procedureKey = 'client='+auth[0]+",procedure="+auth[1];

    var params = {
        Key: {
            "ProcedureKey": {
                S: procedureKey
            }
        },
        TableName: dynamo_campaign_table_name
    };

    return new Promise((resolve, reject)=> {
        dynamo_client.getItem(params, function(err, data) {
        if (err) {
            reject(err);
        } else {
            try {
                resolve([data.Item.testVersions.S,data.Item.templateName.S]);
            } catch (err) {
                if(err.message=="Cannot read property 'S' of undefined"){
                    resolve(data.Item.testVersions.S);
                }else{
                    resolve(null);
                }
            }
        }

    });
});
}

function get_versions_from_procedure_prod(auth) {

    var procedureKey = 'client='+auth[0]+",procedure="+auth[1];

    var params = {
        Key: {
            "ProcedureKey": {
                S: procedureKey
            }
        },
        TableName: dynamo_campaign_table_name
    };

    return new Promise((resolve, reject)=> {
        dynamo_client.getItem(params, function(err, data) {
        if (err) {
            reject(err);
        } else {
            try {
                resolve([data.Item.prodVersions.S,data.Item.templateName.S]);
            } catch (err) {
                if(err.message=="Cannot read property 'S' of undefined"){
                    resolve(data.Item.prodVersions.S);
                }else{
                    resolve(null);
                }
            }
        }

    });
});
}

var run = exports.run = function(event) {
    // Variables definition and initialization

    console.info("Received object is: " + JSON.stringify(event));

    return get_campaign(event.CognitoGroups);
};

var get_campaign = exports.get_campaign = function(groups) {

    var groupsArray = groups.split(",");

    var authorizations = Promise.all(groupsArray.map(g => camp(g)));

    return authorizations;
}

var camp = exports.camp = function(groupName) {

    var params = {
        Key: {
            "Groupname": {
                S: groupName
            }
        },
        TableName: dynamo_auth_table_name
    };

    // Call DynamoDB to read the item from the table
    return get_item_table(params);

}
/**
 *
 *
 * */
var get_item_table = exports.get_item_table = function(params) {

    return new Promise(function(resolve, reject) {
        dynamo_client.getItem(params, function(err, data) {
            if (err) {
                reject(err);
            } else {
                try {
                    resolve(data.Item.AuthJson.S);
                } catch (err) {
                    resolve(null);
                }
            }

        });
    });
}