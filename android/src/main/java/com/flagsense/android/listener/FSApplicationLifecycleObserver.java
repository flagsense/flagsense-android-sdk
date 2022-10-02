package com.flagsense.android.listener;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.flagsense.android.services.FlagsenseService;

public class FSApplicationLifecycleObserver implements DefaultLifecycleObserver {

    private final FlagsenseService flagsenseService;
    private boolean foreground = false;
    private boolean paused = true;

    public FSApplicationLifecycleObserver(final FlagsenseService flagsenseService) {
        this.flagsenseService = flagsenseService;
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;

        if (wasBackground && flagsenseService != null)
            flagsenseService.onBecameForeground();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        paused = true;

        if (foreground) {
            foreground = false;
            if (flagsenseService != null)
                flagsenseService.onBecameBackground();
        }
    }
}
