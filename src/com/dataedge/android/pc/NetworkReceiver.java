package com.dataedge.android.pc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {
    String TAG = NetworkReceiver.class.getName();

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.i(TAG, "onReceive(Context ctx, Intent intent)");
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info.isAvailable()) {
                // queue transmitted ones
                Intent msgIntent = new Intent(ctx,
                        QueuedTransmissionService.class);
                ctx.startService(msgIntent);
            }
        }
    }
}
