
package org.hypest.prepaidtextapi.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.hypest.prepaidtextapi.R;
import org.hypest.prepaidtextapi.ReturnResults;
import org.hypest.prepaidtextapi.PrepaidTextAPIPrefsFile;
import org.hypest.prepaidtextapi.PrepaidTextAPI;
import org.hypest.prepaidtextapi.model.MySMS;
import org.hypest.prepaidtextapi.model.MySMS.Recipient;
import org.hypest.prepaidtextapi.receivers.PrepaidTextAPIWidget;
import org.hypest.prepaidtextapi.utils.DefaultEnabled;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TextView;

public abstract class NetworkOperatorDatum {
    public final static int DELAY_MS = 2500;
    public final static String HIDEIT = "@@";

    static private boolean sInExpandedMode = false;
    private boolean iIsTouch = false;

    static Vector<NetworkOperatorDatum> iAllNetworkOperatorData;
    static Vector<NetworkOperatorDatum> iEnabledNetworkOperatorData;
    static Vector<NetworkOperatorDatum> iCallRelatedNetworkOperatorData;
    Context iContext;
    private Activity iActivity;
    PrepaidTextAPIPrefsFile iPrefs;
    View __iMainView = null;
    View __iWidgetView = null;
    Intent iUpdateIntent;
    String iBaseStringName;
    String iUpdateIntentName;
    IntentFilter updateIntentFilter;
    String iOnGoingKey;
    String iLastUpdateKey;
    int iLayoutId;
    int iWidgetLayoutId;
    int iWidgetRefreshIconId;
    String iFriendlyName;
    String iFriendlySummary;
    protected Recipient iRecipient = null;
    protected String iPrefExtra = HIDEIT;
    boolean iPrevOnGoingState = false;
    boolean iPrevExtraBodyState = false;
    @SuppressWarnings("rawtypes")
    private List<Class> iMutualSMSClasses;
    @SuppressWarnings("rawtypes")
    public static final List<Class> NoMutualSMSClasses = new ArrayList<Class>();
    private String iPreviousRandomTicket;
    protected ScrollView iScrollView;

    public enum NetworkOperator {
        Unknown, Offline, WindHellas, Cosmote, Vodafone, Emulator
    }

    private enum PluginSelection {
        All, EnabledOnly, CallRelated
    }

    @SuppressWarnings("unused")
    private NetworkOperatorDatum() {
        // make it impossible to instantiate with empty constructor;
    }

    protected NetworkOperatorDatum(
            Context aContext,
            PrepaidTextAPIPrefsFile aPrefs,
            String aBaseStringName,
            int aLayoutID,
            int aWidgetLayoutId,
            int aWidgetRefreshIconId,
            Recipient aRecipient,
            String aFriendlyName,
            String aFriendlySummary,
            @SuppressWarnings("rawtypes")
            List<Class> aMutualSMSClasses)
    {
        iContext = aContext;
        iPrefs = aPrefs;
        iBaseStringName = aBaseStringName;
        iOnGoingKey = iBaseStringName + ".update_ongoing";
        iLastUpdateKey = iBaseStringName + ".lastupdate";
        iLayoutId = aLayoutID;
        iWidgetLayoutId = aWidgetLayoutId;
        iWidgetRefreshIconId = aWidgetRefreshIconId;
        iRecipient = aRecipient;
        iFriendlyName = aFriendlyName;
        iFriendlySummary = aFriendlySummary;
        iPrefExtra = iBaseStringName + ".extra";
        iPreviousRandomTicket = iBaseStringName + ".prev_ticket";
        iMutualSMSClasses = aMutualSMSClasses;

        iUpdateIntentName = iBaseStringName + ".updateintentname"
                + Integer.toString((new Random()).nextInt());
        updateIntentFilter = new IntentFilter();
        updateIntentFilter.addAction(iUpdateIntentName);

        iUpdateIntent = new Intent();
        iUpdateIntent.setAction(iUpdateIntentName);
    }

