package org.javacint.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimerTask;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import org.javacint.common.BufferedReader;
import org.javacint.logging.Logger;

/**
 * Email sending class. Totally experimental (not tested once). It is a very
 * basic implementation that should work only in the most simple cases.
 */
public class EmailSender extends TimerTask {

    private final String serverName;
    private final Email email;

    public EmailSender(String serverName, Email email) {
        this.serverName = serverName;
        this.email = email;
    }
    private SocketConnection conn;
    private InputStream is;
    private OutputStream os;
    private BufferedReader reader;

    private void connect() throws IOException {
        conn = (SocketConnection) Connector.open("socket://" + serverName + ":25");
        is = conn.openInputStream();
        os = conn.openOutputStream();
        reader = new BufferedReader(is);
    }

    private void writeLine(String line) throws IOException {
        os.write((line + "\n").getBytes());
        os.flush();
    }

    private String readLine() {
        return reader.readLine();
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
                writeLine("");

                // We send the message
                writeLine(email.message);
                writeLine(".");
                expectCode(250);

                // And quit
                writeLine("QUIT");
                expectCode(221);
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
            // We can never throw anything within the TimerTask
        }
    }

    public String toString() {
        return "EmailSender";
    }
}
