// IMPORT DATA
import {data} from './db-template-data';
import {initCharts} from './db-chart-settings';
import {additionalParams} from './app';
import {apigClient} from './app';
import {
        chartDataParser,
        getAvailableClients,
        getDashboardEnvironment,
        setDashboardScope,
        setAvailableClients,
        setDashboardEnvironment,
        getDefaultScope,
        getAvailableScopes,
    } from './data-handler';

let isFirstLoad = true;

$('body').click(function(event){
    // check if the clicked element is a descendent of navigation
    if ($(event.target).closest('#main-menu-content').length) {
        if ( $(".dropdown-toggle").attr('data-toggle') == 'dropdown') {
            $('.dropdown-toggle').removeAttr('data-toggle', 'dropdown');
        }
       //do nothing if event target is within the navigation
    } else if ($(event.target).closest('#mobile-menu').length) {
        // do nothing
    } else {
        if ($("#main-menu").css("display") == "block") {
            if ( $(".dropdown-toggle").attr('data-toggle') != 'dropdown') {
                $(".dropdown-toggle").attr('data-toggle', 'dropdown');
                $(".dropdown-toggle").dropdown('toggle');
            }
        } else {
            $("#navbar-content").collapse('hide');
        }

    }
});

/**********************************************************************
** HTML TEMPLATE HANDLER
/*********************************************************************/
const dbTemplateDefault = require("./db-template-default.hbs");
const dbTemplateNoEmails = require("./db-template-no-emails.hbs");


/**********************************************************************
** Get whether the template to use is default or not
**
** @returns {bool} template to use is default or not
*********************************************************************/
const isDefaultTemplate = () => {
    const defaultTemplateName = 'default';
    if (data.currentCampaign.template === defaultTemplateName) {
        return true;
    }
    return false;
};

/**********************************************************************
** Returns the model to use for the HTML according to the data values
**
** @returns {template} the Handlebar template to use
*********************************************************************/
const selectTemplateModel = () => {
    if (isDefaultTemplate() === true) {
        return dbTemplateDefault;
    }
    return dbTemplateNoEmails;
};

/**********************************************************************
** Populate the HTML template with data
**
** @param {object} templateData The data to inject in HTML
*********************************************************************/
const createTemplate = (templateData) => {
    let dbTemplateContainer = document.getElementById("db-template-container");
    let dbTemplate = selectTemplateModel();
    dbTemplateContainer.innerHTML = dbTemplate(templateData);
};
const menuTemplate = require("./menu-template.hbs");

const initDropdownMenu = (templateData, firstScope) => {
    $("#menu-template-container").empty();
    $("#db-template-container").empty();

    let clientIterator, procedureIterator, campaignIterator;

    let menuTemplateContainer = document.getElementById("menu-template-container");
    menuTemplateContainer.innerHTML = menuTemplate(templateData[firstScope]);

    $(".dropdown-toggle").click(function(){
        $(this).dropdown('toggle');
        $(this).removeAttr('data-toggle', 'dropdown');
    });

    for (clientIterator = 0; clientIterator < templateData[firstScope].length; clientIterator++) {
        let client = templateData[firstScope][clientIterator];
        for (procedureIterator = 0; procedureIterator < client.value.length;procedureIterator++) {
            let procedure = client.value[procedureIterator];
            for (campaignIterator = 0; campaignIterator < procedure.value.length; campaignIterator++) {

                let version = procedure.value[campaignIterator];
                // Check if is first scope == true otherwise pass dashboards env
                params = {
                    client: client.name,
                    procedure: procedure.name,
                    scope: firstScope,
                    version: version,
                    template: procedure.template,
                    clientIterator : clientIterator,
                    procedureIterator: procedureIterator,
                    campaignIterator: campaignIterator
                };

                $("#" + clientIterator + "_client_btn").click({param: params}, function(event){
                    event.preventDefault();
                    $("[id$=_panel]").hide();
                    $("[id$=_panel] a").removeClass('active');

                    $("#" + event.data.param.clientIterator + "_panel").show();

                    $("#" + event.data.param.clientIterator +"_" + event.data.param.procedureIterator + "_procedura").click({param: event.data.param}, function(event){
                        event.preventDefault();
                        $("[id$=_campaign_panel]").hide();
                        $("[id$=_campaign_panel] a").removeClass('active');
                        $("#" + event.data.param.clientIterator + "_" + event.data.param.procedureIterator + "_campaign_panel").show();
                    });
                });

                $("#" + clientIterator + "_" + procedureIterator + "_" + campaignIterator).click({param: params}, function(event) {
                    $(".dropdown-toggle").attr('data-toggle', 'dropdown');
                    $(".dropdown-toggle").dropdown('toggle');
                    getData(event.data.param);
                    updateDropdownMenu(event.data.param);
                });

                $("#m_" + clientIterator + "_" + procedureIterator + "_" + campaignIterator).click({param: params}, function(event) {
                    $("#navbar-content").collapse('hide');
                    getData(event.data.param);
                    updateDropdownMenu(event.data.param);
                });
            }
        }
    }
};

