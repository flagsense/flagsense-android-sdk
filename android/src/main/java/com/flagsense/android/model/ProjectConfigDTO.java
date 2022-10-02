package com.flagsense.android.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectConfigDTO {
    private Boolean captureDeviceEvents;
    private Boolean captureDeviceEvaluations;

    public Boolean getCaptureDeviceEvents() {
        return captureDeviceEvents;
    }

    public Boolean getCaptureDeviceEvaluations() {
        return captureDeviceEvaluations;
    }
}
