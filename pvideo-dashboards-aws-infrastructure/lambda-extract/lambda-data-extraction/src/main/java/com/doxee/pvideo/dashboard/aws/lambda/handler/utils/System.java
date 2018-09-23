package com.doxee.pvideo.dashboard.aws.lambda.handler.utils;

import java.util.HashMap;
import java.util.Map;

public class System {

    private static Map<String, String> properties = new HashMap<String, String>();

    private System() {

    }

    public static String getenv(String var) {

        properties.put("CONFIGURATION_TABLE", "PvideoDashboardCampaign");
        properties.put("CLUSTER_ID_CA", "ca-dev-frank-cloudanalyticsredshiftcluster-jlt2fskwsdso");
        properties.put("BUCKET_NAME", "pvideo-dash-2");

        return properties.get(var);
    }

    public static long currentTimeMillis() {
        return java.lang.System.currentTimeMillis();
    }

}
