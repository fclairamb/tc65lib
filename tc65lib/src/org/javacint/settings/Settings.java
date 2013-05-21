package org.javacint.settings;

import com.siemens.icm.io.file.FileConnection;
import java.io.*;
import java.util.*;
import javax.microedition.io.Connector;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;

/**
 * Settings management class
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class Settings {

    private static Hashtable settings;
    private static String fileName = "settings.txt";
    private static final Vector consumers = new Vector();
    /**
     * APN setting
     */
    public static final String SETTING_APN = "apn";
    public static final String SETTING_CODE = "code";
    public static final String SETTING_MANAGERSPHONE = "phoneManager";
    public static final String SETTING_IMSI = "imsi";
    public static final String SETTING_PINCODE = "pincode";
    /**
     * JADUrl setting
     */
    public static final String SETTING_JADURL = "jadurl";
    private static boolean madeSomeChanges = false;
    private static boolean loading;

    public static synchronized void setFilename(String filename) {
        fileName = filename;
        settings = null;
    }

    public static void loading(boolean l) {
        loading = l;
    }

    public static String getFilename() {
        return fileName;
    }

    /**
     * Load settings
     */
    public static synchronized void load() {
        if (Logger.BUILD_DEBUG) {
            Logger.log("Settings.load();");
        }

        StringBuffer buffer = new StringBuffer();
        Hashtable newSettings = getDefaultSettings();
        try {


            FileConnection fc = (FileConnection) Connector.open("file:///a:/" + fileName, Connector.READ);

            if (!fc.exists()) {
                if (Logger.BUILD_WARNING) {
                    Logger.log("Settings.load: File \"" + fileName + "\" doesn\'t exist!");
                }

                fc = (FileConnection) Connector.open("file:///a:/" + fileName + ".old", Connector.READ);
                if (fc.exists()) {
                    if (Logger.BUILD_WARNING) {
                        Logger.log("Settings.load: But \"" + fileName + ".old\" exists ! ");
                    }
                } else {
                    return;
                }
            }

            InputStream is = fc.openInputStream();

            while (is.available() > 0) {
                int c = is.read();

                if (c == '\n') {
                    loadLine(newSettings, buffer.toString());
                    buffer.setLength(0);
                } else {
                    buffer.append((char) c);
                }
            }
            is.close();
            fc.close();

        } catch (IOException ex) {
            // The exception we shoud have is at first launch : 
            // There shouldn't be any file to read from

            if (Logger.BUILD_CRITICAL) {
                Logger.log("Settings.Load", ex);
            }
        } finally {
            settings = newSettings;
        }
    }

    /**
     * Treat each line of the file
     *
     * @param def Default settings
     * @param line Line to parse
     */
    private static void loadLine(Hashtable settings, String line) {
//		if (Logger.BUILD_VERBOSE) {
//			Logger.log("loadTreatLine( [...], \"" + line + "\" );");
//		}
        String[] spl = Strings.split('=', line);
        String key = spl[0];
        String value = spl[1];

        // If default settings hashTable contains this key
        // we can use this value
        if (settings.containsKey(key)) {
            settings.remove(key);
            settings.put(key, value);
//			if (Logger.BUILD_DEBUG) {
//				Logger.log("Settings.loadLine: " + key + "=" + value);
//			}
        }

    }

    public static void onSettingsChanged(String[] names) {
        onSettingsChanged(names, null);
    }

    /**
     * Launch an event when some settings
     *
     * @param names Names of the settings
     */
    public static void onSettingsChanged(String[] names, SettingsProvider caller) {
//		if (Logger.BUILD_DEBUG) {
//			Logger.log("Settings.onSettingsChanged( String[" + names.length + "] names );");
//		}
        try {
            synchronized (consumers) {
                for (Enumeration en = consumers.elements(); en.hasMoreElements();) {
                    SettingsProvider cons = (SettingsProvider) en.nextElement();

                    if (cons == caller) {
                        continue;
                    }

                    cons.settingsChanged(names);
                }
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("Settings.OnSeettingChanged", ex);
            }
        }
    }

    /**
     * Get default settings
     *
     * @return Default settings Hashtable
     */
    public static Hashtable getDefaultSettings() {

        Hashtable defaultSettings = new Hashtable();

        // Code is mandatory
        defaultSettings.put(SETTING_CODE, "1234");

        // APN is mandatory
        defaultSettings.put(SETTING_APN, "0");

        // IMSI is mandatory
        defaultSettings.put(SETTING_IMSI, "0");

        synchronized (consumers) {
            for (Enumeration en = consumers.elements(); en.hasMoreElements();) {
                SettingsProvider cons = (SettingsProvider) en.nextElement();
                cons.getDefaultSettings(defaultSettings);
            }
        }

        return defaultSettings;
    }

    /**
     * Add a settings consumer class
     *
     * @param consumer Consumer of settings
     */
    public static void addProvider(SettingsProvider consumer) {
//		if (Logger.BUILD_DEBUG) {
//			Logger.log("Settings.addSettingsConsumer( " + consumer + " );");
//		}

        if (!loading) {
            throw new RuntimeException("Settings.addSettingsConsumer: We're not loading anymore !");
        }

        synchronized (consumers) {
            consumers.addElement(consumer);
            settings = null;
        }
    }

    /**
     * Remove a settings consumer class
     *
     * @param consumer Consumer of settings
     */
    public static void removeSettingsConsumer(SettingsProvider consumer) {
        synchronized (consumers) {
            if (consumers.contains(consumer)) {
                consumers.removeElement(consumer);
            }
            settings = null;
        }
    }

    /**
     * Reset all settings
     */
    public synchronized void resetErything() {
        try {
            FileConnection fc = (FileConnection) Connector.open("file:///a:/" + fileName, Connector.READ_WRITE);
            if (fc.exists()) {
                fc.delete();
            }
            load();
            settings = null;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("Settings.resetErything", ex);
            }
        }
    }

    /**
     * Save setttings
     */
    public static synchronized void save() {
        synchronized (Settings.class) {
//		if (Logger.BUILD_DEBUG) {
//			Logger.log("Settings.save();", true);
//		}

            // If there's no settings, we shouldn't have to save anything
            if (settings == null) {
                return;
            }

            // If no changes were made, we shouldn't have to save anything
            if (!madeSomeChanges) {
                return;
            }

            try {
                Hashtable defSettings = getDefaultSettings();



                String fileNameTmp = fileName + ".tmp";
                String fileNameOld = fileName + ".old";

                String settingFileUrl = "file:///a:/" + fileName;
                String settingFileUrlTmp = "file:///a:/" + fileNameTmp;
                String settingFileUrlOld = "file:///a:/" + fileNameOld;

//			if ( Logger.BUILD_DEBUG ) {
//				Logger.log("Settings.save: Opening \"" + settingFileUrlTmp + "\"...");
//			}

                FileConnection fc = (FileConnection) Connector.open(settingFileUrlTmp, Connector.READ_WRITE);

                //fc = (FileConnection) Connector.open("file:///" + _fileName, Connector.READ_WRITE);

                if (fc.exists()) {
                    fc.delete();
                }

                fc.create();
                OutputStream os = fc.openOutputStream();

                Enumeration e = defSettings.keys();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    String value = (String) settings.get(key);
                    String defValue = (String) defSettings.get(key);

                    if ( // if there is a default value
                            defValue != null && // and
                            // the value isn't the same as the default value
                            defValue.compareTo(value) != 0) {
                        String line = key + "=" + value + '\n';

//					if ( Logger.BUILD_DEBUG ) {
//						Logger.log("Settings.save.line: " + line);
//					}

                        os.write(line.getBytes());
                    }

                }
                os.flush();
                os.close();


                { // We move the current setting file to the old one
                    FileConnection currentFile = (FileConnection) Connector.open(settingFileUrl, Connector.READ_WRITE);

//				if ( Logger.BUILD_DEBUG ) {
//					Logger.log("Settings.save: Renaming \"" + settingFileUrl + "\" to \"" + fileNameOld + "\"");
//				}
                    if (currentFile.exists()) {

                        { // We delete the old setting file
//				if ( Logger.BUILD_DEBUG ) {
//					Logger.log("Settings.save: Deleting \"" + settingFileUrlOld + "\"");
//				}
                            FileConnection oldFile = (FileConnection) Connector.open(settingFileUrlOld, Connector.READ_WRITE);

                            if (oldFile.exists()) {
                                oldFile.delete();
                            }
                        }

                        currentFile.rename(fileNameOld);
                    }
                }





                { // We move the tmp file to the current setting file
//				if ( Logger.BUILD_DEBUG ) {
//					Logger.log("Setting.save: Renaming \"" + settingFileUrlTmp + "\" to \"" + _fileName + "\"");
//				}
                    fc.rename(fileName);
                    fc.close();
                }

                // If we savec the file, we can reset the madeSomeChanges information
                madeSomeChanges = false;
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log("Settings.Save", ex, true);
                }
            }
        }
    }

    /**
     * Init (and ReInit) method
     */
    private static void checkLoad() {
        if (settings == null) {
            load();
        }
    }

    /**
     * Get a setting's value as a String
     *
     * @param key Key Name of the setting
     * @return String value of the setting
     */
    public static synchronized String get(String key) {
        return (String) getSettings().get(key);
    }

    /**
     * Get all the settings
     *
     * @return All the settings
     */
    public static synchronized Hashtable getSettings() {
        checkLoad();
        return settings;
    }

    /**
     * Set a setting
     *
     * @param key Setting to set
     * @param value Value of the setting
     */
    public static synchronized void set(String key, String value) {
        if (Logger.BUILD_DEBUG) {
            Logger.log("Settings.setSetting( \"" + key + "\", \"" + value + "\" );");
        }

        if (setWithoutEvent(key, value)) {
            onSettingsChanged(new String[]{key});
        }
    }

    public void set(String key, int value) {
        set(key, "" + value);
    }

    /**
     * Set a setting without launching the onSettingsChange method
     *
     * @param key Setting to set
     * @param value Value of the setting
     * @return If setting was actually changed
     */
    public static synchronized boolean setWithoutEvent(String key, String value) {
        Hashtable table = getSettings();
        if (table.containsKey(key)) {
            String previousValue = (String) table.get(key);
            if (previousValue.compareTo(value) == 0) {
                return false;
            }
        } else {
            return false;
        }

        if (loading) {
            throw new RuntimeException("Settings.setSettingWithoutChangeEvent: You can't change a setting while loading !");
        }
        table.put(key, value);
        madeSomeChanges = true;
        return true;
    }

    /**
     * Get a setting's value as an int
     *
     * @param key Key Name of the setting
     * @return Integer value of the setting
     * @throws java.lang.NumberFormatException When the int cannot be parsed
     */
    public static int getInt(String key) throws NumberFormatException {
        String value = get(key);

        if (value == null) {
            return -1;
        }

        return Integer.parseInt(value);
    }

    /**
     * Get a setting's value as a boolean
     *
     * @param key Key name of the setting
     * @return The value of the setting (any value not understood will be
     * treated as false)
     */
    public static boolean getBool(String key) {
        String value = get(key);

        if (value == null) {
            return false;
        }

        if (value.compareTo("1") == 0
                || value.compareTo("true") == 0
                || value.compareTo("on") == 0
                || value.compareTo("yes") == 0) {
            return true;
        }
        return false;
    }
}
