package com.doxee.pvideo.dashboard.aws.lambda.handler;

import com.amazonaws.util.IOUtils;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.Classifier;
import com.doxee.pvideo.dashboard.aws.lambda.handler.useragent.DataSet;
import com.doxee.pvideo.dashboard.aws.lambda.handler.utils.Constants;
import com.doxee.pvideo.dashboard.commons.json.ExtractionDTO;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map;
import java.util.Spliterators;

public class TestUserAgent {

    private static Logger log = Logger.getLogger(TestUserAgent.class);

    @Test
    public void testBrowserNameAndVersionUnknown() {
        String userAgent = "Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev>(KHTML, like Gecko) Chrome/<Chrome Rev> Safari/<WebKit Rev>";
        log.info(" User-Agent  : " + userAgent);
        Map r = Classifier.parse(userAgent);
        log.info(" name of browser :" + r.get("name"));
        log.info(" version of browser  :" + r.get("version"));
        String label = (String) r.get(DataSet.DATASET_KEY_LABEL);
        log.info(" label :" + label);
        String browserName = (String) r.get(DataSet.DATASET_KEY_NAME);
        log.info(" category   :" + r.get("category"));
        // => "pc", "smartphone", "mobilephone", "appliance", "crawler", "misc", "unknown"
        log.info(" os from user-agent  :" + r.get("os"));
        // => os from user-agent, or carrier name of mobile phones
        log.info(" version of operating systems  :" + r.get("os_version"));
        assertEquals(true, true);
    }

    @Test
    public void testUserAgentIpadSafari() {

        String userAgent = "Mozilla/5.0 (iPad; CPU OS 10_3_3 like Mac OS X) AppleWebKit/603.3.8 (KHTML, like Gecko) Version/10.0 Mobile/14G60 Safari/602.1";
        Map r = Classifier.parse(userAgent);
        log.info(" name of browser :" + r.get("name"));
        log.info(" version of browser  :" + r.get("version"));
        String label = (String) r.get(DataSet.DATASET_KEY_LABEL);
        log.info(" label :" + label);
        String browserName = (String) r.get(DataSet.DATASET_KEY_NAME);
        log.info(" category   :" + r.get("category"));
        // => "pc", "smartphone", "mobilephone", "appliance", "crawler", "misc", "unknown"
        log.info(" os from user-agent  :" + r.get("os"));
        // => os from user-agent, or carrier name of mobile phones
        log.info(" version of operating systems  :" + r.get("os_version"));
    }

    @Test
    public void testUserAgentLinux() {
        String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.98 Safari/537.36";
        Map r = Classifier.parse(userAgent);
        log.info(" name of browser :" + r.get("name"));
        log.info(" version of browser  :" + r.get("version"));
        String label = (String) r.get(DataSet.DATASET_KEY_LABEL);
        log.info(" label :" + label);
        String browserName = (String) r.get(DataSet.DATASET_KEY_NAME);
        log.info(" category   :" + r.get("category"));
        // => "pc", "smartphone", "mobilephone", "appliance", "crawler", "misc", "unknown"
        log.info(" os from user-agent  :" + r.get("os"));
        // => os from user-agent, or carrier name of mobile phones
        log.info(" version of operating systems  :" + r.get("os_version"));

    }

    @Test
    public void testUserAgentWindowsPhone() {
        String userAgent = "Mozilla/5.0 (Windows Phone 10.0; Android 6.0.1; Microsoft; Lumia 950 XL) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Mobile Safari/537.36 Edge/15.15063";
        Map r = Classifier.parse(userAgent);
        log.info(" name of browser :" + r.get("name"));
        log.info(" version of browser  :" + r.get("version"));
        String label = (String) r.get(DataSet.DATASET_KEY_LABEL);
        log.info(" label :" + label);
        String browserName = (String) r.get(DataSet.DATASET_KEY_NAME);
        log.info(" category   :" + r.get("category"));
        // => "pc", "smartphone", "mobilephone", "appliance", "crawler", "misc", "unknown"
        log.info(" os from user-agent  :" + r.get("os"));
        // => os from user-agent, or carrier name of mobile phones
        log.info(" version of operating systems  :" + r.get("os_version"));

    }

    @Test
    public void testUserAgentError() {
        String userAgent = "Mozilla/5.0 (Linux; Android 7.1.1; S8 Build/NMF26O; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.124 Mobile Safari/537.36";
        Map r = Classifier.parse(userAgent);
        log.info(" name of browser :" + r.get("name"));
        log.info(" version of browser  :" + r.get("version"));
        String label = (String) r.get(DataSet.DATASET_KEY_LABEL);
        log.info(" label :" + label);
        String browserName = (String) r.get(DataSet.DATASET_KEY_NAME);
        log.info(" category   :" + r.get("category"));
        // => "pc", "smartphone", "mobilephone", "appliance", "crawler", "misc", "unknown"
        log.info(" os from user-agent  :" + r.get("os"));
        // => os from user-agent, or carrier name of mobile phones
        log.info(" version of operating systems  :" + r.get("os_version"));

    }

