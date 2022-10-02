package com.flagsense.android.builder;

import android.app.Application;

import com.flagsense.android.Flagsense;
import com.flagsense.android.enums.Environment;
import com.flagsense.android.services.FlagsenseService;

import java.util.Map;

public class FlagsenseServiceBuilder {
    private Application application;
    private String sdkId;
    private String sdkSecret;
    private String env;
    private String userId;
    private Map<String, Object> userAttributes;
    private Map<String, String> deviceInfo;
    private Map<String, String> appInfo;

    public FlagsenseServiceBuilder application(Application application) {
        this.application = application;
        return this;
    }

    public FlagsenseServiceBuilder sdkId(String sdkId) {
        this.sdkId = sdkId;
        return this;
    }

    public FlagsenseServiceBuilder sdkSecret(String sdkSecret) {
        this.sdkSecret = sdkSecret;
        return this;
    }

    public FlagsenseServiceBuilder environment(Environment environment) {
        this.env = environment.name();
        return this;
    }

    public FlagsenseServiceBuilder userId(String userId) {
        this.userId = userId;
        return this;
    }

    public FlagsenseServiceBuilder userAttributes(Map<String, Object> userAttributes) {
        this.userAttributes = userAttributes;
        return this;
    }

    public FlagsenseServiceBuilder deviceInfo(Map<String, String> deviceInfo) {
        this.deviceInfo = deviceInfo;
        return this;
    }

    public FlagsenseServiceBuilder appInfo(Map<String, String> appInfo) {
        this.appInfo = appInfo;
        return this;
    }

    public FlagsenseService build() {
        return Flagsense.createService(application, sdkId, sdkSecret, env, userId, userAttributes, deviceInfo, appInfo);
    }
}