    public String getFriendlyName() {
        return iFriendlyName;
    }

    public String getFriendlySummary() {
        return iFriendlySummary;
    }

    protected Activity getActivity() {
        return iActivity;
    }

    public static NetworkOperator getNetworkOperator(Context aContext) {
        TelephonyManager telephonyManager = ((TelephonyManager) aContext
                .getSystemService(Context.TELEPHONY_SERVICE));
        String op = telephonyManager.getNetworkOperator();
        String simop = telephonyManager.getSimOperator();

        if (op.startsWith(aContext.getString(R.string.operator_WindHellas))
                || simop.startsWith(aContext.getString(R.string.operator_WindHellas))) {
            return NetworkOperator.WindHellas;
        } else if (op.startsWith(aContext.getString(R.string.operator_VodafoneGR))
                || simop.startsWith(aContext.getString(R.string.operator_VodafoneGR))) {
            return NetworkOperator.Vodafone;
        } else if (op.startsWith(aContext.getString(R.string.operator_CosmoteGR))
                || simop.startsWith(aContext.getString(R.string.operator_CosmoteGR))) {
            return NetworkOperator.Cosmote;
        } else if (op.startsWith(aContext.getString(R.string.operator_Emulator))
                || simop.startsWith(aContext.getString(R.string.operator_Emulator))) {
            return NetworkOperator.Emulator;
        } else if ((op == null) || (op.length() == 0) || (simop == null) || (simop.length() == 0)) {
            return NetworkOperator.Offline;
        } else {
            return NetworkOperator.Unknown;
        }
    }

    private static Vector<NetworkOperatorDatum> gather(PluginSelection aPluginSelection,
            Context aContext, PrepaidTextAPIPrefsFile aPrefs)
    {
        Vector<NetworkOperatorDatum> data = new Vector<NetworkOperatorDatum>();

        @SuppressWarnings("unused")
        Boolean tmp = false;

        tmp = aPrefs.getBoolean(Balance1234.class.getSimpleName(),
                Balance1234.class.getAnnotation(DefaultEnabled.class).defaultEnabled())
                || aPluginSelection == PluginSelection.All ?
                data.add(Balance1234.Instantiate(aContext, aPrefs)) : false;

        data.add(Rest.Instantiate(aContext, aPrefs));

        return data;
    }

    public static Vector<NetworkOperatorDatum> getAll(Context aContext,
            PrepaidTextAPIPrefsFile aPrefs)
    {
        if (iAllNetworkOperatorData == null) {
            iAllNetworkOperatorData = gather(PluginSelection.All, aContext, aPrefs);
        }

        return iAllNetworkOperatorData;
    }

    public static Vector<NetworkOperatorDatum> getEnabledOnly(Context aContext,
            PrepaidTextAPIPrefsFile aPrefs)
    {
        if (iEnabledNetworkOperatorData == null) {
            iEnabledNetworkOperatorData = gather(PluginSelection.EnabledOnly, aContext, aPrefs);
        }

        return iEnabledNetworkOperatorData;
    }

    public static Vector<NetworkOperatorDatum> getCallRelated(Context aContext,
            PrepaidTextAPIPrefsFile aPrefs)
    {
        if (iCallRelatedNetworkOperatorData == null) {
            iCallRelatedNetworkOperatorData = gather(PluginSelection.CallRelated, aContext, aPrefs);
        }

        return iCallRelatedNetworkOperatorData;
    }

    public static void regather(Context aContext, PrepaidTextAPIPrefsFile aPrefs) {
        iAllNetworkOperatorData = null;
        iAllNetworkOperatorData = gather(PluginSelection.All, aContext, aPrefs);

        iEnabledNetworkOperatorData = null;
        iEnabledNetworkOperatorData = gather(PluginSelection.EnabledOnly, aContext, aPrefs);

        iCallRelatedNetworkOperatorData = null;
        iCallRelatedNetworkOperatorData = gather(PluginSelection.CallRelated, aContext, aPrefs);
    }

