
package org.hypest.prepaidtextapi.receivers;

import org.hypest.prepaidtextapi.R;
import org.hypest.prepaidtextapi.PrepaidTextAPIPrefsFile;
import org.hypest.prepaidtextapi.PrepaidTextAPI;
import org.hypest.prepaidtextapi.services.ReportService;
import org.hypest.prepaidtextapi.utils.Misc;
import org.hypest.prepaidtextapi.utils.Misc.CONNECTION_LEVEL;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectivityChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        CONNECTION_LEVEL cl = Misc.connectionLevel(context);
        Log.i(context.getString(R.string.app_name),
                "Network state changed. New state: " + cl.name());
        if (cl == CONNECTION_LEVEL.OFFLINE) {
            return;
        }

        PrepaidTextAPIPrefsFile prefs = new PrepaidTextAPIPrefsFile(context);
        Boolean reportOnConnect = prefs.getBoolean(PrepaidTextAPI.PREFS_DO_REPORT, false);

        if (!reportOnConnect) {
            return;
        }

        if (isReporterRunning(context)) {
            return;
        }

        ReportService.initiate(context);
    }

    private boolean isReporterRunning(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ReportService.class.getCanonicalName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
