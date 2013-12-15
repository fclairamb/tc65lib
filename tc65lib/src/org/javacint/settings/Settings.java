package org.javacint.settings;

////#if sdkns == "siemens"
//import com.siemens.icm.io.file.FileConnection;
////#elif sdkns == "cinterion"
////# import com.cinterion.io.file.FileConnection;
////#endif
import java.io.*;
import java.util.*;
import org.javacint.logging.Logger;

/**
 * Settings management class.
 *
 * This class should take advantage of the PropertiesFile class.
 */
public class Settings {

    private static final boolean LOG = false;
    /**
     * Settings container. It has both the default and specific settings.
     */
    private static PropertiesFileWithDefaultValues settings;
    /**
     * Settings filename
     */
    private static String fileName = "settings.txt";
    /**
     * Settings providers. They provide some settings with some default values
     * and they receive events when some settings are changed.
     */
    private static final Vector providers = new Vector();
    /**
     * APN setting. In the AT^SJNET=... format
     */
    public static final String SETTING_APN = "apn";
    /**
     * Protection code
     */
    public static final String SETTING_CODE = "code";
    public static final String SETTING_MANAGERSPHONE = "phoneManager";
    /**
     * ICCID sim card setting. This is very useful to detect iccid card change
     * (SIM card change detection not handled by the settings class itself).
     */
    public static final String SETTING_ICCID = "iccid";
    /**
     * pincode setting name. Using pincode is NOT recommended.
     */
    public static final String SETTING_PINCODE = "pincode";
    /**
     * jadurl setting
     */
    public static final String SETTING_JADURL = "jadurl";
    private static boolean madeSomeChanges = false;
    private static boolean firstStartup = false;
    /**
     * loading state
     */
    private static boolean loading;

    public static synchronized void setFilename(String filename) {
        fileName = filename;
        settings = null;
    }

    /**
     * Define if we are loading the program. If we are loading, we can't
     * get/set/load/save settings. We can only add and remove settings
     * consumers.
     *
     * @param l Loading state
     */
    public static void loading(boolean l) {
        loading = l;
    }

    /**
     * Get the settings filename.
     *
     * @return Filename without the "file:///a:/" path prefix
     */
    public static String getFilename() {
        return fileName;
    }

    /**
     * If this is the first startup.
     *
     * The first statup flag is activated if we don't have any settings file.
     *
     * @return If this is the first startup
     */
    public static boolean firstStartup() {
        return firstStartup;
    }

    /**
     * Load settings. We should replace the line by line loading code by using
     * the PropertiesFile class.
     */
    public static synchronized void load() {
        if (Logger.BUILD_DEBUG) {
            Logger.log("Settings.load();");
        }
        try {
            settings = new PropertiesFileWithDefaultValues(fileName, getDefaultSettings());
        } catch (IOException ex) {
            // The exception we shoud have is at first launch : 
            // There shouldn't be any file to read from

            if (Logger.BUILD_CRITICAL) {
                Logger.log("Settings.load", ex);
            }
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
        try {
            synchronized (providers) {
                for (Enumeration en = providers.elements(); en.hasMoreElements();) {
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

        // The following settings are mandatory but they
        // are NOT handled by the Settings class.

        // Code is mandatory (SMS control protection)
        defaultSettings.put(SETTING_CODE, "1234");

        // APN is mandatory (GPRS setup)
        defaultSettings.put(SETTING_APN, "");

        // ICCID is mandatory (SIM card detection)
        defaultSettings.put(SETTING_ICCID, "");

        synchronized (providers) {
            for (Enumeration en = providers.elements(); en.hasMoreElements();) {
                SettingsProvider prov = (SettingsProvider) en.nextElement();
                prov.getDefaultSettings(defaultSettings);
            }
        }

        return defaultSettings;
    }

    /**
     * Add a settings provier class
     *
     * @param provider Provider of settings and consumer of settings change
     */
    public static void addProvider(SettingsProvider provider) {
//		if (Logger.BUILD_DEBUG) {
//			Logger.log("Settings.addSettingsConsumer( " + consumer + " );");
//		}

        if (!loading) {
            // We should never add or removed a settings provider when we have finished loading
            throw new RuntimeException("Settings.addSettingsConsumer: We're not loading anymore !");
        }

        synchronized (providers) {
            providers.addElement(provider);
            // Adding a provider voids the current state of the settings
            settings = null;
        }
    }

    /**
     * Remove a settings consumer class
     *
     * @param consumer Consumer of settings
     */
    public static void removeProvider(SettingsProvider consumer) {
        synchronized (providers) {
            if (providers.contains(consumer)) {
                providers.removeElement(consumer);
            }
            settings = null;
        }
    }

    /**
     * Reset all settings
     */
    public synchronized static void reset() {
        try {
            checkLoad();
            if (settings != null) {
                settings.delete();
                load();
            }
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

            // If there's no settings, we shouldn't have to save anything
            if (settings == null) {
                return;
            }

            // If no changes were made, we shouldn't have to save anything
            if (!madeSomeChanges) {
                return;
            }

            try {
                settings.save(getDefaultSettings());
                // We don't have anything to be written anymore
                madeSomeChanges = false;
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log("Settings.save", ex, true);
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
        return settings.getData();
    }

    /**
     * Set a setting
     *
     * @param key Name of the setting
     * @param value Value of the setting
     */
    public static void set(String key, String value) {
        if (Logger.BUILD_DEBUG) {
            Logger.log("Settings.set( \"" + key + "\", \"" + value + "\" );");
        }

        if (setWithoutEvent(key, value)) {
            onSettingsChanged(new String[]{key});
        }
    }

    /**
     * Set a setting
     *
     * @param key Name of the setting
     * @param value Value of the setting
     */
    public static void set(String key, int value) {
        set(key, Integer.toString(value));
    }

    /**
     * Set a setting
     *
     * @param key Name of the setting
     * @param value Value of the setting
     */
    public static void set(String key, boolean value) {
        set(key, value ? "1" : "0");
    }

    /**
     * Set a setting without launching the onSettingsChange method
     *
     * @param key Setting to set
     * @param value Value of the setting
     * @return If setting was actually changed
     */
    public static synchronized boolean setWithoutEvent(String key, String value) {
        checkLoad();
        String previousValue = settings.get(key, null);
        if (previousValue != null && previousValue.compareTo(value) == 0) {
            return false;
        }

        if (loading) {
            throw new RuntimeException("Settings.setWithoutEvent: You can't change a setting while loading !");
        }
        settings.set(key, value);
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
        checkLoad();
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(THIS + ".getInt( \"" + key + "\" );");
        }
        return settings.get(key, -1);
    }

    /**
     * Get a setting's value as a boolean
     *
     * @param key Key name of the setting
     * @return The value of the setting (any value not understood will be
     * treated as false)
     */
    public static boolean getBool(String key) {
        checkLoad();
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(THIS + ".getBool( \"" + key + "\" );");
        }
        return settings.get(key, false);
    }

    public static Enumeration names() {
        return getSettings().keys();
    }
    private static final String THIS = "Settings";
}
