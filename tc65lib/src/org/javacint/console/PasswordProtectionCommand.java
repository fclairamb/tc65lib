package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.settings.Settings;

/**
 *
 * @author florent
 */
public class PasswordProtectionCommand implements ConsoleCommand {

    private boolean ok = false;

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (ok) {
            return false;
        }

        if (command.equals(Settings.get(Settings.SETTING_CODE))) {
            out.println("[CODE] OK !");
            ok = true;
            return true;
        }

        out.println("[CODE] Type your code please !");
        return true;
    }
}
