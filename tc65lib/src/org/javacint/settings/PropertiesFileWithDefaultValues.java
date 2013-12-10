package org.javacint.settings;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import org.javacint.common.PropertiesFile;

public class PropertiesFileWithDefaultValues extends PropertiesFile {

    private static final boolean LOG = true;
    
    public PropertiesFileWithDefaultValues(String path, Hashtable defaultValues) throws IOException {
        this.path = path;
        this.data = defaultValues; // We don't copy it, we just use it
        // as defaultValues must never be kept in memory (to save memory)
        load();
    }

    public void save(Hashtable defaultValues) throws IOException {
        { // We will remove every data that has a defaut value
            for (Enumeration en = defaultValues.keys(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                String value = (String) data.get(key);
                String defaultValue = (String) defaultValues.get(key);

                if (defaultValue == null || defaultValue.equals(value)) {
                    data.remove(key);
                }
            }
        }

        // We save this
        save();

        { // We will restore them
            for (Enumeration en = defaultValues.keys(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                String value = (String) data.get(key);
                String defaultValue = (String) defaultValues.get(key);

                if (value == null) {
                    data.put(key, defaultValue);
                }
            }
        }
    }
}
