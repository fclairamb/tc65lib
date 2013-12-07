package org.javacint.control.m2mp;

import com.siemens.icm.io.ATCommand;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Timer;
import org.javacint.at.ATExecution;
import org.javacint.common.Bytes;
import org.javacint.console.ConsoleCommand;
import org.javacint.logging.Logger;
import org.javacint.settings.SettingsProvider;

/**
 *
 * @author Florent
 */
public class M2MPTestClient implements ConsoleCommand, SettingsProvider, IProtocolLayerReceive {

	private final M2MPClient rtclient;
//	private final ATCommand atc;

	public M2MPTestClient(final ATCommand atc, final Timer timer) throws Exception {
//		this.atc = atc;
		rtclient = new M2MPClientImpl(atc, timer);
		rtclient.setIdent("imei:" + ATExecution.getImei());
		rtclient.setCapacities("echo,loc,sensor");
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
				} else if (command.equals("send cap")) {
					rtclient.sendCapabilities();
				} else if (command.startsWith("send ")) {
					command = command.substring(5);
					rtclient.sendData(channel, command);
				} else if (command.startsWith("channel ")) {
					channel = command.substring(8);
					out.println("New channel: " + channel);
				} else if (command.startsWith("ping ")) {
					rtclient.sendAckRequest(Byte.parseByte(command.substring(5)));
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

	public void receivedIdentificationResult(boolean identified) {
		if (Logger.BUILD_DEBUG) {
			Logger.log(this + ".receivedIdentificationResult( " + identified + " );");
		}
	}

	public void receivedAckResponse(byte b) {
		if (Logger.BUILD_DEBUG) {
			Logger.log(this + ".receivedAckResponse( " + Bytes.byteToInt(b) + " );");
		}
	}

	public void receivedAckRequest(byte b) {
		if (Logger.BUILD_DEBUG) {
			Logger.log(this + ".receivedAckRequest( " + Bytes.byteToInt(b) + " );");
		}

		rtclient.sendAckResponse(b);
	}

	public void receivedData(String channelName, byte[] data) {
		if (Logger.BUILD_DEBUG) {
			Logger.log(this + ".receivedData( \"" + channelName + "\", " + Bytes.byteArrayToPrettyString(data) + " );");
		}
	}

	public void receivedData(String channelName, byte[][] data) {
		if (Logger.BUILD_DEBUG) {
			Logger.log(this + ".receivedData( \"" + channelName + "\", byte[][" + data.length + "] );");
		}
	}

	public void receivedCommand(String cmdId, String[] argv) {
		if (Logger.BUILD_DEBUG) {
			Logger.log(this + ".receiveCommand( \"" + cmdId + "\", String[" + argv.length + "] );");
		}
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
		if (Logger.BUILD_DEBUG) {
			Logger.log(this + ".disconnected();");
		}
	}
}
