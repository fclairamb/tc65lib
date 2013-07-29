package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.otap.AutoUpdater;

/**
 * Auto updater triggering command.
 * Usage:<br />
 * <q>update</q>
 * 
 */
public class ConsiderUpdateCommand implements ConsoleCommand {

    private final String version;

    /**
     * Constructor.
     * @param version Current version of the program. 
     */
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
