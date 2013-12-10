package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.io.Connections;
import org.javacint.logging.Logger;

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
            out.println(CMD_CLIENT + " <ip>:<port>");
        }
        return false;
    }
}
