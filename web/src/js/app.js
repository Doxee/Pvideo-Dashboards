// SDK AWS
//var AWS = require('aws-sdk');
import {Config, CognitoIdentityCredentials} from "aws-sdk";

// COGNITO SDK JavaScript: https://github.com/aws/aws-amplify/tree/master/packages/amazon-cognito-identity-js
var AmazonCognitoIdentity = require('amazon-cognito-identity-js');

// FILE CONFIGURAZIONE
//import appConfig from "./config";

// IMPORT JAVASCRIPT LIBRARIES
import 'jquery';
import 'popper.js';
import 'bootstrap';
import 'chart.js';
import 'moment';
import 'daemonite-material';

// IMPORT CSS
import '../css/main.scss';

// IMPORT MAIN JS
import {initUI} from './request-handler';


// LOAD CONFIGURATION FILE
let appConfig = {};
let configUrl = "./config.json";

$.ajax({
    type: 'GET',
    url: configUrl,
    dataType: "json",
    async: false,
    success: function(jsonData, textStatus, jqXHR) {
        // console.log(jsonData);
        // console.log(textStatus);
        // console.log(jqXHR);
        appConfig = jsonData;

    },
    error: function(jqXHR, textStatus, errorThrown) {
        // console.log(jqXHR);
        console.log(textStatus);
        console.log(errorThrown);
    }

});


// console.log(appConfig);

// GLOBALS
let additionalParams = {};
let apigClient = apigClientFactory.newClient({apiUrl: appConfig.apiUrl });

var poolData = {
    UserPoolId : appConfig.UserPoolId, // Your user pool id here
    ClientId : appConfig.ClientId // Your client id here
};

var userPool = new AmazonCognitoIdentity.CognitoUserPool(poolData);

var cognitoUser;

// INIT SESSION
function InitSession(authenticationData, newPassword){

    var authenticationDetails = new AmazonCognitoIdentity.AuthenticationDetails(authenticationData);

    var userData = {
        Username : authenticationData.Username,
        Pool : userPool
    };

    cognitoUser = new AmazonCognitoIdentity.CognitoUser(userData);

    cognitoUser.authenticateUser(authenticationDetails, {
        onSuccess: function (result) {

            setCredentials(result);

            refreshCredentials(result);

        },

        onFailure: function(err) {
            console.log(err);

            if(err.code === "NotAuthorizedException") {
                document.getElementById("authError").innerHTML = "Password o username sbagliate";
            } else if(err.code === "UserNotFoundException") {
                document.getElementById("authError").innerHTML  = "L'utente non esiste";
            } else if(err.message === "Password does not conform to policy: Password not long enough") {
                document.getElementById("authError2").innerHTML = "Password troppo corta";
            } else if(err.message === "Password does not conform to policy: Password must have uppercase characters") {
                document.getElementById("authError2").innerHTML = "La password deve contenere almeno un carattere maiuscolo";
            } else if(err.message === "Password does not conform to policy: Password must have numeric characters") {
                document.getElementById("authError2").innerHTML = "La password deve contenere almeno un numero";
            } else if(err.message === "Password does not conform to policy: Password must have symbol characters") {
                document.getElementById("authError2").innerHTML = "La password deve contenere almeno un carattere speciale";
            } else if(err.message === "Password does not conform to policy: Password must have lowercase characters") {
                document.getElementById("authError2").innerHTML = "La password deve contenere almeno un carattere minuscolo";
            } else {
                document.getElementById("authError2").innerHTML = "Richiesto cambio password";
            }

        },

        mfaRequired: function(codeDeliveryDetails) {
            // MFA is required to complete user authentication.
            // Get the code from user and call
            cognitoUser.sendMFACode(mfaCode, this);
        },

        newPasswordRequired: function(userAttributes, requiredAttributes) {
            // User was signed up by an admin and must provide new
            // password and required attributes, if any, to complete
            // authentication.

            // the api doesn't accept this field back
            delete userAttributes.email_verified;

            console.log("change password");
            console.log(userAttributes);

            document.getElementById("login-form").style.display = 'none';

            document.getElementById("new-password-form").style.display = 'block';
            // Get these details and call
            cognitoUser.completeNewPasswordChallenge(newPassword, userAttributes, this);

        }
    });

}

