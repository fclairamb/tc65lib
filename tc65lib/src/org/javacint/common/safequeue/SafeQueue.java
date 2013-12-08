package org.javacint.common.safequeue;
//#if sdkns == "siemens"
import com.siemens.icm.io.file.FileConnection;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.file.FileConnection;
//#endif
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import org.javacint.logging.Logger;

/**
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class SafeQueue {

    private final String dirName;
    private final static int MIN_FREE_SPACE = 1024 * 400;
    private int maxFileSize = 1024 * 8;
    private int maxFileNb = 20;
    private int maxMemoryQueueSize = 61;
    private boolean filesRotation = true;
    private final Vector memoryQueue = new Vector();

    public SafeQueue(String name) {
        dirName = name;
    }

    public SafeQueue(String name, int memoryQueueSize, int fileNb, int fileSize) {
        dirName = name;
        maxMemoryQueueSize = memoryQueueSize;
        maxFileNb = fileNb;
        maxFileSize = fileSize;
    }

    public boolean hasData() {
        return checkHasData();
    }

    public void setFileRotation(boolean fr) {
        filesRotation = fr;
    }

    public synchronized void addLine(String data) {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".addLine( \"" + data + "\" );");
        }

        if (memoryQueue.size() < maxMemoryQueueSize) {
            memoryQueue.addElement(data);
        } else {
            if (Logger.BUILD_WARNING) {
                Logger.log(this + ".addLine: Already too much elements...");
            }
        }

        if (memoryQueue.size() >= maxMemoryQueueSize) {
            saveMemoryInFile();
        }
    }

    public synchronized SafeQueueLineReader getFirstItemsSetWaitingFile() {
        try {

            FileConnection fc = getFirstFile();
            if (fc == null) {

                if (Logger.BUILD_VERBOSE) {
                    Logger.log(this + ".getFirstItemsListWaiting: null");
                }

                return null;
            }

            if (Logger.BUILD_VERBOSE) {
                Logger.log(this + ".getFirstItemsListWaiting: IS url=\"" + fc.
                        getURL() + "\", size=\"" + fc.fileSize() + "\"");
            }

            return new SafeQueueLineReader(fc);
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".deleteFirstItemsListWaiting", ex);
            }
            return null;
        }
    }

    /**
     * Returns the first set of items waiting to be sent
     *
     * @return The first set of items waiting to be sent
     */
    public synchronized SafeQueueLineReader getFirstItemsSetWaiting() {
        if (Logger.BUILD_VERBOSE) {
            Logger.log(this + ".getFirstItemsListWaiting();");
        }

        // This has to be done in THIS order to prevent creating a deadlock
        // If we have something in memory, we return the memory
        if (!memoryQueue.isEmpty()) {

            if (Logger.BUILD_VERBOSE) {
                Logger.log(this + ".getFirstItemsListWaiting: Vector[" + memoryQueue.
                        size() + "]");
            }

            return new SafeQueueLineReader(memoryQueue);
        } // If we have something in files, we return a file
        else {
            return getFirstItemsSetWaitingFile();
        }
    }

    /**
     * Delete the first set of items that must have been returned earlier
     */
    public synchronized void deleteFirstItemsListWaiting() {
        if (Logger.BUILD_VERBOSE) {
            Logger.log(this + ".deleteFirstItemsListWaiting();");
        }
        if (!memoryQueue.isEmpty()) {
            memoryQueue.setSize(0);
        } else {
            try {
                rotateFiles();
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".deleteFirstItemsListWaiting", ex);
                }
            }
        }
    }

    public synchronized boolean deleteFirstFile() {
        try {
            rotateFiles();
            return true;
        } catch (Exception ex) {
            if (Logger.BUILD_DEBUG) {
                Logger.log(this + ".deleteFirstFile", ex, true);
            }
            return false;
        }
    }

    public synchronized boolean saveMemoryInFile() {
        if (Logger.BUILD_VERBOSE) {
            Logger.log(this + ".saveMemoryInFile();");
        }
        try {
            // If we don't have enough space left on device
            if (!enoughSpaceLeftOnDevice()) {
                if (Logger.BUILD_DEBUG) {
                    Logger.log(this + ".saveMemoryInFile: Not enough space left on device");
                }
                if (filesRotation) {
                    if (Logger.BUILD_DEBUG) {
                        Logger.log(this + ".saveMemoryInFile: Rotating files (1)");
                    }
                    rotateFiles();
                } else {
                    if (Logger.BUILD_DEBUG) {
                        Logger.log(this + ".saveMemoryInFile: Cancelling");
                    }
                    return false;
                }
            }

            FileConnection fc = findNextFile();
            if (fc == null) {
                if (Logger.BUILD_DEBUG) {
                    Logger.log(this + ".saveMemoryInFile: We've reached the last file");
                }
                if (filesRotation) {
                    if (Logger.BUILD_DEBUG) {
                        Logger.log(this + ".saveMemoryInFile: Rotating files (2)");
                    }
                    rotateFiles();
                } else {
                    if (Logger.BUILD_DEBUG) {
                        Logger.log(this + ".saveMemoryInFile: Cancelling");
                    }
                    return false;
                }
                fc = findNextFile();
            }

            if (fc != null) {
                saveStringVectorToFile(memoryQueue, fc);
            }
            return true;

        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".saveMemoryInFile", ex);
            }
        }

        return false;
    }

    public synchronized long usedSize() {
        try {
            FileConnection dir = (FileConnection) Connector.open("file:///a:/" + dirName + "/");
            return dir.directorySize(true);
        } catch (Exception ex) {
            if (Logger.BUILD_VERBOSE) {
                Logger.log(this + ".usedSize", ex);
            }
            return -1;
        }
    }

    public synchronized void deleteEverything() throws IOException {
        for (int i = 0; i < maxFileNb; i++) {
            FileConnection fc = (FileConnection) Connector.open("file:///a:/" + dirName + "/" + i + ".log");
            if (fc.exists()) {
                fc.delete();
            }
        }

        FileConnection fc = (FileConnection) Connector.open("file:///a:/" + dirName + "/");
        if (fc.exists()) {
            fc.delete();
        }
    }

    private boolean checkHasData() {
        if (!memoryQueue.isEmpty()) {
            return true;
        }

        if (getFirstFile() != null) {
            return true;
        }

        return false;
    }

    private void rotateFiles() throws IOException {
        if (Logger.BUILD_VERBOSE) {
            Logger.log(this + ".rotateFiles()");
        }
        for (int i = 0; i < maxFileNb; i++) {
            String fileName = "a:/" + dirName + "/" + i + ".log";
            try {
                FileConnection fc = (FileConnection) Connector.open("file:///" + fileName);
                if (fc.exists()) {
                    if (i == 0) {
                        if (Logger.BUILD_DEBUG) {
                            Logger.log(this + ".rotateFiles: Deleting " + fileName + "...");
                        }
                        fc.delete(); // First file is deleted
                    } else {
                        String newfilename = "" + (i - 1) + ".log";
                        // String newfilename = "a:/" + _dirName + "/" + (i - 1) + ".log";
                        if (Logger.BUILD_DEBUG) {
                            Logger.log(this + ".rotateFiles: Renaming \"" + fileName + "\" to \"" + newfilename + "\".");
                        }
                        fc.rename(newfilename); // Next files are renammed
                    }
                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".rotateFiles: fileName=\"" + fileName + "\"", ex, true);
                }
            }
        }
    }

    private FileConnection getFirstFile() {
        try {
            FileConnection fc = (FileConnection) Connector.open("file:///a:/" + dirName + "/0.log");
            if (fc.exists()) {
                return fc;
            }
        } catch (IOException ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".getFirstFile", ex, true);
            }
        }
        return null;
    }

    public int getNbFiles() {
        int nb = 0;
        for (int i = 0; i < maxFileNb; i++) {
            FileConnection fc;
            try {
                fc = (FileConnection) Connector.open("file:///a:/" + dirName + "/" + i + ".log");
                if (fc.exists()) {
                    nb++;
                } else {
                    break;
                }
            } catch (IOException ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".getNbFiles", ex, true);
                }
            }
        }
        return nb;
    }

    public SafeQueueLineReader getFile(int nb) {
        try {
            FileConnection fc = (FileConnection) Connector.open("file:///a:/" + dirName + "/" + nb + ".log");
            if (fc.exists()) {
                return new SafeQueueLineReader(fc);
            } else {
                return null;
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".getFile", ex);
            }
            return null;
        }
    }

    private FileConnection findNextFile() {
        try {
            { // We check the dir
                FileConnection dir = (FileConnection) Connector.open("file:///a:/" + dirName + "/");

                // If this is a file, we delete it
                if (dir.exists() && !dir.isDirectory()) {
                    dir.delete();
                }

                if (!dir.exists()) {
                    dir.mkdir();
                }
            }

            // We try to get the file
            for (int i = 0; i < maxFileNb; i++) {
                FileConnection fc = (FileConnection) Connector.open("file:///a:/" + dirName + "/" + i + ".log");

                // If current file is too big, we skip to the next file
                if (fc.exists() && fc.fileSize() >= maxFileSize) {
                    continue;
                }

                return fc;
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".findNextFile", ex);
            }
        }

        return null;
    }

    private long spaceLeftOnDevice() throws IOException {
        FileConnection device = (FileConnection) Connector.open("file:///a:");
        long availableSize = device.availableSize();
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".spaceLeftOnDevice: " + availableSize);
        }
        return availableSize;
    }

    private boolean enoughSpaceLeftOnDevice() throws IOException {
        return spaceLeftOnDevice() > MIN_FREE_SPACE;
    }

    private void saveStringVectorToFile(Vector strVector, FileConnection fc) throws IOException {
        try {
            if (Logger.BUILD_VERBOSE) {
                Logger.log(this + ".saveStringVectorToFile( Vector[" + strVector.
                        size() + "], " + fc + " );");
            }
            if (!fc.exists()) {
                fc.create();
            }

            OutputStream os = fc.openOutputStream(fc.fileSize());
            OutputStreamWriter osw = new OutputStreamWriter(os);

            Enumeration en = strVector.elements();

            while (en.hasMoreElements()) {
                osw.write((String) en.nextElement() + "\n");
            }

            osw.close();

            memoryQueue.setSize(0);
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".saveStringVectorToFile", ex, true);
            }
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
        }
    }

    public String toString() {
        return "SafeQueue:" + dirName;
    }
}
