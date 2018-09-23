var cfsign = require('aws-cloudfront-sign'),
    sign_table = process.env.SignDataTable,
    AWS = require("aws-sdk"),
    db = new AWS.DynamoDB.DocumentClient(),
    expiration = 600;
    
var querystring = require("querystring");

exports.handler = (event, context, callback) => {
    
    console.log(JSON.stringify(event));

    // If the function was called by a schedule event return immediately, it is invoked to avoid environment shut down
    if(event.source === "aws.events"){
        console.info("Warm-up scheduled event invocation.");
        context.succeed("Warm-up completed.");
    } else{
        run(event)
            .then(function(result){
                context.succeed(result);
            })
            .catch(function(err){
                console.error(err, err.stack);
                context.fail('{"code":"' + (err.code || 500) + '", "message": "' + (err.msg || 'Internal Server Error.') + ', "name": "' + err.name + '"}');
            });
    }
};

var run = exports.run = function(event){
    // Variables definition and initialization
    var client                                                                           // Get request data Every data is formatted with a text string, a configuration JSON object and a filename (see testString.txt
        , procedure                                                                        // Request body hash created using SHA256 algorithm
        , scope                                                                      // Name of the db table in which required data are stored
        , settings                                                                     // Partner ID, unique for an instance of the DoxeePlatform
        , cfUrl
        , cfBaseUrl
        , version;                                                                       // Cloudfront endpoint which refers to doxee-tts/audio/persistent bucket folder
    console.info(JSON.stringify(event));
    try{
        settings = {};
        cfBaseUrl = process.env.CloudFrontAddress;
        client = event.body.client;
        procedure = event.body.procedure;
        scope = event.body.scope;
        version = event.body.version;
        
        if (version == 'Tutte le campagne') {
            version = "ALL";
        }
        
        // Create cfUrl vera
        cfUrl = cfBaseUrl + "/"+client+"/"+procedure+"/"+scope+"/latest/data_"+version+".json";
        
        cfUrl = encodeURI(cfUrl);
        
        console.error(cfUrl);
        
        
    }catch(err){
        return Promise.reject(err);
    }

    return new Promise(function(resolve, reject){
        sign(cfUrl, settings, client, procedure, scope)
        // If asynchronous execution ends correctly
            .then(function(result){
                console.info("The signed url is: " + JSON.stringify(result));
                resolve(result);
            })
            // Asynchronous errors cannot be handled with a try catch statement
            .catch(function(err){
                reject(err);
            });
    });
};

var sign = exports.sign = function(cfUrl, settings){
    return new Promise(function(resolve, reject){
        dbReq(sign_table, settings)
            .then(function(value){
                settings = value;
                
                // Qua fai il sign please
                var signignParams = {
                        keypairId: settings.keypairId,
                        privateKeyString: settings.privateKeyString,
                        expireTime: (new Date().getTime() + expiration*1000) // epoch-expiration-time
                    };
                
                var response = cfsign.getSignedUrl(cfUrl, signignParams);
                
                resolve(response);
            })
            .catch(function(err){
                reject(err);
            })
    });   
}

/**
 * This function fetches ivona authorizations from a dynamo DB by using partnerID; it is supposed that each partner has a unique ivona account
 *
 * @param   {String} dbTable        name of the dynamoDB table in which data are stored
 * @param   {Object} settings       containing docID, partnerID, appDn, hashZip, caching flag, environment tag, authorization
 *
 * @returns {Object} Promise        to wait for for the asynchronous task to end
 *
 * @throws  {TypeError}
 * @throws  {InternalError}
 * @throws  {AuthorizationError}
 */
var dbReq = exports.dbReq = function(dbTable, settings) {
    return new Promise(function(resolve, reject) {
        var params = { // Structure used to send requests to Dynamo DB
            TableName: dbTable,
            ProjectionExpression: "PUBLIC_KEY, PRIVATE_KEY",
            FilterExpression: "#active = :boolean",
            ExpressionAttributeNames: {
                "#active": "ACTIVE"
            },
            ExpressionAttributeValues: {
                ":boolean": true
            },
            Limit: 1
        };
        // Send a scan request to Dynamo with the previously defined parameters
        db.scan(params, function(err, result) {
            if (err) {
                console.error(err, err.stack);
                reject(new InternalError("Unable to scan the db table."));
            }
            else {
                console.info("Query succeeded.");
                //console.info("Available items within the DB: " + JSON.stringify(result));
                // If there is at least one available key proceed
                if (result.Items.length >= 1) {
                    // Set keys in settings
                    settings.keypairId = result.Items[0].PUBLIC_KEY;
                    settings.privateKeyString = result.Items[0].PRIVATE_KEY;
                    console.log(JSON.stringify(settings))
                    resolve(settings);
                }
                // otherwise throw an error
                else {
                    reject(new InternalError("No active key-pair is available to sign urls"));
                }
            }
        });
    });
};

/**
 * Error thrown on internal server error.
 *
 * @param {String} msg
 * @constructor
 */
var InternalError = exports.InternalError = function(msg){
    this.msg = (msg || '');
    this.code = 500;
    this.name = "Internal Server Error";
    this.stack = (new Error()).stack;
};