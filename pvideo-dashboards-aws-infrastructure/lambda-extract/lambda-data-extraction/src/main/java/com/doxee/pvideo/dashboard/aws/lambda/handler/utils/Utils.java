package com.doxee.pvideo.dashboard.aws.lambda.handler.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.commons.lang.StringUtils;

import com.doxee.productionservice.util.UtilDn;
import com.doxee.pvideo.dashboard.commons.json.CampaignTitleDTO;

public class Utils {

    private Utils() {
    }

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static double rateTwoDecimals(long dividend, long divisor) {
        if (dividend == 0 || divisor == 0)
            return 0;
        int places = 2;
        double quotient = (float) dividend / (float) divisor;
        BigDecimal bd = BigDecimal.valueOf(quotient);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static int rateTwoDecimalsPercentual(long dividend, long divisor) {
        double d = rateTwoDecimals(dividend, divisor);
        Double d100 = d * 100;
        return d100.intValue();
    }

    public static double rateTwoDecimals(double dividend, double divisor) {
        if (dividend == 0 || divisor == 0)
            return 0;
        int places = 2;
        double quotient = dividend / divisor;
        BigDecimal bd = BigDecimal.valueOf(quotient);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double roundTwoDecimals(Double value) {
        int places = 2;
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double roundFourDecimals(Double value) {
        int places = 4;
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String createPath(CampaignTitleDTO campaignTitleDTO) {
        return campaignTitleDTO.getProcedure().concat(AmazonS3.pathSeparator);
    }

    public static String getClientFromDn(String envDn) {
        return UtilDn.getClientValue(envDn);
    }

    public static String getProcedureFromDn(String envDn) {
        return UtilDn.getProcedureValue(envDn);
    }

    public static String getProcessFromDn(String envDn) {
        return UtilDn.getProcessValue(envDn);
    }

    public static String getEnvironmentFromDn(String envDn) {
        return UtilDn.getEnvironmentValue(envDn);
    }

    public static String getProcedureKey(String client, String procedure) {
        return String.format("client=%s,procedure=%s", client, procedure);
    }

    public static String getClientNameFromProcKey(String procedureKey) {
        return singleValueByDn(procedureKey, "client");
    }

    public static String getProcedureNameFromProcKey(String procedureKey) {
        return singleValueByDn(procedureKey, "procedure");
    }

    private static String singleValueByDn(String dnValue, String el) {
        try {
            LdapName dn = new LdapName(dnValue);

            for (Rdn rdn : dn.getRdns()) {
                if (el.equals(rdn.getType()))
                    return rdn.getValue().toString();
            }
            return null;
        } catch (InvalidNameException ine) {

        }
        return null;
    }

    public static String getJsonFileName(String version) {
        return Constants.DATA_FILE_NAME.concat("_").concat(version).concat(Constants.DATA_FILE_NAME_EXTENSION);
    }

    public static String getJsonFolder(String bucketName, String procedurePath, String scope) {
        return bucketName.concat(AmazonS3.pathSeparator).concat("data").concat(AmazonS3.pathSeparator).concat(procedurePath).concat(AmazonS3.pathSeparator).concat(scope.toLowerCase())
                .concat(AmazonS3.pathSeparator).concat("latest");
    }

    private static String INT_CONV_REG_EXP = ".*_[0-9][0-9](.*)";

    public static String getConversionOrInteractionName(String conversionInteraction) {

        Pattern pattern = Pattern.compile(INT_CONV_REG_EXP);

        Matcher matcher = pattern.matcher(conversionInteraction);
        boolean matches = matcher.matches();

        if (matches) {
            return StringUtils.trim(matcher.group(1));
        }

        return conversionInteraction;
    }
}
