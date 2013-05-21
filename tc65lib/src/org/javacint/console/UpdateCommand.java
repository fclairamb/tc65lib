package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.at.ATExecution;
import org.javacint.common.Strings;

/**
 *
 * @author florent
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
        }
        return false;
    }
}
