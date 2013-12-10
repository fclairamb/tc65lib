package org.javacint.demo;

import java.util.Hashtable;
import org.javacint.settings.Settings;
import org.javacint.settings.SettingsProvider;

/**
 * Base settings. It provides some default values for some commonly used
 * settings.
 *
 */
public class BaseSettingsProvider implements SettingsProvider {

    public void getDefaultSettings(Hashtable settings) {

        // JAD URL, used to trigger (possibly automatic) updates
        settings.put(Settings.SETTING_JADURL, "http://94.23.55.152:8080/demo/demo.jad");

        // The management code
        settings.put(Settings.SETTING_CODE, "4444");
    }

    public void settingsChanged(String[] settings) {
    }
}
