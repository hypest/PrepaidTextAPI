
package org.hypest.prepaidtextapi.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.hypest.prepaidtextapi.R;
import org.hypest.prepaidtextapi.ReturnResults;
import org.hypest.prepaidtextapi.PrepaidTextAPIPrefsFile;
import org.hypest.prepaidtextapi.PrepaidTextAPI;
import org.hypest.prepaidtextapi.filters.NetworkOperatorDatum;
import org.hypest.prepaidtextapi.filters.Rest;
import org.hypest.prepaidtextapi.model.MySMS;
import org.hypest.prepaidtextapi.model.PduSMS;
import org.hypest.prepaidtextapi.model.SMSDBAdapter;
import org.hypest.prepaidtextapi.receivers.PrepaidTextAPIWidget;
import org.hypest.prepaidtextapi.utils.Misc;
import org.hypest.prepaidtextapi.utils.Misc.NOTIFICATION;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ParserService extends Service {
    public static String UPDATED_REST = "UPDATED_REST";

    public static void initiate(Context context) {
        Intent us = new Intent(context, ParserService.class);
        context.startService(us);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.v("my", "service create");
        super.onCreate();

        PrepaidTextAPIPrefsFile prefs = new PrepaidTextAPIPrefsFile(this);

        Vector<NetworkOperatorDatum> iNetworkOperatorData = NetworkOperatorDatum.getEnabledOnly(
                this, prefs);

        int updates = 0;

        SMSDBAdapter smsAdapter = new SMSDBAdapter(this);
        smsAdapter.open();

        List<List<PduSMS>> lists = new ArrayList<List<PduSMS>>();

        {
            // gather the concats
            Hashtable<Long, List<PduSMS>> preconcat = new Hashtable<Long, List<PduSMS>>();
            List<PduSMS> pdusmss = smsAdapter.fetchNewPDUs();
            for (PduSMS pdusms : pdusmss) {
                if (pdusms.getConcatLength() == 1) {
                    List<PduSMS> l = new ArrayList<PduSMS>();
                    l.add(pdusms);
                    lists.add(l);
                } else {
                    long unq = pdusms.getUniqueConcatRef();
                    if (preconcat.containsKey(unq) == false)
                        preconcat.put(unq, new ArrayList<PduSMS>());

                    List<PduSMS> l = preconcat.get(unq);
                    l.add(pdusms);
                }
            }

            for (List<PduSMS> l : preconcat.values()) {
                lists.add(l);
            }
        }

        // create a MySMS for each concat group and the single messages
        for (List<PduSMS> l : lists) {
            PduSMS last = l.get(l.size() - 1);
            if (last.getConcatLength() != l.size()) {
                continue; // ignore incomplete concats
            }

            String bodyText = "";
            for (PduSMS pdu : l) {
                bodyText += pdu.getSmsMessage().getMessageBody();
            }

            MySMS mysms = new MySMS(last.getTimestamp(), bodyText, last.getSmsMessage()
                    .getOriginatingAddress());
            long newid = smsAdapter.insert(mysms);

            if (smsAdapter.setArchiveId(l, newid) == false) {
                Log.i("my", "failed to update the pduSMSs with the new archive id!");
            }
        }

        boolean haveRest = false;

        List<MySMS> smss = smsAdapter.fetchNewSMSs();
        for (MySMS sms : smss) {
            Boolean atleastonepending = false;

            for (NetworkOperatorDatum iNetworkOperatorDatum : iNetworkOperatorData) {
                Boolean wasongoing = iNetworkOperatorDatum.isOnGoing();
                atleastonepending |= wasongoing;
                ReturnResults ret = iNetworkOperatorDatum.parse(sms);
                updates += (ret.updated) ? 1 : 0;
                if (ret.updated) {
                    if (!(iNetworkOperatorDatum instanceof Rest)) {
                        sms.setParseClass(iNetworkOperatorDatum.getClass().getSimpleName());
                    } else {
                        haveRest = true;
                    }
                    break;
                }
            }

            sms.setProcessed(true);
            smsAdapter.update(sms);

            if (updates > 0) {
                PrepaidTextAPIWidget.doUpdate(this);

                if (atleastonepending && prefs.getBoolean(PrepaidTextAPI.PREFS_NOTIFICATION, false)) {
                    Boolean ongoing = false;
                    for (NetworkOperatorDatum iNetworkOperatorDatum : iNetworkOperatorData)
                        ongoing |= iNetworkOperatorDatum.isOnGoing();

                    if (!ongoing) {
                        Misc.notify(
                                NOTIFICATION.UPDATE,
                                this,
                                getString(R.string.app_name),
                                getString(R.string.refresh_finished),
                                getString(R.string.refresh_finished),
                                false);
                    }
                }
            }
        }

        smsAdapter.close();

        if (haveRest) {
            Intent i = new Intent();
            i.setPackage(getPackageName());
            i.setAction(UPDATED_REST);
            sendBroadcast(i);
        }

        stopSelf();
    }
}
