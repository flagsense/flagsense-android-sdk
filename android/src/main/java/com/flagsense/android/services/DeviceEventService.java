package com.flagsense.android.services;

import com.flagsense.android.model.FSUser;
import com.flagsense.android.model.ProjectConfigDTO;

import java.util.Map;

public interface DeviceEventService {
    void start();
    void close();
    void flush();
    void setFSUser(FSUser fsUser);
    void setDeviceInfo(Map<String, String> deviceInfo);
    void setAppInfo(Map<String, String> appInfo);
    void addEvaluationCount(String flagId, String variantKey);
    void recordExperimentEvent(String flagKey, String flagVariation, String eventName, double eventValue, String eventType, Map<String, String> eventAttributes);
    void setConfig(ProjectConfigDTO config);
}
