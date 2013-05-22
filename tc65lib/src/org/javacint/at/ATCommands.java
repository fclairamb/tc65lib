/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.at;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import org.javacint.io.ATDataConnection;
import org.javacint.io.Connection;
import org.javacint.logging.Logger;

/**
 * AT wrapper class
 */
public final class ATCommands {

//#if DebugLevel=="debug"
    private static final boolean DEBUG = true;
//#elif DebugLevel=="warn" || DebugLevel=="fatal"
//# private static final boolean DEBUG = false;
//#endif
    private static final ATCommand atCommand;
    private static final ATCommand atCommandURC;
    private static final ATCommand atCommandData;

    static {
        // We enforce the static final
        ATCommand a = null, b = null, c = null;
        try {
            a = new ATCommand(false, false, false, false, false, false);
            b = new ATCommand(false, true, false, false, false, false);
            c = new ATCommand(false, false, false, false, false, false);
        } catch (Exception e) {
            if (DEBUG) {
                Logger.log("ATCommands:static", ATCommands.class);
            }
        }
        atCommand = a;
        atCommandURC = b;
        atCommandData = c;
    }

    public static ATCommand getATCommand() {
        return atCommand;
    }

    public static ATCommand getATCommandData() {
        return atCommandData;
    }

    public static ATCommand getATCommandURC() {
        return atCommandURC;
    }

    public static String send(String cmd) {
        try {
            synchronized (atCommand) {
                return atCommand.send(cmd);
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("ATCommands.send", ex);
            }
            return null;
        }
    }

    public static String sendr(String cmd) {
        return send(cmd + '\r');
    }

    public static String sendrAll(String ATCmd) {
        send(atCommandURC, ATCmd + '\r');
        send(atCommandData, ATCmd + '\r');
        return send(atCommand, ATCmd + '\r');
    }

    private static String send(ATCommand atcmd, String ATCmd) {
        String result = null;
        try {
            if (DEBUG) {
                Logger.log((atcmd == atCommand ? "atCommand" : (atcmd == atCommandURC ? "atCommandURC" : (atcmd == atCommandData ? "atCommandData" : "UnknownATCommand!"))) + " S: " + ATCmd);
            }
            synchronized (atcmd) {
                result = atcmd.send(ATCmd);
            }
            if (DEBUG) {
                Logger.log((atcmd == atCommand ? "atCommand" : (atcmd == atCommandURC ? "atCommandURC" : (atcmd == atCommandData ? "atCommandData" : "UnknownATCommand!"))) + " R: " + result);
            }
        } catch (Exception ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static void addListener(ATCommandListener listener) {
        atCommandURC.addListener(listener);
    }

    public String sendURCToggleCommand(String ATCmd) {
        return send(atCommandURC, ATCmd + '\r');
    }

    public static Connection getATDataConnection() {
        return new ATDataConnection();
    }
//    public static Object getSyncObject() {
//        return atCommand;
//    }
}
