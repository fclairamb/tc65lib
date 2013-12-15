package org.javacint.common;

//#if sdkns == "siemens"
import com.siemens.icm.io.file.FileConnection;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.file.FileConnection;
//#endif
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import org.javacint.logging.Logger;

/**
 * Properties file.
 *
 * This can be used for saving properties.
 */
public class PropertiesFile {

    protected static final String PATH_PREFIX = "file:///a:/";
    protected String path;
    protected Hashtable data;
    private static final boolean LOG = false;

    public PropertiesFile(String path) throws IOException {
        this.path = path;
        data = new Hashtable();
        load();
    }

    protected PropertiesFile() {
    }

    public Hashtable getData() {
        return data;
    }

    public void set(String name, String value) {
        data.put(name, value);
    }

    public void set(String name, boolean value) {
        data.put(name, value ? "1" : "0");
    }

    public void set(String name, int value) {
        data.put(name, "" + value);
    }

    public void set(String name, long value) {
        data.put(name, "" + value);
    }

    public String get(String name, String defaultValue) {
        String value = (String) data.get(name);
        return value != null ? value : defaultValue;
    }

    public boolean get(String name, boolean defaultValue) {
        String value = (String) data.get(name);
        if ("1".equals(value)) {
            return true;
        } else if ("0".equals(value)) {
            return false;
        } else {
            return defaultValue;
        }
    }

    public int get(String name, int defaultValue) {
        try {
            return Integer.parseInt((String) data.get(name));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public long get(String name, long defaultValue) {
        try {
            return Long.parseLong((String) data.get(name));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    protected final void load() throws IOException {
        FileConnection fc = (FileConnection) Connector.open(PATH_PREFIX + path, Connector.READ);
        if (!fc.exists()) { // If we can't find it, we revert to the old file
            fc = (FileConnection) Connector.open(PATH_PREFIX + path + ".old", Connector.READ);
            if (fc.exists()) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".load: Could not find settings file, reverting to backup file !");
                }
            } else {
                return;
            }
        }
        BufferedReader reader = new BufferedReader(fc.openInputStream());
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                int c = line.indexOf('=');
                if (c > 0) {
                    String key = line.substring(0, c);
                    String value = line.substring(c + 1);
                    data.put(key, value);
                } else {
                    if (Logger.BUILD_DEBUG) {
                        Logger.log(this + ".load: Invalid line \"" + line + "\"");
                    }
                }
            }
        } finally {
            reader.close();
        }
    }

    public void save() throws IOException {
        { // First we save the previous file (path --> path.old)
            FileConnection old = (FileConnection) Connector.open(PATH_PREFIX + path + ".old", Connector.READ_WRITE);
            FileConnection current = (FileConnection) Connector.open(PATH_PREFIX + path, Connector.READ_WRITE);
            if (current.exists()) {

                if (old.exists()) {
                    if (LOG) {
                        Logger.log("Deleting \"" + path + ".old\".");
                    }
                    old.delete();
                }

                // If the program crashes here, config is lost

                if (LOG) {
                    Logger.log("Renaming \"" + path + "\" to \"" + path + ".old\".");
                }
                current.rename(path + ".old");
            }
        }
        // The we try to open the new file ( path.tmp )
        FileConnection fc = (FileConnection) Connector.open(PATH_PREFIX + path + ".tmp", Connector.READ_WRITE);
        if (fc.exists()) {
            fc.delete();
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".save: Previous temporary file exists, expect data loss !", true);
            }
        }
        fc.create();
        PrintStream out = new PrintStream(fc.openOutputStream());
        try {
            for (Enumeration en = data.keys(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                out.print(key + "=" + data.get(key) + "\n");
            }
        } finally {
            out.close();
        }
        // And if everything when fine (no exception triggered, we save it to the correct name)
        if (LOG) {
            Logger.log("Renaming \"" + path + ".tmp\" to \"" + path + "\".");
        }
        fc.rename(path);
    }

    public void delete() throws IOException {
        FileConnection fc = (FileConnection) Connector.open(PATH_PREFIX + path, Connector.READ_WRITE);
        if (fc.exists()) {
            fc.delete();
        }
        fc = (FileConnection) Connector.open(PATH_PREFIX + path + ".old", Connector.READ_WRITE);
        if (fc.exists()) {
            fc.delete();
        }
    }

    public String toString() {
        return "ParametersFile{" + path + "}";
    }
}
