/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.utilities;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import org.javacint.io.Connection;

/**
 * AT wrapper class
 */
public final class ATClass {

//#if DebugLevel=="debug"
    private static final boolean DEBUG = true;
//#elif DebugLevel=="warn" || DebugLevel=="fatal"
//# private static final boolean DEBUG = false;
//#endif
    // <Singleton pattern>
    private static ATClass instance;

    public static synchronized ATClass getInstance() {
        if (instance == null) {
            if (DEBUG) {
                Log.println("\n<Making SyncATCommand IRZ>");
            }
            //Log.println("Opening AT Command... ");
            instance = new ATClass();
            try {
                atCommand = new ATCommand(false, false, false, false, false, false);
                atCommandURC = new ATCommand(false, true, false, false, false, false);
                //atCommand = atCommandURC;
                atCommandData = new ATCommand(false, false, false, false, false, false);
                if (DEBUG) {
                    Log.println(instance != null ? "Opening AT Command... OK" : "Opening AT Command... Error");
                }
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
            new SetSettings().setSettings();

            if (DEBUG) {
                Log.println("</Making SyncATCommand IRZ>\n");
            }
        }
        return instance;
    }
    // </Singleton pattern>
    private static ATCommand atCommand;
    private static ATCommand atCommandURC;
    private static ATCommand atCommandData;

    public String send(String ATCmd) {
        return sendRAW(atCommand, ATCmd + '\r');
    }

    public String sendAll(String ATCmd) {
        sendRAW(atCommandURC, ATCmd + '\r');
        sendRAW(atCommandData, ATCmd + '\r');
        return sendRAW(atCommand, ATCmd + '\r');
    }

    private String sendRAW(ATCommand atcmd, String ATCmd) {
        String result = null;
        try {
            if (DEBUG) {
                Log.println((atcmd == atCommand ? "atCommand" : (atcmd == atCommandURC ? "atCommandURC" : (atcmd == atCommandData ? "atCommandData" : "UnknownATCommand!"))) + " S: " + ATCmd);
            }
            synchronized (atcmd) {
                result = atcmd.send(ATCmd);
            }
            if (DEBUG) {
                Log.println((atcmd == atCommand ? "atCommand" : (atcmd == atCommandURC ? "atCommandURC" : (atcmd == atCommandData ? "atCommandData" : "UnknownATCommand!"))) + " R: " + result);
            }
        } catch (Exception ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public void addListener(ATCommandListener listener) {
        atCommandURC.addListener(listener);
    }

    public String sendURCToggleCommand(String ATCmd) {
        return sendRAW(atCommandURC, ATCmd + '\r');
    }

    public Connection getATDataConnection() {
        return new ATDataConnection();
    }

    static ATCommand getDataATCommand() {
        return atCommandData;
    }

    public Object getSyncObject() {
        return atCommand;
    }

    static public class SetSettings {

        public void setSettings() {
            try {
                boolean ok = false;
                
                //Enable verbose errors
                int i = 0;
                do {
                    Thread.sleep(i * 1000);
                    ok = ATClass.getInstance().sendAll("AT+CMEE=2").indexOf("OK") >= 0;
                    if (DEBUG) {
                        Log.println("Enable verbose errors... " + ok);
                    }
                } while (!ok);

                //setting preferred SMS message storage to SIM
                i = 0;
                do {
                    Thread.sleep(3000 + i * 1000);
                    i++;
                    ok = ATClass.getInstance().send("AT+CPMS=\"MT\",\"MT\",\"MT\"").indexOf("OK") >= 0;
                    if (DEBUG) {
                        Log.println("setting preferred SMS message storage to SIM + ME... " + ok);
                    }
                } while (!ok);

                //set mode to PDU Mode
                i = 0;
                do {
                    Thread.sleep(i * 1000);
                    ok = ATClass.getInstance().send("AT+CMGF=0").indexOf("OK") >= 0;
                    if (DEBUG) {
                        Log.println("set mode to PDU Mode... " + ok);
                    }
                } while (!ok);
            } catch (Exception ex) {
                if (DEBUG) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

