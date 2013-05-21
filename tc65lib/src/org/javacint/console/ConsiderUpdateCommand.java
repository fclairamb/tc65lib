package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.otap.AutoUpdater;

/**
 * Program update consideration command
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class ConsiderUpdateCommand implements ConsoleCommand {

    private final String version;

    public ConsiderUpdateCommand(String version) {
        this.version = version;
    }

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.equals("consider")) {
            AutoUpdater.schedule(version);
            return true;
        }

        return false;
    }
}
