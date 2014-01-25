package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.at.ATExecution;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;

/**
 * Pin management command receiver.
 *
 * Helps setting / changing / removing PIN from sim cards.
 *
 */
public class PinManagementCommandReceiver implements ConsoleCommand {
    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        try {
            if (command.startsWith("pin ")) {
                command = command.substring("pin ".length());
                if (command.startsWith("unlock ")) {
                    String code = command.substring("unlock ".length());
                    ATExecution.PIN.pinLock(false, code);
                    return true;
                } else if (command.startsWith("lock ")) {
                    String code = command.substring("lock ".length());
                    ATExecution.PIN.pinLock(true, code);
                    return true;
                } else if (command.startsWith("change ")) {
                    command = command.substring("change ".length());
                    String[] spl = Strings.split(' ', command);
                    String before = spl[0];
                    String after = spl[1];
                    ATExecution.PIN.pinChange(before, after);
                    return true;
                }
            } else if (command.equals("help")) {
                out.println("[HELP] pin lock <code>                  - Lock SIM with a pin code");
                out.println("[HELP] pin unlock <code>                - Unlock SIM using a pin code");
                out.println("[HELP] pin change <old code> <new code> - Change the SIM pin code");
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("PinManagement.consoleCommand", ex, true);
            }
        }
        return false;
    }
}
