package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.task.Timers;
import org.javacint.time.TimeRetriever;
import org.javacint.time.ntp.SntpClient;

/**
 * NTP client testing command. Usage:<br /><q>ntp &lt;server&gt;</q>
 */
public class NTPTestCommand implements ConsoleCommand {

    private static final String COMMAND = "ntp ";

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.startsWith(COMMAND)) {
            String server = command.substring(COMMAND.length());
            Timers.getSlow().schedule(new TimeRetriever(new SntpClient(server), 24 * 3600 * 1000 /* Every 24h for success */, 900 * 1000 /* Every 15 minutes for failure */), 0);
            return true;
        } else {
            return false;
        }
    }
}
