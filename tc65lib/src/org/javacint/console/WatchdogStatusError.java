package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.watchdog.WatchdogManager;
import org.javacint.watchdog.WatchdogStatusProvider;

/**
 * Create a watchdog error.
 * This command can be used to test the watchdog.
 */
public class WatchdogStatusError implements WatchdogStatusProvider, ConsoleCommand {

    private boolean status = true;

    public WatchdogStatusError() {
        WatchdogManager.add(this);
    }

    public String getWorkingStatus() {
        return status ? null : "Watchdog error";
    }

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.equals("watchdog error")) {
            status = !status;
            out.println("[WATCHDOG BREAK] " + (status ? "OK" : "NOK"));
            return true;
        } else if (command.equals("help")) {
            out.println("[HELP] watchdog error                   - Break the watchdog status");
        }
        return false;
    }
}
