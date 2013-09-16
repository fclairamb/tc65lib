/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.apnauto;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.InputConnection;
import org.javacint.common.BufferedReader;
import org.javacint.logging.Logger;

/**
 * GPRS Settings reader.
 *
 */
public final class GPRSSettingsReader {

    private final Vector names;
    private BufferedReader reader;
    // The GPRSSettings object will be kept through the complete parsing
    private final GPRSSettings settings = new GPRSSettings();
    private static final boolean LOG = false;
    private final InputConnection source;

    /**
     * GPRS settings reader.
     *
     * @param source Source file to read in
     * @param names Names of the elements to search for in priority (usually
     * MCC-MNC, MCC).
     * @throws IOException
     */
    public GPRSSettingsReader(InputConnection source, Vector names) throws IOException {
        if (source == null || names == null) {
            if (Logger.BUILD_DEBUG && LOG) {
                Logger.log("GPRSSettingsReader:28: is/names == null", true);
            }
        }
        this.source = source;
        this.names = names;
        reset();
    }

    /**
     * Get the next list name
     *
     * @return Next list name
     */
    private String getNextListName() {
        if (names.isEmpty()) {
            return null;
        } else {
            try { // We return the element
                String name = (String) names.elementAt(0);
                if (Logger.BUILD_DEBUG && LOG) {
                    Logger.log(this + ".getNextListName: " + name);
                }
                return name;
            } finally { // And remove it from the stack of names
                names.removeElementAt(0);
            }
        }
    }

    /**
     * Search for a line
     *
     * @param searchedLine Line to search for
     * @return If the line was found.
     */
    private boolean searchLine(String searchedLine) {
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(this + ".searchLine( \"" + searchedLine + "\" );");
        }
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.equals(searchedLine)) {
                if (Logger.BUILD_DEBUG && LOG) {
                    Logger.log(this + ".searchLine: Found !");
                }
                return true;
            }
        }
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(this + ".searchLine: Not found !");
        }
        return false;
    }

    /**
     * Reach the settings list name.
     *
     * @param name Name of the list to reach
     * @return If it could be reached
     * @throws IOException
     */
    private boolean reachName(String name) throws IOException {
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(this + ".reachName( \"" + name + "\" );");
        }

        // This is what we are searching for
        final String match = "[" + name + "]";

        // We search it after the current line
        if (searchLine(match)) {
            return true;
        }

        // We go back to the beginning of the file
        reset();
        if (searchLine(match)) {
            return true;
        }

        // Note that the next line is going to be null
        return false;
    }
    private boolean tryEverything = false;
//	private boolean allowReset = false;

    public GPRSSettings next() throws IOException {
        while (true) {
            String line = reader.readLine();

            if (Logger.BUILD_DEBUG && LOG) {
                Logger.log(this + ".next: line=\"" + line + "\"");
            }

            if (line == null || line.startsWith("[")) { // If we reached the end of file or we are reaching an other list


                if (!tryEverything) { // If we're not in "try everything" mode
                    // We get the next list name
                    String name = getNextListName();

                    if (name != null) { // If there's a list
                        reachName(name); // We reach it
                        continue; // and go on
                    } else { // If there's no more list
                        if (Logger.BUILD_DEBUG && LOG) {
                            Logger.log(this + ".next: Let's try everything !");
                        }

                        // We try everything
                        tryEverything = true;
                        try {
                            reset();
                            continue;
                        } catch (Exception ex) {
                            if (Logger.BUILD_CRITICAL) {
                                Logger.log("GPRS:138", ex, true);
                            }
                        }
                    }
                } else if (line == null) { // if we're trying everything and that's the end of file
                    return null; // that's the end
                } else { // if we're trying everything and we reached an other list name
                    continue; // We just go on
                }
            } else if (line.startsWith("#i ")) {
                names.insertElementAt(line.substring(3), 0);
            } else if (line.startsWith("#c ")) {
                settings.setColumns(line.substring(3));
            } else if (line.startsWith("#f ")) {
                reader = new BufferedReader(getClass().getResourceAsStream("/" + line.substring(3)));
            } else if (line.length() > 0) {
                // If we got a setting,
                settings.parse(line);
                // we return it
                return settings;
            }
            // We keep reading loop until we have something that actually is 
            // a setting
        }
    }

    public String toString() {
        return "GPRSSettingsReader";
    }
    private int nbResets;

//	private void reset() throws IOException {
//		reset(getClass().getResourceAsStream("/apns.txt"));
//	}
    private void reset() throws IOException {
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log(this + ".reset();");
        }
        if (nbResets++ > 100 && LOG) {
            throw new RuntimeException(this + ":Too many resets !");
        }
        if (reader != null) {
            reader.close();
        }
        InputStream is = source.openInputStream();
        if (is == null) {
            if (Logger.BUILD_CRITICAL && LOG) {
                Logger.log(this + ".reset: Settings file not found !!!", true);
            }
        }
        reader = new BufferedReader(is);
    }
}
