package com.flagsense.android.model;

public class FSVariation<T> {
    private final String key;
    private final T value;

    public FSVariation(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }
}
