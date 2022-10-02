package com.flagsense.android.services.impl;

import com.flagsense.android.model.Data;
import com.flagsense.android.model.SdkConfig;
import com.flagsense.android.services.DataPollerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagsense.android.services.DeviceEventService;
import com.flagsense.android.util.StringUtil;
import com.flagsense.android.util.Utility;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.flagsense.android.util.Constants.*;

import android.content.Context;

import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DataPollerServiceImpl implements DataPollerService, AutoCloseable {

    private final Context context;
    private final Data data;
    private final SdkConfig sdkConfig;
    private final DataPoller dataPoller;
    private final DeviceEventService eventService;
    private ScheduledExecutorService scheduledExecutorService;

    private volatile boolean started;
    private ScheduledFuture<?> scheduledFuture;

    public DataPollerServiceImpl(final Context context, Data data, SdkConfig sdkConfig, DeviceEventService eventService) {
        this.data = data;
        this.sdkConfig = sdkConfig;
        this.context = context;
        this.eventService = eventService;
        this.dataPoller = new DataPoller(this.context, this.data, this.sdkConfig, this.eventService);
    }

    @Override
    public synchronized void start() {
        if (started)
            return;

        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = threadFactory.newThread(runnable);
            thread.setDaemon(true);
            return thread;
        });

        scheduledFuture = scheduledExecutorService.schedule(this.dataPoller, 0, TimeUnit.MILLISECONDS);
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

    private class DataPoller implements Runnable {

        private final Context context;
        private final Data data;
        private final SdkConfig sdkConfig;
        private final DeviceEventService eventService;
        private final OkHttpClient httpClient;
        private final ObjectMapper objectMapper;
        private long lastSuccessfulCallOn;

        public DataPoller(final Context context, Data data, SdkConfig sdkConfig, DeviceEventService eventService) {
            this.context = context;
            this.data = data;
            this.sdkConfig = sdkConfig;
            this.eventService = eventService;
            this.objectMapper = new ObjectMapper();
            this.httpClient = new OkHttpClient.Builder()
                    .connectionPool(new ConnectionPool(1, EVENT_FLUSH_INTERVAL * 2, TimeUnit.MILLISECONDS))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
            this.lastSuccessfulCallOn = 0;
        }

        @Override
        public void run() {
            if (this.data.getLastUpdatedOn() > 0 &&
                    System.currentTimeMillis() - this.lastSuccessfulCallOn < DATA_REFRESH_INTERVAL)
                return;

//            System.out.println("fetching data at: " + LocalDateTime.now());
            for (int attempt = 0; attempt < 4; attempt++) {
                if (attempt > 0) {
                    try {
                        Thread.sleep(attempt * 1000);
                    } catch (InterruptedException ignored) {}
                }

                Request request = createRequest();

                try (Response response = this.httpClient.newCall(request).execute()) {
                    if (response.code() != 200) {
                        if (Utility.isHttpErrorRecoverable(response.code())) {
                            continue;
                        }
                    }

                    this.lastSuccessfulCallOn = System.currentTimeMillis();
                    parseResponseAndUpdateData(response);
                    break;
                } catch (Exception exception) {
//                    System.out.println(exception.getMessage());
                }
            }

            synchronized (this.data) {
                this.data.notifyAll();
            }
        }

        private Request createRequest() {
            Headers headers = Headers.of(
                    HEADER_AUTH_TYPE, "fsdk",
                    HEADER_SDK_ID, this.sdkConfig.getSdkId(),
                    HEADER_SDK_SECRET, this.sdkConfig.getSdkSecret(),
                    HEADER_ANDROID_PACKAGE, this.context.getPackageName(),
                    "Content-Type", "application/json"
            );
            return new Request.Builder()
                    .url(BASE_URL + this.sdkConfig.getSdkId() + "/" + this.sdkConfig.getEnvironment())
                    .headers(headers)
                    .get()
                    .build();
        }

        private void parseResponseAndUpdateData(Response response) throws IOException {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                response.close();
                return;
            }

            String responseString = responseBody.string();
            response.close();
            if (StringUtil.isBlank(responseString))
                return;

            Data newData = objectMapper.readValue(responseString, Data.class);
            if (newData == null)
                return;

            if (newData.getLastUpdatedOn() != null && this.data.getLastUpdatedOn() <
                    newData.getLastUpdatedOn() && newData.getSegments() != null &&
                    newData.getFlags() != null && newData.getExperiments() != null) {
                if (!newData.getSegments().isEmpty())
                    this.data.setSegments(newData.getSegments());
                if (!newData.getFlags().isEmpty())
                    this.data.setFlags(newData.getFlags());
                if (!newData.getExperiments().isEmpty())
                    this.data.setExperiments(newData.getExperiments());
                this.data.setLastUpdatedOn(newData.getLastUpdatedOn());
            }
            if (newData.getConfig() != null) {
                this.eventService.setConfig(newData.getConfig());
            }
        }
    }
}
