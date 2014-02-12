package org.javacint.control.basichttp;

import org.javacint.console.Console;
import org.javacint.logging.Logger;

public class ConsoleHttpCommandReceiver implements HttpCommandReceiver {

    private final Console console;

    public ConsoleHttpCommandReceiver(Console console) {
        this.console = console;
    }

    public boolean httpCommand(String command) {
        try {
            return console.parseCommand(command);
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".httpCommand(" + command + ")", ex, true);
            }
            return false;
        }
    }
}
