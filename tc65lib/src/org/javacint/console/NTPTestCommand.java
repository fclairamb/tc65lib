/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import org.javacint.task.Timers;
import org.javacint.time.DateManagement;
import org.javacint.time.ntp.SntpClient;

/**
 *
 * @author florent
 */
public class NTPTestCommand implements ConsoleCommand {

    private static final String COMMAND = "ntp ";

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.startsWith(COMMAND)) {
            String server = command.substring(COMMAND.length());
            Timers.getSlow().schedule(new SntpClient(server), 0);
            return true;
        } else {
            return false;
        }
    }
}
