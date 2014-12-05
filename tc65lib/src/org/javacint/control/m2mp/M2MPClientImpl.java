package org.javacint.control.m2mp;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.javacint.at.ATCommandPooled;
import org.javacint.at.ATCommands;
import org.javacint.at.ATExecution;
import org.javacint.common.Bytes;
import org.javacint.common.Vectors;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.settings.SettingsProvider;
import org.javacint.sms.SimpleSMS;

/**
 * The main application layer.
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class M2MPClientImpl implements M2MPClient, SettingsProvider {

    protected final Hashtable statuses = new Hashtable();
    protected final ProtocolLayer protSend;
    protected IProtocolLayerReceive protRecv;
    public static boolean m2mpLog_;
    static final String SETTING_M2MP_LOG = "m2mp.log";
    static final String SETTING_M2MP_SERVERS = "m2mp.servers";
    static final String SETTING_M2MP_KEEPALIVE = "m2mp.keepalive";
    private static final String CHANNEL_SETTING = "_set";
    private static final String CHANNEL_STATUS = "_sta";
    private static final String CHANNEL_COMMAND = "_cmd";
    private static final String STATUS_CAPABILITIES = "cap";

    /**
     * Constructor
     */
    public M2MPClientImpl() {
        if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".ctor();");
        }

        protSend = new ProtocolLayer();
        protSend.setAppLayer(this);

        Settings.addProvider(this);
    }

    /**
     * Define the identifier sent to the server
     *
     * @param ident Identifier that will be sent to the server
     */
    public void setIdent(String ident) {
        protSend.setIdent(ident);
    }

    /**
     * Get the current identifier sent to the server
     *
     * @return Identifier that will be sent to the server
     */
    public String getIdent() {
        return protSend.getIdent();
    }

    /**
     * Get the capacities of the current equipment
     *
     * @param capacities Capacities of the equipment
     *
     * Capacities could be defined like this :<br>
     * gpioState,chat,sms,batteryState
     */
    public void setCapabilities(String capacities) {
        statuses.put(STATUS_CAPABILITIES, capacities);
    }

    /**
     * Get the capacities of the equipment
     *
     * @return Capacities of the equipment
     */
    public String getCapabilities() {
        return (String) statuses.get(STATUS_CAPABILITIES);
    }

    /**
     * Get the sending protocol layer
     *
     * @return The ClientSend class instance
     */
    public ProtocolLayer getSendingProtocolLayer() {
        return protSend;
    }

    /**
     * Add an INetAppLayer receiver
     *
     * @param listener Listener
     */
    public void setListener(IProtocolLayerReceive listener) {
        protRecv = listener;
    }

    void onReceivedData(String channelName, byte[] data) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".receiveData( \"" + channelName + "\", " + Bytes.byteArrayToPrettyString(data) + " );");
        } else if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".receiveData( \"" + channelName + "\", byte[" + data.length + "] );");
        }

        if (protRecv != null) {
            protRecv.receivedData(channelName, data);
        }
    }

    void onReceivedData(String channelName, byte[][] data) {
        try {
            if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                Logger.log("AppLayer.receivedData( \"" + channelName + "\", byte[" + data.length + "][] );");
            }

            if (channelName.compareTo(CHANNEL_SETTING) == 0) {
                treatMsgSetting(data);
            } else if (channelName.compareTo(CHANNEL_COMMAND) == 0) {
                treatMsgCommand(data);
            } else if (channelName.compareTo(CHANNEL_STATUS) == 0) {
                treatMsgStatus(data);
            } else if (protRecv != null) {
                protRecv.receivedData(channelName, data);
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("AppLayer.receivedData( \"" + channelName + "\" )", ex);
            }
        }
    }

    private void treatMsgStatus(byte[][] data) {
        String cmd = new String(data[0]);
        if (cmd.equals("g")) {
            String statusName = new String(data[1]);
            Vector response = new Vector();
            response.addElement("g");
            String value = (String) statuses.get(statusName);
            if (value == null) {
                value = "";
            }
            response.addElement(statusName + "=" + value);
            protSend.sendData(CHANNEL_STATUS, response);
        }
    }

    private void treatMsgSetting(byte[][] data) {
//        Settings settings = Settings.getInstance();
        String cmd = new String(data[0]);

        if (cmd.compareTo("ga") == 0) {
            treatMsgSettingGetAllSettings();
        }

        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("Setting: cmd = \"" + cmd + "\"");
        }

        boolean cmdSet = (cmd.compareTo("s") == 0 || cmd.compareTo("sg") == 0);
        boolean cmdGet = (cmd.compareTo("g") == 0 || cmd.compareTo("sg") == 0);

        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("Setting: cmdSet = " + cmdSet + " ; cmdGet = " + cmdGet);
        }

        Vector response = null;

        if (cmdGet) {
            response = new Vector();
            response.addElement("g");
        }
        Vector settingsChanged = new Vector();
        Hashtable defaultSettings = Settings.getDefaultSettings();

        for (int i = 1; i < data.length; i++) {
            String str = new String(data[i]);

            if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
                Logger.log("- " + str);
            }

            int p = str.indexOf("=");
            String var, val = null;

            if (p != -1) {
                var = str.substring(0, p);
                val = str.substring(p + 1);
            } else {
                var = str;
            }

            if (cmdSet) {
                if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                    Logger.log("Setting parameter \"" + var + "\" to \"" + val + "\".");
                }
                Settings.setWithoutEvent(var, val);
                parseSetting(var); // We still need to apply them locally
                settingsChanged.addElement(var);
            }


            String defaultValue = (String) defaultSettings.get(var);
            if (defaultValue == null) {
                if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
                    Logger.log("Unknown setting \"" + var + "\" !");
                }
                Vector unknownResponse = new Vector();
                unknownResponse.addElement("u");
                unknownResponse.addElement(var);
                protSend.sendData(CHANNEL_SETTING, unknownResponse);
            } else if (cmdGet) {
                String finalValue = Settings.get(var);
                if (finalValue == null) {
                    response.addElement(var);
                } else {
                    response.addElement(var + "=" + finalValue);
                }
            }
        }
        Settings.save();

        // We apply changed settings everywhere but here
        Settings.onSettingsChanged(Vectors.toStringArray(settingsChanged), this);

        // We send the reply if needed
        if (response != null) {
            protSend.sendData(CHANNEL_SETTING, response);
        }
    }

    protected void reportSettingChange(String name, String value) {
        protSend.sendData(CHANNEL_SETTING, new String[]{"g", name + "=" + value});
    }

    /**
     * Starts the inner threads (sending & reception) that will launch the
     * connection
     *
     * @throws java.lang.Exception
     *
     * Exceptions can be set if the ident or the capacities of the M2MP client
     * haven't been set
     */
    public void start() throws Exception {
        parseSetting(SETTING_M2MP_LOG);
        parseSetting(SETTING_M2MP_SERVERS);
        parseSetting(SETTING_M2MP_KEEPALIVE);
        if (getIdent() == null) {
            throw new Exception("Ident must be defined !");
        }

        if (statuses.get(STATUS_CAPABILITIES) == null) {
            throw new Exception("Capacities must be defined !");
        }

        protSend.start();
    }

    /**
     * Stop the connection and kill the inner threads (sending & reception)
     */
    public void stop() {
        protSend.stop();
    }

    /**
     * Get the last time we've received something from the server
     *
     * @return Last time (in UNIX time) we've received something from the server
     */
    public long getLastRecvTime() {
        return protSend.getLastRecvTime();
    }

    /**
     * Get the last time we've sent something to the server
     *
     * @return Last time (in UNIX time) we've sent something to the server
     */
    public long getLastSendTime() {
        return protSend.getLastSendTime();
    }

    void onReceivedAckRequest(byte b) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("AppLayer.ReceivedAcknowledgeRequest( " + (int) b + " );");
        }
        protSend.sendAckResponse(b);

        // For-each loop not supported in this java version, I should check if
        // we can imcrease the java source version number

        // This method isn't ideal because their could be one listener for more than one channel
