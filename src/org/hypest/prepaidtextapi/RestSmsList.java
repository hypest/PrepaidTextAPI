
package org.hypest.prepaidtextapi;

import java.util.ArrayList;
import java.util.List;

import org.hypest.prepaidtextapi.model.MySMS;
import org.hypest.prepaidtextapi.model.SMSDBAdapter;
import org.hypest.prepaidtextapi.services.ParserService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class RestSmsList extends Activity {

    private ListView lv;
    private RestSmsListAdapter la;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sms_list);
    }

    protected void onResume() {
        super.onResume();

        lv = (ListView) findViewById(R.id.smsList);

        SMSDBAdapter smsdb = new SMSDBAdapter(this);
        smsdb.open();
        boolean haveUnreportedMothership = smsdb.haveUnmarkedUnreportedSMSs();
        List<MySMS> smss = smsdb.fetchAllRest();
        smsdb.close();

        View report = findViewById(R.id.Button_Report);
        report.setVisibility(haveUnreportedMothership ? View.VISIBLE : View.GONE);

        la = new RestSmsListAdapter(this, smss);
        lv.setAdapter(la);

        List<MySMS> displayedUnviewed = new ArrayList<MySMS>();

        for (MySMS sms : smss) {
            if (sms.getViewed() == false) {
                displayedUnviewed.add(sms);
            }
        }

        if (displayedUnviewed.size() > 0) {
            smsdb.open();
            smsdb.setViewed(displayedUnviewed, true);
            smsdb.close();
        }

        IntentFilter inf = new IntentFilter(ParserService.UPDATED_REST);
        registerReceiver(restListener, inf);
    };

    protected void onPause() {
        super.onPause();

        unregisterReceiver(restListener);
    };

    BroadcastReceiver restListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SMSDBAdapter smsdb = new SMSDBAdapter(RestSmsList.this);
            smsdb.open();
            List<MySMS> smss = smsdb.fetchAllRest();
            smsdb.close();
            la.setData(smss);
            la.refresh();
            lv.setSelection(0);
            Toast.makeText(RestSmsList.this, getString(R.string.new_message_received),
                    Toast.LENGTH_SHORT).show();

            List<MySMS> displayedUnviewed = new ArrayList<MySMS>();
            for (MySMS sms : smss) {
                if (sms.getViewed() == false)
                    displayedUnviewed.add(sms);
            }

            smsdb.open();
            smsdb.setViewed(displayedUnviewed, true);
            smsdb.close();

        }
    };

    public void reportClick(View v) {
        ReportSmsList.viewReport(this);
    }

    public static void viewRest(Context context) {
        Intent i = new Intent(context, RestSmsList.class);
        context.startActivity(i);
    }
}
