package com.flagsense.android.util;

import com.fasterxml.jackson.core.Version;

public class VersionUtil {

    private static final String VERSION_PARSE_ERROR =
            "Invalid version string, Could not parse segment %s within %s";

    public static Version parse(String version) {
        if (StringUtil.isBlank(version))
            throw new IllegalArgumentException(String.format(VERSION_PARSE_ERROR, "", version));

        String[] parts = version.trim().split("\\.");
        int[] intParts = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            String input = i == parts.length - 1 ? parts[i].replaceAll("\\D.*", "") : parts[i];

            if (!StringUtil.isBlank(input)) {
                try {
                    intParts[i] = Integer.parseInt(input);
                } catch (IllegalArgumentException o_O) {
                    throw new IllegalArgumentException(String.format(VERSION_PARSE_ERROR, input, version), o_O);
                }
            }
        }

        int intPartsLength = intParts.length;
        int major = intPartsLength > 0 ? intParts[0] : 0;
        int minor = intPartsLength > 1 ? intParts[1] : 0;
        int patchLevel = intPartsLength > 2 ? intParts[2] : 0;

        return new Version(major, minor, patchLevel, null, null, null);
    }

    public static boolean isGreaterThan(Version version1, Version version2) {
        return version1.compareTo(version2) > 0;
    }

    public static boolean isGreaterThanOrEqualTo(Version version1, Version version2) {
        return version1.compareTo(version2) >= 0;
    }

    public static boolean isLessThan(Version version1, Version version2) {
        return version1.compareTo(version2) < 0;
    }

    public static boolean isLessThanOrEqualTo(Version version1, Version version2) {
        return version1.compareTo(version2) <= 0;
    }
}
