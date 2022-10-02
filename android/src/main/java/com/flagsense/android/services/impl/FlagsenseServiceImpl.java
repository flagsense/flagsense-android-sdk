package com.flagsense.android.services.impl;

import static com.flagsense.android.util.Constants.MAX_INITIALIZATION_WAIT_TIME;

import android.app.Application;
import android.content.IntentFilter;
import android.os.Build;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagsense.android.enums.Environment;
import com.flagsense.android.enums.VariantType;
import com.flagsense.android.listener.FSApplicationLifecycleObserver;
import com.flagsense.android.model.Data;
import com.flagsense.android.model.ExperimentDTO;
import com.flagsense.android.model.FSFlag;
import com.flagsense.android.model.FSUser;
import com.flagsense.android.model.FSVariation;
import com.flagsense.android.model.SdkConfig;
import com.flagsense.android.model.UserVariantDTO;
import com.flagsense.android.listener.FSConnectivityReceiver;
import com.flagsense.android.services.DataPollerService;
import com.flagsense.android.services.DeviceEventService;
import com.flagsense.android.services.FlagsenseService;
import com.flagsense.android.services.UserVariantService;
import com.flagsense.android.util.FlagsenseException;
import com.flagsense.android.util.StringUtil;
import com.flagsense.android.util.Utility;

import java.util.Map;

public class FlagsenseServiceImpl implements FlagsenseService {

    private final Application application;
    private final ObjectMapper objectMapper;
    private final Data data;
    private final SdkConfig sdkConfig;
    private final UserVariantService userVariantService;
    private final DataPollerService dataPollerService;
    private final DeviceEventService eventService;
    private FSUser fsUser;
    private FSConnectivityReceiver connectivityReceiver;
    private FSApplicationLifecycleObserver applicationLifecycleObserver;
    private long maxInitializationWaitTime;

