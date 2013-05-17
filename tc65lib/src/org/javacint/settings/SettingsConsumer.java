package org.javacint.settings;

import java.util.Hashtable;

/**
 * Settings consuming interface.
 *
 * Each settings consumer must implement this interface
 *
 * Note: This should be renamed to SettingsConsumer.
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public interface SettingsConsumer {

	/**
	 * Define the settings (and their default value) the settings consumer class
	 * will require
	 *
	 * @param settings hashtable to modify
	 */
	void getDefaultSettings(Hashtable settings);

	/**
	 * This is received when one the settings has been modified
	 *
	 * @param settings Settings that have been modified
	 */
	void settingsChanged(String[] settings);
}
