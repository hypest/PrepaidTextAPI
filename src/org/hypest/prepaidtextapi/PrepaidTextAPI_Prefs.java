
package org.hypest.prepaidtextapi;

import java.util.Vector;

import org.hypest.prepaidtextapi.filters.NetworkOperatorDatum;
import org.hypest.prepaidtextapi.filters.Rest;
import org.hypest.prepaidtextapi.utils.DefaultEnabled;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class PrepaidTextAPI_Prefs extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_screen);

        PreferenceScreen ps = getPreferenceScreen();
        PreferenceScreen plugs = (PreferenceScreen) ps.findPreference("enabled_plugins");

        Vector<NetworkOperatorDatum> nets = NetworkOperatorDatum.getAll(this,
                new PrepaidTextAPIPrefsFile(this));
        for (NetworkOperatorDatum net : nets) {
            if (net instanceof Rest) {
                continue;
            }
            Preference pr = new CheckBoxPreference(this);
            String name = net.getClass().getSimpleName();
            pr.setKey(PrepaidTextAPIPrefsFile.PREFS + name);
            pr.setTitle(net.getFriendlyName());
            pr.setSummary(net.getFriendlySummary());
            pr.setDefaultValue(net.getClass().getAnnotation(DefaultEnabled.class).defaultEnabled());
            plugs.addPreference(pr);
        }
    }
}
