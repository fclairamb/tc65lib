package org.javacint.logging;

/**
 * Logging class
 *
 * @author Florent Clairambault
 */
public class Logger {

	/**
	 * Debug logging level
	 */
	public static final int E_DEBUG = 5;
	/**
	 * Verbose logging level
	 */
	public static final int E_VERBOSE = 4;
	/**
	 * Notice logging level
	 */
	public static final int E_NOTICE = 3;
	/**
	 * Warning logging level
	 */
	public static final int E_WARNING = 2;
	/**
	 * Critical logging level
	 */
	public static final int E_CRITICAL = 1;
	/**
	 * Enable stdout logging at build time
	 */
	public static final boolean BUILD_LOG_ON_STDOUT = true;
	/**
	 * Enable listener logging at build time
	 */
	public static final boolean BUILD_LOG_ON_LISTENER = true;
	public static final boolean BUILD_LOG_TIME = true;
	/**
	 * Build logging level
	 */
	public static final int buildLoggingLevel =
			//#if DebugLevel=="debug"
			E_DEBUG;
//#elif	DebugLevel=="warn"		
//# E_NOTICE;
//#endif
	/**
	 * Shows everything
	 */
	public static final boolean BUILD_DEBUG = (buildLoggingLevel >= E_DEBUG);
	/**
	 * Shows verbose logging
	 */
	public static final boolean BUILD_VERBOSE = (buildLoggingLevel >= E_VERBOSE);
	/**
	 * Shows notice logging
	 */
	public static final boolean BUILD_NOTICE = (buildLoggingLevel >= E_NOTICE);
	/**
	 * Shows warning logging
	 */
	public static final boolean BUILD_WARNING = (buildLoggingLevel >= E_WARNING);
	/**
	 * Shows critical logging
	 */
	public static final boolean BUILD_CRITICAL = (buildLoggingLevel >= E_CRITICAL);
	/**
	 * Logging receiver
	 */
	private static LoggingReceiver receiver;
	/**
	 * Stdout logging
	 */
	private static boolean stdoutLogging = true;

	/**
	 * Define a logging receiver (of important message to transmit to high
	 * autorities)
	 *
	 * @param r Logging receiver
	 */
	public static void setLoggingReceiver(LoggingReceiver r) {
		receiver = r;
	}

	/**
	 * Active or desactivate stdout logging
	 *
	 * @param activate Stdout logging activation
	 */
	public static void setStdoutLogging(boolean activate) {
		stdoutLogging = activate;
	}

	/**
	 * Logs anything
	 *
	 * @param str String sent to logging
	 */
	public static void log(String str) {
		log(str, false);
	}
	private static long lastTime = System.currentTimeMillis();

	/**
	 * Logs anything (with external receiver support)
	 *
	 * @param str String sent to logging
	 * @param report If we report this to a high authority
	 */
	public static void log(String str, boolean report) {
		if (!BUILD_LOG_ON_STDOUT && !BUILD_LOG_ON_LISTENER) {
			return;
		}
		String content = Thread.currentThread().
				getName() + ":";

		if (BUILD_LOG_TIME && report) {
			long t = System.currentTimeMillis();
			long diff = (t - lastTime);
			content += diff + ":";
			lastTime = t;
		}

		content += str;

		if (BUILD_LOG_ON_STDOUT && stdoutLogging) {
			synchronized (System.out) {
				System.out.println(content);
			}
		}
		if (BUILD_LOG_ON_LISTENER && report && receiver != null) {
			receiver.log(content);
		}
	}

	/**
	 * Logs exception
	 *
	 * @param str String sent to logging
	 * @param th Exception caught
	 */
	public static void log(String str, Throwable th) {
		log(str, th, false);
	}

	public static void log(String str, Throwable th, boolean report) {
		log(str + " ex : " + th.getClass() + " : " + th.
				getMessage(), report);

		// If we have an exception thrown in loop, we prefer that it doesn't slows down
		// the other threads
		Thread.yield();
	}
}
