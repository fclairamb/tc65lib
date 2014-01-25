package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;

public class VersionCommand implements ConsoleCommand {

    private final String version;

    public VersionCommand(String version) {
        this.version = version;
    }

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.equals("version")) {
            out.println("[VERSION] " + version);
            return true;
        } else if (command.equals("help")) {
            out.println("[HELP] version                          - Show the current version");
        }
        return false;
    }
}
