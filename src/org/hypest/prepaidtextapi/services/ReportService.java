
package org.hypest.prepaidtextapi.services;

import java.util.List;

import org.hypest.prepaidtextapi.R;
import org.hypest.prepaidtextapi.PrepaidTextAPIPrefsFile;
import org.hypest.prepaidtextapi.PrepaidTextAPI;
import org.hypest.prepaidtextapi.filters.Rest;
import org.hypest.prepaidtextapi.model.MySMS;
import org.hypest.prepaidtextapi.model.SMSDBAdapter;
import org.hypest.prepaidtextapi.utils.Mailer;
import org.hypest.prepaidtextapi.utils.Misc;
import org.hypest.prepaidtextapi.utils.Misc.CONNECTION_LEVEL;
import org.hypest.prepaidtextapi.utils.Misc.NOTIFICATION;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class ReportService extends IntentService {
    public static String app_name;
    private WakeLock iWakeLock;

    public static void scheduleReport(Context context) {
        PrepaidTextAPIPrefsFile prefs = new PrepaidTextAPIPrefsFile(context);
        prefs.putBoolean(PrepaidTextAPI.PREFS_DO_REPORT, true);
        prefs.commit();

        if (ReportService.checkNetworkAndWarnForReport(context)) {
            ReportService.initiate(context);
        }
    }

    public static void initiate(Context context) {
        Intent us = new Intent(context, ReportService.class);
        context.startService(us);
    }

    private static boolean check(Context context) {
        PrepaidTextAPIPrefsFile prefs = new PrepaidTextAPIPrefsFile(context);
        boolean doReport = prefs.getBoolean(PrepaidTextAPI.PREFS_DO_REPORT, false);
        if (!doReport) {
            return false;
        }

        CONNECTION_LEVEL cl = Misc.connectionLevel(context);

        boolean ableToReport = true;

        switch (cl) {
            case OFFLINE:
                ableToReport = false;
                break;

            case NO_BACKGROUND_DATA:
                ableToReport = false;
                break;

            case CELLULAR:
                boolean wifionly = prefs.getBoolean(PrepaidTextAPI.PREFS_REPORT_WIFI_ONLY, false);

                if (wifionly)
                    ableToReport = false;
                break;

            case WIFI:
                // do the report
                break;
        }

        return ableToReport;
    }

    private static boolean checkNetworkAndWarnForReport(Context context) {
        CONNECTION_LEVEL cl = Misc.connectionLevel(context);

        switch (cl) {
            case OFFLINE:
                Misc.notify(
                        NOTIFICATION.REPORT,
                        context,
                        context.getString(R.string.sending_postponed),
                        context.getString(R.string.sending_postponed_offline_info),
                        context.getString(R.string.sending_postponed_offline),
                        false);

                return false;

            case NO_BACKGROUND_DATA:
                Misc.notify(
                        NOTIFICATION.REPORT,
                        context,
                        context.getString(R.string.sending_postponed),
                        context.getString(R.string.sending_postponed_no_background_data_info),
                        context.getString(R.string.sending_postponed_no_background_data),
                        false);

                return false;

            case CELLULAR:
                PrepaidTextAPIPrefsFile prefs = new PrepaidTextAPIPrefsFile(context);
                boolean wifionly = prefs.getBoolean(PrepaidTextAPI.PREFS_REPORT_WIFI_ONLY, false);

                if (wifionly) {
                    Misc.notify(
                            NOTIFICATION.REPORT,
                            context,
                            context.getString(R.string.sending_postponed),
                            context.getString(R.string.sending_postponed_cellular_info),
                            context.getString(R.string.sending_postponed_cellular),
                            false);

                    return false;
                }
                else
                    return true;

            case WIFI:
                return true;
        }

        return true;
    }

    public ReportService() {
        super("ReportService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onCreate();

        app_name = getString(R.string.app_name);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        iWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, app_name + "_SMSReportService");
        iWakeLock.acquire();

        if (check(this))
            doReport();

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        iWakeLock.release();
    }

    protected void doReport() {
        Log.i(this.getString(R.string.app_name), "Entered report");

        SMSDBAdapter smsdb = new SMSDBAdapter(this);
        smsdb.open();
        List<MySMS> smss = smsdb.fetchMarkedUnreportedSMSs();

        if (smss.size() > 0) {
            String version = "unknown";
            try {
                PackageInfo pkgi = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pkgi.versionName + "/" + pkgi.versionCode;
            } catch (NameNotFoundException e1) {
            }

            String allsms = "App version: " + version + "<br/><br/>";

            for (MySMS sms : smss) {
                if (sms.getUserDontReport())
                    continue;

                String txt = sms.getReport(smsdb);

                allsms += txt + "<br/><br/>";
            }

            String addr = app_name + "@hypest.org";
            Mailer mailer = new Mailer(addr, app_name);
            mailer.setFrom(addr);
            mailer.setTo(new String[] {
                    addr
            });
            mailer.setSubject(app_name + " SMS Report");
            mailer.setBody(allsms);

            Misc.notify(
                    NOTIFICATION.REPORT,
                    this,
                    getString(R.string.sms_reporting),
                    getString(R.string.sms_reporting_background_info),
                    getString(R.string.sms_reporting_background),
                    true);

            PrepaidTextAPIPrefsFile prefs = new PrepaidTextAPIPrefsFile(this);
            prefs.putBoolean(PrepaidTextAPI.PREFS_DO_REPORT, false);
            prefs.commit();

            boolean sentok = false;
            try {
                sentok = mailer.send();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (sentok) {
                smsdb.setReportedMothership(smss, true);

                Rest.refresh();

                Misc.notify(
                        NOTIFICATION.REPORT,
                        this,
                        getString(R.string.sms_reporting_succeeded),
                        getString(R.string.sms_reporting_succeeded_thanx_info),
                        getString(R.string.sms_reporting_succeeded_thanx),
                        false);
            } else {
                Misc.notify(
                        NOTIFICATION.REPORT,
                        this,
                        getString(R.string.sms_reporting_failed),
                        getString(R.string.sms_reporting_failed_unknown_info),
                        getString(R.string.sms_reporting_failed_unknown),
                        false);
            }
        }

        smsdb.close();
    }
}