    protected static NetworkOperatorDatum getSpecificInstance(
            @SuppressWarnings("rawtypes")
            Class aClass,
            Context aContext,
            PrepaidTextAPIPrefsFile aPrefs)
    {
        Vector<NetworkOperatorDatum> nets = getEnabledOnly(aContext, aPrefs);

        String clname = aClass.getSimpleName();
        for (NetworkOperatorDatum net : nets) {
            if (net.getClass().getSimpleName().equals(clname))
                return (NetworkOperatorDatum) (net.getInstance());
        }

        return null;
    }

    protected static Boolean isEnabled(String aSimpleClassName){
        if (iEnabledNetworkOperatorData != null) {
            for (NetworkOperatorDatum net : iEnabledNetworkOperatorData)
                if (aSimpleClassName.equals(net.getClass().getSimpleName()))
                    return true;
        }

        return false;
    }

    public static void unregisterAllInstances() {
        Object singleton = null;
        for (NetworkOperatorDatum enablednet : iEnabledNetworkOperatorData) {
            if (enablednet != null) {
                singleton = enablednet.getInstance();

                if (singleton != null) {
                    enablednet.unregister();
                }

                enablednet.iActivity = null;
            }
        }
    }

    public static void registerAllInstances(Activity activity) {
        for (NetworkOperatorDatum enablednet : iEnabledNetworkOperatorData) {
            if (enablednet != null) {
                enablednet.register(activity);
            }
        }
    }

    public final static void Delay() {
        try {
            Thread.sleep(DELAY_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setScrollView(ScrollView sv) {
        iScrollView = sv;
    }

    protected void onHideExtra() {
    }

    protected void onShowExtra() {
    }

    protected void hideExtra() {
        View v = MainView().findViewById(R.id.extra_content);
        v.setVisibility(View.GONE);

        MainView().setOnLongClickListener(null);
        onHideExtra();
    }

    private void showExtra() {
        View v = MainView().findViewById(R.id.extra_content);
        v.setVisibility(View.VISIBLE);

        if (iRecipient != Recipient.RNONE) {
            MainView().setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    checkedNetworkRefresh();
                    return true;
                }
            });
        }

        onShowExtra();
    }

    private void toggleExtra() {
        View v = MainView().findViewById(R.id.extra_content);
        if (v.getVisibility() == View.VISIBLE) {
            hideExtra();
            sInExpandedMode = false;
        } else{
            showExtra();
            sInExpandedMode = true;
            for (NetworkOperatorDatum enablednet : iEnabledNetworkOperatorData) {
                if (enablednet != null) {
                    if (enablednet != this) {
                        enablednet.hideExtra();
                    }
                }
            }

            MainView().requestFocus();
        }
    }

    public static boolean closeAllExtras() {
        boolean openExisted = sInExpandedMode;
        for (NetworkOperatorDatum enablednet : iEnabledNetworkOperatorData) {
            if (enablednet != null) {
                View v = enablednet.MainView().findViewById(R.id.extra_content);
                if (v.getVisibility() == View.VISIBLE) {
                    enablednet.hideExtra();
                }
            }
        }

        sInExpandedMode = false;

        return openExisted;
    }

