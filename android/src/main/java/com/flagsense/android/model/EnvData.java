package com.flagsense.android.model;

import com.flagsense.android.enums.Status;

import java.util.List;
import java.util.Map;

public class EnvData {
    private List<String> prerequisites;
    private Status status;
    private String offVariant;
    private Map<String, String> targetUsers;
    private Map<String, Map<String, Integer>> targetSegments;
    private List<String> targetSegmentsOrder;
    private Map<String, Integer> traffic;

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public Status getStatus() {
        return status;
    }

    public String getOffVariant() {
        return offVariant;
    }

    public Map<String, String> getTargetUsers() {
        return targetUsers;
    }

    public Map<String, Map<String, Integer>> getTargetSegments() {
        return targetSegments;
    }

    public List<String> getTargetSegmentsOrder() {
        return targetSegmentsOrder;
    }

    public Map<String, Integer> getTraffic() {
        return traffic;
    }
}
