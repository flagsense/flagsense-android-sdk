package com.flagsense.android.model;

import com.flagsense.android.enums.VariantType;

import java.util.Map;

public class UserVariantDTO {
    private String userId;
    private Map<String, Object> attributes;
    private String flagId;
    private String defaultKey;
    private Object defaultValue;
    private String key;
    private Object value;
    private VariantType expectedVariantType;

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

    public String getFlagId() {
        return flagId;
    }

    public void setFlagId(String flagId) {
        this.flagId = flagId;
    }

    public String getDefaultKey() {
        return defaultKey;
    }

    public void setDefaultKey(String defaultKey) {
        this.defaultKey = defaultKey;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public VariantType getExpectedVariantType() {
        return expectedVariantType;
    }

    public void setExpectedVariantType(VariantType expectedVariantType) {
        this.expectedVariantType = expectedVariantType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId = null;
        private Map<String, Object> attributes = null;
        private String flagId = null;
        private String defaultKey = null;
        private Object defaultValue = null;
        private String key = null;
        private Object value = null;
        private VariantType expectedVariantType = null;

        private Builder() {
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder flagId(String flagId) {
            this.flagId = flagId;
            return this;
        }

        public Builder defaultKey(String defaultKey) {
            this.defaultKey = defaultKey;
            return this;
        }

        public Builder defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder expectedVariantType(VariantType expectedVariantType) {
            this.expectedVariantType = expectedVariantType;
            return this;
        }

        public UserVariantDTO build() {
            UserVariantDTO userVariantDTO = new UserVariantDTO();
            userVariantDTO.setUserId(this.userId);
            userVariantDTO.setAttributes(this.attributes);
            userVariantDTO.setFlagId(this.flagId);
            userVariantDTO.setDefaultKey(this.defaultKey);
            userVariantDTO.setDefaultValue(this.defaultValue);
            userVariantDTO.setKey(this.key);
            userVariantDTO.setValue(this.value);
            userVariantDTO.setExpectedVariantType(this.expectedVariantType);
            return userVariantDTO;
        }
    }
}
