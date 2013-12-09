package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.TimerTask;
import org.javacint.task.Timers;

public class UptimeCommand implements ConsoleCommand {

    private static final long startup = System.currentTimeMillis();

    public boolean consoleCommand(String command, InputStream is, final PrintStream out) {
        if (command.equals("uptime")) {
            showUptime(out);
            return true;
        } else if (command.startsWith("uptime ")) {
            int time = Integer.parseInt(command.substring("uptime ".length()));
            Timers.getSlow().schedule(new TimerTask() {
                public void run() {
                    showUptime(out);
                }
            }, 0, time * 1000);
            return true;
        } else if (command.equals("help")) {
            out.println("[HELP] uptime - Show the program's uptime");
            out.println("[HELP] uptime <s> - Show the program's frequently");
        }
        return false;
    }

    private void showUptime(PrintStream out) {
        long s = (System.currentTimeMillis() - startup) / 1000;
        long m = s / 60;
        s -= (60 * m);
        long h = m / 60;
        m -= (60 * h);
        out.println("[UPTIME] " + h + "h " + m + "m " + s + "s");
    }
}
