package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.at.ATExecution;
import org.javacint.common.Strings;

/**
 * Console command to trigger an update. Usage:<br /> <q>update &lt;apn&gt;
 * &lt;jadurl&gt;</q><br />If "apn" and "jadurl" parameters are omitted, their
 * value is fetched from the settings.
 */
public class UpdateCommand implements ConsoleCommand {

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.equals("update") || command.startsWith("update ")) {
            String args[] = Strings.split(' ', command);
            if (args.length == 1) {
                ATExecution.update();
            } else if (args.length == 2) {
                ATExecution.update(args[1]);
            } else if (args.length == 3) {
                ATExecution.update(args[1], args[2]);
            }
            return true;
        } else if (command.equals("help")) {
            out.println("[HELP] update                           - Update the program");
        }
        return false;
    }
}
