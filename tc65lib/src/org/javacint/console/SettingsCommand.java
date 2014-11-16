package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import org.javacint.common.sorting.Sorter;
import org.javacint.settings.Settings;

public class SettingsCommand implements ConsoleCommand {

    public boolean consoleCommand(String line, InputStream is, PrintStream out) {
        if (line.equals("help")) {
            out.println("[HELP] conf list                        - List all configuration settings");
            out.println("[HELP] conf <key>=<value>               - Define a configuration setting");
            out.println("[HELP] conf <key>                       - Get a configuration setting");
            out.println("[HELP] conf save                        - Save the configuration settings");
            out.println("[HELP] conf save                        - Reset all settings to their default value");
            return false;
        } else if (line.equals("conf list") || line.equals("conf")) {

            Hashtable defSettings = Settings.getDefaultSettings();

            Object keys[] = new Object[defSettings.size()];

            { // Filling and sorting keys
                Enumeration e = defSettings.keys();
                for (int i = 0; e.hasMoreElements(); i++) {
                    keys[i] = e.nextElement();
                }
                Sorter s = new Sorter();
                s.sort(keys);
            }

            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                String defValue = (String) defSettings.get(key);
                String value = (String) Settings.get(key);
                out.println("[CONF LIST] " + key + " = \"" + value + "\"" + (!defValue.equals(value) ? " (\"" + defValue + "\")" : ""));
            }
        } else if (line.equals("conf save")) {
            Settings.save();
        } else if (line.equals("conf reset")) {
            Settings.reset();
        } else if (line.startsWith("conf ")) {
            String content = line.substring("conf ".length());
            int p = content.indexOf('=');
            if (p != -1) {
                String key = content.substring(0, p);
                String value = content.substring(p + 1);
                Settings.set(key, value);
                value = Settings.get(key);
                out.println("[CONF SET] " + key + " = \"" + value + "\"");
            } else {
                String value = Settings.get(content);
                out.println("[CONF GET] " + content + " = \"" + value + "\"");
            }
        } else {
            return false;
        }
        return true;
    }
}
