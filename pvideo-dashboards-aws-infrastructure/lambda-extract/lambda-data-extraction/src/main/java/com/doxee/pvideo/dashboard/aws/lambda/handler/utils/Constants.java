package com.doxee.pvideo.dashboard.aws.lambda.handler.utils;

/**
 * This class contains all constants.
 * 
 * @author smurino
 * 
 */
public class Constants {

    private Constants() {
    }

    public static final String DATA_FILE_NAME = "data";

    public static final String DATA_FILE_NAME_EXTENSION = ".json";

    public static final String CONFIGURATION_TABLENAME = "pVideoDashboard-dev";

    public static final String BUCKET_NAME = "pvideodashboard";

    public static final String INTERACTIONS = "INTERACTIONS";

    public static final String CONVERSIONS = "CONVERSIONS";

    public static final String VIEWUNIQUE_TOTAL = "VIEWUNIQUE_TOTAL";

    public static final String SENT = "SENT";

    public static final String OPEN = "OPEN";

    public static final String CLICK = "CLICK";

    public static final String BOUNCE = "BOUNCE";

    public static final String OPEN_FIRST = "OPEN_FIRST";

    public static final String VIEW_FIRST = "VIEW_FIRST";

    public static final String CONV_FIRST = "CONV_FIRST";

    public static final String PURL_PROD_CLICKED = "PURL_PROD_CLICKED";

    public static final String UNDEFINED = "undefined";

    // Env variable
    public static final String CONFIGURATION_TABLE_ENV = "CONFIGURATION_TABLE";

    public static final String CLUSTER_ID_CA_ENV = "CLUSTER_ID_CA";

    public static final String BUCKET_NAME_ENV = "BUCKET_NAME";

    public static final String ALL_VERSION = "ALL";

    public static final String ALL_VERSION_LABEL = "Tutte le campagne";

    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    public static final String QUERY_PARAMETER_VERSION = "version";

    public static final String QUERY_PARAMETER_SCOPE = "scope";

    public static final String QUERY_PARAMETER_TOTAL_UNIQUE = "total_unique";

    public static final String LOGGING_FROM_DATE = " from date: ";

    public static final String LOGGING_CAMPAIGN = " campaign ";

}
