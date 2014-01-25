package org.javacint.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.TimerTask;
import javax.microedition.io.StreamConnection;
import org.javacint.common.BufferedReader;
import org.javacint.io.Connections;
import org.javacint.logging.Logger;
import org.javacint.time.DateManagement;

/**
 * Email sending class. Totally experimental (not tested once). It is a very
 * basic implementation that should work only in the most simple cases.
 */
public class EmailSender extends TimerTask {

    private final String serverName;
    private final Email email;
    public static final boolean LOG = true;

    public EmailSender(String serverName, Email email) {
        this.serverName = serverName;
        this.email = email;
    }
    private StreamConnection conn;
    private InputStream is;
    private OutputStream os;
    private BufferedReader reader;

    private void connect() throws IOException {
        if (LOG && Logger.BUILD_NOTICE) {
            Logger.log(this + " - Connecting to " + serverName + "...");
        }
        conn = Connections.tcp(serverName, 25);
        is = conn.openInputStream();
        os = conn.openOutputStream();
        reader = new BufferedReader(is);
        if (LOG && Logger.BUILD_NOTICE) {
            Logger.log(this + " - Connected to " + serverName + " ! ");
        }
    }

    private void writeLine(String line) throws IOException {
        if (LOG && Logger.BUILD_NOTICE) {
            Logger.log(this + " --> " + line);
        }
        os.write((line + "\n").getBytes());
        os.flush();
    }

    private String readLine() {
        String line = reader.readLine();
        if (line != null && line.length() == 0) { // \r is not correclty handled
            line = reader.readLine();
        }
        if (LOG && Logger.BUILD_NOTICE) {
            Logger.log(this + " <-- " + line);
        }
        return line;
    }

    private void expectCode(int expected) {
        String line = readLine();
        int p = line.indexOf(' ');
        int code;
        if (p != -1) {
            String sCode = line.substring(0, p);
            code = Integer.parseInt(sCode);
        } else {
            code = -1;
        }
        if (code != expected) {
            throw new IllegalArgumentException("We expected " + expected + " and got this: " + line);
        }
    }

    public void run() {
        if (LOG) {
            Logger.log(this + " called !");
        }
        try {
            try {
                connect();
                expectCode(220);
                // Helo
                writeLine("HELO email.tc65lib.webingenia.com");
                expectCode(250);

                // We set the sender and recipients
                writeLine("MAIL FROM: <" + email.from + ">");
                expectCode(250);
                writeLine("RCPT TO: <" + email.to + ">");
                expectCode(250);
                writeLine("DATA");
                expectCode(354);

                // We send the header
                writeLine("From: <" + email.from + ">");
                writeLine("To: <" + email.to + ">");
                writeLine("Subject: " + email.subject);
                writeLine("X-Java-Date: " + DateManagement.date());
                writeLine("");

                // We send the message
                writeLine(email.message);
                writeLine(".");
                expectCode(250);

                // And quit
                writeLine("QUIT");
                expectCode(221);
                Logger.log("Email to " + email.to + " was sent !", true);
            } catch (IOException ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".run", ex, true);
                }
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
        } catch (Throwable ex) {
            if (LOG && Logger.BUILD_NOTICE) {
                Logger.log(this + " - Error", ex, true);
            }
        }
    }

    public String toString() {
        return "EmailSender";
    }
}
