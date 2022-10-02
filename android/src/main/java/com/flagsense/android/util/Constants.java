package com.flagsense.android.util;

public class Constants {
    public static final String HEADER_AUTH_TYPE = "authType";
    public static final String HEADER_SDK_ID = "sdkId";
    public static final String HEADER_SDK_SECRET = "sdkSecret";
    public static final String HEADER_ANDROID_PACKAGE = "androidPackage";
    public static final String BASE_URL = "https://v1-cdn-service.flagsense.com/";
    public static final String EVENTS_BASE_URL = "https://app-events.flagsense.com/v1/event-service/";

    public static final double MAX_HASH_VALUE = Math.pow(2, 32);
    public static final int TOTAL_THREE_DECIMAL_TRAFFIC = 100000;
    public static final long DATA_REFRESH_INTERVAL = 5 * 60 * 1000L;
    public static final boolean CAPTURE_DEVICE_EVENTS = true;
    public static final boolean CAPTURE_FLAG_EVALUATIONS = true;
    public static final long EVENT_FLUSH_INTERVAL = 5 * 1000L;
    public static final int EVENT_CAPACITY = 100;
    public static final long MAX_INITIALIZATION_WAIT_TIME = 10 * 1000L;
}
