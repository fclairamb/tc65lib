package org.javacint.control.basichttp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import org.javacint.at.ATExecution;
import org.javacint.common.BufferedReader;
import org.javacint.common.safequeue.SafeQueue;
import org.javacint.logging.Logger;
import org.javacint.logging.LoggingReceiver;
import org.javacint.settings.Settings;
import org.javacint.settings.SettingsProvider;
import org.javacint.time.DateManagement;
import org.javacint.watchdog.WatchdogStatusProvider;

/**
 * Basic HTTP Server Communication.
 *
 * Communication is done by opening an HTTP communication and sending raw data.
 * The protocol is not pretty or smart but it's easy to implement. The device
 * sends its data (the client to server messages) and then waits for the
 * server's data ending with "OK". To acknoledge new settings or commands, the
 * client has to open a new connection.
 * <br />
 * Messages are:
 * <strong>client to server</strong><br />
 * <ul>
 * <li>&lt;time&gt;:SEN:type=data (any kind of sensor data)</li>
 * <li>&lt;time&gt;:STA:type=data (any kind of status data)</li>
 * <li>&lt;time&gt;:LOG:type:data (any kind of log data)</li>
 * <li>&lt;time&gt;:SETA:name=value (setting acknowledge)</li>
 * <li>&lt;time&gt;:CMDA:id (commmand acknowledge)</li>
 * </ul>
 * <strong>server to client</strong><br />
 * <ul>
 * <li>&lt;time&gt;:SET:name=value (setting change)</li>
 * <li>&lt;time&gt;:CMD:id:command (command send)</li>
 * <li>OK (data sent by client correctly handled)</li>
 * </ul>
 */
public class HttpServerCommunication implements SettingsProvider, LoggingReceiver, WatchdogStatusProvider, Runnable {

    private static final boolean LOG = true;
    /**
     * The loop of the thread
     */
    private boolean loop;
    /**
     * The thread of this HTTP management class
     */
    private final Thread thread = new Thread(this, "htt");
    private SafeQueue queue = new SafeQueue("http", 30, 50, 4096);
    private final Vector dataToSend = new Vector();
    private long waitBeforeReceive = 1;
    private long waitBeforeSend = 1;
    private long lastRequestTime = System.currentTimeMillis() / 1000;
    private long lastSuccessfulRequestTime = System.currentTimeMillis() / 1000;
    private String url;
    private String error;
    private final String SETTING_HTTP_LOG = "http.log";
    private final String SETTING_HTTP_URL = "http.url";
    private final String SETTING_HTTP_WAIT_MAX = "http.waitmax";
    private final String SETTING_HTTP_WAIT_MIN = "http.waitmin";
    private String ident;
    private Vector receivers = new Vector();
    private String serverUrl;

    class CommandsReceiver implements HttpCommandReceiver {

