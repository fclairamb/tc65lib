package org.javacint.at;

import com.siemens.icm.io.ATCommand;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;

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
     * Get the IMEI number of the GSM chip
     *
     * @param atc ATCommand used to get it
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
     * @param atc ATCommand
     * @return SIM card IMSI
     */
    public static String getImsi(ATCommand atc) {
        try {
            String tab[] = Strings.split('\n', ATCommands.send("AT+CIMI"));
            String imsi = (tab[1]).trim();

            if (imsi.equals("ERROR")) {
                return null;
            }
            return imsi;
        } catch (Exception ex) {
            System.out.println(THIS + ".getIMSI.19 : ex : " + ex.getClass().
                    toString() + " : " + ex.getMessage());
            return null;
        }
    }

    /**
     * Display the SIM card identification number
     *
     * @param atc ATCommand
     * @return SIM card identification number
     */
    public static String getScid(ATCommand atc) {
        try {
            String tab[] = Strings.split('\n', ATCommands.send("AT^SCID"));
            String scid = (tab[1]).trim().substring(7);
            if (Logger.BUILD_DEBUG) {
                Logger.log("Common.getImsi: \"" + scid + "\"");
            }
            if (scid.equals("ERROR")) {
                return null;
            }
            return scid;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(THIS + ".getScid", ex, true);
            }
            return null;
        }
    }

    /**
     * Restarts the chip
     *
     * @param atc ATCommand to use
     */
    public static void restart() {
        if (Logger.BUILD_DEBUG) {
            Logger.log(THIS + ".restart();");
        }
        ATCommands.send("AT+CFUN=1,1");
    }

    public static void update() {
        update(null, null);
    }

    public static void update(String target) {
        update(null, target);
    }

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

        {// If we are on a TC65 and not a TC65i (might be a good time to remove this code)
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

        // If not, we just trigger the OTAP
        {
            String ret = ATCommands.send("AT^SJOTAP");
            if (Logger.BUILD_DEBUG) {
                Logger.log("ATCommandsWrapper.update: ret2=\"" + ret.replace('\r', '.').
                        replace('\n', '.') + "\"", true);
            }
        }
    }
}
