package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import org.javacint.task.Timers;
import org.javacint.time.TimeClient;
import org.javacint.time.TimeRetriever;
import org.javacint.time.ntp.SntpClient;

/**
 * NTP client testing command. Usage:<br /><q>ntp &lt;server&gt;</q>
 */
public class NTPTestCommand implements ConsoleCommand {

    private static final String COMMAND = "ntp ";

    public boolean consoleCommand(String command, InputStream is, final PrintStream out) {
        if (command.startsWith(COMMAND)) {
            final String server = command.substring(COMMAND.length()).trim();
            TimeRetriever timeRetriever = new TimeRetriever(new TimeClient() {
                final TimeClient src = new SntpClient(server);

                public long getTime() throws Exception {
                    long time = src.getTime();

                    if (time != 0) {
                        out.println("[ NTP ] " + server + " - OK - " + new Date(time * 1000).toString());
                    } else {
                        out.println("[ NTP ] " + server + " - ERROR");
                    }

                    return time;
                }
            });

            Timers.getSlow().schedule(timeRetriever, 0);
            return true;
        } else if (command.equals("help")) {
            out.println("[HELP] ntp <server> - Get time from a server");
        }
        return false;
    }
}