function setCredentials(result) {

    //POTENTIAL: Region needs to be set if not already set previously elsewhere.
    AWS.config.region = appConfig.Region;

    AWS.config.region = appConfig.Region;

    var input = {
        IdentityPoolId : appConfig.IdentityPoolId, // your identity pool id here
        Logins : {}
    };
    var loginUrl = 'cognito-idp.' + appConfig.Region + '.amazonaws.com/' + appConfig.UserPoolId;
    // console.log(loginUrl);
    input.Logins[loginUrl] = result.getIdToken().getJwtToken();

    AWS.config.credentials = new AWS.CognitoIdentityCredentials(input);
}

function refreshCredentials(result){

    //refreshes credentials using AWS.CognitoIdentity.getCredentialsForIdentity()
    AWS.config.credentials.refresh((error) => {
        if (error) {
            console.log(error);
            AWS.config.credentials = null;
            //window.location = window.location.origin;
        } else {
            actionsWhenLoggedIn(result.getIdToken().getJwtToken());
        }
    });

}

function retriveUserFromStorage() {

    cognitoUser = userPool.getCurrentUser();

    if (cognitoUser != null) {
        cognitoUser.getSession(function(err, session) {
            if (err) {
                alert(err.message || JSON.stringify(err));
                return;
            }
            // console.log('session validity: ' + session.isValid());

            // NOTE: getSession must be called to authenticate user before calling getUserAttributes
            cognitoUser.getUserAttributes(function(err, attributes) {
                if (err) {
                    // Handle error
                } else {
                    // Do something with attributes
                    // console.log(attributes);
                }
            });

            setCredentials(session);
            refreshCredentials(session);
        });
    }
}

// JWT TOKEN PARSER
function parseJwt(token) {
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace('-', '+').replace('_', '/');
    return JSON.parse(window.atob(base64));
}

// ACTIONS WHEN SIGNED IN
function actionsWhenLoggedIn(JWT){
    // console.log('Successfully logged!');
    document.getElementById("login-screen").style.display = 'none';
    document.getElementById("header-wrapper").style.display = 'block';
    // console.log(parseJwt(JWT));
    // console.log(JWT);

    additionalParams = {
        //If there are any unmodeled query parameters or headers that need to be sent with the request you can add them here
        headers: {
            dashtoken: JWT
        }
    };
    // Build Dashboard
    initUI();
}

// LOGIN FORM SUBMISSION HANDLER

(function() {
    'use strict';
    window.addEventListener('load', function() {

        $("#scope-switch").hide();

        $("#logOut").click(function(){
        if (cognitoUser != null){
                cognitoUser.signOut();
                $("#db-template-container").empty();
                $("#menu-template-container").empty();
                document.getElementById("login-screen").style.display = 'block';
                document.getElementById("header-wrapper").style.display = 'none';
            }
        });

        retriveUserFromStorage();
        document.getElementById("loading-icon").style.display = 'none';

        // Fetch all the forms we want to apply custom Bootstrap validation styles to
        var forms = document.getElementsByClassName('needs-validation');
        // Loop over them and prevent submission
        var validation = Array.prototype.filter.call(forms, function(form) {
            form.addEventListener('submit', function(event) {
                if (form.checkValidity() === false) {
                    event.preventDefault();
                    event.stopPropagation();
                } else {
                    event.preventDefault();

                    // GET AUTHENTICATION DATA
                    var authenticationData = {
                        Username : $("#username").val(),
                        Password : $("#password").val()
                    };

                    // GEY NEW PASSWORD
                    var newPassword = $("#new-password").val();

                    InitSession(authenticationData, newPassword);
                }
                form.classList.add('was-validated');
            }, false);
        });

        if (AWS.config.credentials == null){
            document.getElementById("login-screen").style.display = 'block';
        }


        $( "#scope-switch" )
            .mouseup(function() {
                $( this ).removeClass('activated');
            })
            .mousedown(function() {
                $( this ).addClass('activated');
            });

    }, false);
})();


// EXPORTS
export {additionalParams};
export {apigClient};
export {appConfig};

