package com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.misc;

import java.util.Map;

import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.AgentCategory;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.DataSet;

public class SmartPhonePatterns extends AgentCategory {

    public static boolean challenge(final String ua, final Map<String, String> result) {
        Map<String, String> data = null;

        if (ua.indexOf("CFNetwork/") > -1) {
            data = DataSet.get("iOS");
            updateCategory(result, data.get(DataSet.DATASET_KEY_CATEGORY));
            updateOs(result, data.get(DataSet.DATASET_KEY_NAME));
            return true;
        }

        return false;
    }
}
