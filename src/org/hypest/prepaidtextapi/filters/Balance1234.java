
package org.hypest.prepaidtextapi.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hypest.prepaidtextapi.R;
import org.hypest.prepaidtextapi.ReturnResults;
import org.hypest.prepaidtextapi.PrepaidTextAPIPrefsFile;
import org.hypest.prepaidtextapi.model.MySMS;
import org.hypest.prepaidtextapi.model.MySMS.Recipient;
import org.hypest.prepaidtextapi.utils.DefaultEnabled;

import android.content.Context;

@DefaultEnabled()
public class Balance1234 extends SimpleBalance {
    private static final String pat_balance_1234 = "YOUR BALANCE IS .(\\d+?\\.\\d+?)\\.(.*?)$";

    private static NetworkOperatorDatum __Singleton = null;

    protected NetworkOperatorDatum getInstance() {
        return __Singleton;
    }

    static public NetworkOperatorDatum Instantiate(Context aContext, PrepaidTextAPIPrefsFile aPrefs) {
        if (__Singleton != null) {
            __Singleton.iContext = aContext;
            return __Singleton;
        }
        if (__Singleton == null) {
            __Singleton = new Balance1234(aContext, aPrefs);
        }
        return __Singleton;
    }

    private Balance1234(Context aContext, PrepaidTextAPIPrefsFile aPrefs) {
        super(
                aContext,
                aPrefs,
                "balance_1234",
                R.id.widget_part_balance_1234,
                R.id.widget_TextView_balance_1234,
                R.id.widget_ImageView_balance_1234_refresh,
                "Balance in Euro",
                "Your account balance",
                "€",
                Recipient.R1234,
                "bal",
                NetworkOperatorDatum.NoMutualSMSClasses);
    }

    public ReturnResults parse(MySMS sms) {
        ReturnResults ret = new ReturnResults();
        String body = sms.getBodyText();
        long smsMillis = sms.getTimestamp();

        Pattern p = Pattern.compile(pat_balance_1234);
        Matcher m = p.matcher(body);
        String balance = "unknown";

        if (m.find()) {
            ret.shouldCancelBroadcast = true;

            balance = m.group(1);

            storeExtraBody((m.groupCount() > 1) ? m.group(2) : HIDEIT);

            iPrefs.putString(iPrefBalance, balance);
            markFinished(smsMillis);

            updateOwnUIAsync();
            ret.updated = true;
        }

        return ret;
    }
}
