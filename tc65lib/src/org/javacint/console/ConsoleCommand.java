package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Interface that defines how to write a console command receiver.
 */
public interface ConsoleCommand {
	
	/**
	 * Executes a command.
	 * @param command Command to execute
	 * @param is Inputstream in case the command receiver wants to take over control.
	 * @param out Output printstream to display some outputs
	 * @return true if the command was found and executed, false otherwise
	 */
	public boolean consoleCommand( String command, InputStream is, PrintStream out );
}
