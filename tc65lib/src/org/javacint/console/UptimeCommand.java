package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;

public class UptimeCommand implements ConsoleCommand {

    private static final long startup = System.currentTimeMillis();

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.equals("uptime")) {
            long s = (System.currentTimeMillis() - startup) / 1000;
            long m = s / 60;
            s -= (60 * m);
            long h = m / 60;
            m -= (60 * h);
            out.println("[UPTIME] " + h + "h " + m + "m " + s + "s");
            return true;
        } else if (command.equals("help")) {
            out.println("[HELP] uptime - Show the program's uptime");
        }
        return false;
    }
}
