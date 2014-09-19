
package org.hypest.prepaidtextapi.receivers;

import org.hypest.prepaidtextapi.R;
import org.hypest.prepaidtextapi.RestSmsList;
import org.hypest.prepaidtextapi.PrepaidTextAPI;
import org.hypest.prepaidtextapi.model.SMSDBAdapter;
import org.hypest.prepaidtextapi.utils.Misc;
import org.hypest.prepaidtextapi.utils.Misc.NOTIFICATION;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestAlarm extends BroadcastReceiver {

    // 30min period
    final static long PERIOD = 1000 * 60 * 30;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(context.getString(R.string.app_name), "Alarm received!");
        SMSDBAdapter smsdb = new SMSDBAdapter(context);
        smsdb.open();
        int count = smsdb.countUnreadRest();
        smsdb.close();

        if (count > 0 && PrepaidTextAPI._activity == null) {
            Log.i(context.getString(R.string.app_name), "about to notify for unread SMSs...");
            Misc.notify(
                    NOTIFICATION.ALARM,
                    context,
                    context.getString(R.string.unread_sms),
                    context.getString(R.string.unread_sms_info),
                    context.getString(R.string.unread_sms),
                    false,
                    RestSmsList.class,
                    PrepaidTextAPI.ACTION_REST_ALARM,
                    false);
        }
    }

    public static void SetAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, RestAlarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + PERIOD, PERIOD, pi);
        Log.i(context.getString(R.string.app_name), "Alarm set...");
    }

    public static void CancelAlarm(Context context) {
        Intent intent = new Intent(context, RestAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
