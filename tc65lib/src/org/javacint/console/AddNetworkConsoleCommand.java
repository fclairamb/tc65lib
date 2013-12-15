package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.io.Connections;
import org.javacint.logging.Logger;

/**
 * Add a new console on a remote server connection.
 * It's only made available to show how we can create new consoles.
 */
public class AddNetworkConsoleCommand implements ConsoleCommand {

    private static final String CMD_CLIENT = "console net client ";
    private final Console parent;

    public AddNetworkConsoleCommand(Console parent) {
        this.parent = parent;
    }

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.startsWith(CMD_CLIENT)) {
            try {
                String host = command.substring(CMD_CLIENT.length()).trim();
                Console console = new Console(Connections.tcp(host));
                console.copyCommandReceivers(parent);
                console.start();
                return true;
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".consoleCommand", ex, true);
                }
            }
        } else if (command.equals("help")) {
            out.println("[HELP] " + CMD_CLIENT + " <ip>:<port>");
        }
        return false;
    }
}
