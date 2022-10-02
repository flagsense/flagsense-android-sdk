package com.flagsense.android.model;

public class FSFlag<T> {
    private final String flagId;
    private final String defaultKey;
    private final T defaultValue;

    public FSFlag(String flagId, String defaultKey, T defaultValue) {
        this.flagId = flagId;
        this.defaultKey = defaultKey;
        this.defaultValue = defaultValue;
    }

    public String getFlagId() {
        return flagId;
    }

    public String getDefaultKey() {
        return defaultKey;
    }

    public T getDefaultValue() {
        return defaultValue;
    }
}
