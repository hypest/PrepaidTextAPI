
package org.hypest.prepaidtextapi.filters;

import java.util.Date;
import java.util.List;

import org.hypest.prepaidtextapi.R;
import org.hypest.prepaidtextapi.PrepaidTextAPIPrefsFile;
import org.hypest.prepaidtextapi.PrepaidTextAPI;
import org.hypest.prepaidtextapi.model.MySMS.Recipient;

import android.content.Context;
import android.text.Html;
import android.widget.RemoteViews;
import android.widget.TextView;

public abstract class SimpleBalance extends NetworkOperatorDatum {
    protected String iPrefBalance = null;
    protected String iUnitString = null;
    protected String iSMSQuestion = null;
    protected int iWidgetBalanceId = -1;

    protected SimpleBalance(
            Context aContext,
            PrepaidTextAPIPrefsFile aPrefs,
            String aBaseStringName,
            int aWidgetLayoutId,
            int aWidgetBalanceId,
            int aWidgetRefreshIconId,
            String aFriendlyName,
            String aFriendlySummary,
            String aUnitString,
            Recipient aRecipient,
            String aSMSQuestion,
            @SuppressWarnings("rawtypes")
            List<Class> aMutualSMSClasses)
    {
        super(
                aContext,
                aPrefs,
                aBaseStringName,
                R.layout.simple_balance,
                aWidgetLayoutId,
                aWidgetRefreshIconId,
                aRecipient,
                aFriendlyName,
                aFriendlySummary,
                aMutualSMSClasses);

        iPrefBalance = iBaseStringName;
        iUnitString = aUnitString;
        iSMSQuestion = aSMSQuestion;
        iWidgetBalanceId = aWidgetBalanceId;
    }

    public void updateUI() {
        updateRefreshIcon();

        final String unknown = iContext.getString(R.string.unknown);
        String balance = iPrefs.getString(iPrefBalance, unknown);
        Long balance_lastupdate_millis = getLastUpdateMillis();

        String dys = iContext.getString(R.string.never);
        if (balance_lastupdate_millis > 0) {
            dys = (new Date(balance_lastupdate_millis)).toLocaleString();
        }

        TextView tv = (TextView) MainView().findViewById(R.id.TextView_balance);
        if (balance.startsWith(unknown) == false) {
            balance += "<small><small>" + iUnitString + "</small></small>";
        }
        tv.setText(Html.fromHtml(balance));
        tv = (TextView) MainView().findViewById(R.id.TextView_balance_lastupdate);
        tv.setText(iContext.getString(R.string.updated) + dys);

        updateExtraBody();
    }

    void animStart() {
        return;
    }

    public void sendSMS() {
        PrepaidTextAPI.sendSMS(iRecipient.number(), iSMSQuestion,
                iPrefs.getBoolean(PrepaidTextAPI.PREFS_FORCE_SMSC, true));
        NetworkOperatorDatum.Delay();
    }

    public void widgetUpdate(RemoteViews aWidgetView) {
        if (iWidgetBalanceId == 0) {
            return;
        }

        final String unknown = iContext.getString(R.string.unknown);
        String balance = iPrefs.getString(iPrefBalance, unknown);

        if (balance.startsWith(unknown) == false) {
            balance += "<small>" + iUnitString + "</small>";
        }
        aWidgetView.setTextViewText(iWidgetBalanceId, Html.fromHtml(balance));
    }
}
