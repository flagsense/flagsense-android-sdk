package com.flagsense.android.model;

import java.util.HashMap;
import java.util.Map;

public class FSUser {
    private String userId;
    private Map<String, Object> attributes;

    public FSUser(String userId, Map<String, Object> attributes) {
        this.userId = userId;
        this.attributes = attributes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String key, Object value) {
        if (key == null)
            return;
        if (this.attributes == null)
            this.attributes = new HashMap<>();
        this.attributes.put(key, value);
    }
}
