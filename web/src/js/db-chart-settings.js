// IMPORT DATA TO BUILD CHARTS
import {data} from './db-template-data';
import {isDefaultTemplate} from './request-handler';
import {chartColors} from './db-template-data';
import {labels12} from './db-template-data';
import {labels24} from './db-template-data';

// GLOBAL CHARTS CONFIGURATION
Chart.defaults.global.title.display = true;
Chart.defaults.global.responsive = true;
Chart.defaults.global.tooltips.mode = 'index';
Chart.defaults.global.tooltips.intersect = false;
Chart.defaults.global.hover.mode = 'index';
Chart.defaults.global.hover.intersect = false;

Chart.defaults.global.hover.animationDuration = 0;
Chart.defaults.global.animation.duration = 0;
Chart.defaults.global.responsiveAnimationDuration = 0;

// FIRST DAY CHART
let firstDayConfig = {
    type: 'line',
    data: data.firstDay,
    options: {
        responsive:true,
        maintainAspectRatio: false,
        title:{
            text:''
        },
        scales: {
            xAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: ''
                }
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: ''
                }
            }]
        },
        onResize: function(chart, size){
            let label;
            if ($("#main-menu").css("display") == "block") {
                label = labels24;
            } else {
                label = labels12;
            }
            if (data.firstDay.labels != label) {
                data.firstDay.labels = label;
                $("#first-day-hours").text(label.length);
            }
        }
    }
};




// SINGLE METRICS CHARTS
let singleMetricChartsOptions = {
    title:{
        display:false
    },
    legend: {
        display: false
    },
    tooltips: {
        enabled: false
    },
    hover: {
        endable: false
    },
    responsive: true,
    scales: {
        xAxes: [{
            display: false,
            stacked: true,
            categorySpacing: 0
        }],
        yAxes: [{
            display: false,
            stacked: true,
            barThickness: 20
        }]
    }
};

let openRateConfig = {
    type: 'horizontalBar',
    data: data.openRate,
    options: singleMetricChartsOptions
};

let bounceRateConfig = {
    type: 'horizontalBar',
    data: data.bounceRate,
    options: singleMetricChartsOptions
};

let viewsRateConfig = {
    type: 'horizontalBar',
    data: data.viewsRate,
    options: singleMetricChartsOptions
};

let conversionRateConfig = {
    type: 'horizontalBar',
    data: data.conversionRate,
    options: singleMetricChartsOptions
};

// ENGAGEMENT CHART

let engagementConfig = {
    type: 'line',
    data: data.engagement,
    options: {
        title:{
            text:''
        },
        legend: {
            display: false,
        },
        tooltips: {
            enabled: true,
            mode: 'single',
            callbacks: {
                label: function(tooltipItems, data) {
                    let tooltipText = `Visualizzazioni: ${data.datasets[0].data[tooltipItems.index]}`;
                    return  tooltipText;
                }
            }
        },
        scales: {
            xAxes: [{
                display: true,
                scaleLabel: {
                    display: false,
                }
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: false,
                },
                ticks: {
                    min: 0,
                    max: 100,
                    beginAtZero: true,
                    stepSize: 10,
                    suggestedMax: 100,
                    suggestedMin: 0,

                    // Include a dollar sign in the ticks
                    callback: function(value, index, values) {
                        return value + '%';
                    }
                }
            }]
        }
    }
};

let operatingSystemConfig = {
    type: 'pie',
    data: data.operatingSystem,
    options: {
        title:{
            text:''
        },
    }
};

let deviceTypeConfig = {
    type: 'pie',
    data: data.deviceType,
    options: {
        title:{
            text:''
        },
    }
};



const initCharts = () => {

    // Init only some charts depending on the template set
    if (isDefaultTemplate()) {
        // OPEN RATE
        try {
            let openRateChart = document.getElementById("open-rate").getContext("2d");
            window.openRateChart = new Chart(openRateChart, openRateConfig);
        } catch(error) {
            console.error(`Error initializing openRateChart: ${error}`);
        };
        // BOUNCE RATE
        try {
            let bounceRateChart = document.getElementById("bounce-rate").getContext("2d");
            window.openRateChart = new Chart(bounceRateChart, bounceRateConfig);
        } catch (error) {
            console.error(`Error initializing bounceRateChart: ${error}`);
        };
    }

    // FIRST DAY
    try {
        let firstDayChart = document.getElementById("first-day").getContext("2d");
        window.firstDayChart = new Chart(firstDayChart, firstDayConfig);
    } catch (error) {
        console.error(`Error initializing firstDayChart: ${error}`);
    };

    // VIEWS RATE
    try {
        let viewsRateChart = document.getElementById("views-rate").getContext("2d");
        window.viewsRateChart = new Chart(viewsRateChart, viewsRateConfig);

    } catch(error) {
        console.error(`Error initializing viewsRateChart: ${error}`);
    };

    // CONVERSION RATE
    try {
        let conversionRateChart = document.getElementById("conversion-rate").getContext("2d");
        window.conversionRateChart = new Chart(conversionRateChart, conversionRateConfig);

    } catch(error) {
        console.error(`Error initializing conversionRateChart: ${error}`);
    }

    // ENGAGEMENT
    try {
        let engagementChart = document.getElementById("engagement-chart").getContext("2d");
        window.engagementChart = new Chart(engagementChart, engagementConfig);

    } catch(error) {
        console.error(`Error initializing engagementChart: ${error}`);
    };

    // OPERATING SYSTEM
    try {
        let operatingSystemChart = document.getElementById("operatingsystem-chart").getContext("2d");
        window.operatingSystemChart = new Chart(operatingSystemChart, operatingSystemConfig);

    } catch(error) {
        console.error(`Error initializing operatingSystemChart: ${error}`);
    };

    // DEVICE TYPE
    try {
        let deviceTypeChart = document.getElementById("devicetype-chart").getContext("2d");
        window.deviceTypeChart = new Chart(deviceTypeChart, deviceTypeConfig);

    } catch(error) {
        console.error(`Error initializing deviceTypeChart: ${error}`);
    };
};

export {initCharts};


