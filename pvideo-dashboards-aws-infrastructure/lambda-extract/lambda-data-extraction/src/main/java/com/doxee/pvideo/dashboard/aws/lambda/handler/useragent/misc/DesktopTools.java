package com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.misc;

import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.AgentCategory;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.DataSet;

import java.util.Map;

public class DesktopTools extends AgentCategory {

    public static boolean challenge(final String ua, final Map<String, String> result) {
        Map<String, String> data = null;

        if (ua.indexOf("AppleSyndication/") > -1) {
            data = DataSet.get("SafariRSSReader");
        } else if (ua.indexOf("compatible; Google Desktop/") > -1) {
            data = DataSet.get("GoogleDesktop");
        } else if (ua.indexOf("Windows-RSS-Platform") > -1) {
            data = DataSet.get("WindowsRSSReader");
        }

        if (data == null)
            return false;

        updateMap(result, data);
        return true;
    }
}
