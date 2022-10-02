package com.flagsense.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utility {
    public static List<Map<String, String>> toKeyValueArray(Map<String, ?> map) {
        List<Map<String, String>> keyValueArray = new ArrayList<>();
        if (map == null)
            return keyValueArray;

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            Map<String, String> keyValueMap = new HashMap<>();
            keyValueMap.put("key", entry.getKey());
            keyValueMap.put("value", String.valueOf(entry.getValue()));
            keyValueArray.add(keyValueMap);
        }

        return keyValueArray;
    }

    public static boolean isHttpErrorRecoverable(int statusCode) {
        if (statusCode < 500 || statusCode >= 600) {
            switch (statusCode) {
                case 205:
                case 408:
                case 422:
                case 429:
                    return true;
                default:
                    return false;
            }
        }
        return true;
    }

    public static boolean isInternetConnected(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network net = cm.getActiveNetwork();
                if (net == null)
                    return false;

                NetworkCapabilities nwc = cm.getNetworkCapabilities(net);
                return nwc != null && (
                        nwc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                || nwc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                || nwc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                                || nwc.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
                                || nwc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                );
            } else {
                NetworkInfo active = cm.getActiveNetworkInfo();
                return active != null && active.isConnectedOrConnecting();
            }
        } catch (SecurityException ignored) {
            return true;
        }
    }

    public static boolean isEmptyList(List<?> list) {
        return list == null || list.isEmpty();
    }
}
