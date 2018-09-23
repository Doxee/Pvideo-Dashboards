package com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.browser;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;

import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.AgentCategory;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.DataSet;

public class Firefox extends AgentCategory {

    private static Pattern firefoxVerRegex = Pattern.compile("Firefox/([.0-9]+)");

    public static boolean challenge(final String ua, final Map<String, String> result) {
        int pos = ua.indexOf("Firefox/");
        if (pos < 0) // not Firefox
            return false;

        String version = DataSet.VALUE_UNKNOWN;

        // Firefox (PC)
        Matcher firefox = firefoxVerRegex.matcher(ua);
        if (firefox.find(pos))
            version = firefox.group(1);
        updateMap(result, DataSet.get("Firefox"));
        updateVersion(result, version);
        return true;
    }
}
