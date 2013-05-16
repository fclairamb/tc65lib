package org.javacint.logging;

/**
 * Logging receiver
 */
public interface LoggingReceiver {

	/**
	 * Logging string received
	 *
	 * @param str Logging string
	 */
	public void log(String str);
}