    public FlagsenseServiceImpl(final Application application, String sdkId, String sdkSecret,
                                Environment environment, FSUser fsUser,
                                Map<String, String> deviceInfo, Map<String, String> appInfo) {
        this.objectMapper = new ObjectMapper();
        this.application = application;
        this.sdkConfig = new SdkConfig(sdkId, sdkSecret, environment);
        this.fsUser = fsUser;
        this.data = new Data();
        this.userVariantService = new UserVariantServiceImpl(this.data);
        this.eventService = new DeviceEventServiceImpl(this.application, this.sdkConfig, this.fsUser, deviceInfo, appInfo);
        this.dataPollerService = new DataPollerServiceImpl(this.application, this.data, this.sdkConfig, this.eventService);
        this.dataPollerService.start();
        this.eventService.start();
        this.maxInitializationWaitTime = MAX_INITIALIZATION_WAIT_TIME;

        applicationLifecycleObserver = new FSApplicationLifecycleObserver(this);
        ProcessLifecycleOwner.get()
                .getLifecycle()
                .addObserver(applicationLifecycleObserver);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityReceiver = new FSConnectivityReceiver();
            IntentFilter filter = new IntentFilter(FSConnectivityReceiver.CONNECTIVITY_CHANGE);
            this.application.registerReceiver(connectivityReceiver, filter);
        }
    }

    @Override
    public boolean initializationComplete() {
        return this.data.getLastUpdatedOn() > 0 || !Utility.isInternetConnected(this.application);
    }

    @Override
    public void waitForInitializationComplete() {
        try {
            synchronized (this.data) {
                if (!this.initializationComplete())
                    this.data.wait(this.maxInitializationWaitTime);
            }
        }
        catch (InterruptedException e) {
//             System.out.println(e.toString());
        }
    }

    @Override
    public void setFSUser(String userId) {
        this.setFSUser(userId, null);
    }

    @Override
    public void setFSUser(String userId, Map<String, Object> userAttributes) {
        this.fsUser.setUserId(userId);
        this.fsUser.setAttributes(userAttributes);
        this.eventService.setFSUser(this.fsUser);
    }

    @Override
    public void setDeviceInfo(Map<String, String> deviceInfo) {
        this.eventService.setDeviceInfo(deviceInfo);
    }

    @Override
    public void setAppInfo(Map<String, String> appInfo) {
        this.eventService.setAppInfo(appInfo);
    }

    @Override
    public void setMaxInitializationWaitTime(long timeInMillis) {
        this.maxInitializationWaitTime = timeInMillis;
    }

    @Override
    public FSVariation<Boolean> booleanVariation(FSFlag<Boolean> fsFlag) {
        try {
            return (FSVariation<Boolean>) this.evaluate(fsFlag, VariantType.BOOL);
        }
        catch (Exception e) {
            return new FSVariation<>(fsFlag.getDefaultKey(), fsFlag.getDefaultValue());
        }
    }

    @Override
    public FSVariation<Integer> integerVariation(FSFlag<Integer> fsFlag) {
        try {
            return (FSVariation<Integer>) this.evaluate(fsFlag, VariantType.INT);
        }
        catch (Exception e) {
            return new FSVariation<>(fsFlag.getDefaultKey(), fsFlag.getDefaultValue());
        }
    }

    @Override
    public FSVariation<Double> decimalVariation(FSFlag<Double> fsFlag) {
        try {
            return (FSVariation<Double>) this.evaluate(fsFlag, VariantType.DOUBLE);
        }
        catch (Exception e) {
            return new FSVariation<>(fsFlag.getDefaultKey(), fsFlag.getDefaultValue());
        }
    }

    @Override
    public FSVariation<String> stringVariation(FSFlag<String> fsFlag) {
        try {
            return (FSVariation<String>) this.evaluate(fsFlag, VariantType.STRING);
        }
        catch (Exception e) {
            return new FSVariation<>(fsFlag.getDefaultKey(), fsFlag.getDefaultValue());
        }
    }

    @Override
    public FSVariation<JsonNode> jsonVariation(FSFlag<JsonNode> fsFlag) {
        try {
            FSVariation<Map<String, Object>> fsVariation = (FSVariation<Map<String, Object>>) this.evaluate(fsFlag, VariantType.JSON);
            return new FSVariation<>(fsVariation.getKey(), objectMapper.valueToTree(fsVariation.getValue()));
        }
        catch (Exception e) {
            return new FSVariation<>(fsFlag.getDefaultKey(), fsFlag.getDefaultValue());
        }
    }

    @Override
    public FSVariation<Map<String, Object>> mapVariation(FSFlag<Map<String, Object>> fsFlag) {
        try {
            return (FSVariation<Map<String, Object>>) this.evaluate(fsFlag, VariantType.JSON);
        }
        catch (Exception e) {
            return new FSVariation<>(fsFlag.getDefaultKey(), fsFlag.getDefaultValue());
        }
    }

    @Override
    public void recordEvent(FSFlag<?> fsFlag, String eventName) {
        this.recordEvent(fsFlag, eventName, 1);
    }

    @Override
    public void recordEvent(FSFlag<?> fsFlag, String eventName, double value) {
        this.recordEvent(fsFlag, eventName, value, null, null);
    }

    @Override
    public void recordEvent(FSFlag<?> fsFlag, String eventName, double value, String eventType,
                            Map<String, String> eventAttributes) {
        if (this.fsUser == null || StringUtil.isBlank(fsFlag.getFlagId()) || StringUtil.isBlank(eventName))
            return;
        ExperimentDTO experimentDTO = this.getExperimentData(fsFlag.getFlagId());
        if (experimentDTO == null || experimentDTO.getEventNames() == null || !experimentDTO.getEventNames().contains(eventName))
            return;
        String variantKey = this.getVariantKey(fsFlag.getFlagId(), fsFlag.getDefaultKey());
        if (StringUtil.isBlank(variantKey))
            return;
        this.eventService.recordExperimentEvent(fsFlag.getFlagId(), variantKey, eventName, value, eventType, eventAttributes);
    }

    @Override
    public void onNetworkConnectivityChange(boolean connected) {
        if (connected) {
            this.dataPollerService.start();
            this.eventService.start();
        }
        else {
            this.dataPollerService.close();
            this.eventService.close();
        }
    }

    @Override
    public void onBecameForeground() {
        this.dataPollerService.start();
        this.eventService.start();
    }

    @Override
    public void onBecameBackground() {
        this.dataPollerService.close();
        this.eventService.close();
        this.eventService.flush();
    }

    @Override
    public void close() {
        this.dataPollerService.close();
        this.eventService.close();
        this.eventService.flush();
        if (connectivityReceiver != null) {
            this.application.unregisterReceiver(connectivityReceiver);
            connectivityReceiver = null;
        }
        if (applicationLifecycleObserver != null) {
            ProcessLifecycleOwner.get()
                    .getLifecycle()
                    .removeObserver(applicationLifecycleObserver);
            applicationLifecycleObserver = null;
        }
    }

    private FSVariation<?> evaluate(FSFlag<?> fsFlag, VariantType expectedVariantType) {
        UserVariantDTO userVariantDTO = UserVariantDTO.builder()
                .flagId(fsFlag.getFlagId())
                .userId(this.fsUser.getUserId())
                .attributes(this.fsUser.getAttributes())
                .defaultKey(fsFlag.getDefaultKey())
                .defaultValue(fsFlag.getDefaultValue())
                .expectedVariantType(expectedVariantType)
                .build();

        this.evaluate(userVariantDTO);
        return new FSVariation<>(userVariantDTO.getKey(), userVariantDTO.getValue());
    }

    private void evaluate(UserVariantDTO userVariantDTO) {
        try {
            if (this.data.getLastUpdatedOn() == 0)
                throw new FlagsenseException("Loading data");
            this.userVariantService.getUserVariant(userVariantDTO);
            this.eventService.addEvaluationCount(userVariantDTO.getFlagId(), userVariantDTO.getKey());
        }
        catch (Exception e) {
//            System.out.println(e.toString());
            userVariantDTO.setKey(userVariantDTO.getDefaultKey());
            userVariantDTO.setValue(userVariantDTO.getDefaultValue());
            this.eventService.addEvaluationCount(userVariantDTO.getFlagId(),
                    StringUtil.isNotBlank(userVariantDTO.getDefaultKey()) ? userVariantDTO.getDefaultKey() : "FS_Empty");
        }
    }

    private String getVariantKey(String flagId, String defaultVariantKey) {
        try {
            if (this.data.getLastUpdatedOn() == 0)
                throw new FlagsenseException("Loading data");
            UserVariantDTO userVariantDTO = UserVariantDTO.builder()
                    .flagId(flagId)
                    .userId(this.fsUser.getUserId())
                    .attributes(this.fsUser.getAttributes())
                    .expectedVariantType(VariantType.ANY)
                    .build();
            this.userVariantService.getUserVariant(userVariantDTO);
            return userVariantDTO.getKey();
        }
        catch (Exception e) {
            return StringUtil.isNotBlank(defaultVariantKey) ? defaultVariantKey : "FS_Empty";
        }
    }

    private ExperimentDTO getExperimentData(String flagId) {
        if (this.data == null || this.data.getExperiments() == null)
            return null;
        return this.data.getExperiments().get(flagId);
    }
}
