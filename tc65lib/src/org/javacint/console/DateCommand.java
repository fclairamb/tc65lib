package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import org.javacint.time.DateManagement;

/**
 * Date displaying command.
 * This command displays the date from the chip's time + correcting offset.
 */
public class DateCommand implements ConsoleCommand {

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.equals("date")) {
            out.println("[DATE] " + new Date(DateManagement.time() * 1000));
            return true;
        } else {
            return false;
        }
    }
}