function updateDropdownMenu(params){
    $(".menu-text-cliente-procedura").text(params.client + " > " + params.procedure);
    $(".menu-text-campagna").text(params.version);
}

// INITIATE CUSTOM API GATEWAY
let params = {};


/***********************************************************************************************
** Manage the very first time get data
**
**
** [PS: Sorry for the code quality, we inherited this and working hard to improve it!]
***********************************************************************************************/
const manageLoadData = (templateData, firstScope) => {
    // This block should be executed only on first load.
    // After this getData() method is called by individual functions.
    if (isFirstLoad === true) {
        let firstClient = templateData[firstScope][0];
        params = {
            client: firstClient.name,
            procedure: firstClient.value[0].name,
            scope: firstScope,
            version: firstClient.value[0].value[0],
            template: firstClient.value[0].template
        };

        updateDropdownMenu(params);
        getData(params);
        isFirstLoad = false;
    } else {
        console.error("no campaigns available");
        params = {
            client: "No Campaigns available",
            procedure: "",
            scope: " ",
            version: "MENU"
        };

        updateDropdownMenu(params);
    }
}

/***********************************************************************************************
** Initialize the environment switch according to the selected campaign
**
***********************************************************************************************/
const updateEnvironmentSwitch = () => {

    // Init the environment switch
    let $scopeSwitch = $("#scope-switch");
    let $scopeSwitchButtonA = $("#radio-a");
    let $scopeSwitchButtonB = $("#radio-b");

    $scopeSwitch.hide();
    $scopeSwitchButtonA.unbind('change');
    $scopeSwitchButtonB.unbind('change');

    let currentEnvironment = getDashboardEnvironment();
    if (currentEnvironment == 'prod') {
        $scopeSwitchButtonB.prop('checked', true);
    } else if (currentEnvironment == 'test') {
        $scopeSwitchButtonA.prop('checked', true);
    }

    $scopeSwitchButtonA.change(function(event) {
        if(this.checked) {
            // Update the campaign scope and request data
            let found = false;
            data.availableClients.test.forEach(function(client){
                if(client.name === data.currentCampaign.client){
                    //continue
                    client.value.forEach(function(procedure){
                        if(procedure.name === data.currentCampaign.procedure){
                            //switch to the relative test campaign
                            setDashboardEnvironment('test');
                            initDropdownMenu(data.availableClients, getDashboardEnvironment());
                            $(".menu-text-cliente-procedura").text(data.currentCampaign.client + " > " + data.currentCampaign.procedure);
                            $(".menu-text-campagna").text(data.currentCampaign.version);
                            getData(data.currentCampaign);
                            // se ne trovo 1 devo interrompere
                            found = true;
                            return;
                        }
                    });
                }
            });

            if (!found) {
                // show the first campaign in the list
                console.log("show the first test campaign");
                setDashboardEnvironment('test');
                initDropdownMenu(data.availableClients, getDashboardEnvironment());
                $(".menu-text-cliente-procedura").text(data.availableClients.test[0].name + " > " + data.availableClients.test[0].value[0].name);
                $(".menu-text-campagna").text(data.availableClients.test[0].value[0].value[0]);

                //maybe there's a better way to create this object
                const firstTestCampaign = {
                    availableScopes: "all",
                    client: data.availableClients.test[0].name,
                    procedure: data.availableClients.test[0].value[0].name,
                    scope: "test",
                    template: data.availableClients.test[0].value[0].template,
                    version: data.availableClients.test[0].value[0].value[0],
                };

                getData(firstTestCampaign);
            }
        }
    });
    
    $scopeSwitchButtonB.change(function(event) {
        if(this.checked) {
            // Update the campaign scope and request data
            let found = false;
            data.availableClients.test.forEach(function(client){
                if(client.name === data.currentCampaign.client) {
                    //continue
                    client.value.forEach(function (procedure) {
                        if (procedure.name === data.currentCampaign.procedure) {
                            //switch to the relative test campaign
                            setDashboardEnvironment('prod');
                            initDropdownMenu(data.availableClients, getDashboardEnvironment());
                            $(".menu-text-cliente-procedura").text(data.currentCampaign.client + " > " + data.currentCampaign.procedure);
                            $(".menu-text-campagna").text(data.currentCampaign.version);
                            getData(data.currentCampaign);
                            found = true;
                            return;
                        }
                    });
                }
            });

            if (!found) {
                // show the first campaign in the list
                console.log("show the first prod campaign");
                setDashboardEnvironment('prod');
                initDropdownMenu(data.availableClients, getDashboardEnvironment());
                $(".menu-text-cliente-procedura").text(data.availableClients.prod[0].name + " > " + data.availableClients.prod[0].value[0].name);
                $(".menu-text-campagna").text(data.availableClients.prod[0].value[0].value[0]);

                //maybe there's a better way to create this object
                const firstProdCampaign = {
                    availableScopes : "all",
                    client : data.availableClients.prod[0].name,
                    procedure : data.availableClients.prod[0].value[0].name,
                    scope : "prod",
                    template : data.availableClients.prod[0].value[0].template,
                    version : data.availableClients.prod[0].value[0].value[0],
                };

                getData(firstProdCampaign);
            }
        }
    });

    // Show the switch only if both prod and test scopes are returned
    if (getAvailableScopes() === 'all') {
        console.log("CURRENT DASHBOARD ENVIRONMENT: " + getDashboardEnvironment());
        $scopeSwitch.show();
    }
}


