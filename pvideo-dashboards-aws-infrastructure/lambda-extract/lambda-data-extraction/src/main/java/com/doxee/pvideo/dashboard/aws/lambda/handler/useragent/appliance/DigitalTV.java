package com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.appliance;

import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.AgentCategory;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.DataSet;

import java.util.Map;

public class DigitalTV extends AgentCategory {

    public static boolean challenge(final String ua, final Map<String, String> result) {
        Map<String, String> data = null;

        if (ua.indexOf("InettvBrowser/") > -1) {
            data = DataSet.get("DigitalTV");
        }

        if (data == null)
            return false;

        updateMap(result, data);
        return true;
    }
}
