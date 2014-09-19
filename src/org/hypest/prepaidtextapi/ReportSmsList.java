
package org.hypest.prepaidtextapi;

import java.util.ArrayList;
import java.util.List;

import org.hypest.prepaidtextapi.model.MySMS;
import org.hypest.prepaidtextapi.model.SMSDBAdapter;
import org.hypest.prepaidtextapi.services.ParserService;
import org.hypest.prepaidtextapi.services.ReportService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class ReportSmsList extends Activity implements OnClickListener, OnCheckedChangeListener {

    private ListView iLv;
    private ReportSmsListAdapter iLa;
    private SMSDBAdapter iSMSDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sms_list_report);

        iLv = (ListView) findViewById(R.id.smsList);

        iSMSDB = new SMSDBAdapter(this);
        iSMSDB.open();
        List<MySMS> smss = iSMSDB.fetchUnmarkedUnreportedSMSs();
        iSMSDB.close();

        iLa = new ReportSmsListAdapter(this, smss, this, this);
        iLv.setAdapter(iLa);
    }

    protected void onResume() {
        super.onResume();

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
            SMSDBAdapter smsdb = new SMSDBAdapter(ReportSmsList.this);
            smsdb.open();
            List<MySMS> smss = smsdb.fetchUnmarkedUnreportedSMSs();
            smsdb.close();
            iLa.setData(smss);
            iLa.refresh();
            iLv.setSelection(0);
            Toast.makeText(ReportSmsList.this, getString(R.string.new_message_received),
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        MySMS sms = (MySMS) buttonView.getTag();

        List<MySMS> l = new ArrayList<MySMS>();
        l.add(sms);

        iSMSDB.open();
        iSMSDB.setUserDontReport(l, !isChecked);
        iSMSDB.close();
    }

    @Override
    public void onClick(View v) {
        CheckBox cb = (CheckBox) v.findViewById(R.id.checkBox);
        cb.setChecked(!cb.isChecked());
    }

    public void reportClick(View v) {
        List<MySMS> l = new ArrayList<MySMS>();

        int count = iLv.getChildCount();
        for (int k = 0; k < count; k++) {
            View vc = iLv.getChildAt(k);
            CheckBox cb = (CheckBox) vc.findViewById(R.id.checkBox);
            if (cb != null) {
                MySMS sms = (MySMS) cb.getTag();
                l.add(sms);
            }
        }

        if (l.size() > 0) {
            iSMSDB.open();
            iSMSDB.setMarkForReport(l, true);
            iSMSDB.close();

            ReportService.scheduleReport(this);
        }

        finish();
    }

    public static void viewReport(Context context) {
        Intent i = new Intent(context, ReportSmsList.class);
        context.startActivity(i);
    }
}