        public boolean httpCommand(String command) {
            if (command.equals("settings")) {
                Hashtable settings = Settings.getSettings();
                for (Enumeration en = settings.keys(); en.hasMoreElements();) {
                    String key = (String) en.nextElement();
                    addData("SETA:" + key + "=" + settings.get(key));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Default constructor of the HttpServerCommunication class
     */
    public HttpServerCommunication(String serverUrl, String ident) {
        this.serverUrl = serverUrl;
        this.ident = ident;
        init();
    }

    private void init() {
        Settings.addProvider(this);
        Logger.setLoggingReceiver(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        parseSetting(SETTING_HTTP_LOG);
        parseSetting(SETTING_HTTP_WAIT_MIN);
        parseSetting(SETTING_HTTP_WAIT_MAX);
        receivers.addElement(new CommandsReceiver());
    }

    public void addReceiver(HttpCommandReceiver receiver) {
        receivers.addElement(receiver);
    }

    public void delReceiver(HttpCommandReceiver receiver) {
        receivers.removeElement(receiver);
    }

    /**
     * Perform an HTTP request
     *
     * @param url Destination URL
     * @param lines Lines to send
     * @return Lines of the answer
     */
    public Vector httpRequest(String url, Vector lines) {
        lastRequestTime = System.currentTimeMillis() / 1000;
        if (lines == null) {
            lines = new Vector();
        }

        if (LOG) {
            Logger.log("HTTP.httpRequest( \"" + url + "\", Vector.size()=" + lines.size() + " );");
        }
        Vector vector = null;

        HttpConnection conn = null;
        InputStream is = null;
        OutputStream os = null;

        try { // Test HTTP connection

            // We prepare the POST request
            conn = (HttpConnection) Connector.open(url);
            conn.setRequestMethod(HttpConnection.POST);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            os = conn.openOutputStream();

            for (Enumeration en = lines.elements(); en.hasMoreElements();) {
                String line = (String) en.nextElement();
                if (LOG) {
                    Logger.log("HTTP --> " + line);
                }
                os.write(line.getBytes());
                os.write('\n');
            }

            if (LOG) {
                Logger.log("HTTP --> EOF");
            }

            int code = conn.getResponseCode();
            if (LOG) {
                Logger.log("HTTP CODE: " + code);
            }

            // We display the generated content
            vector = new Vector();

            is = conn.openInputStream();
            BufferedReader bbr = new BufferedReader(is);
            String line;
            while ((line = bbr.readLine()) != null) {
                if (LOG) {
                    Logger.log("HTTP <-- " + line);
                }
                vector.addElement(line);
            }

        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".httpRequest:1", ex, true);
            }

            error = "HTTP.httpRequest.1:" + ex.getClass() + ":" + ex.getMessage();

            ATExecution.setGprsAttach(false);

            if (ex.getClass() == ConnectionNotFoundException.class) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException ex1) {
                    if (Logger.BUILD_CRITICAL) {
                        Logger.log(this + ".httpRequest:14", ex1, true);
                    }
                }
            }
            return null;
        } // Whatever happens, we close everything
        finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log("HttpServerCommunication.httpRequest.2", ex, true);
                }
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log("HttpServerCommunication.httpRequest.3", ex, true);
                }
            }
        }

        error = null; // Everything is going fine
        lastSuccessfulRequestTime = System.currentTimeMillis() / 1000;
        if (LOG) {
            Logger.log("HTTP.httpRequest: Vector.size() = " + vector.size());
        }
        return vector;
    }

    /**
     * Add data to send
     *
     * @param line
     */
    public void addData(String line) {
        if (Logger.BUILD_DEBUG) {
            Logger.log("HttpServerCommunication.addData( \"" + line + "\" );");
        }
        synchronized (dataToSend) {
            if (dataToSend.size() < 200) {
                dataToSend.addElement(DateManagement.time() + ":" + line);
            } else if (Logger.BUILD_CRITICAL) {
                Logger.log("dataToSend is already too big ! ");
            }

            if (dataToSend.size() > 50) {
                sendNow();
            }
        }

        synchronized (thread) {
            thread.notify();
        }
    }

    private void parseSetting(String name) {
        if (name.compareTo(SETTING_HTTP_WAIT_MAX) == 0) {
            waitBeforeReceive = Settings.getInt(SETTING_HTTP_WAIT_MAX);
        } else if (name.compareTo(SETTING_HTTP_WAIT_MIN) == 0) {
            waitBeforeSend = Settings.getInt(SETTING_HTTP_WAIT_MIN);
        } else if (name.compareTo(SETTING_HTTP_URL) == 0) {
            url = Settings.get(SETTING_HTTP_URL) + "/tc65?ident=" + ident;
        } else if (name.compareTo(SETTING_HTTP_LOG) == 0) {
            Logger.setLoggingReceiver(Settings.getBool(SETTING_HTTP_LOG) ? this : null);
        }
    }

    // === ISettingsConsumer interface methods ===
    public void getDefaultSettings(Hashtable settings) {
        settings.put(SETTING_HTTP_URL, serverUrl);
        settings.put(SETTING_HTTP_WAIT_MAX, "900");
        settings.put(SETTING_HTTP_WAIT_MIN, "1");
        settings.put(SETTING_HTTP_LOG, "1");
    }

    public void settingsChanged(String[] settings) {
        for (int i = 0; i < settings.length; i++) {
            parseSetting(settings[i]);
        }
    }
    // === /ISettingsConsumer ===

    // === ILoggingReceiver interface methods ===
    public void log(String str) {
        addData("LOG:" + str);
    }
    // === /ILoggingreceiver ===

    private void treatCommand(String line) {
        if (Logger.BUILD_NOTICE) {
            Logger.log("Received command: \"" + line + "\"", true);
        }

        try {
            int pos = line.indexOf(':');
            if (pos != -1) {
                String cmdId = line.substring(0, pos);
                String cmd = line.substring(pos + 1);

                // We add a command acknowledge
                addData("CMDA:" + cmdId);

                // But it might be too slow, so we also immediately send an acknowledge command
                httpRequest(Settings.get(SETTING_HTTP_URL) + "/tc65CmdAck?ident=" + ident + "&cmdId=" + cmdId, null);

                try {
                    synchronized (receivers) {
                        for (Enumeration en = receivers.elements(); en.hasMoreElements();) {
                            HttpCommandReceiver r = (HttpCommandReceiver) en.nextElement();
                            if (r.httpCommand(cmd)) {
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    if (Logger.BUILD_CRITICAL) {
                        Logger.log(this + ".treatCommand(" + line + "):26", ex, true);
                    }
                }
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".treatCommand( \"" + line + "\" );");
            }
        }
    }

    private void treatCommands(Vector lines) {
        for (Enumeration en = lines.elements(); en.hasMoreElements();) {
            String line = (String) en.nextElement();
            treatCommand(line);
        }
    }

    private void treatSettings(Vector lines) {
        try {
            for (Enumeration en = lines.elements(); en.hasMoreElements();) {
                String line = (String) en.nextElement();
                int pos = line.indexOf('=');
                String name = line.substring(0, pos);
                String value = line.substring(pos + 1);
                String before = Settings.get(name);
                if (before != null) {
                    Settings.setWithoutEvent(name, value);
                    addData("SETA:" + name + "=" + value);
                    Settings.onSettingsChanged(new String[]{name});
                } else {
                    addData("SETA:" + name);
                }
            }
            Settings.save();
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("HttpServerCommunication.treatSettings", ex, true);
            }
        }
    }

    private void treatReceivedLines(Vector lines) {
        try {
            Vector settings = new Vector();
            Vector commands = new Vector();

            for (Enumeration en = lines.elements(); en.hasMoreElements();) {
                try {
                    String line = (String) en.nextElement();

                    if (LOG) {
                        Logger.log("treatReceivedLines: line=\"" + line + "\"");
                    }

                    if (line.compareTo("OK") == 0) {
                        continue;
                    }
                    int pos = line.indexOf(':');
                    if (pos == -1) {
                        continue;
                    }
                    String type = line.substring(0, pos);
                    String value = line.substring(pos + 1);
                    if (type.compareTo("SET") == 0) {
                        settings.addElement(value);
                    } else if (type.compareTo("CMD") == 0) {
                        commands.addElement(value);
                    }
                } catch (Exception ex) {
                    if (Logger.BUILD_CRITICAL) {
                        Logger.log("HTTP.treatReceivedLines:1", ex, true);
                    }
                }
            }

            if (!settings.isEmpty()) {
                treatSettings(settings);
            }

            if (!commands.isEmpty()) {
                treatCommands(commands);
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("HTTP.treatReceivedLines:2", ex, true);
            }
        }
    }

    // === Runnable ===
    /**
     * The thread method
     */
    public void run() {
        while (loop) {
            try {
                parseSetting(SETTING_HTTP_URL);
                parseSetting(SETTING_HTTP_LOG);
                parseSetting(SETTING_HTTP_WAIT_MAX);
                parseSetting(SETTING_HTTP_WAIT_MIN);

                while (loop) {
                    Vector received = null;

                    long time = System.currentTimeMillis() / 1000;


                    // We copy the data to send
                    synchronized (dataToSend) {
                        for (Enumeration en = dataToSend.elements(); en.
                                hasMoreElements();) {
                            queue.addLine((String) en.nextElement());
                        }
                        dataToSend.setSize(0);
                    }

                    // If we have some data to send, we send it
                    if (queue.hasData() && (time - lastRequestTime > waitBeforeSend)) {
                        if (LOG) {
                            Logger.log("HTTP: Sending data");
                        }
                        received = httpRequest(url, queue.
                                getFirstItemsSetWaiting().
                                toVector());
                    }


                    // If we need to request something, we do it now
                    if (received == null && (time - lastRequestTime > waitBeforeReceive)) {
                        if (LOG) {
                            Logger.log("HTTP: Receiving data");
                        }
                        received = httpRequest(url, null);
                    }

                    if (received != null && received.size() > 0) {
                        String lastLine = (String) received.lastElement();
                        if (lastLine.compareTo("OK") == 0) {
                            queue.deleteFirstItemsListWaiting();
                            treatReceivedLines(received);
                        }
                    }

                    if (received != null) {
                        sleep(waitBeforeSend * 1000);
                    } else {
                        sleep(waitBeforeReceive * 1000);
                    }
                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log("HttpServerCommunication.run", ex);
                }
                queue.saveMemoryInFile();
            }
        }
        // When the thread ends, we save memory content to file
        queue.saveMemoryInFile();
    }
// === /Runnable ===

    private void sleep(long time) {
        if (LOG) {
            Logger.log("Sleeping " + time + "ms");
        }
        try {
            synchronized (thread) {
                thread.wait(time);
            }
        } catch (InterruptedException ex) {
            Logger.log("Http.sleep", ex);
        }
    }

    private void sendNow() {
        synchronized (thread) {
            thread.notify();
        }
    }

    /**
     * Start the HTTP Communication class
     */
    public void start() {
        loop = true;
        synchronized (thread) {
            thread.start();
        }
    }

    /**
     * Stop the HTTP communication class
     */
    public void stop() {
        loop = false;
        synchronized (thread) {
            thread.notifyAll();
        }
    }

    public String getWorkingStatus() {
        long timeWithoutTransmission = System.currentTimeMillis() / 1000 - lastSuccessfulRequestTime;
        // If it's been less than 45 minutes (3*15 minutes for http.waitmax=900), it's ok
        if (timeWithoutTransmission < waitBeforeReceive * 3) {
            return null;
        } else {
            if (error != null) {
                return error;
            } else {
                return "No error detected but nothing was transmitted for " + timeWithoutTransmission + "s";
            }
        }
    }

    public void resetQueue() throws IOException {
        queue.deleteEverything();
    }

    public String toString() {
        return "HttpServerCommunication";
    }
}
