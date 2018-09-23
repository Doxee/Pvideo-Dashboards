import {data} from './db-template-data';
import {isDefaultTemplate} from './request-handler';
import {getObjectKeysAlphabetical} from './utils';

const chartDataParser = (data) => {
    emailVideoDataHandler(data);
    overviewChartsHandler(data);
    otherChartsHandler(data);
};

// SET FIRST PART
const overviewChartsHandler = (jsonToParse) => {
    data.setOverviewChartData(jsonToParse.firstDay, isDefaultTemplate());
};

// SET SECOND PART
const calculateRate = (rate) => {
    let scale = 100 - rate;
    return scale;
};

const otherChartsHandler = (jsonToParse) => {

    // CONVERSIONS
    data.conversions = [];
    data.conversions.push(jsonToParse.conversions);

    // INTERACTIONS
    data.interactions = [];
    data.interactions.push(jsonToParse.interactions);

    // ENGAGEMENT
    let engagementUsers = jsonToParse.engagement.data;
    let engagementPercentage = jsonToParse.engagement.rate;

    engagementPercentage.forEach(function(value, i){
        engagementPercentage[i] = value * 100;
    });
    // If someone watched the video then the beginning is always 100 otherwise is 0
    let engagementPercentageAtBeginning = (jsonToParse.video.viewsUnique > 0) ? 100 : 0;
    data.engagement.datasets[0].data = engagementUsers;
    data.engagement.datasets[0].data.unshift(jsonToParse.video.viewsUnique);
    data.engagement.datasets[1].data = engagementPercentage;
    data.engagement.datasets[1].data.unshift(engagementPercentageAtBeginning);

    // OPERATING SYSTEM
    data.operatingSystem.datasets[0].data = jsonToParse.operatingSystem.data;
    data.operatingSystem.labels = jsonToParse.operatingSystem.labels;

    // DEVICE TYPE
    data.deviceType.datasets[0].data = [];
    data.deviceType.datasets[0].data.push(jsonToParse.deviceType.mobile);
    data.deviceType.datasets[0].data.push(jsonToParse.deviceType.desktop);
    data.deviceType.datasets[0].data.push(jsonToParse.deviceType.tablet);
    data.deviceType.datasets[0].data.push(jsonToParse.deviceType.other);

    // SINGLE METRIC BARS
    let openRate = jsonToParse.email.openRate;
    data.openRate.datasets[0].data = [];
    data.openRate.datasets[0].data.push(openRate);
    data.openRate.datasets[1].data = [];
    data.openRate.datasets[1].data.push(calculateRate(openRate));

    let bounceRate = jsonToParse.email.bounceRate;
    data.bounceRate.datasets[0].data = [];
    data.bounceRate.datasets[0].data.push(bounceRate);
    data.bounceRate.datasets[1].data = [];
    data.bounceRate.datasets[1].data.push(calculateRate(bounceRate));

    let viewsRate = jsonToParse.video.viewsRate;
    data.viewsRate.datasets[0].data = [];
    data.viewsRate.datasets[0].data.push(viewsRate);
    data.viewsRate.datasets[1].data = [];
    data.viewsRate.datasets[1].data.push(calculateRate(viewsRate));

    let conversionRate = jsonToParse.video.conversionRate;
    data.conversionRate.datasets[0].data = [];
    data.conversionRate.datasets[0].data.push(conversionRate);
    data.conversionRate.datasets[1].data = [];
    data.conversionRate.datasets[1].data.push(calculateRate(conversionRate));

};

// SET SINGLE METRICS
const emailVideoDataHandler = (jsonToParse) => {

    for (var property in jsonToParse) {
        if (jsonToParse.hasOwnProperty(property)) {

            // SET EMAIL METRICS IN DATA
            if(property === 'email'){
                for (let metric in jsonToParse[property]) {
                    if (jsonToParse[property].hasOwnProperty(metric)) {
                        data.email[metric].number = jsonToParse[property][metric].toLocaleString('it-IT');
                    }
                }

            // SET VIDEO METRICS IN DATA
            } else if(property === 'video') {
                for (let metric in jsonToParse[property]) {
                    if (jsonToParse[property].hasOwnProperty(metric)) {
                        data.video[metric].number = jsonToParse[property][metric].toLocaleString('it-IT');
                    }
                }


                // SET CAMPAIGN INFO METRICS IN DATA
            } /* else if(property === 'campaignInfo') {
                for (let metric in jsonToParse[property]) {
                    if (jsonToParse[property].hasOwnProperty(metric)) {
                        data.campaignInfo[metric] = jsonToParse[property][metric];
                    }
                }
            } */
        }
    }
};