    public View createMainView() {
        View v = View.inflate(iContext, iLayoutId, null);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExtra();
            }
        });
        v.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                iIsTouch = true;

                return false;
            }
        });
        v.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (sInExpandedMode || iIsTouch) {
                        sInExpandedMode = true;
                        showExtra();
                    }
                } else {
                    hideExtra();
                }

                iIsTouch = false;
            }
        });

        View ex = v.findViewById(R.id.extra_content);
        ex.setVisibility(View.GONE);

        TextView name = (TextView) v.findViewById(R.id.TextView_name);
        name.setText(iFriendlyName);

        return v;
    }

    public View MainView() {
        if (__iMainView == null) {
            __iMainView = createMainView();
        }
        return __iMainView;
    }

    static public NetworkOperatorDatum NetFromView(View v) {
        if (iEnabledNetworkOperatorData != null) {
            for (NetworkOperatorDatum net : iEnabledNetworkOperatorData) {
                if (net.MainView() == v) {
                    return net;
                }
            }
        }

        return null;
    }

    private void register(Activity activity) {
        iActivity = activity;
        iActivity.registerReceiver(updateIntentReceiver, updateIntentFilter);
    }

    private void unregister() {
        try {
            if (iActivity != null) {
                iActivity.unregisterReceiver(updateIntentReceiver);
            }
        } catch (IllegalArgumentException e) {
            Log.i("PrepaidTextAPILog", e.getMessage());
        }
    }

    public final Boolean isOnGoing() {
        return iPrefs.getBoolean(iOnGoingKey, false);
    }

    protected final Long getLastUpdateMillis() {
        return iPrefs.getLong(iLastUpdateKey, 0l);
    }

    protected final boolean isSameRandomTicket(long aNewRandomTicket) {
        return (aNewRandomTicket == iPrefs.getLong(iPreviousRandomTicket, 0l));
    }

    protected final void setPreviousRandomTicket(long aNewRandomTicket) {
        iPrefs.putLong(iPreviousRandomTicket, aNewRandomTicket);
    }

    protected final void updateRefreshIcon() {
        ImageView refr = null;
        refr = (ImageView) MainView().findViewById(R.id.ImageView_refresh);
        if (refr == null) {
            return;
        }

        ImageView refrExtra = null;
        refrExtra = (ImageView) MainView().findViewById(R.id.ImageView_extrabody);
        boolean newState = isOnGoing();
        boolean newExtraBodyState = !(iPrefs.getString(iPrefExtra, HIDEIT)).startsWith(HIDEIT);

        if (newState) {
            if (iPrevOnGoingState == false) {
                Animation anim = AnimationUtils.loadAnimation(iContext, R.anim.fadein);
                anim.setFillAfter(true);
                anim.setFillEnabled(true);
                refr.startAnimation(anim);
            } else {
                refr.setVisibility(View.VISIBLE);
            }
        } else {
            if (iPrevOnGoingState == true) {
                Animation anim = AnimationUtils.loadAnimation(iContext, R.anim.fadeout);
                anim.setFillAfter(true);
                anim.setFillEnabled(true);
                refr.startAnimation(anim);
            } else {
                refr.setVisibility(View.INVISIBLE);
            }
        }

        if (newExtraBodyState) {
            if (iPrevExtraBodyState == false) {
                Animation anim = AnimationUtils.loadAnimation(iContext, R.anim.fadein);
                anim.setFillAfter(true);
                anim.setFillEnabled(true);
                refrExtra.startAnimation(anim);
            } else {
                refrExtra.setVisibility(View.VISIBLE);
            }
        } else {
            if (iPrevExtraBodyState == true) {
                Animation anim = AnimationUtils.loadAnimation(iContext, R.anim.fadeout);
                anim.setFillAfter(true);
                anim.setFillEnabled(true);
                refrExtra.startAnimation(anim);
            } else {
                refrExtra.setVisibility(View.INVISIBLE);
            }
        }

        iPrevOnGoingState = newState;
        iPrevExtraBodyState = newExtraBodyState;
    }

    abstract public void updateUI();

    abstract void animStart();

    abstract protected Object getInstance();

    public void storeExtraBody(String extrabody) {
        iPrefs.putString(iPrefExtra, (extrabody.length() > 0 ? extrabody : HIDEIT));
    }

    public void clearExtraBody() {
        iPrefs.putString(iPrefExtra, HIDEIT);
    }

    public void updateExtraBody() {
        String extra = iPrefs.getString(iPrefExtra, HIDEIT);

        View tv = MainView().findViewById(R.id.extra_body);
        tv.setVisibility(extra.startsWith(HIDEIT) ? View.GONE : View.VISIBLE);
        if (tv instanceof TextView) {
            ((TextView) tv).setText(Html.fromHtml(extra));
        }
    }

    public void updateOwnUIAsync() {
        iContext.sendBroadcast(iUpdateIntent);
    }

    private void doUpdateUI() {
        updateUI();
        PrepaidTextAPI.updateOwnUIAsync(iContext);
        PrepaidTextAPIWidget.doUpdate(iContext);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            doUpdateUI();
        }
    };

    private Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            animStart();
            doUpdateUI();
        }
    };

    private BroadcastReceiver updateIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.startsWith(iUpdateIntentName)) {
                iActivity.runOnUiThread(updateRunnable);
            }
        }
    };

    abstract protected void sendSMS();

    public void markOnGoing() {
        iPrefs.putBoolean(iOnGoingKey, true);
        iPrefs.commit();
    }

    public void markFinished(Long aMillis) {
        iPrefs.putBoolean(iOnGoingKey, false);
        iPrefs.putLong(iLastUpdateKey, aMillis);
        iPrefs.commit();
    }

    private void clearRefreshIcon() {
        iPrefs.putBoolean(iOnGoingKey, false);
        iPrefs.commit();
        updateRefreshIcon();
    }

    public static void clearRefreshIcons(PrepaidTextAPI aPrepaidTextAPI) {
        Vector<NetworkOperatorDatum> networkOperatorData = getEnabledOnly(aPrepaidTextAPI,
                new PrepaidTextAPIPrefsFile(aPrepaidTextAPI));

        for (NetworkOperatorDatum networkOperatorDatum : networkOperatorData) {
            networkOperatorDatum.clearRefreshIcon();
        }
    }

    private void checkedNetworkRefresh() {
        NetworkOperator net = getNetworkOperator(iContext);

        switch (net) {
            case Cosmote:
                doNetworkRefresh(newRandomTicket());
                break;
            case Offline:
                AlertDialog.Builder builder = new AlertDialog.Builder(iContext);
                builder.setMessage(
                        iContext.getString(R.string.network_offline_info))
                        .setTitle(iContext.getString(R.string.network_offline))
                        .setCancelable(true)
                        .setNegativeButton(iContext.getString(R.string.button_back),
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
                builder = new AlertDialog.Builder(iContext);
                builder.setMessage(
                        iContext.getString(R.string.network_foreign))
                        .setTitle(iContext.getString(R.string.warning))
                        .setIcon(R.drawable.ic_bullet_key_permission)
                        .setCancelable(true)
                        .setNegativeButton(iContext.getString(android.R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                builder.setPositiveButton(iContext.getString(R.string.button_send),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                doNetworkRefresh(newRandomTicket());
                            }
                        });
                alert = builder.create();
                alert.show();
                break;
        }
    }

    private Runnable clearNetworkRefreshIconRunnable = new Runnable() {
        @Override
        public void run() {
            clearRefreshIcon();
        }
    };

    public void doNetworkRefresh(long aRandomTicket) {
        if (isSameRandomTicket(aRandomTicket)) {
            return;
        }
        setPreviousRandomTicket(aRandomTicket);

        List<NetworkOperatorDatum> enabledMutualSMSClasses = new ArrayList<NetworkOperatorDatum>();
        for (Class<NetworkOperatorDatum> cl : iMutualSMSClasses) {
            if (NetworkOperatorDatum.isEnabled(cl.getSimpleName())) {
                NetworkOperatorDatum net = getSpecificInstance(cl, iContext, iPrefs);
                if (net != null) {
                    enabledMutualSMSClasses.add(net);
                }
            }
        }

        for (NetworkOperatorDatum n : enabledMutualSMSClasses) {
            n.setPreviousRandomTicket(aRandomTicket);
        }

        if (iActivity != null) {
            iActivity.runOnUiThread(clearNetworkRefreshIconRunnable);
            for (NetworkOperatorDatum n : enabledMutualSMSClasses) {
                iActivity.runOnUiThread(n.clearNetworkRefreshIconRunnable);
            }
        }

        sendSMS();

        markOnGoing();
        if (iActivity != null) {
            iActivity.runOnUiThread(animRunnable);
        }

        for (NetworkOperatorDatum n : enabledMutualSMSClasses) {
            n.markOnGoing();
            if (iActivity != null) {
                iActivity.runOnUiThread(n.animRunnable);
            }
        }

        PrepaidTextAPIWidget.doUpdate(iContext);
    }

    static public void runNetworkRefresh(PrepaidTextAPI amyApp)
    {
        boolean inAutoRefresh = amyApp.inAutoRefresh();
        PrepaidTextAPIPrefsFile prefs = new PrepaidTextAPIPrefsFile(amyApp);
        Vector<NetworkOperatorDatum> nets = NetworkOperatorDatum.getEnabledOnly(amyApp, prefs);
        Intent ca = amyApp.getIntent();
        Bundle extras = ca.getExtras();

        if (extras == null) {
            extras = new Bundle();
        }

        boolean any = true;
        for (NetworkOperatorDatum net : nets) {
            boolean upthis = extras.getBoolean("refresh_" + net.getClass().getSimpleName(), false);
            any &= !upthis;
        }

        long randomTicket = newRandomTicket();

        for (NetworkOperatorDatum net : nets) {
            boolean dorefresh = false;

            if (inAutoRefresh) {
                if (any || extras.getBoolean("refresh_" + net.getClass().getSimpleName(), false))
                    dorefresh = true;
            } else {
                dorefresh = true;
            }

            if (dorefresh) {
                net.doNetworkRefresh(randomTicket);
            }
        }

        prefs.putBoolean(PrepaidTextAPI.PREFS_SENDING, false);
        prefs.putBoolean(PrepaidTextAPI.PREFS_ALLPLUGINSSENT, true);
        prefs.commit();

        if (inAutoRefresh) {
            amyApp.finish();
        }
    }

    static public void runNetworkRefreshCallRelated(Context ctx) {
        PrepaidTextAPIPrefsFile prefs = new PrepaidTextAPIPrefsFile(ctx);
        Vector<NetworkOperatorDatum> nets = NetworkOperatorDatum.getCallRelated(ctx, prefs);
        Bundle extras = null;

        if (extras == null) {
            extras = new Bundle();
        }

        long randomTicket = newRandomTicket();

        for (NetworkOperatorDatum net : nets) {
            net.doNetworkRefresh(randomTicket);
        }

        prefs.putBoolean(PrepaidTextAPI.PREFS_SENDING, false);
        prefs.putBoolean(PrepaidTextAPI.PREFS_ALLPLUGINSSENT, true);
        prefs.commit();
    }

    abstract public ReturnResults parse(MySMS sms);

    public final int getWidgetLayoutId() {
        return iWidgetLayoutId;
    }

    abstract public void widgetUpdate(RemoteViews aWidgetView);

    public final void updateWidgetRefreshIcon(RemoteViews aWidgetView) {
        if ((iWidgetLayoutId != 0) && (iWidgetRefreshIconId != 0)) {
            if (isOnGoing()) {
                aWidgetView.setViewVisibility(iWidgetRefreshIconId, View.VISIBLE);
            } else {
                aWidgetView.setViewVisibility(iWidgetRefreshIconId, View.GONE);
            }
        }
    }

    static protected long newRandomTicket() {
        Random r = new Random();
        r.setSeed(System.currentTimeMillis());
        return r.nextLong();
    }
}
