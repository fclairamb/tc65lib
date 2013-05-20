package org.javacint.demo;

import java.util.Hashtable;
import org.javacint.settings.Settings;
import org.javacint.settings.SettingsConsumer;

/**
 * Provides some default values for some commonly used settings.
 */
public class BaseSettingsProvider implements SettingsConsumer {

    public void getDefaultSettings(Hashtable settings) {

        // JAD URL, used to trigger (possibly automatic) updates
        settings.put(Settings.SETTING_JADURL, "http://192.168.12.13:8090/tc65libdemo/tc65libdemo.jad");

        // The management code
        settings.put(Settings.SETTING_CODE, "4444");
    }

    public void settingsChanged(String[] settings) {
    }
}
