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
    private static final ATCommandPooled atCommand1, atCommand2;
    private static final ATCommand atCommandURC;
    private static final ATCommand atCommandData;

    static {
        // We enforce the static final
        ATCommandPooled atc1 = null, atc2 = null;
        ATCommand aturc = null, atdata = null;
        try {
            atc1 = new ATCommandPooled(new ATCommand(false), (byte) 1);
            atc2 = new ATCommandPooled(new ATCommand(false), (byte) 2);
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

    public static ATCommand getATCommandData() {
        return atCommandData;
    }

    public static ATCommand getATCommandURC() {
        return atCommandURC;
    }
    private static final int POOL_MAX_WAIT = 30000; // ms

    public static ATCommandPooled getATCommand() {
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
                    atcPoolLock &= ~i;
                    switch (i) { // We actually get the corresponding instance
                        case 1:
                            return atCommand1;
                        case 2:
                            return atCommand2;
                    }
                }
            }
        }
        throw new RuntimeException("Could not get a PooledATCommand");
    }

    public static String sendUrcRaw(String cmd) {
        return sendRaw(getATCommandURC(), cmd);
    }

    public static String send(String cmd) {
        return sendRaw(cmd + '\r');
    }

    public static String sendRaw(String cmd) {
        ATCommandPooled atc = null;
        try {
            atc = getATCommand();
            return atc.sendRaw(cmd);
        } finally {
            release(atc);
        }
    }

    public static String sendUrc(String cmd) {
        return send(getATCommandURC(), cmd);
    }

    public static String sendAll(String ATCmd) {
        send(atCommandURC, ATCmd);
        send(atCommandData, ATCmd);
        ATCommandPooled atc1 = null, atc2 = null;
        try {
            atc1 = getATCommand();
            atc2 = getATCommand();
            atc1.send(ATCmd);
            return atc2.send(ATCmd);
        } finally {
            release(atc1);
            release(atc2);
        }
    }

    private static String atcInstanceToString(ATCommand atc) {
        if (atc == atCommandURC) {
            return "ATURC";
        } else if (atc == atCommandData) {
            return "ATData";
        } else if (atc == atCommand1.getATCommand()) {
            return "AT1";
        } else if (atc == atCommand2.getATCommand()) {
            return "AT2";
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
                Logger.log(atcInstanceToString(atc) + " --> " + result);
            }
            return result;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("ATCommands.sendRaw", ex, true);
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

    static void release(ATCommandPooled atcp) {
        synchronized (ATCommands.class) {
            atcPoolLock |= atcp.getBit(); // We put back the bit instance in the pool
            ATCommands.class
                    .notify();
        }
    }
}
