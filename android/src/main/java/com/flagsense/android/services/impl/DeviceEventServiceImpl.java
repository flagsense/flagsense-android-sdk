package com.flagsense.android.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flagsense.android.model.FSUser;
import com.flagsense.android.model.ProjectConfigDTO;
import com.flagsense.android.model.SdkConfig;
import com.flagsense.android.request.DeviceEventRequest;
import com.flagsense.android.request.FlagVariation;
import com.flagsense.android.services.DeviceEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagsense.android.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.flagsense.android.util.Constants.*;

import android.content.Context;

import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceEventServiceImpl implements DeviceEventService, AutoCloseable {

    private final ObjectMapper objectMapper;

    private final Context context;
    private final SdkConfig sdkConfig;
    private final DeviceEventRequest request;
    private final BlockingQueue<FlagVariation> variations;
    private final BlockingQueue<Map<String, Object>> events;

    private volatile boolean started;
    private volatile boolean forceFlush;
    private volatile boolean captureDeviceEvents;
    private volatile boolean captureFlagEvaluations;
    private final EventSender eventSender;
    private ScheduledFuture<?> scheduledFuture;
    private ScheduledExecutorService scheduledExecutorService;

    public DeviceEventServiceImpl(final Context context, SdkConfig sdkConfig, FSUser fsUser,
                                  Map<String, String> deviceInfo, Map<String, String> appInfo) {
        String machineId = UUID.randomUUID().toString();
        this.objectMapper = new ObjectMapper();
        this.captureDeviceEvents = CAPTURE_DEVICE_EVENTS;
        this.captureFlagEvaluations = CAPTURE_FLAG_EVALUATIONS;

        this.context = context;
        this.sdkConfig = sdkConfig;
        this.variations = new ArrayBlockingQueue<>(EVENT_CAPACITY);
        this.events = new ArrayBlockingQueue<>(EVENT_CAPACITY);
        this.request = new DeviceEventRequest(machineId, sdkConfig.getEnvironment(), fsUser, deviceInfo, appInfo);

        this.eventSender = new EventSender(this.context, this.sdkConfig, this.request, this.variations,
                this.events, this.captureDeviceEvents, this.captureFlagEvaluations);
    }

    @Override
    public synchronized void start() {
        if (started || (!this.captureDeviceEvents && !this.captureFlagEvaluations))
            return;

        forceFlush = false;
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = threadFactory.newThread(runnable);
            thread.setDaemon(true);
            return thread;
        });

        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this.eventSender, 0, EVENT_FLUSH_INTERVAL, TimeUnit.MILLISECONDS);
        started = true;
    }

    @Override
    public synchronized void close() {
        if (!started || scheduledExecutorService.isShutdown())
            return;

        scheduledFuture.cancel(true);
        scheduledExecutorService.shutdownNow();
        started = false;
    }

    @Override
    public void flush() {
        if (!this.captureDeviceEvents && !this.captureFlagEvaluations)
            return;

        forceFlush = true;
        new Thread(this.eventSender).start();
    }

    @Override
    public void setFSUser(FSUser fsUser) {
        this.request.setFSUser(fsUser);
    }

    @Override
    public void setDeviceInfo(Map<String, String> deviceInfo) {
        this.request.setDeviceInfo(Utility.toKeyValueArray(deviceInfo));
    }

    @Override
    public void setAppInfo(Map<String, String> appInfo) {
        this.request.setAppInfo(Utility.toKeyValueArray(appInfo));
    }

    @Override
    public void addEvaluationCount(String flagId, String variantKey) {
        try {
            if (!this.captureFlagEvaluations || this.variations.remainingCapacity() == 0)
                return;
            this.variations.offer(new FlagVariation(flagId, variantKey));
        }
        catch (Exception ignored) {
        }
    }

    @Override
    public void recordExperimentEvent(String flagKey, String flagVariation, String eventName, double eventValue, String eventType, Map<String, String> eventAttributes) {
        try {
            if (!this.captureDeviceEvents || this.events.remainingCapacity() == 0)
                return;

            Map<String, Object> event = new HashMap<>();
            event.put("time", System.currentTimeMillis());
            event.put("flagKey", flagKey);
            event.put("flagVariation", flagVariation);
            event.put("eventName", eventName);
            event.put("eventValue", eventValue);
            event.put("eventType", eventType);
            event.put("eventAttributes", Utility.toKeyValueArray(eventAttributes));

            this.events.offer(event);
        }
        catch (Exception ignored) {
        }
    }

    @Override
    public synchronized void setConfig(ProjectConfigDTO config) {
        if (config == null)
            return;

        if (config.getCaptureDeviceEvents() != null) {
            this.captureDeviceEvents = config.getCaptureDeviceEvents();
            this.eventSender.setCaptureDeviceEvents(this.captureDeviceEvents);
        }

        if (config.getCaptureDeviceEvaluations() != null) {
            this.captureFlagEvaluations = config.getCaptureDeviceEvaluations();
            this.eventSender.setCaptureFlagEvaluations(this.captureFlagEvaluations);
        }
    }

    private class EventSender implements Runnable {

        private final Context context;
        private final SdkConfig sdkConfig;
        private final DeviceEventRequest request;
        private final BlockingQueue<FlagVariation> variations;
        private final BlockingQueue<Map<String, Object>> events;
        private final OkHttpClient httpClient;
        private long lastSuccessfulCallOn;
        private boolean captureDeviceEvents;
        private boolean captureFlagEvaluations;

        public EventSender(final Context context, SdkConfig sdkConfig, DeviceEventRequest request,
                           BlockingQueue<FlagVariation> variations, BlockingQueue<Map<String, Object>> events,
                           boolean captureDeviceEvents, boolean captureFlagEvaluations) {
            this.context = context;
            this.sdkConfig = sdkConfig;
            this.request = request;
            this.variations = variations;
            this.events = events;
            this.httpClient = new OkHttpClient.Builder()
                    .connectionPool(new ConnectionPool(1, EVENT_FLUSH_INTERVAL * 2, TimeUnit.MILLISECONDS))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
            this.lastSuccessfulCallOn = 0;
            this.captureDeviceEvents = captureDeviceEvents;
            this.captureFlagEvaluations = captureFlagEvaluations;
        }

        @Override
        public void run() {
            if (!forceFlush && this.lastSuccessfulCallOn > 0 &&
                    System.currentTimeMillis() - this.lastSuccessfulCallOn < EVENT_FLUSH_INTERVAL)
                return;

            String requestBody = getRequestBody();
            if (requestBody != null) {
                sendEvents("device-events", requestBody);
            }
        }

        public synchronized void setCaptureDeviceEvents(boolean captureDeviceEvents) {
            this.captureDeviceEvents = captureDeviceEvents;
        }

        public synchronized void setCaptureFlagEvaluations(boolean captureFlagEvaluations) {
            this.captureFlagEvaluations = captureFlagEvaluations;
        }

        private synchronized String getRequestBody() {
            if ((!this.captureDeviceEvents && !this.captureFlagEvaluations) ||
                    !Utility.isInternetConnected(this.context))
                return null;

            DeviceEventRequest deviceEventRequest = this.request.clone();

            int variationsSize = this.variations.size();
            int eventsSize = this.events.size();

            if (this.captureFlagEvaluations && variationsSize > 0) {
                List<FlagVariation> variationsToSend = new ArrayList<>(variationsSize + 1);
                this.variations.drainTo(variationsToSend);
                deviceEventRequest.setVariations(variationsToSend);
            }

            if (this.captureDeviceEvents && eventsSize > 0) {
                List<Map<String, Object>> eventsToSend = new ArrayList<>(eventsSize + 1);
                this.events.drainTo(eventsToSend);
                deviceEventRequest.setEvents(eventsToSend);
            }

            if (Utility.isEmptyList(deviceEventRequest.getVariations()) &&
                    Utility.isEmptyList(deviceEventRequest.getEvents()))
                return null;

            if (!this.captureDeviceEvents) {
                deviceEventRequest.setUserAttributes(Utility.toKeyValueArray(null));
                deviceEventRequest.setDeviceInfo(Utility.toKeyValueArray(null));
                deviceEventRequest.setAppInfo(Utility.toKeyValueArray(null));
            }

            try {
                return objectMapper.writeValueAsString(deviceEventRequest);
            } catch (JsonProcessingException e) {
                return null;
            }
        }

        private void sendEvents(String api, String requestBody) {
            for (int attempt = 0; attempt < 2; attempt++) {
                if (attempt > 0) {
                    try {
                        Thread.sleep(attempt * 1000);
                    } catch (InterruptedException ignored) {}
                }

                Request request = createRequest(api, requestBody);

                try (Response response = this.httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        if (Utility.isHttpErrorRecoverable(response.code())) {
                            response.close();
                            continue;
                        }
                    }
                    this.lastSuccessfulCallOn = System.currentTimeMillis();
                    response.close();
                    break;
                } catch (Exception exception) {
//                    System.out.println(exception.getMessage());
                }
            }
        }

        private Request createRequest(String api, String requestBody) {
            Headers headers = Headers.of(
                    HEADER_AUTH_TYPE, "fsdk",
                    HEADER_SDK_ID, this.sdkConfig.getSdkId(),
                    HEADER_SDK_SECRET, this.sdkConfig.getSdkSecret(),
                    HEADER_ANDROID_PACKAGE, this.context.getPackageName(),
                    "Content-Type", "application/json"
            );
            return new Request.Builder()
                    .url(EVENTS_BASE_URL + api)
                    .headers(headers)
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();
        }
    }
}
