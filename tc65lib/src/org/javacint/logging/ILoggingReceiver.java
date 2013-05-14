package org.javacint.logging;

/**
 * Logging receiver
 * @author Florent Clairambault / www.webingenia.com
 */
public interface ILoggingReceiver {
	/**
	 * Logging string received
	 * @param str Logging string
	 */
	public void log( String str );
}
