package com.flagsense.android.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.flagsense.android.Flagsense;
import com.flagsense.android.util.Utility;

public class FSConnectivityReceiver extends BroadcastReceiver {

    public static final String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private boolean knownState = false;
    private boolean lastState = false;

    @Override
    public synchronized void onReceive(Context context, Intent intent) {
        if (!CONNECTIVITY_CHANGE.equals(intent.getAction())) {
            return;
        }

        boolean connectionStatus = Utility.isInternetConnected(context);
        if (knownState && lastState == connectionStatus) {
            return;
        }

        knownState = true;
        lastState = connectionStatus;
        Flagsense.onNetworkConnectivityChangeInstances(connectionStatus);
    }
}
