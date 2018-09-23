package com.doxee.pvideo.dashboard.aws.lambda.handler.useragent;

import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.browser.Firefox;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.browser.MSIE;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.browser.Opera;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.browser.SafariChrome;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.browser.Sleipnir;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.browser.Webview;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.misc.DesktopTools;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.misc.HTTPLibrary;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.misc.MayBeRSSReader;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.misc.SmartPhonePatterns;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.os.Linux;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.os.MiscOS;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.os.OSX;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.os.SmartPhone;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.os.Windows;

import java.util.Map;
import java.util.HashMap;

public final class Classifier {

    public static Map<String, String> parse(final String useragent) {
        return fillResult(execParse(useragent));
    }

    public static Map<String, String> execParse(final String useragent) {
        HashMap<String, String> result = new HashMap<String, String>(6, (float) 1.0); // initial capacity, load factor

        if (useragent == null || useragent.length() < 1 || useragent.equals("-"))
            return result;

        if (tryBrowser(useragent, result)) {
            if (tryOS(useragent, result))
                return result;
            else
                return result;
        }

        // browser unknown. check os only
        if (tryOS(useragent, result))
            return result;

        if (tryRareCases(useragent, result))
            return result;

        return result;
    }

    public static boolean tryBrowser(final String useragent, final Map<String, String> result) {
        if (MSIE.challenge(useragent, result))
            return true;
        if (SafariChrome.challenge(useragent, result))
            return true;
        if (Firefox.challenge(useragent, result))
            return true;
        if (Opera.challenge(useragent, result))
            return true;
        if (Webview.challenge(useragent, result))
            return true;

        return false;
    }

    public static boolean tryOS(final String useragent, final Map<String, String> result) {
        // Windows PC, and Windows Phone OS
        if (Windows.challenge(useragent, result))
            return true;

        // Mac OS X PC, and iOS devices(strict check)
        if (OSX.challenge(useragent, result))
            return true;

        // Linux PC, and Android
        if (Linux.challenge(useragent, result))
            return true;

        // all useragents matches /(iPhone|iPad|iPod|Andorid|BlackBerry)/
        if (SmartPhone.challenge(useragent, result))
            return true;

        // Win98,BSD
        if (MiscOS.challenge(useragent, result))
            return true;

        return false;
    }

    public static boolean tryMisc(final String useragent, final Map<String, String> result) {
        if (DesktopTools.challenge(useragent, result))
            return true;

        return false;
    }

    public static boolean tryRareCases(final String useragent, final Map<String, String> result) {
        if (SmartPhonePatterns.challenge(useragent, result))
            return true;
        if (Sleipnir.challenge(useragent, result))
            return true;
        if (HTTPLibrary.challenge(useragent, result))
            return true;
        if (MayBeRSSReader.challenge(useragent, result))
            return true;

        return false;
    }

    public static Map<String, String> fillResult(final Map<String, String> result) {
        if (result.get(DataSet.ATTRIBUTE_NAME) == null)
            result.put(DataSet.ATTRIBUTE_NAME, DataSet.VALUE_UNKNOWN);
        if (result.get(DataSet.ATTRIBUTE_CATEGORY) == null)
            result.put(DataSet.ATTRIBUTE_CATEGORY, DataSet.VALUE_UNKNOWN);
        if (result.get(DataSet.ATTRIBUTE_OS) == null)
            result.put(DataSet.ATTRIBUTE_OS, DataSet.VALUE_UNKNOWN);
        if (result.get(DataSet.ATTRIBUTE_VERSION) == null)
            result.put(DataSet.ATTRIBUTE_VERSION, DataSet.VALUE_UNKNOWN);
        if (result.get(DataSet.ATTRIBUTE_VENDOR) == null)
            result.put(DataSet.ATTRIBUTE_VENDOR, DataSet.VALUE_UNKNOWN);
        return result;
    }
}
