// GLOBAL CHARTS COLORS
let chartColors = {
    // pvideo standard
    // blue: 'rgb(23,125,251)',
    // lightblue: 'rgb(43,194,253)',

    // dashboard custom
    darkblue: 'rgb(68,114,196)',
    green: 'rgb(101,214,125)',
    blue: 'rgb(23,125,251)',
    lightblue: 'rgb(43,194,253)',
    yellow: 'rgb(253,212,91)',
    white: 'rgb(255,255,255)',
    gray: 'rgb(115, 117, 119)',
    lightgray: 'rgb(228, 228, 228)',
    purple: 'rgb(252, 96, 152)',
    orange: 'rgb(254, 101, 32)'
};

let labels24 = ["1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h", "11h", "12h", "13h", "14h", "15h", "16h", "17h", "18h", "19h", "20h", "21h", "22h", "23h", "24h"];
let labels12 = labels24.slice(0,12);

// MAIN DATA TO BUILD TEMPLATE
let data = {
    // CAMPAIGN INFO
    currentCampaign: {
        availableScopes: 'all',
    },

    availableClients: {

    },

    // EMAIL METRICS
    email: {
        sent: {
            number: [],
            label: 'Email Spedite',
            tooltip: 'Numero di email spedite'
        },
        bounces: {
            number: [],
            label: 'Email Rimbalzate',
            tooltip: 'Email non consegnate o rifiutate'
        },
        bounceRate: {
            number: [],
            label: 'Rimbalzi Email',
            tooltip: 'Email rimbalzate / Email spedite'
        },
        delivered: {
            number: [],
            label: 'Email consegnate con successo',
            tooltip: 'Email spedite - Email rimbalzate'
        },
        opened: {
            number: [],
            label: 'Email Aperte',
            tooltip: 'Numero di email aperte'
        },
        openRate: {
            number:[],
            label: 'Apertura Email',
            tooltip: 'Email aperte / Email consegnate'
        },
        clicks: {
            number: [],
            label: 'Video Aperti',
            tooltip: 'Numero di click sul link al video personalizzato nella mail'
        },
        clickRate: {
            number: [],
            label: 'Click al video nella email',
            tooltip: 'Click sul link al video personalizzato nella mail / email consegnate'
        },
        purlProduced: {
            number: [],
            label: 'Video prodotti',
            tooltip: 'Video prodotti',
        }
    },

    // VIDEO METRICS
    video: {
        views: {
            number: [],
            label: 'Visualizzazioni video totali',
            tooltip: 'Nnumero totale di click sul tasto play'
        },
        viewsUnique: {
            number: [],
            label: 'Visualizzazioni video',
            tooltip: 'Numero di utenti che hanno cliccato sul tasto play del video almeno una volta'
        },
        viewsUser: {
            number: [],
            label: 'Visualizzazioni per utente',
            tooltip: 'Visualizzazioni uniche / Visualizzazioni totali'
        },
        viewsRate: {
            number: [],
            label: 'Visualizzazioni video',
            tooltip: 'Numero di utenti che hanno clicckato sul tasto play almeno una volta / mail consegnate'
        },
        interactions: {
            number: [],
            label: 'Interazioni',
            tooltip: 'Numero di click su popups e user directed storytelling'
        },
        interactionRate: {
            number: [],
            label: 'Interazioni',
            tooltip: 'Numero di click su popups e user directed storytelling'
        },
        conversions: {
            number: [],
            label: 'Conversioni',
            tooltip: 'Numero di click sulle conversioni del video'
        },
        conversionRate: {
            number: [],
            label: 'Conversioni',
            tooltip: 'Click su conversioni / visualizzazioni uniche'
        }

    },

    // CONVERSIONS TABLE
    conversions: [],

    // INTERACTIONS TABLE
    interactions: [],

    // FIRST 24h MULTIPLE LINES CHART
    firstDay: {
        labels: labels24,
        datasets: [],
    },

    // ENGAGEMENT PIE CHART
    engagement:{
        labels: ["0%", "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%"],
        datasets: [{
            label: 'Numero di Utenti',
            fill: false,
            borderColor: chartColors.lightblue,
            backgroundColor: chartColors.lightblue,
            hidden: true,
            data: [],
            },{
            label: 'Percentuale Visualizzazioni Uniche/Visualizzazioni Uniche Totali',
            fill: false,
            borderColor: chartColors.lightblue,
            backgroundColor: chartColors.lightblue,
            showTooltip: false,
            lineTension: 0.15,
            data: []
        }]
    },

    // DEVICE TYPE PIE CHART
    deviceType:{
        datasets: [{
            label: 'Dataset 2',
            backgroundColor: [chartColors.blue, chartColors.lightblue, chartColors.yellow],
            data:[]
        }],
        labels: ["Mobile", "Desktop", "Tablet", "Other"]
    },

    // OPERATING SYSTEM PIE CHART
    operatingSystem:{
        datasets: [{
            label: 'Dataset 1',
            backgroundColor: [chartColors.blue, chartColors.lightblue, chartColors.yellow, chartColors.green, chartColors.purple, chartColors.orange],
            data: []
        }]
    },

    openRate: {
        datasets: [{
            data: [],
            backgroundColor: chartColors.green
            //borderColor: chartColors.gray,
            //borderWidth: 2

        }, {
            data: [],
            //borderColor: chartColors.gray,
            //borderWidth: 2,
            backgroundColor: chartColors.lightgray
        }]
    },

    bounceRate: {
        datasets: [{
            data: [],
            backgroundColor: chartColors.blue
            //borderColor: chartColors.gray,
            //borderWidth: 2

        }, {
            data: [],
            //borderColor: chartColors.gray,
            //borderWidth: 2,
            backgroundColor: chartColors.lightgray
        }]
    },

    viewsRate: {
        datasets: [{
            data: [],
            backgroundColor: chartColors.yellow
            //borderColor: chartColors.gray,
            //borderWidth: 2
        }, {
            data: [],
            //borderColor: chartColors.gray,
            //borderWidth: 2,
            backgroundColor: chartColors.lightgray
        }]
    },

    conversionRate: {
        datasets: [{
            data: [],
            backgroundColor: chartColors.lightblue,
            //borderColor: chartColors.gray,
            //borderWidth: 2

        }, {
            data: [],
            //borderColor: chartColors.gray,
            //borderWidth: 2,
            backgroundColor: chartColors.lightgray
        }]
    },


    setProcedureTemplate(template) {
        this.currentCampaign.template = template;
    },

    /**********************************************************************
    ** Set the object data for the overview chart depending on the template
    **
    ** @param {object} templateData The data to use for the chart
    ** @param {bool} isDefaultTemplate Whether the template is default
    *********************************************************************/
    setOverviewChartData(series, isDefaultTemplate) {
        if (isDefaultTemplate === true) {
            this.firstDay.datasets = [{
                label: 'Email Aperte',
                fill: false,
                borderColor: chartColors.green,
                backgroundColor: chartColors.green,
                data: series.opened,
            }, {
                label: 'Visualizzazioni Video',
                fill: false,
                borderColor: chartColors.yellow,
                backgroundColor: chartColors.yellow,
                data: series.views,
            }, {
                label: 'Conversioni',
                fill: false,
                borderColor: chartColors.lightblue,
                backgroundColor: chartColors.lightblue,
                data: series.conversions,

            }];
        } else {
            this.firstDay.datasets = [{
                label: 'Visualizzazioni Video',
                fill: false,
                borderColor: chartColors.yellow,
                backgroundColor: chartColors.yellow,
                data: series.views,
            }, {
                label: 'Conversioni',
                fill: false,
                borderColor: chartColors.lightblue,
                backgroundColor: chartColors.lightblue,
                data: series.conversions,
            }];
        }
    },

    eItalian(x) {
        return x.toLocaleString('it-IT');
    }
};

export {data};
export {chartColors};
export {labels12};
export {labels24};

