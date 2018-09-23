package com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.browser;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;

import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.AgentCategory;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.DataSet;

public class Sleipnir extends AgentCategory {

    private static Pattern sleipnirVer = Pattern.compile("Sleipnir/([.0-9]+)");

    public static boolean challenge(final String ua, final Map<String, String> result) {
        int pos = ua.indexOf("Sleipnir/");
        if (pos < 0) // not Sleipnir
            return false;

        String version = DataSet.VALUE_UNKNOWN;

        Matcher sleipnir = sleipnirVer.matcher(ua);
        if (sleipnir.find(pos))
            version = sleipnir.group(1);
        updateMap(result, DataSet.get("Sleipnir"));
        updateVersion(result, version);

        // Sleipnir's user-agent doesn't contain Windows version, so put 'Windows UNKNOWN Ver'.
        // Sleipnir is IE component browser, so for Windows only.
        Map<String, String> win = DataSet.get("Win");
        updateCategory(result, win.get(DataSet.DATASET_KEY_CATEGORY));
        updateOs(result, win.get(DataSet.DATASET_KEY_NAME));

        return true;
    }
}
