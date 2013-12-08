package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.gps.GpsManager;
import org.javacint.gps.GpsPosition;
import org.javacint.gps.GpsPositionListener;

/**
 * GPS Testing command.
 *
 * This command is only intended for testing.
 */
public class GPSTestCommand implements ConsoleCommand, GpsPositionListener {

    private final GpsManager gps;
    private PrintStream out;

    public GPSTestCommand() {
        this.gps = new GpsManager(this);
    }

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.startsWith("gps ")) {
            command = command.substring(4);
            if ("start".equals(command)) {
                gps.start();
                this.out = out;
                return true;
            } else if ("stop".equals(command)) {
                gps.stop();
                return true;
            } else {
                out.println("[HELP] gps [start|stop]");
            }
        } else if (command.equals("help")) {
            out.println("[HELP] gps [start|stop]");
        }
        return false;
    }

    public void positionReceived(GpsPosition pos) {
        if (out != null) {
            out.println("[GPS POS] " + pos);
        }
    }

    public void positionAdditionnalReceived(String type, String value) {
        if (out != null) {
            out.println("[GPS LOG] " + type + " : " + value);
        }
    }
}
