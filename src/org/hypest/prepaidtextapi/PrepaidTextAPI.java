
package org.hypest.prepaidtextapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.hypest.prepaidtextapi.filters.NetworkOperatorDatum;
import org.hypest.prepaidtextapi.filters.NetworkOperatorDatum.NetworkOperator;
import org.hypest.prepaidtextapi.receivers.RestAlarm;
import org.hypest.prepaidtextapi.receivers.PrepaidTextAPIWidget;
import org.hypest.prepaidtextapi.services.ReportService;
import org.hypest.prepaidtextapi.utils.Misc;
import org.hypest.prepaidtextapi.utils.Misc.NOTIFICATION;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PrepaidTextAPI extends Activity implements Animation.AnimationListener {
    public static final String PREFS_INFOCUS = "infocus";
    public static final String PREFS_IN_CALL = "in_call";
    public static final String PREFS_RINGING = "ringing";
    public static final String PREFS_NOTIFICATION = "notification";
    public static final String PREFS_SENDING = "sending";
    public static final String PREFS_ALLPLUGINSSENT = "allpluginssent";
    public static final String PREFS_FORCE_SMSC = "force_smsc";
    public static final String PREFS_ON_CALL_END = "on_call_end";
    public static final String PREFS_DO_REPORT = "do_report";
    public static final String PREFS_REPORT_WIFI_ONLY = "report_wifi_only";

    public static Activity _activity = null;

    private static int _SentCount = 0;
    private static final String SMS_SENT_UID = "SMS_SENT_UID";
    private static final String ACTION_SMS_SENT = "ACTION_SMS_SENT";
    private static final String ACTION_SMS_DELIVERED = "ACTION_SMS_DELIVERED";
    public static final String ACTION_REST_ALARM = "ACTION_REST_ALARM";

    private enum RESULT_CODES {
        CODE_PREFERENCES,
    }

    PrepaidTextAPIPrefsFile prefs;

    public static final String HYPEST_UPDATE_OWNUI_INTENT = "org.hypest.prepaidtextapi.UPDATE_OWNUI";
    IntentFilter updateOwnUIIntentFilter;

    Animation fadein_balance_end;
    Animation fadeout_balance_end;
    Animation fadein_bonus_end;
    Animation fadeout_bonus_end;
    Animation fadein_MBs_end;
    Animation fadeout_MBs_end;
    Animation fadein_balance_active;
    Animation fadeout_balance_active;
    Animation fadein_bonus_active;
    Animation fadeout_bonus_active;
    Animation fadein_MBs_active;
    Animation fadeout_MBs_active;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _activity = this;

        prefs = new PrepaidTextAPIPrefsFile(this);

        setWholeView();

        updateOwnUIIntentFilter = new IntentFilter();
        updateOwnUIIntentFilter.addAction(HYPEST_UPDATE_OWNUI_INTENT);

        fadein_balance_end = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadein_bonus_end = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadein_MBs_end = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadein_balance_active = AnimationUtils.loadAnimation(this, R.anim.rotatefadein);
        fadein_bonus_active = AnimationUtils.loadAnimation(this, R.anim.rotatefadein);
        fadein_MBs_active = AnimationUtils.loadAnimation(this, R.anim.rotatefadein);
        fadeout_balance_end = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        fadeout_bonus_end = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        fadeout_MBs_end = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        fadeout_balance_active = AnimationUtils.loadAnimation(this, R.anim.rotatefadeout);
        fadeout_bonus_active = AnimationUtils.loadAnimation(this, R.anim.rotatefadeout);
        fadeout_MBs_active = AnimationUtils.loadAnimation(this, R.anim.rotatefadeout);

        // there seems to be a problem specifying the interpolator in the xml,
        // so setting it manually...
        fadein_balance_active.setInterpolator(getApplicationContext(), R.anim.linearinterpol);
        fadein_bonus_active.setInterpolator(getApplicationContext(), R.anim.linearinterpol);
        fadein_MBs_active.setInterpolator(getApplicationContext(), R.anim.linearinterpol);

        fadein_balance_end.setAnimationListener(this);
        fadeout_balance_end.setAnimationListener(this);
        fadein_bonus_end.setAnimationListener(this);
        fadeout_bonus_end.setAnimationListener(this);
        fadein_MBs_end.setAnimationListener(this);
        fadeout_MBs_end.setAnimationListener(this);
        fadein_balance_active.setAnimationListener(this);
        fadeout_balance_active.setAnimationListener(this);
        fadein_bonus_active.setAnimationListener(this);
        fadeout_bonus_active.setAnimationListener(this);
        fadein_MBs_active.setAnimationListener(this);
        fadeout_MBs_active.setAnimationListener(this);

        if (inAutoRefresh()) {
            if (prefs.getBoolean("launch_update", false)) {
                Button sendButton = (Button) findViewById(R.id.ButtonSend);
                sendButton.setEnabled(false);
                sendButton.setText(getString(R.string.auto_update_in_progress));

                onRefreshClick();
            }
        }
    }

    BroadcastReceiver smsSendingListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    // Toast.makeText(getBaseContext(), "SMS sent",
                    // Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(getBaseContext(), "SMS sending: Generic failure",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(getBaseContext(), "SMS sending: No service", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(getBaseContext(), "SMS sending: Null PDU", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(getBaseContext(), "SMS sending: Radio off", Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    };

    BroadcastReceiver smsDeliveryListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    // Toast.makeText(getBaseContext(), "SMS delivered",
                    // Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(getBaseContext(), "SMS not delivered!", Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    };

    private void registerSMSListeners() {
        registerReceiver(smsSendingListener, new IntentFilter(ACTION_SMS_SENT));
        registerReceiver(smsDeliveryListener, new IntentFilter(ACTION_SMS_DELIVERED));
    }

    private void unregisterSMSListeners() {
        unregisterReceiver(smsSendingListener);
        unregisterReceiver(smsDeliveryListener);
    }

    public boolean inAutoRefresh() {
        Intent ca = this.getIntent();
        if (ca == null) {
            return false;
        }

        String action = ca.getAction();
        if (action == null) {
            return false;
        }

        if (prefs == null) {
            return false;
        }

        int flags = ca.getFlags();
        if (((flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0)
                && (action.contains("ACTION_REFRESH"))
                && (prefs.getBoolean("launch_update", false))) {
            return true;
        } else {
            return false;
        }
    }

    private void setWholeView() {
        LayoutInflater li = LayoutInflater.from(this);

        View whole = li.inflate(R.layout.main, null);
        ScrollView sv = (ScrollView) whole.findViewById(R.id.ScrollView01);
        LinearLayout lin = (LinearLayout) (whole.findViewById(R.id.LinearLayout_body));

        Vector<NetworkOperatorDatum> nets = NetworkOperatorDatum.getEnabledOnly(this, prefs);

        int count = 0;
        for (NetworkOperatorDatum net : nets) {
            View child = net.MainView();
            ViewGroup vg = (ViewGroup) (child.getParent());
            if (vg != null)
                vg.removeView(child);

            lin.addView(child, count);
            count += 1;// 2;

            net.setScrollView(sv);
        }

        Button sendButton = (Button) whole.findViewById(R.id.ButtonSend);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRefreshClick();
            }
        });

        View reportButton = whole.findViewById(R.id.Button_Report);
        reportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportSmsList.viewReport(PrepaidTextAPI.this);
            }
        });

        setContentView(whole);
        sendButton.requestFocus();
    }

    private void onRefreshClick() {
        NetworkOperator net = NetworkOperatorDatum.getNetworkOperator(this);

        switch (net) {
            case Cosmote:
                doForceRefresh();
                break;
            case Offline:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(
                        getString(R.string.network_offline_info))
                        .setTitle(getString(R.string.network_offline))
                        .setCancelable(true)
                        .setNegativeButton(getString(android.R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            default:
                builder = new AlertDialog.Builder(this);
                builder.setMessage(
                        getString(R.string.network_foreign))
                        .setTitle(getString(R.string.warning))
                        .setIcon(R.drawable.ic_bullet_key_permission)
                        .setCancelable(true)
                        .setNegativeButton(getString(android.R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        dialog.cancel();
                                    }
                                });
                builder.setPositiveButton(getString(R.string.button_send),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int id) {
                                doForceRefresh();
                            }
                        });
                alert = builder.create();
                alert.show();
                break;
        }
    }

    private void doForceRefresh() {
        doRefresh(true);
    }

    private void doRefresh(Boolean forceRefresh) {
        Button sendButton = (Button) findViewById(R.id.ButtonSend);
        sendButton.setEnabled(false);

        prefs.putBoolean(PREFS_SENDING, true);
        prefs.putBoolean(PREFS_ALLPLUGINSSENT, false);
        prefs.commit();

        new Thread(sendSMSsRunable).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder;
        AlertDialog alert;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.about:
                builder = new AlertDialog.Builder(this);

                try {
                    builder.setTitle(getString(R.string.app_name) + " v"
                            + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                builder.setMessage(getString(R.string.about))
                        .setCancelable(true)
                        .setPositiveButton("http://hypest.org",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        Intent web = new Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("http://hypest.org/PrepaidTextAPI"));
                                        startActivity(web);
                                    }
                                })
                        .setNegativeButton(getString(R.string.button_back),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        dialog.cancel();
                                    }
                                });

                alert = builder.create();
                alert.show();
                return true;
            case R.id.preferences:
                Intent settingsActivity = new Intent().setClass(this, PrepaidTextAPI_Prefs.class);
                startActivityForResult(settingsActivity, RESULT_CODES.CODE_PREFERENCES.ordinal());
                return true;
            case R.id.renewal:
                NetworkOperator net = NetworkOperatorDatum.getNetworkOperator(this);

                switch (net) {
                    case Cosmote:
                        renewal();
                        break;
                    case Offline:
                        builder = new AlertDialog.Builder(this);
                        builder.setMessage(
                                getString(R.string.network_offline_info))
                                .setTitle(getString(R.string.network_offline))
                                .setCancelable(true)
                                .setNegativeButton(getString(R.string.button_back),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                    int id) {
                                                dialog.cancel();
                                            }
                                        });
                        alert = builder.create();
                        alert.show();
                        break;
                    default:
                        builder = new AlertDialog.Builder(this);
                        builder.setMessage(
                                getString(R.string.network_foreign))
                                .setTitle(getString(R.string.warning))
                                .setIcon(R.drawable.ic_bullet_key_permission)
                                .setCancelable(true)
                                .setPositiveButton(getString(R.string.button_send),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                renewal();
                                            }
                                        })
                                .setNegativeButton(getString(android.R.string.cancel),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        alert = builder.create();
                        alert.show();
                        break;
                }
                return true;
            case R.id.forceupdate:
                onRefreshClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void renewal() {
        AlertDialog.Builder renewal_number = new AlertDialog.Builder(this);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        renewal_number.setView(input);

        renewal_number.setTitle(getString(R.string.renewal_title));
        renewal_number.setMessage(getString(R.string.renewal_info));
        renewal_number.setPositiveButton(getString(R.string.button_send),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        sendSMS("<put your network number for renewal here", value,
                                prefs.getBoolean(PrepaidTextAPI.PREFS_FORCE_SMSC, true));
                    }
                });
        renewal_number.setNegativeButton(getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        renewal_number.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (RESULT_CODES.values()[requestCode]) {
            case CODE_PREFERENCES:
                NetworkOperatorDatum.unregisterAllInstances();
                NetworkOperatorDatum.regather(this, prefs);
                NetworkOperatorDatum.registerAllInstances(this);

                setWholeView();
                updateUI();
                PrepaidTextAPIWidget.doUpdate(this);

                ReportService.initiate(this);

                break;
        }
    }

    @Override
    protected void onResume() {
        _activity = this;

        registerSMSListeners();

        registerReceiver(updateIntentReceiver, updateOwnUIIntentFilter);

        NetworkOperatorDatum.registerAllInstances(this);

        prefs.putBoolean(PrepaidTextAPI.PREFS_INFOCUS, true);
        prefs.commit();
        updateUI();

        Misc.notify_cancel(NOTIFICATION.UPDATE, this);

        PrepaidTextAPIWidget.doUpdate(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterSMSListeners();

        unregisterReceiver(updateIntentReceiver);
        prefs.putBoolean(PrepaidTextAPI.PREFS_INFOCUS, false);
        prefs.putBoolean(PrepaidTextAPI.PREFS_SENDING, false);
        prefs.commit();

        NetworkOperatorDatum.unregisterAllInstances();

        RestAlarm.CancelAlarm(this);
        RestAlarm.SetAlarm(this);

        _activity = null;

        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (NetworkOperatorDatum.closeAllExtras()) {
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void updateOwnUI() {
        Boolean sending = prefs.getBoolean(PREFS_SENDING, false);
        if (!sending) {
            Button sendButton = (Button) findViewById(R.id.ButtonSend);
            sendButton.setEnabled(true);
        } else {
            Button sendButton = (Button) findViewById(R.id.ButtonSend);
            sendButton.setEnabled(false);
        }
    }

    private void updateUI() {
        Vector<NetworkOperatorDatum> nets = NetworkOperatorDatum.getEnabledOnly(this, prefs);

        for (NetworkOperatorDatum net : nets) {
            net.updateUI();
        }

        updateOwnUI();
    }

    public static void updateOwnUIAsync(Context aContext) {
        Intent i = new Intent();
        i.setAction(HYPEST_UPDATE_OWNUI_INTENT);
        aContext.sendBroadcast(i);
    }

    private Runnable updateOwnUIRunnable = new Runnable() {
        @Override
        public void run() {
            updateOwnUI();
        }
    };

    private BroadcastReceiver updateIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(updateOwnUIRunnable);
        }
    };

    public void doRunNetworkRefresh() {
        NetworkOperatorDatum.runNetworkRefresh(this);
    }

    private Runnable sendSMSsRunable = new Runnable() {
        @Override
        public void run() {
            doRunNetworkRefresh();
        }
    };

    public static void sendSMS(String phoneNumber, String message, Boolean forceSMSC) {
        _SentCount++;

        PendingIntent sentPendingIntent = null;
        PendingIntent deliveredPendingIntent = null;
        if (PrepaidTextAPI._activity != null) {
            Intent sentIntent = new Intent();
            sentIntent.putExtra(SMS_SENT_UID, _SentCount);
            sentIntent.setAction(ACTION_SMS_SENT);
            Intent deliverIntent = new Intent();
            deliverIntent.putExtra(SMS_SENT_UID, _SentCount);
            deliverIntent.setAction(ACTION_SMS_DELIVERED);
            sentPendingIntent = PendingIntent.getBroadcast(PrepaidTextAPI._activity, 0, sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            deliveredPendingIntent = PendingIntent.getBroadcast(PrepaidTextAPI._activity, 0,
                    deliverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        ArrayList<PendingIntent> sentintents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredintents = new ArrayList<PendingIntent>();
        sentintents.add(sentPendingIntent);
        deliveredintents.add(deliveredPendingIntent);

        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> smsparts = sms.divideMessage(message);
        sms.sendMultipartTextMessage(phoneNumber, (forceSMSC ? "<SMS center number>" : null),
                smsparts, sentintents, deliveredintents);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    protected class EmailClientAdapter extends ArrayAdapter<ResolveInfo> {

        public List<ResolveInfo> items;

        public EmailClientAdapter(Context context, List<ResolveInfo> items) {
            super(context, R.layout.email_client_item, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.email_client_item, null);
            }
            final PackageManager pm = getPackageManager();
            ResolveInfo o = items.get(position);
            if (o != null) {
                TextView tt = (TextView) v.findViewById(R.id.label);
                ImageView icon = (ImageView) v.findViewById(R.id.icon);
                if (tt != null) {
                    tt.setText(pm.getApplicationLabel(o.activityInfo.applicationInfo));
                }
                if (icon != null) {
                    Drawable d = pm.getApplicationIcon(o.activityInfo.applicationInfo);
                    if (d != null) {
                        icon.setImageDrawable(d);
                    } else {
                        icon.setImageResource(R.drawable.empty);
                    }
                }
            }
            return v;
        }
    }
}
