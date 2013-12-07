package org.javacint.common.safequeue;

import com.siemens.icm.io.file.FileConnection;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import org.javacint.common.BufferedReader;
import org.javacint.logging.Logger;

/**
 * Allows to read data from a safe queue (without loading everything in memory)
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class SafeQueueLineReader {

    BufferedReader br;
    InputStream is;
    FileConnection fc;
    Vector mem;
    int lineNumber;

    public SafeQueueLineReader(Vector v) {
        mem = v;
    }

    public SafeQueueLineReader(FileConnection fc) throws IOException {
        this.fc = fc;
        is = fc.openInputStream();
        br = new BufferedReader(is);
    }

    public String readLine() {
        if (mem != null) {
            if (mem.size() == lineNumber) {
                return null;
            }

            return (String) mem.elementAt(lineNumber++);
        } else {
            String line = br.readLine();

            if (line == null) {
                try {
                    is.close();
                    fc.close();
                } catch (Exception ex) {
                    if (Logger.BUILD_CRITICAL) {
                        Logger.log("SafeQueueLineReader.readLine: ", ex);
                    }
                }
            }

            return line;
        }
    }

    public Vector toVector() {
        Vector v = new Vector();
        String line = null;
        while ((line = readLine()) != null) {
            v.addElement(line);
        }
        return v;
    }

    public void delete() throws IOException {
        if (mem != null) {
            mem.setSize(0);
        } else {
            String url = fc.getURL();
            try {
                is.close();
                fc.close();
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log("SafeQueueLineReader.delete:58", ex);
                }
            }
            FileConnection fileConn = (FileConnection) Connector.open(url);
            fileConn.delete();
        }
    }

    public void close() throws IOException {
        if (br != null) {
            br.close();
        }
    }
}
