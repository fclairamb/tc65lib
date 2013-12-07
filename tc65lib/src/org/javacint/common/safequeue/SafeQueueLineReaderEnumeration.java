package org.javacint.common.safequeue;

import java.util.Enumeration;

/**
 * SafeQueueLineReader to Enumeration wrapper class
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class SafeQueueLineReaderEnumeration implements Enumeration {

    SafeQueueLineReader reader;

    public SafeQueueLineReaderEnumeration(SafeQueueLineReader reader) {
        this.reader = reader;
    }
    private String line;

    private String getNextLine(boolean remove) {
        try {
            // If we don't have a line, we try to fetch one
            if (line == null) {
                line = reader.readLine();
            }

            // We return whatever we found
            return line;
        } finally {
            // If we are supposed to remove the fetched line, we do it
            if (remove) {
                line = null;
            }
        }
    }

    public boolean hasMoreElements() {
        return getNextLine(false) != null;
    }

    public Object nextElement() {
        return getNextLine(true);
    }
}
