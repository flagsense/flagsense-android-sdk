package com.flagsense.android.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.flagsense.android.model.FSFlag;
import com.flagsense.android.model.FSVariation;

import java.util.Map;

public interface FlagsenseService {
    boolean initializationComplete();
    void waitForInitializationComplete();
    void setFSUser(String userId);
    void setFSUser(String userId, Map<String, Object> userAttributes);
    void setDeviceInfo(Map<String, String> deviceInfo);
    void setAppInfo(Map<String, String> appInfo);
    void setMaxInitializationWaitTime(long timeInMillis);
    FSVariation<Boolean> booleanVariation(FSFlag<Boolean> fsFlag);
    FSVariation<Integer> integerVariation(FSFlag<Integer> fsFlag);
    FSVariation<Double> decimalVariation(FSFlag<Double> fsFlag);
    FSVariation<String> stringVariation(FSFlag<String> fsFlag);
    FSVariation<JsonNode> jsonVariation(FSFlag<JsonNode> fsFlag);
    FSVariation<Map<String, Object>> mapVariation(FSFlag<Map<String, Object>> fsFlag);
    void recordEvent(FSFlag<?> fsFlag, String eventName);
    void recordEvent(FSFlag<?> fsFlag, String eventName, double value);
    void recordEvent(FSFlag<?> fsFlag, String eventName, double value, String eventType, Map<String, String> eventAttributes);
    void onNetworkConnectivityChange(boolean connected);
    void onBecameForeground();
    void onBecameBackground();
    void close();
}
