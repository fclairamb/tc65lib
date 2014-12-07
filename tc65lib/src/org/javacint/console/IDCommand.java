/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.at.ATExecution;

/**
 *
 */
public class IDCommand implements ConsoleCommand {

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.equals("help")) {
            out.println("[HELP] imei                             - print the IMEI of the GSM chip");
            out.println("[HELP] iccid                            - print the ICCID of the SIM card");
            out.println("[HELP] imsi                             - print the IMSI of the SIM card");
            out.println("[HELP] hw                               - print the hardware information");
            return false;
        } else if (command.equals("imei")) {
            out.println("[IMEI] " + ATExecution.getImei());
            return true;
        } else if (command.equals("iccid")) {
            out.println("[ICCID] " + ATExecution.getIccid());
            return true;
        } else if (command.equals("imsi")) {
            out.println("[IMSI] " + ATExecution.getImsi());
            return true;
        } else if (command.equals("hw")) {
            out.println("[HW] " + ATExecution.getChipIdentification());
            return true;
        }
        return false;
    }
}