//		for ( Enumeration e = _listeners.elements(); e.hasMoreElements();) {
//			IClientReceive listener = (IClientReceive) e.nextElement();
//			listener.receivedAckRequest( b );
//		}

        if (protRecv != null) {
            protRecv.receivedAckRequest(b);
        }
    }

    void onReceivedAckResponse(byte b) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log("AppLayer.ReceivedAckResponse( " + (int) b + " );");
        }

//		for ( Enumeration e = _listeners.elements(); e.hasMoreElements();) {
//			IClientReceive listener = (IClientReceive) e.nextElement();
//			listener.receivedAckResponse( b );
//		}
        if (protRecv != null) {
            protRecv.receivedAckResponse(b);
        }
    }

    void onDisconnected() {
        if (protRecv != null) {
            protRecv.disconnected();
        }
    }

    void onReceivedIdentificationResult(boolean identified) {
        if (Logger.BUILD_VERBOSE && M2MPClientImpl.m2mpLog_) {
            Logger.log("AppLayer.ReceivedIdentificationResult( " + identified + " );");
        }

//		for ( Enumeration e = _listeners.elements(); e.hasMoreElements();) {
//			IClientReceive listener = (IClientReceive) e.nextElement();
//			listener.receivedIdentificationResult( identified );
//		}
        if (protRecv != null) {
            protRecv.receivedIdentificationResult(identified);
        }
    }

    public void getDefaultSettings(Hashtable settings) {
        settings.put(SETTING_M2MP_LOG, "0");
        settings.put(SETTING_M2MP_SERVERS, "188.165.213.210:3000,188.165.213.210:3010");
        settings.put(SETTING_M2MP_KEEPALIVE, "1200");
    }

    protected void parseSetting(String settingName) {
        if (settingName.equals(SETTING_M2MP_LOG)) {
            M2MPClientImpl.m2mpLog_ = Settings.getBool(SETTING_M2MP_LOG);
        } else if (settingName.equals(SETTING_M2MP_KEEPALIVE)) {
            protSend.setKeepAlive(Settings.getInt(SETTING_M2MP_KEEPALIVE));
        }
    }

    public void settingsChanged(String[] chg) {
//        Settings settings = Settings.getInstance();
        for (int i = 0; i < chg.length; i++) {
            String settingName = chg[i];
            parseSetting(settingName);
            {
                String value = Settings.get(settingName);
                reportSettingChange(settingName, value);
            }

            if (settingName.equals(SETTING_M2MP_SERVERS)) {
                disconnect();
            }
        }
    }

    public void sendData(String channelName, byte[] data) {
        protSend.sendData(channelName, data);
    }

    public void sendData(String channelName, String data) {
        protSend.sendData(channelName, data);
    }

    public void sendData(byte channelId, byte[] data) {
        protSend.sendData(channelId, data);
    }

    public void sendData(String channelName, byte[][] data) {
        protSend.sendData(channelName, data);
    }

    public void sendData(String channelName, String[] data) {
        protSend.sendData(channelName, data);
    }

    public void sendData(String channelName, Vector data) {
        protSend.sendData(channelName, data);
    }

    public void sendData(byte channelId, byte[][] data) {
        protSend.sendData(channelId, data);
    }

    public void sendCapabilities() {
        sendData(CHANNEL_STATUS, (String) statuses.get(STATUS_CAPABILITIES));
    }

    public void disconnect() {
        protSend.disconnect();
    }

    public void sendAckResponse(byte b) {
        protSend.sendAckResponse(b);
    }

    public void sendAckRequest(byte b) {
        protSend.sendAckRequest(b);
    }

    public void setStatus(String name, String value) {
        statuses.put(name, value);
    }

    private void treatMsgCommand(byte[][] data) {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".treatMsgCommand();");
        }

        if (data.length >= 3) {
            String type = new String(data[0]);
            if (Logger.BUILD_DEBUG) {
                Logger.log("type = " + type);
            }
            if ("e".equals(type)) {
                String cmdId = new String(data[1]);
                String[] argv = new String[(data.length - 2)];
                for (int i = 0; i < argv.length; i++) {
                    argv[i] = new String(data[(i + 2)]);
                    if (Logger.BUILD_DEBUG) {
                        Logger.log("argv[" + i + "] = \"" + argv[i] + "\";");
                    }
                }

                // First we acknowledge the command
                acknowledgeCommand(cmdId);

                // Then we try to execute it in the library directly
                if (!executeCommand(cmdId, argv)) {

                    // And if we can't we give it to the listener
                    if (protRecv != null) {
                        protRecv.receivedCommand(cmdId, argv);
                    }
                }
            }
        }
    }

    public void acknowledgeCommand(String cmdId) {
        protSend.sendData(CHANNEL_COMMAND, new String[]{"a", cmdId});
    }

    private boolean executeCommand(String cmdId, String[] argv) {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".executeCommand( \"" + cmdId + "\", \"" + argv[0] + "\", ... );");
        }
        try {
            if ("update".equals(argv[0])) {
                if (argv.length > 1) {
                    String jadUrl = argv[1];
                    if (jadUrl.startsWith("http://")) {
                        ATExecution.update(jadUrl);
                    }
                } else {
                    ATExecution.update();
                }
            } else if ("restart".equals(argv[0])) {
                ATExecution.restart();
            } else if ("atc".equals(argv[0])) {
                if (argv.length > 1) {
                    protSend.sendData("sen:atc", ATCommands.send(argv[1]));
                }
            } else if ("cellid".equals(argv[0])) {
                StringBuffer sb = new StringBuffer();

//                CellInformation[] cells = new CellInformationRetriever(atc).getCells();
//                for (int i = 0; i < cells.length; i++) {
//                    if (i > 0) {
//                        sb.append(";");
//                    }
//                    CellInformation cell = cells[i];
//                    sb.append(cell.mcc).append(",").append(cell.mnc).append(",").append(cell.lac).append(",").append(cell.cell);
//                }

                protSend.sendData("sen:cellid", sb.toString());

            } else if ("settings".equals(argv[0])) {
                treatMsgSettingGetAllSettings();
            } else if ("send_sms_pdu".equals(argv[0])) {
                String pdu = argv[1].trim();
                ATCommandPooled atc = ATCommands.getATCommand();
                try {
                    atc.send("AT+CMGF=0\n");
                    atc.send("AT+CMGS=" + (pdu.length() / 2 - 1) + "\r");
                    String response = atc.send(pdu + "\032\r");
                    sendData("sen:atc", response);
                } finally {
                    atc.release();
                }
            } else if ("send_sms".equals(argv[0])) {
                SimpleSMS.send(argv[1], argv[2]);
            } else if ("get_simnb".equals(argv[0])) {
                sendData("sta:simnb", ATExecution.getSimNum());
            } else {
                return false;
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".executeCommand", ex, true);
            }
        }
        return true;
    }

    private void treatMsgSettingGetAllSettings() {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".treatMsgSettingGetAllSettings();", true);
        }
        Enumeration keys = Settings.names();

        Vector settingsMessage = new Vector();

        settingsMessage.addElement("g");

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String content = key + "=" + Settings.get(key);
            settingsMessage.addElement(content);
            if (Logger.BUILD_DEBUG) {
                Logger.log("content: " + content);
            }
        }

        protSend.sendData(CHANNEL_SETTING, settingsMessage);
    }
}
