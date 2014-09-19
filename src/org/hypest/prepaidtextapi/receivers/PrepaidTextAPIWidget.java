
package org.hypest.prepaidtextapi.receivers;

import java.util.Vector;

import org.hypest.prepaidtextapi.R;
import org.hypest.prepaidtextapi.PrepaidTextAPIPrefsFile;
import org.hypest.prepaidtextapi.PrepaidTextAPI;
import org.hypest.prepaidtextapi.filters.NetworkOperatorDatum;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

public class PrepaidTextAPIWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        doUpdate(context);
    }

    public static void doUpdate(Context context) {
        PrepaidTextAPIPrefsFile prefs = new PrepaidTextAPIPrefsFile(context);

        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget);

        LayoutInflater li = LayoutInflater.from(context);
        ViewGroup vg = (ViewGroup) li.inflate(R.layout.widget, null);
        int cc = vg.getChildCount();
        for (int k = 0; k < cc; k++) {
            remoteView.setViewVisibility(vg.getChildAt(k).getId(), View.GONE);
        }

        int count = 0;
        Vector<NetworkOperatorDatum> iNetworkOperatorData = NetworkOperatorDatum.getEnabledOnly(
                context, prefs);
        for (NetworkOperatorDatum iNetworkOperatorDatum : iNetworkOperatorData) {
            int id = iNetworkOperatorDatum.getWidgetLayoutId();
            if (id != 0) {
                if (count < 5) {
                    remoteView.setViewVisibility(iNetworkOperatorDatum.getWidgetLayoutId(),
                            View.VISIBLE);
                }

                count++;
            }
        }

        ComponentName thisWidget = new ComponentName(context, PrepaidTextAPIWidget.class);
        // Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, PrepaidTextAPI.class);
        intent.setAction("org.hypest.prepaidtextapi.ACTION_REFRESH");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteView.setOnClickPendingIntent(R.id.widget_whole, pendingIntent);

        for (NetworkOperatorDatum iNetworkOperatorDatum : iNetworkOperatorData) {
            iNetworkOperatorDatum.widgetUpdate(remoteView);
            iNetworkOperatorDatum.updateWidgetRefreshIcon(remoteView);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(thisWidget, remoteView);
    }
}
