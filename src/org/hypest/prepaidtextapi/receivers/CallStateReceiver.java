
package org.hypest.prepaidtextapi.receivers;

import org.hypest.prepaidtextapi.PrepaidTextAPIPrefsFile;
import org.hypest.prepaidtextapi.PrepaidTextAPI;
import org.hypest.prepaidtextapi.services.UpdateService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class CallStateReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        PrepaidTextAPIPrefsFile prefs = new PrepaidTextAPIPrefsFile(context);
        Boolean onend = prefs.getBoolean(PrepaidTextAPI.PREFS_ON_CALL_END, true);
        if (!onend) {
            return;
        }

        TelephonyManager telephonyManager = ((TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE));
        int cs = telephonyManager.getCallState();
        switch (cs) {
            case TelephonyManager.CALL_STATE_IDLE:
                Boolean incall = prefs.getBoolean(PrepaidTextAPI.PREFS_IN_CALL, false);
                Boolean ringing = prefs.getBoolean(PrepaidTextAPI.PREFS_RINGING, false);
                if (incall && !ringing) {
                    UpdateService.initiate(context);
                }
                prefs.putBoolean(PrepaidTextAPI.PREFS_IN_CALL, false);
                prefs.putBoolean(PrepaidTextAPI.PREFS_RINGING, false);
                prefs.commit();
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                prefs.putBoolean(PrepaidTextAPI.PREFS_IN_CALL, true);
                prefs.commit();
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                prefs.putBoolean(PrepaidTextAPI.PREFS_RINGING, true);
                prefs.commit();
                break;
        }
    }
}
