package com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.os;

import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.AgentCategory;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.DataSet;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Windows extends AgentCategory {

    private static Pattern windowsVer = Pattern.compile("Windows ([ .a-zA-Z0-9]+)[;\\)]");

    private static Pattern windowsPhoneOsVer = Pattern.compile("Phone(?: OS)? ([.0-9]+)");

    public static boolean challenge(final String ua, final Map<String, String> result) {
        int pos = ua.indexOf("Windows");
        if (pos < 0)
            return false;

        // Xbox Series
        if (ua.indexOf("Xbox") > -1) {
            Map<String, String> d;
            if (ua.indexOf("Xbox; Xbox One)") > -1)
                d = DataSet.get("XboxOne");
            else
                d = DataSet.get("Xbox360");
            // overwrite browser detections as appliance
            updateMap(result, d);
            return true;
        }

        Map<String, String> data = DataSet.get("Win");

        Matcher win = windowsVer.matcher(ua);
        if (!win.find(pos)) {
            // windows, but version unknown
            updateCategory(result, data.get(DataSet.DATASET_KEY_CATEGORY));
            updateOs(result, data.get(DataSet.DATASET_KEY_NAME));
            return true;
        }

        String versionString = win.group(1);
        if (versionString.equals("NT 10.0"))
            data = DataSet.get("Win10");
        else if (versionString.equals("NT 6.3"))
            data = DataSet.get("Win8.1");
        else if (versionString.equals("NT 6.2"))
            data = DataSet.get("Win8");
        else if (versionString.equals("NT 6.1"))
            data = DataSet.get("Win7");
        else if (versionString.equals("NT 6.0"))
            data = DataSet.get("WinVista");
        else if (versionString.equals("NT 5.1"))
            data = DataSet.get("WinXP");
        else if (versionString.startsWith("Phone")) { // "Phone OS" or "Phone 8.1", ...
            data = DataSet.get("WinPhone");
            Matcher phoneOs = windowsPhoneOsVer.matcher(ua);
            if (phoneOs.find()) {
                versionString = phoneOs.group(1);
            }
        } else if (versionString.equals("NT 5.0"))
            data = DataSet.get("Win2000");
        else if (versionString.equals("NT 4.0"))
            data = DataSet.get("WinNT4");
        else if (versionString.equals("98")) // wow, WinMe is shown as 'Windows 98; Win9x 4.90', fxxxk
            data = DataSet.get("Win98");
        else if (versionString.equals("95"))
            data = DataSet.get("Win95");
        else if (versionString.equals("CE"))
            data = DataSet.get("WinCE");

        // else, windows, but version unknown

        updateCategory(result, data.get(DataSet.DATASET_KEY_CATEGORY));
        updateOs(result, data.get(DataSet.DATASET_KEY_NAME));
        updateOsVersion(result, versionString);
        return true;
    }
}
