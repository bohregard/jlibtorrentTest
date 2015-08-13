package com.bohregard.updater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Anthony on 8/13/2015.
 */
public class OnBoot extends BroadcastReceiver {

    private final String TAG = OnBoot.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "OnBoot Received for Matricom Updater");
    }
}
