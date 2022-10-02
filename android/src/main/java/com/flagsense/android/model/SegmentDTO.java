package com.flagsense.android.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SegmentDTO {
    private String id;
    private List<List<Rule>> rules;

    public String getId() {
        return id;
    }

    public List<List<Rule>> getRules() {
        return rules;
    }
}
