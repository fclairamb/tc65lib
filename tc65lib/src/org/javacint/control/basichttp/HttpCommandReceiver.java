package org.javacint.control.basichttp;

public interface HttpCommandReceiver {

    /**
     * Execute an HTTP command.
     * @param command Command to execute
     * @return <strong>true</strong> if it could be executed, <strong>false</strong> otherwise
     */
    boolean httpCommand(String command);
}
