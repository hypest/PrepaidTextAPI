
package org.hypest.prepaidtextapi.utils;

import org.hypest.prepaidtextapi.R;
import org.hypest.prepaidtextapi.PrepaidTextAPI;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Misc {

    public enum CONNECTION_LEVEL
    {
        OFFLINE,
        NO_BACKGROUND_DATA,
        CELLULAR,
        WIFI
    }

    public static CONNECTION_LEVEL connectionLevel(Context context)
    {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (!connManager.getBackgroundDataSetting())
            return CONNECTION_LEVEL.NO_BACKGROUND_DATA;

        NetworkInfo ni = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (ni.isConnected())
            return CONNECTION_LEVEL.WIFI;

        ni = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (ni.isConnected())
            return CONNECTION_LEVEL.CELLULAR;

        return CONNECTION_LEVEL.OFFLINE;
    }

    public static boolean isDataEnabled(Context context)
    {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        return connManager.getBackgroundDataSetting();
    }

    public enum NOTIFICATION
    {
        UPDATE,
        REPORT,
        ALARM
    }

    public static void notify(NOTIFICATION id, Context context, String title,
            String tickerText, String text, boolean onGoing) {
        notify(id, context, title, tickerText, text, onGoing, PrepaidTextAPI.class, null, true);
    }

    public static void notify(NOTIFICATION id, Context context, String title,
            String tickerText, String text, boolean onGoing, Class<?> intentClass, String action,
            boolean cancelPrevious) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        int icon = R.drawable.ic_menu_refresh_active;
        long when = System.currentTimeMillis();

        Notification no = new Notification(icon, tickerText, when);
        if (onGoing) {
            no.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        } else {
            no.flags |= Notification.FLAG_AUTO_CANCEL;
        }

        Intent notificationIntent = new Intent(context, intentClass);
        if (action != null) {
            notificationIntent.setAction(action);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        no.setLatestEventInfo(context, title, text, contentIntent);

        int idnum = id.ordinal();
        if (cancelPrevious) {
            nm.cancel(idnum);
        }
        nm.notify(idnum, no);
    }

    public static void notify_cancel(NOTIFICATION id, Context context)
    {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int idnum = id.ordinal();
        nm.cancel(idnum);
    }
}