/***********************************************************************************************
** Store the list of clients information available
**
** @params {jsonToParse} JSON returned from the server
***********************************************************************************************/
const setAvailableClients = (jsonToParse) => {
    // BUILD AND ORDER MENU
    let menuData2 = {};
    let i, j, k;
    for (var key in jsonToParse) {
        let menuDataArray = [];
        let clients = getObjectKeysAlphabetical(jsonToParse[key]);

        for (i = 0; i < clients.length; i++) {
            let currentClientName = clients[i];
            let currentClientValue = jsonToParse[key][currentClientName];
            let procedures = getObjectKeysAlphabetical(currentClientValue);
            let currentClientValueArray = [];
            for (j = 0; j < procedures.length; j++) {
                let currentProcedureName = procedures[j];
                let currentProcedureValue = currentClientValue[currentProcedureName];
                let currentProcedure = {name: procedures[j], value: currentProcedureValue.campaigns, template: currentProcedureValue.template };
                currentClientValueArray.push(currentProcedure);
            }
            let currentClient = { name: currentClientName, value: currentClientValueArray};

            menuDataArray.push(currentClient);
        }
        menuData2[key] = menuDataArray;
    }

    data.availableClients = menuData2;
    setAvailableScopes();
}

/***********************************************************************************************
** Get the list of clients information available
**
** @return {data.availableClients} OBJECT containing the list of available clients
***********************************************************************************************/
const getAvailableClients = () => {
    return data.availableClients;
}


/***********************************************************************************************
** Store the available environments for the campaigns loaded
**
***********************************************************************************************/
const setAvailableScopes = () => {
    if (data.availableClients.hasOwnProperty('prod') && data.availableClients.prod.length > 0) {
        //there is at least one prod campaign
        data.currentCampaign.availableScopes = "prod";
    }
    if (data.availableClients.hasOwnProperty('test')  && data.availableClients.test.length > 0) {
        if (data.currentCampaign.availableScopes == "prod") {
            //there are test and prod campaigns
            data.currentCampaign.availableScopes = "all";
        } else {
            //there is at least one test campaign
            data.currentCampaign.availableScopes = "test";
        }
    }
}


/***********************************************************************************************
** Get the available scopes for the campaign
**
***********************************************************************************************/
const getAvailableScopes = () => {
    return data.currentCampaign.availableScopes;
}


/***********************************************************************************************
** Store the environment selected for the current dashboard
**
** @params {environment} string indicating the selected environment
***********************************************************************************************/
const setDashboardEnvironment = (environment) => {
    data.currentCampaign.scope = environment;
}

/***********************************************************************************************
** Get the environment selected for the current dashboard
**
** @return {data.currentCampaign.scope} string indicating the selected environment
***********************************************************************************************/
const getDashboardEnvironment = () => {
    return data.currentCampaign.scope;
}


/***********************************************************************************************
** Store all the infos about the selected campaign
**
** @params {dashboardScope} object containing all the info about the selected campaign/client
***********************************************************************************************/
const setDashboardScope = (dashboardScope) => {
    data.currentCampaign.client = dashboardScope.client;
    data.currentCampaign.procedure = dashboardScope.procedure;
    data.currentCampaign.version = dashboardScope.version;
    data.currentCampaign.scope = dashboardScope.scope;
    data.currentCampaign.template = dashboardScope.template;
}

/***********************************************************************************************
** Should the environment switch be shown? Check on the firstScope
**
** @return {firstScope} string the initial dashboard scope
***********************************************************************************************/
const getDefaultScope = () => {
    let firstScope = data.currentCampaign.availableScopes;

    if (firstScope == 'all') {
        firstScope = 'prod';
    }
    return firstScope;
}

export {chartDataParser};
export {setAvailableClients};
export {getAvailableClients};
export {setDashboardScope};
export {getAvailableScopes}
export {setDashboardEnvironment};
export {getDashboardEnvironment};
export {getDefaultScope};




