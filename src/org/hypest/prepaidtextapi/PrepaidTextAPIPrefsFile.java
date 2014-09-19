
package org.hypest.prepaidtextapi;

import android.content.Context;
import android.content.SharedPreferences;

public class PrepaidTextAPIPrefsFile {
    public static final String PREFS_FILE = "org.hypest.prepaidtextapi_preferences";
    public static final String PREFS = "org.hypest.prepaidtextapi.pref.";

    SharedPreferences iPrefs;
    SharedPreferences.Editor iPrefsEditor;

    public PrepaidTextAPIPrefsFile(Context aContext) {
        iPrefs = aContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        iPrefsEditor = iPrefs.edit();
    }

    public Boolean getBoolean(String aKey, Boolean aDefaultValue) {
        return iPrefs.getBoolean(PREFS + aKey, aDefaultValue);
    }

    public String getString(String aKey, String aDefaultValue) {
        return iPrefs.getString(PREFS + aKey, aDefaultValue);
    }

    public Long getLong(String aKey, Long aDefaultValue) {
        return iPrefs.getLong(PREFS + aKey, aDefaultValue);
    }

    public int getInt(String aKey, int aDefaultValue) {
        return iPrefs.getInt(PREFS + aKey, aDefaultValue);
    }

    public void putBoolean(String aKey, Boolean aValue) {
        iPrefsEditor.putBoolean(PREFS + aKey, aValue);
    }

    public void putString(String aKey, String aValue) {
        iPrefsEditor.putString(PREFS + aKey, aValue);
    }

    public void putLong(String aKey, Long aValue) {
        iPrefsEditor.putLong(PREFS + aKey, aValue);
    }

    public void putInt(String aKey, int aValue) {
        iPrefsEditor.putInt(PREFS + aKey, aValue);
    }

    public void commit() {
        iPrefsEditor.commit();
    }
}
