
package org.hypest.prepaidtextapi.services;

import org.hypest.prepaidtextapi.filters.NetworkOperatorDatum;
import org.hypest.prepaidtextapi.filters.NetworkOperatorDatum.NetworkOperator;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdateService extends Service {
    public static void initiate(Context context) {
        Intent us = new Intent(context, UpdateService.class);
        context.startService(us);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("PrepaidTextAPI", "UpdateService...");
        super.onCreate();

        Context ctx = getBaseContext();
        NetworkOperator net = NetworkOperatorDatum.getNetworkOperator(this);
        if (net == org.hypest.prepaidtextapi.filters.NetworkOperatorDatum.NetworkOperator.Cosmote) {
            NetworkOperatorDatum.runNetworkRefreshCallRelated(ctx);
        }

        stopSelf();
    }
}
