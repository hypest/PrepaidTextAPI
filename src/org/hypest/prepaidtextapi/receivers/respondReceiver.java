
package org.hypest.prepaidtextapi.receivers;

import org.hypest.prepaidtextapi.model.MySMS;
import org.hypest.prepaidtextapi.model.PduSMS;
import org.hypest.prepaidtextapi.model.SMSDBAdapter;
import org.hypest.prepaidtextapi.model.MySMS.Sender;
import org.hypest.prepaidtextapi.services.ParserService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class respondReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final long ts = System.currentTimeMillis();
        Bundle bundle = intent.getExtras();

        Object messages[] = (Object[]) bundle.get("pdus");

        if (messages.length == 0) {
            Log.i("PrepaidTextAPI", "hmm... no messages...");
            return;
        }

        boolean recognized = false;
        SMSDBAdapter smsAdapter = new SMSDBAdapter(context);
        smsAdapter.open();

        // TODO: multiple messages means trouble! How to abort only the
        //  recognized ones??
        for (Object smsobj : messages) {
            PduSMS pdusms = new PduSMS((byte[]) smsobj, ts);
            boolean consumeit = MySMS.ourSender(pdusms.getSmsMessage()) != Sender.UNSUPPORTED_SENDER;
            recognized |= consumeit;

            if (consumeit) {
                smsAdapter.insert(pdusms);
            }
        }

        smsAdapter.close();

        if (recognized) {
            abortBroadcast();
            ParserService.initiate(context);
        }

        return;
    }

    void checkCancelBroadcast(Boolean aShouldCancelBroadcast, int aResponseHidingOptionValue,
            Boolean aInfocus) {
        if (aShouldCancelBroadcast) {
            switch (aResponseHidingOptionValue) {
                case 1:
                    this.abortBroadcast();
                    break;
                case 2:
                    if (aInfocus)
                        this.abortBroadcast();
                    break;
                case 3:
                    // don't hide response...
                    break;
            }
        }
    }
}
