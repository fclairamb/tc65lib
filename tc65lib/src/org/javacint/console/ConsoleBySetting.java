package org.javacint.console;

import java.util.Hashtable;
import javax.microedition.io.StreamConnection;
import org.javacint.settings.Settings;
import org.javacint.settings.SettingsProvider;

/**
 * Console by setting management.
 *
 * This console wrapper allows to enable/disable the console by changing the
 * console.enabled setting.
 *
 * If you don't need to disable the console by settings, use directly the
 * Console instance.
 */
public class ConsoleBySetting extends Console implements SettingsProvider {

    public ConsoleBySetting(StreamConnection conn) {
        super(conn);
        Settings.addProvider(this);
    }
    private static final String SETTING_CONSOLE_ENABLED = "console.enabled";

    public void getDefaultSettings(Hashtable settings) {
        // By default, the console is enabled
        settings.put(SETTING_CONSOLE_ENABLED, "1");
    }

    public void settingsChanged(String[] settings) {
        for (int i = 0; i < settings.length; i++) {
            parseSetting(settings[i]);
        }
    }

    private void parseSetting(String setName) {
        if (setName.equals(SETTING_CONSOLE_ENABLED)) {
            setEnabled(Settings.getBool(SETTING_CONSOLE_ENABLED));
        }
    }
    private boolean enabled;

    private void setEnabled(boolean en) {
        if (en != enabled) {
            enabled = en;
            if (enabled) {
                start();
            } else {
                stop();
            }
        }
    }

    public void start() {
        if (Settings.getBool(SETTING_CONSOLE_ENABLED)) {
            super.start();
        }
    }
}
