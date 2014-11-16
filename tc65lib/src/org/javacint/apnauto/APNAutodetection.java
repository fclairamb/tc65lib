package org.javacint.apnauto;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#elif sdkns == "gemalto"
//#endif
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import org.javacint.at.ATExecution;
import org.javacint.common.ResourceProvider;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.sms.SimpleSMS;

/**
 * APN Autodetection class.
 *
 * This class uses the Android APN list rewritten in an other (more compact)
 * format.
 *
 * The core method is autoLoadRightGPRSSettings
 *
 */
public final class APNAutodetection {

    private final Vector sources = new Vector();
    public static final boolean LOG = false;
    private boolean showApn;

    /**
     * Show the APN.
     *
     * Show the APN during APN auto-detection.
     *
     * @param show Show APN.
     */
    public void setShowApn(boolean show) {
        this.showApn = show;
    }

    public void addParametersSource(ResourceProvider source) {
        sources.addElement(source);
    }

    public void addDefaultParameters() {
        try {
            Vector names = getSettingsListNames();

            if (names == null) {
                Logger.log("We couldn't find some settings names. Probably an issue with SIM card!");
                return;
            }

            sources.addElement( // My list of APNS
                    new GPRSParametersResourceReader(
                    new ResourceProvider(GPRSParametersResourceReader.class, "/apns.txt"),
                    names));

            sources.addElement( // Android's list of APNS
                    new GPRSParametersResourceReader(
                    new ResourceProvider(GPRSParametersResourceReader.class, "/android.txt"),
                    names));
        } catch (IOException ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".addDefaultParameters", ex, true);
            }
        }
    }

    private Vector getSettingsListNames() {
        try {
            String mcc, mnc;
            {
                String[] monc = ATExecution.getMONC();
                mcc = monc[0];
                mnc = monc[1];
            }
            String operator = ATExecution.getCopsOperator().
                    trim().
                    toLowerCase().
                    replace(' ', '_');
            if (Logger.BUILD_DEBUG && LOG) {
                Logger.log("Operator: " + operator);
                Logger.log("MCC: " + mcc);
                Logger.log("MNC: " + mnc);
            }


            Vector names = new Vector();
            // These are the different types of configuration that might have been defined
            names.addElement(mcc + "-" + operator); // covers "MVNO"
            names.addElement(mcc + "-" + mnc); // This is the most important one
            names.addElement(mcc); // This is the per-country one
            return names;
        } catch (Exception ex) {
            Logger.log(this + ".getSettingsListNames", ex);
            return null;
        }
    }

    /**
     * Automatically load the right APN setting.
     * This method automatically loads the right APN settings for the modem.
     *
     * @return The detect APN setting
     */
    public String autoLoadRightGPRSSettings() {
        String apn = null;
        try { // Let's detect the IMSI
            String simDetected, simSaved;

            { // We check the SIM card pin status
                int pinStatus = ATExecution.PIN.pinStatus();
                if (pinStatus != ATExecution.PIN.PINSTATUS_READY) {
                    Logger.log("SIM PIN is not READY ! / status = " + pinStatus);
                    return apn;
                }
            }

            simDetected = ATExecution.getIccid();
            simSaved = Settings.get(Settings.SETTING_ICCID);

            if (simDetected == null) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".AutoAPN: No sim card detected !");
                }
            } else if (!simDetected.equals(simSaved)) {
                if (Logger.BUILD_WARNING) {
                    Logger.log(this + ".AutoAPN: Sim card changed since last successful APN detection ! saved=" + simSaved + ", detected=" + simDetected);
                }

                { // First we try if we have any chance with the current APN (maybe it was defined in the console or by SMS)
                    String apnSetting = Settings.get(Settings.SETTING_APN);
                    if (apnSetting != null) {
                        if (Logger.BUILD_DEBUG) {
                            Logger.log(this + ".AutoAPN:  Trying current APN setting...");
                        }
                        if (testApn(apnSetting, GPRSSettings.DEFAULT_TARGET)) {
                            apn = apnSetting;
                            if (Logger.BUILD_DEBUG) {
                                Logger.log(this + ".AutoAPN: Current APN setting is ok!");
                            }
                        }
                    }

                    int size = sources.size();
                    if (sources.isEmpty()) {
                        Logger.log(this + ".AutoAPN: No sources specified !");
                    }
                    for (int i = 0; i < size && apn == null; i++) {
                        GPRSParametersProvider provider = (GPRSParametersProvider) sources.elementAt(i);
                        if (Logger.BUILD_DEBUG) {
                            Logger.log(this + ".AutoAPN: Trying APN parameters provider " + provider + " (" + (i + 1) + "/" + size + ")");
                        }
                        GPRSSettings set = testGPRSParameters(provider);
                        apn = set != null ? set.toSjnet() : null;
                    }
                }

                String phoneManager = Settings.get(Settings.SETTING_MANAGERSPHONE);

                if (apn != null) {
                    if (showApn) {
                        Logger.log(this + ".AutoAPN: New APN is : " + apn);
                    }

                    // We only save the ICCID if we found a good APN
                    // This renew APN detection until we have the right APN detected
                    // The main drawback is : We will detect APN at each startup
                    // even if network is working fine
                    Settings.set(Settings.SETTING_ICCID, simDetected);
                    Settings.set(Settings.SETTING_APN, apn);
                    Settings.save();

                    if (phoneManager != null) {
                        String imei = ATExecution.getImei();
                        SimpleSMS.send(phoneManager, "New detection!\nIMEI:" + imei + "\nICCID:" + simDetected + "\nAPN:" + apn + "\n");
                    }
                } else {
                    if (phoneManager != null) {
                        String mcc, mnc;
                        {
                            String[] monc = ATExecution.getMONC();
                            mcc = monc[0];
                            mnc = monc[1];
                        }
                        String imei = ATExecution.getImei();
                        SimpleSMS.send(phoneManager, "APN NOT FOUND!\nIMEI:" + imei + "\nICCID:" + simDetected + "\nOperator: " + ATExecution.getCopsOperator() + " (" + mcc + "-" + mnc + ")");
                    }
                }
            } else {
                apn = Settings.get(Settings.SETTING_APN);
                if (apn != null) {
                    if (Logger.BUILD_VERBOSE) {
                        Logger.log(this + ".AutoAPN: APN used: " + apn);
                    }
                    ATExecution.applyAPN(apn);
                }
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".autoLoadRightGPRSSettings", ex);
            }
        }
        return apn;
    }

    /**
     * Test all the APN settings of a GPRS parameters providers.
     *
     * @param reader Settings provider
     * @return The working APN setting or null
     * @throws ATCommandFailedException
     * @throws IOException
     */
    private GPRSSettings testGPRSParameters(GPRSParametersProvider reader) throws ATCommandFailedException, IOException {
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(this + ".testGPRSSettingsReader:" + reader);
        }

        for (GPRSSettings set; (set = reader.next()) != null;) {
            if (testApn(set)) {
                if (showApn) {
                    Logger.log("APN " + set + " : OK");
                }
                return set;
            } else {
                if (showApn) {
                    Logger.log("APN " + set + " : FAIL");
                }
            }
        }
        return null;
    }

    /**
     * Test an APN setting
     *
     * @param set Setting to test
     * @return If it worked
     * @throws ATCommandFailedException In case of AT failure
     */
    private boolean testApn(GPRSSettings set) throws ATCommandFailedException {
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(this + ".testApn: Testing " + set);
        }
        return testApn(set.toSjnet(), set.getTarget());
    }

    private boolean testApn(String apn, String target) throws ATCommandFailedException {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".testApn( \"" + apn + "\", \"" + target + "\" );");
        }
        ATExecution.applyAPN(apn);

        try {
            SocketConnection conn = (SocketConnection) Connector.open("socket://" + target);
            conn.close();
            if (Logger.BUILD_DEBUG) {
                Logger.log("Connected ok !");
            }
            return true;
        } catch (Exception ex) {
            // This is the standard message for APN detection failure, let's skip it
            if (ex instanceof IOException && ex.getMessage().equals("Profile could not be activated")) {
                return false;
            }
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".testApn: " + ex);
            }
        }
        return false;
    }

    public void applySettingsApn() {
        ATExecution.applyAPN(Settings.get(Settings.SETTING_APN));
    }

    public String toString() {
        return "APNAutodetection";
    }
}
