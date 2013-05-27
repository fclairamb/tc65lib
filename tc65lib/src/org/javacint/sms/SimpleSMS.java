/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.sms;

import org.javacint.at.ATCommands;
import org.javacint.logging.Logger;

/**
 *
 * @author florent
 */
public class SimpleSMS {

    private static final char CTRL_Z = 0x1a;
    private static final boolean DEBUG = true;

    public static boolean send(String dest, String msg) {
        try {
            String ret;
            synchronized (ATCommands.getATCommand()) {
                ATCommands.sendNoR("at+cmgf=1\r");
                ret = ATCommands.sendNoR("at+cmgs=" + dest + "\r");
                ret += ATCommands.sendNoR(msg + CTRL_Z);
            }

            if (Logger.BUILD_DEBUG && DEBUG) {
                Logger.log("SMSManagement.send.ret = \"" + ret + "\"");
            }

        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("SMSManagement.send", ex, true);
            }
            return false;
        }

        return true;
    }
}
