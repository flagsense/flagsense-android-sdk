package com.flagsense.android.request;

import com.flagsense.android.enums.Environment;
import com.flagsense.android.model.FSUser;
import com.flagsense.android.util.Utility;

import java.util.List;
import java.util.Map;

public class DeviceEventRequest {
    private String machineId;
    private String sdkType;
    private Environment environment;
    private String userId;
    private List<Map<String, String>> userAttributes;
    private List<Map<String, String>> deviceInfo;
    private List<Map<String, String>> appInfo;
    private List<FlagVariation> variations;
    private List<Map<String, Object>> events;

    private DeviceEventRequest() {}

    public DeviceEventRequest(String machineId, Environment environment, FSUser fsUser, Map<String, String> deviceInfo, Map<String, String> appInfo) {
        this.machineId = machineId;
        this.sdkType = "android";
        this.environment = environment;
        this.userId = fsUser.getUserId() == null ? "" : fsUser.getUserId();
        this.userAttributes = Utility.toKeyValueArray(fsUser.getAttributes());
        this.deviceInfo = Utility.toKeyValueArray(deviceInfo);
        this.appInfo = Utility.toKeyValueArray(appInfo);
    }

    public DeviceEventRequest clone() {
        DeviceEventRequest request = new DeviceEventRequest();
        request.machineId = this.machineId;
        request.sdkType = this.sdkType;
        request.environment = this.environment;
        request.userId = this.userId;
        request.userAttributes = this.userAttributes;
        request.deviceInfo = this.deviceInfo;
        request.appInfo = this.appInfo;
        request.variations = this.variations;
        request.events = this.events;
        return request;
    }

    public void setFSUser(FSUser fsUser) {
        this.userId = fsUser.getUserId() == null ? "" : fsUser.getUserId();
        this.userAttributes = Utility.toKeyValueArray(fsUser.getAttributes());
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getSdkType() {
        return sdkType;
    }

    public void setSdkType(String sdkType) {
        this.sdkType = sdkType;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Map<String, String>> getUserAttributes() {
        return userAttributes;
    }

    public void setUserAttributes(List<Map<String, String>> userAttributes) {
        this.userAttributes = userAttributes;
    }

    public List<Map<String, String>> getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(List<Map<String, String>> deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public List<Map<String, String>> getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(List<Map<String, String>> appInfo) {
        this.appInfo = appInfo;
    }

    public List<FlagVariation> getVariations() {
        return variations;
    }

    public void setVariations(List<FlagVariation> variations) {
        this.variations = variations;
    }

    public List<Map<String, Object>> getEvents() {
        return events;
    }

    public void setEvents(List<Map<String, Object>> events) {
        this.events = events;
    }
}

/*
An event map has the following keys:
    - eventName: string
    - time: long
    - eventType: string (optional)
    - eventValue: double (optional)
    - eventAttributes: map<string, string> (optional)
    - flagKey: string (optional)
    - flagVariation: string (optional)
 */
