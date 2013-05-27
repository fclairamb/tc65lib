package org.javacint.sms;

import org.javacint.at.ATCommands;
import org.javacint.at.ATExecution;
import org.javacint.common.Strings;
import org.javacint.settings.Settings;

/**
 * Basic general-purpose SMS Consumer class.
 *
 * This handles few very useful commands.
 *
 */
public class StandardFeaturesSMSConsumer implements SMSConsumer {

    public boolean smsReceived(String from, String content) {
        String spl[] = Strings.split('#', content);

        // We check the code
        if (!spl[0].equals(Settings.get(Settings.SETTING_CODE))) {
            return false;
        }

        String cmd = spl[1];

        if (cmd.equals("set")) { // Set a settings (and save it)
            Settings.set(spl[1], spl[2]);
            Settings.save();
        } else if (cmd.equals("get")) { // Get a setting and return it by SMS
            String name = spl[1];
            SimpleSMS.send(from, name + "=" + Settings.get(name));
        } else if (cmd.equals("reset")) { // Reset all settings
            Settings.reset();
        } else if (cmd.equals("restart")) { // Restart the chip
            ATExecution.restart();
        } else if (cmd.equals("atc")) { // Execute and AT command
            SimpleSMS.send(from, ATCommands.send(spl[1]));
        } else {
            return false;
        }

        return true;
    }

    public String toString() {
        return "StandardFeaturesSMSC";
    }
}