    @Test
    public void testUserAgentProduction() throws Exception {

        TxtReader txtReader = new TxtReader();
        Map<String, List<String>> values = txtReader.read("src/test/resources/userAgent.txt");

        ExtractionDTO extractionDTO = new ExtractionDTO();

        for (Map.Entry<String, List<String>> entry : values.entrySet()) {

            System.out.println(entry.getKey() + "/" + entry.getValue());
            int category_pc = 0;
            int category_smartphone_tablet = 0;
            int category_smartphone_mobile = 0;
            int category_not_detected = 0;

            int os_android = 0;
            int os_iOS = 0;
            int os_Win = 0;
            int os_Mac = 0;
            int os_Linux = 0;
            int os_WindowsPhone = 0;
            int os_not_detected = 0;

            for (String val : entry.getValue()) {
                String[] field = val.split("%");
                String userAgent = field[0].trim();
                int uniqueCount = Integer.valueOf(field[1].trim());

                Map<String, String> r = null;
                try {
                    r = Classifier.parse(userAgent.trim());
                } catch (Exception e) {
                    // System.out.println("######## Errore parsing '" + userAgent + "'");
                }
                // => "pc", "smartphone", "mobilephone", "appliance", "crawler",
                // "misc", "unknown"
                String category = Constants.UNDEFINED;
                String os = Constants.UNDEFINED;
                if (r != null) {
                    category = r.get(DataSet.DATASET_KEY_CATEGORY);
                    os = r.get(DataSet.DATASET_TYPE_OS);
                }

                // log.info("|pvideo-dashboard| extract UserAgentMetricsForClient  category: " + category + " os: " + os);
                if (category.equalsIgnoreCase(DataSet.DATASET_CATEGORY_PC)) {
                    category_pc = category_pc + uniqueCount;
                } else if (category.equalsIgnoreCase(DataSet.DATASET_CATEGORY_SMARTPHONE)) {
                    if (os.equalsIgnoreCase("iPad")) {
                        category_smartphone_tablet = category_smartphone_tablet + uniqueCount;
                    } else {
                        category_smartphone_mobile = category_smartphone_mobile + uniqueCount;
                    }
                } else if (category.equalsIgnoreCase(DataSet.DATASET_CATEGORY_MOBILEPHONE)) {
                    category_smartphone_mobile = category_smartphone_mobile + uniqueCount;
                } else {
                    System.out.println("######## Not detect CATEGORY '" + userAgent + "'");
                    category_not_detected = category_not_detected + uniqueCount;
                }
                // => os from user-agent, or carrier name of mobile phones
                // ["Android", "iOS", "Windows", "MacOS"]
                List<Integer> listOs = extractionDTO.getOperatingSystem().getData();
                if (os.equalsIgnoreCase("Android")) {
                    os_android = os_android + uniqueCount;
                } else if (os.equalsIgnoreCase("iOS") || os.equalsIgnoreCase("iPad")) {
                    os_iOS = os_iOS + uniqueCount;
                } else if (os.contains("Win")) {
                    os_Win = os_Win + uniqueCount;
                } else if (os.contains("Mac")) {
                    os_Mac = os_Mac + uniqueCount;
                } else if (os.contains("Linux")) {
                    os_Linux = os_Linux + uniqueCount;
                } else if (os.contains("Windows Phone")) {
                    os_WindowsPhone = os_WindowsPhone + uniqueCount;
                } else {
                    os_not_detected = os_not_detected + uniqueCount;
                }

            }

            log.info(" -----------------------------------");
            log.info(" category_pc   :" + category_pc);
            log.info(" category_smartphone_tablet   :" + category_smartphone_tablet);
            log.info(" category_smartphone_mobile   :" + category_smartphone_mobile);
            log.info(" category_not_detected   :" + category_not_detected);
            log.info(" -----------------------------------");
            log.info(" os_android   :" + os_android);
            log.info(" os_iOS   :" + os_iOS);
            log.info(" os_Win   :" + os_Win);
            log.info(" os_Mac   :" + os_Mac);
            log.info(" os_Linux   :" + os_Linux);
            log.info(" os_WindowsPhone   :" + os_WindowsPhone);
            log.info(" os_not_detected   :" + os_not_detected);
            log.info(" -----------------------------------");

        }

    }

}
