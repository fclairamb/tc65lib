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
    private static byte atcPoolLock = 1 | 2;
    private static final ATCommand atCommand1, atCommand2;
    private static final ATCommand atCommandURC;
    private static final ATCommand atCommandData;

    static {
        // We enforce the static final
        ATCommand atc1 = null, atc2 = null, aturc = null, atdata = null;
        try {
            atc1 = new ATCommand(false);
            atc2 = new ATCommand(false);
            aturc = new ATCommand(false);
            atdata = new ATCommand(true);

        } catch (Exception e) {
            if (DEBUG) {
                Logger.log("ATCommands:static", ATCommands.class);
            }
        }
        atCommand1 = atc1;
        atCommand2 = atc2;
        atCommandURC = aturc;
        atCommandData = atdata;
    }

    public static ATCommand getATCommand() {
        return atCommand1;
    }

    public static ATCommand getATCommandData() {
        return atCommandData;
    }

    public static ATCommand getATCommandURC() {
        return atCommandURC;
    }

    public static String sendRaw(String cmd) {
        return sendRawPool(cmd);
    }
    private static final int POOL_MAX_WAIT = 10000; // ms

    /**
     * Send a raw AT Command using a pool of ATCommands.
     * 
     * This code is useless if other parts of the program use the same ATCommand
     * instance directly.
     * 
     * @param cmd
     * @return 
     */
    private static String sendRawPool(String cmd) {
        ATCommand atc = null;
        byte bitInstance = 0;
        try {
            synchronized (ATCommands.class) {
                if (atcPoolLock == 0) { // If we have no available instance, we wait for one
                    try {
                        ATCommands.class.wait(POOL_MAX_WAIT);
                    } catch (InterruptedException ex) {
                        if (Logger.BUILD_CRITICAL) {
                            Logger.log("ATC.sendRawPool:78", ex);
                        }
                    }
                }
                for (byte i = 1; i <= 2; i *= 2) { // We are searching for defined bit values (thus *= 2)
                    if ((atcPoolLock & i) != 0) {
                        bitInstance = i; // We are defining a bit lock
                        atcPoolLock &= ~bitInstance; // We remove this available instance from pool
                        switch (bitInstance) { // We actually get the corresponding instance
                            case 1:
                                atc = atCommand1;
                                break;
                            case 2:
                                atc = atCommand2;
                                break;
                        }
                    }
                }
            }
            if (atc != null) { // If we found an instance
                return sendRaw(atc, cmd); // We use it
            }
        } finally {
            synchronized (ATCommands.class) {
                atcPoolLock |= bitInstance; // We put back the bit instance in the pool
                ATCommands.class.notify();
            }
        }
        throw new RuntimeException("Could not find a free ATCommand instance in the pool !");
    }

    public static String sendUrcRaw(String cmd) {
        return sendRaw(getATCommandURC(), cmd);
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
        return sendRaw(atCommand1, ATCmd + '\r');
    }

    private static String atcInstanceToString(ATCommand atc) {
        if (atc == atCommand1) {
            return "AT1";
        } else if (atc == atCommandURC) {
            return "ATURC";
        } else if (atc == atCommandData) {
            return "ATData";
        } else {
            return "ATUnk";
        }
    }

    public static String send(ATCommand atc, String cmd) {
        return sendRaw(atc, cmd + '\r');
    }

    public static String sendRaw(ATCommand atc, String cmd) {

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
