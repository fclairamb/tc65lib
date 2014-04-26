package org.javacint.at;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import java.util.Calendar;
import java.util.Date;
import java.io.IOException;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.watchdog.WatchdogManager;

/**
 * AT Commands wrapper.
 *
 * Most of the "dirty" AT commands handling code is put here.
 */
public class ATExecution {

    private static final String THIS = "ATExec";

    public static String[] getMONC() {
        try {
            String ret = ATCommands.send("AT^SMONC");
            String[] spl = Strings.split('\n', ret);
            spl = Strings.split(',', spl[1].substring(7).trim());
            return spl;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".getMCC", ex, true);
            }
            return null;
        }
    }

    /**
     * Get the current RSSI.
     *
     * @return RSSI level
     */
    public static int getRssi() {
        try {
            String ret = ATCommands.send("AT+CSQ");
            String tab[] = Strings.split('\n', ret);
            ret = tab[1];
            if (ret.equals("ERROR")) {
                return -1;
            }
            tab = Strings.split(',', ret.substring(6).trim());
            String rssi = tab[0];
            return Integer.parseInt(rssi);
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".getRssi", ex, true);
            }
            return -1;
        }
    }

    /**
     * Get the IMEI number of the GSM chip
     *
     * @return IMEI number
     */
    public static String getImei() {
        try {
            String tab[] = Strings.split('\n', ATCommands.send("AT+GSN"));
            return (tab[1]).trim();
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".getImei", ex);
            }
            return null;
        }
    }

    /**
     * Get the IMSI of the SIM card
     *
     * @return SIM card IMSI
     */
    public static String getImsi() {
        try {
            String tab[] = Strings.split('\n', ATCommands.send("AT+CIMI"));
            String imsi = (tab[1]).trim();

            if (imsi.equals("ERROR")) {
                return null;
            }
            return imsi;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".getImsi", ex);
            }
            return null;
        }
    }

    /**
     * Display the SIM card identification number
     *
     * @return SIM card identification number
     */
    public static String getIccid() {
        try {
            String tab[] = Strings.split('\n', ATCommands.send("AT^SCID"));
            String scid = (tab[1]).trim().substring(7);
//            if (Logger.BUILD_DEBUG) {
//                Logger.log("Common.getIccid: \"" + scid + "\"");
//            }
            if (scid.equals("ERROR")) {
                return null;
            }
            return scid;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".getIccid", ex);
            }
            return null;
        }
    }

    /**
     * Set the GPRS attachment state. This can force GPRS attachment or force
     * its dettachment.
     *
     * @param attach <strong>true</strong> for GPRS attachment,
     * <strong>false</strong> for GPRS detachment
     */
    public static void setGprsAttach(boolean attach) {
        ATCommands.send("AT+CGATT=" + (attach ? "1" : "0"));
    }

    /**
     * Set airplane mode
     *
     * @param mode true for ON (no GSM)
     */
    public static void setAirplaneMode(boolean mode) {
        ATCommands.send("at^scfg=\"MEopMode/Airplane\",\"" + (mode ? "on" : "off") + "\"");
    }

    /**
     * Setup SYNC pin.
     *
     * @param mode SYNC pin mode
     */
    public static void setSsync(int mode) {
        ATCommands.send("AT^SSYNC=" + mode + "\r");
    }

    /**
     * Restarts the chip.
     */
    public static void restart() {
        if (Logger.BUILD_DEBUG) {
            Logger.log(THIS + ".restart();");
        }
        ATCommands.send("AT+CFUN=1,1");
    }

    /**
     * Trigger an update.
     * The update is performed using the settings "apn" and "jadurl".
     */
    public static void update() {
        update(null, null);
    }

    /**
     * Trigger an update
     *
     * @param target JAD file to use
     */
    public static void update(String target) {
        update(null, target);
    }

    /**
     * Trigger an update
     *
     * @param apn APN to use (using "apn" setting if null)
     * @param target JAD file to use (using "jadurl" setting if null)
     */
    public static void update(String apn, String target) {
        if (apn == null) {
            apn = Settings.get(Settings.SETTING_APN);
        }
        if (target == null) {
            target = Settings.get(Settings.SETTING_JADURL);
        }
        if (Logger.BUILD_DEBUG) {
            Logger.log("ATCommandsWrapper.update( ATCommand, \"" + apn + "\", \"" + target + "\" );", true);
        }

        {// Only useful if we are on a TC65 and not a TC65i (it might be a good time to remove this code)
            String[] spl = Strings.split(",", apn);
            apn = "";
            for (int i = 0; i < spl.length && i < 4; i++) {
                if (i > 0) {
                    apn += ",";
                }

                // We need to remove "" parameters, they are the one
                // that make the AT^SJOTAP command fail
                if (spl[i].compareTo("\"\"") == 0) {
                    continue;
                }

                apn += spl[i];
            }
        }

        // We setup, if possible the correct OTAP parameters
        if (apn != null && target != null) {
            String ret = ATCommands.send("AT^SJOTAP=," + target + ",a:,,," + apn);
            if (Logger.BUILD_DEBUG) {
                Logger.log("ATCommandsWrapper.update: ret1=\"" + ret.replace('\r', '.').
                        replace('\n', '.') + "\"", true);
            }
        } else {
            if (Logger.BUILD_DEBUG) {
                Logger.log("ATCommandsWrapper.update: No APN or no target !");
            }
        }

        // If not, we just trigger the OTAP. Because we could have some
        // AT^SJOTAP pre-defined parameters.
        {
            // We stop the watchdog code
            WatchdogManager.stop();

            String ret = ATCommands.send("AT^SJOTAP");
            if (Logger.BUILD_DEBUG) {
                Logger.log("ATCommandsWrapper.update: ret2=\"" + ret.replace('\r', '.').
                        replace('\n', '.') + "\"", true);
            }
        }
    }

    /**
     * Apply an APN.
     * This is only using the AT^SJNET AT command
     *
     * @param apn APN to apply
     * @return true if the APN was correctly formatted
     */
    public static boolean applyAPN(String apn) {
        try {
            String ret = ATCommands.send("AT^SJNET=" + apn);
            String[] spl = Strings.split('\n', ret);
            ret = spl[1].trim();
            return "OK".equals(ret);
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".applyAPN", ex);
            }
        }
        return false;
    }

    /**
     * Get the network operator's name.
     *
     * @return Network operator's name
     */
    public static String getCopsOperator() {
        try {
            String ret = ATCommands.send("AT+COPS?");
            String[] spl = Strings.split('\n', ret);
            ret = spl[1].trim();
            ret = ret.substring(ret.indexOf('"') + 1, ret.length() - 1);
            return ret;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".getCopsOperator", ex);
            }
        }
        return null;
    }

    /**
     * Get the power input voltage status.
     *
     * @return Power input voltage (mV)
     */
    public static int getVoltage() {
        try {
            String ret = ATCommands.send("AT^SBV");
            String[] spl = Strings.split('\n', ret);
            ret = spl[1].trim().substring("^SBV: ".length());
            return Integer.parseInt(ret);
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".getVoltage", ex, true);
            }
            return -1;
        }
    }

    public static void enableTemp(boolean en) {
        ATCommands.send("AT^SCTM=1," + (en ? "1" : "0"));
    }

    /**
     * Get the temperature.
     *
     * @return Temperature of the chip (-500 in case of error)
     */
    public static int getTemp() {
        String ret = "";
        try {
            ret = ATCommands.send("AT^SCTM?");
            String tab[] = Strings.split('\n', ret.trim());
//            Logger.log("getTemp:312 = " + tab[1]);
            tab = Strings.split(':', tab[1]);
//            Logger.log("getTemp:314 = " + tab[1]);
            tab = Strings.split(',', tab[1]);
//            Logger.log("getTemp:316 = " + tab[2]);
            int value = Integer.parseInt(tab[2]);
            return value;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".tempGet:2/ret=\"" + ret.replace('\n', '.').
                        replace('\r', '.') + "\"", ex, true);
            }
            return -500;
        }
    }

    /**
     * Get our own phone number.
     *
     * It relies on som SIM entries. It is unreliable.
     *
     * @return SIM Number
     */
    public static String getSimNum() {
        try {
            String ret = ATCommands.send("AT+CNUM");
            String[] spl = Strings.split('\n', ret);
            ret = spl[1].trim();
            spl = Strings.split(',', ret);
            ret = spl[1].trim();
            ret = ret.substring(1, ret.length() - 2);
            return ret;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".getSimNum", ex, true);
            }
            return ex.getClass() + ":" + ex.getMessage();
        }
    }

    /**
     * Set the internal real-time clock.
     *
     * @param date Date to define
     */
    public static void setRTClock(Date date) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            String clk = ("" + cal.get(Calendar.YEAR)).substring(2) + "/"
                    + Strings.addZeros("" + (cal.get(Calendar.MONTH) + 1), 2) + "/"
                    + Strings.addZeros("" + cal.get(Calendar.DAY_OF_MONTH), 2) + ","
                    + Strings.addZeros("" + cal.get(Calendar.HOUR_OF_DAY), 2) + ":"
                    + Strings.addZeros("" + cal.get(Calendar.MINUTE), 2) + ":"
                    + Strings.addZeros("" + cal.get(Calendar.SECOND), 2);
            ATCommands.send("AT+CCLK=\"" + clk + "\"");
        } catch (Throwable ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".setRTClock", ex);
            }
        }
    }

    public static void setWatchdogMode(int mode) {
        ATCommands.send("AT^SCFG=\"Userware/Watchdog\",\"" + mode + "\"");
    }

    /**
     * Pin code handling.
     *
     */
    public static class PIN {

        public static boolean pinLock(boolean lock, String code) {
            try {
                String ret = ATCommands.send("AT+CLCK=\"SC\"," + (lock ? "1" : "0") + ",\"" + code + "\"");
                String[] spl = Strings.split('\n', ret);
                ret = spl[1].trim();
                return "OK".equals(ret);
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(THIS + ".pinSet( atc, " + lock + ", \"" + code + "\" );");
                }
                return false;
            }
        }
        /**
         * Pin status: READY
         */
        public static int PINSTATUS_READY = 0;
        /**
         * Pin status: PIN required
         */
        public static int PINSTATUS_SIMPIN = 1;
        /**
         * Pin status: PUK required
         */
        public static int PINSTATUS_SIMPUK = 2;
        /**
         * Pin status: UNKNOWN
         */
        public static int PINSTATUS_UNKNOWN = -1;

        /**
         * Get the current pin status
         *
         * @return Pin status
         */
        public static int pinStatus() {
            try {
//                synchronized (atc) {
                String ret = ATCommands.send("AT+CPIN?");
//				Logger.log("ret = " + ret);
                String[] spl = Strings.split('\n', ret);
                ret = spl[1].trim();
                ret = ret.substring("+CPIN: ".length()).trim();
//				Logger.log("status = " + ret);
                if ("READY".equals(ret)) {
                    return PINSTATUS_READY;
                } else if ("SIM PIN".equals(ret)) {
                    return PINSTATUS_SIMPIN;
                } else if ("SIM PUK".equals(ret)) {
                    return PINSTATUS_SIMPUK;
                } else {
                    return PINSTATUS_UNKNOWN;
                }
//                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(THIS + ".pinStatus( atc )", ex);
                }
                return PINSTATUS_UNKNOWN;
            }
        }

        /**
         * Issue a pin.
         *
         * @param code Pin code to issue
         * @return true if PIN is correct
         */
        public static boolean pinSend(String code) {
            try {
//                synchronized (atc) {
                String ret = ATCommands.send("AT+CPIN=" + code);
//				Logger.log(ret);
                String[] spl = Strings.split('\n', ret);
                ret = spl[1].trim();
                return "OK".equals(ret);
//                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(THIS + ".pinSend( atc, \"" + code + "\" )", ex);
                }
                return false;
            }
        }

        /**
         * Change the PIN code.
         *
         * @param oldPin Old PIN code
         * @param newPin New PIN code
         * @return true if it could be done correctly
         */
        public static boolean pinChange(String oldPin, String newPin) {
            try {
//                synchronized (atc) {
                String ret = ATCommands.send("AT+CPWD=\"SC\",\"" + oldPin + "\",\"" + newPin + "\"");
                String[] spl = Strings.split('\n', ret);
                ret = spl[1].trim();
                return "OK".equals(ret);
//                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(THIS + ".pinChange( atc, \"" + oldPin + "\", \"" + newPin + "\" )", ex);
                }
                return false;
            }
        }

        /**
         * Get the number of PIN code tries left.
         *
         * @return Number of pin code left (-1 in case of error)
         */
        public static int pinNbTriesLeft() {
            try {
//                synchronized (atc) {
                String ret = ATCommands.send("AT^SPIC");
//				Logger.log(ret);
                String[] spl = Strings.split('\n', ret);
                ret = spl[1].trim().substring("^SPIC: ".length());
                return Integer.parseInt(ret);
//                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(THIS + ".pinNbTriesLeft( atc )", ex);
                }
                return -1;
            }
        }
    }
}
