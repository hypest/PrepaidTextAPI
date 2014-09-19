
package org.hypest.prepaidtextapi.receivers;

import org.hypest.prepaidtextapi.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.i(context.getString(R.string.app_name), "Boot receiver signaled!");
            RestAlarm.SetAlarm(context);
        }
    }
}
