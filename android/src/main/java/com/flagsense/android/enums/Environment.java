package com.flagsense.android.enums;

public enum Environment {
    DEV, STAGE, PROD;

    public static boolean isValid(String env) {
        for (Environment environment : Environment.values()) {
            if (environment.name().equals(env))
                return true;
        }
        return false;
    }
}
