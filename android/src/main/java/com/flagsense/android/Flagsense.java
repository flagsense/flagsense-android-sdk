package com.flagsense.android;

import android.app.Application;

import com.fasterxml.jackson.databind.JsonNode;
import com.flagsense.android.builder.FlagsenseServiceBuilder;
import com.flagsense.android.enums.Environment;
import com.flagsense.android.model.FSFlag;
import com.flagsense.android.model.FSUser;
import com.flagsense.android.services.FlagsenseService;
import com.flagsense.android.services.impl.FlagsenseServiceImpl;
import com.flagsense.android.util.FlagsenseException;
import com.flagsense.android.util.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Flagsense {
    private static final ConcurrentMap<String, FlagsenseService> flagsenseServiceMap = new ConcurrentHashMap<>();

    private Flagsense() {}

    public static FlagsenseServiceBuilder serviceBuilder() {
        return new FlagsenseServiceBuilder();
    }

    public static FlagsenseService createService(final Application application, String sdkId, String sdkSecret, String env, String userId, Map<String, Object> attributes, Map<String, String> deviceInfo, Map<String, String> appInfo) {
        if (application == null || StringUtil.isBlank(sdkId) || StringUtil.isBlank(sdkSecret))
            throw new FlagsenseException("Empty application and sdk params not allowed");

        if (!flagsenseServiceMap.containsKey(sdkId)) {
            Environment environment = Environment.isValid(env) ? Environment.valueOf(env) : Environment.PROD;
            synchronized (Flagsense.class) {
                if (!flagsenseServiceMap.containsKey(sdkId))
                    flagsenseServiceMap.put(sdkId, new FlagsenseServiceImpl(application, sdkId, sdkSecret, environment, new FSUser(userId, attributes), deviceInfo , appInfo));
            }
        }

        return flagsenseServiceMap.get(sdkId);
    }

    public static FSFlag<Boolean> booleanFlag(String flagId, String defaultKey, boolean defaultValue) {
        return new FSFlag<>(flagId, defaultKey, defaultValue);
    }

    public static FSFlag<Integer> integerFlag(String flagId, String defaultKey, int defaultValue) {
        return new FSFlag<>(flagId, defaultKey, defaultValue);
    }

    public static FSFlag<Double> decimalFlag(String flagId, String defaultKey, double defaultValue) {
        return new FSFlag<>(flagId, defaultKey, defaultValue);
    }

    public static FSFlag<String> stringFlag(String flagId, String defaultKey, String defaultValue) {
        return new FSFlag<>(flagId, defaultKey, defaultValue);
    }

    public static FSFlag<JsonNode> jsonFlag(String flagId, String defaultKey, JsonNode defaultValue) {
        return new FSFlag<>(flagId, defaultKey, defaultValue);
    }

    public static FSFlag<Map<String, Object>> mapFlag(String flagId, String defaultKey, Map<String, Object> defaultValue) {
        return new FSFlag<>(flagId, defaultKey, defaultValue);
    }

    public static void onNetworkConnectivityChangeInstances(boolean connected) {
        if (flagsenseServiceMap.isEmpty())
            return;

        for (FlagsenseService flagsenseService : flagsenseServiceMap.values()) {
            flagsenseService.onNetworkConnectivityChange(connected);
        }
    }

    public static void close(String sdkId) {
        FlagsenseService flagsenseService = flagsenseServiceMap.remove(sdkId);
        if (flagsenseService != null)
            flagsenseService.close();
    }
}
