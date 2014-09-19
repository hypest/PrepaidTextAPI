
package org.hypest.prepaidtextapi.filters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hypest.prepaidtextapi.R;
import org.hypest.prepaidtextapi.RestSmsList;
import org.hypest.prepaidtextapi.ReturnResults;
import org.hypest.prepaidtextapi.PrepaidTextAPIPrefsFile;
import org.hypest.prepaidtextapi.model.MySMS;
import org.hypest.prepaidtextapi.model.SMSDBAdapter;
import org.hypest.prepaidtextapi.model.MySMS.Recipient;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

public class Rest extends NetworkOperatorDatum {
    private static int VISIBLE_COUNT = 4;
    private List<MySMS> iDisplayedUnviewed;

    private static NetworkOperatorDatum __Singleton = null;

    protected NetworkOperatorDatum getInstance() {
        return __Singleton;
    }

    static public NetworkOperatorDatum Instantiate(Context aContext, PrepaidTextAPIPrefsFile aPrefs) {
        if (__Singleton == null) {
            __Singleton = new Rest(aContext, aPrefs);
        }
        return __Singleton;
    }

    protected Rest(
            Context aContext,
            PrepaidTextAPIPrefsFile aPrefs)
    {
        super(
                aContext,
                aPrefs,
                "Rest",
                R.layout.rest,
                0,
                0,
                Recipient.RNONE,
                aContext.getString(R.string.various),
                aContext.getString(R.string.various_info),
                NoMutualSMSClasses);

        iDisplayedUnviewed = new ArrayList<MySMS>();
    }

    public void updateUI() {
        updateRefreshIcon();

        SMSDBAdapter smsdb = new SMSDBAdapter(iContext);
        smsdb.open();
        boolean haveUnreportedMothership = smsdb.haveUnmarkedUnreportedSMSs();
        int countUnread = smsdb.countUnreadRest();
        int countAll = smsdb.countAllRest();
        List<MySMS> smss = smsdb.fetchRest(VISIBLE_COUNT);
        smsdb.close();

        View report = MainView().findViewById(R.id.Button_Report);
        report.setVisibility(haveUnreportedMothership ? View.VISIBLE : View.GONE);

        String balance = "" + countUnread;

        TextView tv = (TextView) MainView().findViewById(R.id.TextView_balance);
        tv.setText(Html.fromHtml(balance));

        View va = MainView().findViewById(R.id.Button_ViewAllRest);
        boolean more = countAll > VISIBLE_COUNT;
        va.setVisibility(more ? View.VISIBLE : View.GONE);
        if (more) {
            va.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    RestSmsList.viewRest(getActivity());
                    hideExtra();
                }
            });
        }

        LinearLayout ll = (LinearLayout) MainView().findViewById(R.id.extra_body);
        ll.removeAllViews();
        LayoutInflater li = LayoutInflater.from(iContext);

        iDisplayedUnviewed.clear();

        int k = 0;
        for (MySMS sms : smss) {
            k++;
            View v = li.inflate(R.layout.rest_sms, null);

            TextView rest_sms = (TextView) v.findViewById(R.id.single_rest_sms);
            rest_sms.setText(sms.getBodyText());

            TextView rest_sms_info = (TextView) v.findViewById(R.id.single_rest_sms_info);
            rest_sms_info.setText((new Date(sms.getTimestamp())).toLocaleString());

            View indicator = v.findViewById(R.id.indicator);
            if (sms.getViewed()) {
                indicator.setBackgroundResource(R.color.sms_viewed);
            } else {
                indicator.setBackgroundResource(R.color.sms_unviewed);
            }

            if (sms.getViewed() == false) {
                iDisplayedUnviewed.add(sms);
            }

            ll.addView(v);
            if (k > VISIBLE_COUNT) {
                break;
            }
        }

        storeExtraBody(" ");
        iPrefs.commit();

        updateExtraBody();
    }

    @Override
    protected void onHideExtra() {
        updateUI();
    }

    @Override
    protected void onShowExtra() {
        if (iDisplayedUnviewed.size() > 0) {
            SMSDBAdapter smsdb = new SMSDBAdapter(iContext);
            smsdb.open();
            smsdb.setViewed(iDisplayedUnviewed, true);
            smsdb.close();
        }

        MainView().post(new Runnable() {
            @Override
            public void run() {
                View mv = __iMainView;
                iScrollView.scrollTo(0, mv.getTop() - 10);
            }
        });
    }

    void animStart() {
    }

    public void sendSMS() {
    }

    public ReturnResults parse(MySMS sms) {
        ReturnResults ret = new ReturnResults();

        updateOwnUIAsync();

        ret.updated = true;

        return ret;
    }

    public void widgetUpdate(RemoteViews aWidgetView) {
    }

    public static void refresh() {
        if (__Singleton == null) {
            return;
        }

        __Singleton.updateOwnUIAsync();
    }
}