const getData = (params) => {

    // Keep the current dashboard environment: if first load it is not set, therefore use the default one
    if (!isFirstLoad) {
        params.scope = getDashboardEnvironment();
    }
    // TODO: start loading spinner here
    apigClient.getjsonPost({}, params, additionalParams)
        .then(function(result){
            //This is where you would put a success callback
            $.ajax({
                type: 'GET',
                url: result.data,
                dataType: "json",
                headers: {
                    'Access-Control-Allow-Origin': '*'
                },
                success: function(jsonData, textStatus, jqXHR) {

                    // First of all: set the HTML template model for the procudure
                    data.setProcedureTemplate(params.template);

                    // Parse json and update data template
                    chartDataParser(jsonData);
                    setDashboardScope(params);
                    updateEnvironmentSwitch();

                    // TODO:close loading spinner here

                    // console.log("*************   JSON data   ******************");
                    // console.log(jsonData);
                    // console.log("*******************************");
                    // console.log("============   Data   ===================");
                    // console.log(data);
                    // console.log("=========================================");


                    // CREATE HTML TEMPLATE
                    createTemplate(data);

                    // INIT CHARTS
                    initCharts();

                    // INIT TOOLTIPS
                    $('[data-toggle="tooltip"]').tooltip();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                 console.log(jqXHR);
                 console.log(textStatus);
                 console.log(errorThrown);
                }

            });

        }).catch( function(result){
        //This is where you would put an error callback
        console.log(result);
    });
};

/***********************************************************************************************
** Initialise the UI: use a promise to get the data, store them and init other components
**
** @params {environment} string indicating the selected environment
***********************************************************************************************/
const initUI = () => {
    // Single page application: this variable need to be initiate everytime you init the page (login-logout)
    isFirstLoad = true;

    apigClient.campaignsGet({}, {}, additionalParams)
        .then(function(result){
            //Parse result and save in global DATA
            setAvailableClients(JSON.parse(result.data));

            // Populate the dropdown with the available clients
            let availableClients = getAvailableClients();
            let firstScope = getDefaultScope();

            initDropdownMenu(availableClients, firstScope);
            manageLoadData(availableClients, firstScope);

        }).catch( function(result){
        //This is where you would put an error callback
        console.log(result);
    });
}

export {isDefaultTemplate};
export {getData};
export {initUI};

