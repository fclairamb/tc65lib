package org.javacint.control.basichttp;

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
 * Communication is done by opening an HTTP communication: Messages are: -
 * <strong>client-server</strong><br />
 * <ul>
 * <li>SEN:type=data (any kind of sensor data)</li>
 * <li>STA:type=data (any kind of status data)</li>
 * <li>LOG:type:data (any kind of log data)</li>
 * <li>SETA:name=value (setting acknowledge)</li>
 * <li>CMDA:id (commmand acknowledge)</li>
 * </ul>
 * <strong>server-client</strong><br />
 * <ul>
 * <li>SET:name=value (setting change)</li>
 * <li>CMD:id:command (command send)</li>
 * <li>OK (data sent by client correctly handled)</li>
 * </ul>
 */
public class HttpServerCommunication implements SettingsProvider, LoggingReceiver, WatchdogStatusProvider, Runnable {

    /**
     * The loop of the thread
     */
    private boolean l;
    /**
     * The thread of this HTTP management class
     */
    private final Thread thread = new Thread(this, "htt");
    private SafeQueue queue = new SafeQueue("http");
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
    private HttpCommandReceiver consumer;
    private String serverUrl;

    /**
     * Default constructor of the HttpServerCommunication class
     */
    public HttpServerCommunication(String serverUrl, String ident, HttpCommandReceiver receiver) {
        this.serverUrl = serverUrl;
        this.ident = ident;
        //this.consumer = consumer;
        init();
    }

    private void init() {
        Settings.addProvider(this);
        Logger.setLoggingReceiver(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        parseSetting(SETTING_HTTP_LOG);
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

        if (Logger.BUILD_DEBUG) {
            Logger.log("HttpServerCommunication.httpRequest( \"" + url + "\", Vector.size()=" + lines.
                    size() + " );");
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
                if (Logger.BUILD_DEBUG) {
                    Logger.log("HttpServerCommunication.httpRequest --> " + line);
                }
                os.write(line.getBytes());
                os.write('\n');
            }

            // We display the generated content
            vector = new Vector();
//			StringBuffer buffer = new StringBuffer();

            is = conn.openInputStream();

            BufferedReader bbr = new BufferedReader(is);
            String line;
            while ((line = bbr.readLine()) != null) {
                if (Logger.BUILD_DEBUG) {
                    Logger.log("HttpServerCommunication.httpRequest <-- " + line);
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

//				_error = "HTTP.httpRequest.2:" + ex.getClass() + ":" + ex.
//						getMessage();
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log("HttpServerCommunication.httpRequest.3", ex, true);
                }

//				_error = "HTTP.httpRequest.2:" + ex.getClass() + ":" + ex.
//						getMessage();
            }

            if (Logger.BUILD_DEBUG) {
                Logger.log("HttpServerCommunication.httpRequest: " + (vector != null ? "vector.size()=" + vector.
                        size() : "null"));
            }
        }

        error = null; // Everything is going fine
        lastSuccessfulRequestTime = System.currentTimeMillis() / 1000;
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
                Logger.log("_dataToSend is already too big ! ");
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
                    consumer.httpCommand(cmd);
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
                addData("SETA:" + name + "=" + value);
                Settings.set(name, value);
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

                    if (Logger.BUILD_DEBUG) {
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
        while (l) {
            try {
                parseSetting(SETTING_HTTP_URL);
                parseSetting(SETTING_HTTP_LOG);
                parseSetting(SETTING_HTTP_WAIT_MAX);
                parseSetting(SETTING_HTTP_WAIT_MIN);

                while (l) {
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
                        if (Logger.BUILD_DEBUG) {
                            Logger.log("HTTP: Sending data");
                        }
                        received = httpRequest(url, queue.
                                getFirstItemsSetWaiting().
                                toVector());
                    }


                    // If we need to request something, we do it now
                    if (received == null && (time - lastRequestTime > waitBeforeReceive)) {
                        if (Logger.BUILD_DEBUG) {
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

                    sleep(Math.min(waitBeforeReceive, waitBeforeSend) * 1000);
                }
            } catch (Exception ex) {
                queue.saveMemoryInFile();
                if (Logger.BUILD_CRITICAL) {
                    Logger.log("HttpServerCommunication.run", ex);
                }
            }
        }
        // When the thread ends, we save memory content to file
        queue.saveMemoryInFile();
    }
// === /Runnable ===

    private void sleep(long time) {
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
        l = true;
        synchronized (thread) {
            thread.start();
        }
    }

    /**
     * Stop the HTTP communication class
     */
    public void stop() {
        l = false;
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

    public String toString() {
        return "HttpServerCommunication";
    }
}
