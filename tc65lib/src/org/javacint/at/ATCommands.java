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
import org.javacint.sms.SMSReceiver;

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
        ATCommand atc1 = null, aturc = null, atdata = null;
        try {
            atc1 = new ATCommand(false);
            aturc = new ATCommand(false);
            atdata = new ATCommand(true);
        } catch (Exception e) {
            if (DEBUG) {
                Logger.log("ATCommands:static", ATCommands.class);
            }
        }
        atCommand = atc1;
        atCommandURC = aturc;
        atCommandData = atdata;
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

    public static String sendRaw(String cmd) {
        return sendRaw(getATCommand(), cmd);
    }

    public static String sendUrcRaw(String cmd) {
        return sendRaw(getATCommand(), cmd);
    }

    public static String send(String cmd) {
        return send(getATCommand(), cmd);
    }

    public static String sendUrc(String cmd) {
        return send(getATCommandURC(), cmd);
    }

    public static String sendrAll(String ATCmd) {
        sendRaw(atCommandURC, ATCmd + '\r');
        sendRaw(atCommandData, ATCmd + '\r');
        return sendRaw(atCommand, ATCmd + '\r');
    }

    private static String atcInstanceToString(ATCommand atc) {
        if (atc == atCommand) {
            return "AT1";
        } else if (atc == atCommandURC) {
            return "ATURC";
        } else if (atc == atCommandData) {
            return "ATData";
        } else {
            return "ATUnk";
        }
    }

    private static String send(ATCommand atc, String cmd) {
        return sendRaw(atc, cmd + '\r');
    }

    private static String sendRaw(ATCommand atc, String cmd) {

        try {
            String result;
            if (DEBUG) {
                Logger.log(atcInstanceToString(atc) + " <-- " + cmd);
            }
            synchronized (atc) {
                result = atc.send(cmd);
            }
            if (DEBUG) {
                Logger.log(atcInstanceToString(atc) + " --> " + cmd);
            }
            return result;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("ATCommands.send", ex);
            }
            return null;
        }
    }

    public static void addListener(ATCommandListener listener) {
        atCommandURC.addListener(listener);
    }

    public static void removeListener(ATCommandListener listener) {
        atCommandURC.removeListener(listener);
    }

    public static Connection getATDataConnection() {
        return new ATDataConnection();
    }
}
