/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.at;

//#if sdkns == "siemens"
import org.javacint.utilities.*;
import org.javacint.io.ATDataConnection;
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import org.javacint.io.Connection;

/**
 * AT wrapper class
 */
public final class ATCommands {

//#if DebugLevel=="debug"
	private static final boolean DEBUG = true;
//#elif DebugLevel=="warn" || DebugLevel=="fatal"
//# private static final boolean DEBUG = false;
//#endif

	static {
		try {
			atCommand = new ATCommand(false, false, false, false, false, false);
			atCommandURC = new ATCommand(false, true, false, false, false, false);
			atCommandData = new ATCommand(false, false, false, false, false, false);
		} catch (Exception e) {
			if (DEBUG) {
				e.printStackTrace();
			}
		}
	}
	private static ATCommand atCommand;
	private static ATCommand atCommandURC;
	private static ATCommand atCommandData;

	public static String send(String ATCmd) {
		return sendRAW(atCommand, ATCmd + '\r');
	}

	public static String sendAll(String ATCmd) {
		sendRAW(atCommandURC, ATCmd + '\r');
		sendRAW(atCommandData, ATCmd + '\r');
		return sendRAW(atCommand, ATCmd + '\r');
	}

	private static String sendRAW(ATCommand atcmd, String ATCmd) {
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

	public static void addListener(ATCommandListener listener) {
		atCommandURC.addListener(listener);
	}

	public String sendURCToggleCommand(String ATCmd) {
		return sendRAW(atCommandURC, ATCmd + '\r');
	}

	public static Connection getATDataConnection() {
		return new ATDataConnection();
	}

	public static ATCommand getDataATCommand() {
		return atCommandData;
	}

	public static Object getSyncObject() {
		return atCommand;
	}

	static public class SetSettings {

		public static void setSettings() {
			try {
				boolean ok = false;

				//Enable verbose errors
				int i = 0;
				do {
					Thread.sleep(i * 1000);
					ok = sendAll("AT+CMEE=2").indexOf("OK") >= 0;
					if (DEBUG) {
						Log.println("Enable verbose errors... " + ok);
					}
				} while (!ok);

				//setting preferred SMS message storage to SIM
				i = 0;
				do {
					Thread.sleep(3000 + i * 1000);
					i++;
					ok = ATCommands.send("AT+CPMS=\"MT\",\"MT\",\"MT\"").indexOf("OK") >= 0;
					if (DEBUG) {
						Log.println("setting preferred SMS message storage to SIM + ME... " + ok);
					}
				} while (!ok);

				//set mode to PDU Mode
				i = 0;
				do {
					Thread.sleep(i * 1000);
					ok = ATCommands.send("AT+CMGF=0").indexOf("OK") >= 0;
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
