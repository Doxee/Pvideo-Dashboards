package com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.os;

import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.AgentCategory;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.DataSet;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Linux extends AgentCategory {

    private static Pattern androidOs = Pattern.compile("Android[- ](\\d+\\.\\d+(?:\\.\\d+)?)");

    public static boolean challenge(final String ua, final Map<String, String> result) {
        int pos = ua.indexOf("Linux");
        if (pos < 0) // not Linux
            return false;

        Map<String, String> data = DataSet.get("Linux");
        String version = null;

        if (ua.indexOf("Android") > -1) {
            data = DataSet.get("Android");
            Matcher android = androidOs.matcher(ua);
            if (android.find(pos)) {
                version = android.group(1);
            }
        }

        updateCategory(result, data.get(DataSet.DATASET_KEY_CATEGORY));
        updateOs(result, data.get(DataSet.DATASET_KEY_NAME));
        if (version != null) {
            updateOsVersion(result, version);
        }
        return true;
    }
}
