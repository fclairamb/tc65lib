package org.javacint.control.m2mp;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import org.javacint.at.ATExecution;
import org.javacint.common.Bytes;
import org.javacint.console.ConsoleCommand;
import org.javacint.control.m2mp.data.AcknowledgeRequest;
import org.javacint.control.m2mp.data.AcknowledgeResponse;
import org.javacint.control.m2mp.data.Event;
import org.javacint.control.m2mp.data.NamedData;
import org.javacint.logging.Logger;
import org.javacint.settings.SettingsProvider;

public class M2MPTestClient implements ConsoleCommand, SettingsProvider, M2MPEventsListener {

    private final M2MPClient rtclient;
//	private final ATCommand atc;

    public M2MPTestClient() throws Exception {
//		this.atc = atc;
        rtclient = new M2MPClientImpl();
        rtclient.setIdent("imei:" + ATExecution.getImei());
        rtclient.setCapabilities("echo,loc,sensor");
        rtclient.setListener(this);
    }
    private String channel = "echo:default";

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        try {
            if (command.startsWith("m2 ")) {
                command = command.substring(3);

                if (command.equals("start")) {
                    rtclient.start();
                } else if (command.equals("stop")) {
                    rtclient.stop();
                } else if (command.startsWith("send ")) {
                    command = command.substring(5);
                    rtclient.send(new NamedData(channel, command));
                } else if (command.startsWith("channel ")) {
                    channel = command.substring(8);
                    out.println("New channel: " + channel);
                } else if (command.startsWith("ping ")) {
                    rtclient.send(new AcknowledgeRequest(Byte.parseByte(command.substring(5))));
                } else {
                    return false;
                }

                return true;
            }
            return false;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".consoleCommand", ex, true);
            }
            return true;
        }
    }

    public void getDefaultSettings(Hashtable settings) {
    }

    public void settingsChanged(String[] settings) {
    }

    public String toString() {
        return "M2MPTestClient";
    }

    public M2MPClient getRtClient() {
        return rtclient;
    }

    public void start() throws Exception {
        rtclient.start();
    }

    public void disconnected() {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".disconnected();");
        }
    }

    public void m2mpEvent(Event event) {
        if (event instanceof AcknowledgeRequest) {
            receivedAckRequest(((AcknowledgeRequest) event).nb);
        }
    }

    public void receivedAckRequest(byte b) {
        if (Logger.BUILD_DEBUG && M2MPClientImpl.m2mpLog_) {
            Logger.log(this + ".receivedAckRequest( " + Bytes.byteToInt(b) + " );");
        }

        rtclient.send(new AcknowledgeResponse(b));
    }
}
