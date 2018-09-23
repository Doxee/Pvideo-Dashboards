package com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.os;

import java.util.Map;

import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.AgentCategory;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.DataSet;

public class Appliance extends AgentCategory {

    public static boolean challenge(final String ua, final Map<String, String> result) {
        // various appliance

        if (ua.indexOf("Nintendo DSi;") > -1) {
            Map<String, String> data = DataSet.get("NintendoDSi");
            updateCategory(result, data.get(DataSet.DATASET_KEY_CATEGORY));
            updateOs(result, data.get(DataSet.DATASET_KEY_OS));
            return true;
        }
        if (ua.indexOf("Nintendo Wii;") > -1) {
            Map<String, String> data = DataSet.get("NintendoWii");
            updateCategory(result, data.get(DataSet.DATASET_KEY_CATEGORY));
            updateOs(result, data.get(DataSet.DATASET_KEY_OS));
            return true;
        }

        return false;
    }
}
