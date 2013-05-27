package org.javacint.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import org.javacint.at.ATCommands;
import org.javacint.at.ATExecution;
import org.javacint.common.Strings;
import org.javacint.common.sorting.Sorter;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.settings.SettingsProvider;

/**
 * Console management
 *
 * @author Florent Clairambault / www.webingenia.com
 *
 * This is only a draft of the console code, I need to somehow to support the
 * features of at least 3 implementation of a console.
 */
public class Console implements Runnable {

    private Thread thread = new Thread(this, "con");
    private final StreamConnection stream;
    private InputStream in;
    private PrintStream out;
    private String header = "Console";
    private final Vector commandsReceiver = new Vector();

    public void addCommandReceiver(ConsoleCommand receiver) {
        synchronized (commandsReceiver) {
            commandsReceiver.addElement(receiver);
        }
    }

    public void removeCommandReceiver(ConsoleCommand receiver) {
        synchronized (commandsReceiver) {
            if (commandsReceiver.contains(receiver)) {
                commandsReceiver.removeElement(receiver);
            }
        }
    }

    public Console(StreamConnection stream) {
        this.stream = stream;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    private void writeLine(String line) throws IOException {
        out.println(line);
    }

    private void parseCommand(String line) throws Exception {
        addHistory(line);
        out.println();
        synchronized (commandsReceiver) {
            for (Enumeration en = commandsReceiver.elements(); en.hasMoreElements();) {
                try {
                    ConsoleCommand commandReceiver = (ConsoleCommand) en.nextElement();
                    if (commandReceiver.consoleCommand(line, in, out)) {
                        return;
                    }
                } catch (Throwable ex) {
                    if (Logger.BUILD_CRITICAL) {
                        out.println("Command receiver " + en + " failed: " + ex.getClass() + " : " + ex.getMessage());
                    }
                }
            }
        }

        if (line.startsWith("AT")) {
            String[] lines = Strings.split('\n', ATCommands.sendNoR(line + "\r"));
            for (int i = 0; i < lines.length; ++i) {
                writeLine("[AT] " + lines[i]);
            }
        } else if (line.equals("restart")) {
            ATExecution.restart();
        } else if (line.equals("help")) {
            writeLine(header);
            writeLine("[HELP] help                         This help");
            writeLine("[HELP] conf list                    List all configuration settings");
            writeLine("[HELP] conf <key>=<value>           Define a configuration setting");
            writeLine("[HELP] conf <key>                   Get a configuration setting");
            writeLine("[HELP] conf save                    Save the configuration settings");
            writeLine("[HELP] restart                      Restart the device");
            writeLine("[HELP] AT***                        Send AT commads");
            writeLine("[HELP] stats                        Get some system stats");
            writeLine("[HELP] sms send <phone> <message>   Send an SMS");
        } else if (line.equals("conf list") || line.equals("conf")) {

            Hashtable defSettings = Settings.getDefaultSettings();

            Object keys[] = new Object[defSettings.size()];

            { // Filling and sorting keys
                Enumeration e = defSettings.keys();
                for (int i = 0; e.hasMoreElements(); i++) {
                    keys[i] = e.nextElement();
                }
                Sorter s = new Sorter();
                s.sort(keys);
            }

            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                String defValue = (String) defSettings.get(key);
                String value = (String) Settings.get(key);
                writeLine("[CONF LIST] " + key + " = \"" + value + "\"" + (!defValue.equals(value) ? " (\"" + defValue + "\")" : ""));
            }
        } else if (line.equals("conf save")) {
            Settings.save();
        } else if (line.startsWith("conf ")) {
            String content = line.substring("conf ".length());
            int p = content.indexOf('=');
            if (p != -1) {
                String key = content.substring(0, p);
                String value = content.substring(p + 1);
                Settings.set(key, value);
                value = Settings.get(key);
                writeLine("[CONF SET] " + key + " = \"" + value + "\"");
            } else {
                String value = Settings.get(content);
                writeLine("[CONF GET] " + content + " = \"" + value + "\"");
            }
        } else if (line.equals("stats")) {
            Runtime.getRuntime().
                    gc();
            writeLine("[STATS] Threads:" + Thread.activeCount());
            writeLine("[STATS] MEM free:" + Runtime.getRuntime().freeMemory() + " total:" + Runtime.getRuntime().totalMemory());
        } else if (line.equals("history")) {
            Enumeration e = history.elements();

            while (e.hasMoreElements()) {
                String h = (String) e.nextElement();
                writeLine("[HISTORY] " + h);
            }
        } else if (line.startsWith("log ")) {
            String spl[] = Strings.split(' ', line);
            if (spl.length >= 2) {
                String value = spl[1];
                boolean en = value.equals("1");
                Logger.setStdoutLogging(en);
            }
        } else {
            writeLine("[ERROR] Command \"" + line + "\" not found. Type help for help.");
        }
    }

    private void portOpen() throws IOException {
        out = new PrintStream(stream.openOutputStream());
    }

    private void portClose() {
        if (in != null) {
            try {
                in.close();
            } catch (Exception ex) {
            }
        }
        in = null;

        if (out != null) {
            try {
                out.close();
            } catch (Exception ex) {
            }
        }
        out = null;

        if (stream != null) {
            try {
                stream.close();
            } catch (Exception ex) {
            }
        }
    }
    StringBuffer _buffer = new StringBuffer(64);
    Vector history = new Vector();

    private void addHistory(String line) {
        history.addElement(line);
        if (history.size() > 30) {
            history.removeElementAt(0);
        }
    }

    public String readLine() {
        try {
            while (true) {
                int c = in.read();
                if (c == '\n' || c == '\r') { // If we have an end of line
                    String str = _buffer.toString();
                    _buffer.setLength(0);

                    out.write(c);

                    return str;
                } else if (c == 127) { // If we have backspace
                    if (_buffer.length() > 0) { // If there's chars to remove
                        out.write(c);
                        _buffer.setLength(_buffer.length() - 1);
                    }
                } else if (c == 13) {
                    return "";
                } else if (c == 27) {
                    // I'm not really sure about this part
                    handleEscape(getEscapeSequence());
                } else { // We add chars to the buffer
                    _buffer.append((char) c);
                    out.write(c);
                }
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("Console.readLine", ex);
            }
        }

        return null;
    }

    private int[] getEscapeSequence() throws IOException {
        int c;
        int[] esc = new int[10];
        esc[0] = in.read();
        if (esc[0] == '[') {
            for (int i = 1; i < 10; i++) {
                c = in.read();
                esc[i] = c;
                if (c >= 64 && c <= 95) {
                    break;
                }
            }
        } else {
            esc[1] = in.read();
        }
        return esc;
    }

    // TODO: test it
    private void writeErasePrompt() {
        out.write(27);
        out.write('[');
        out.write('1');
        out.write('S');
        out.print(PROMPT);
        _buffer.setLength(0);
    }

    // TODO: test it
    private void writeClearScreen() {
        out.write(27);
        out.write('[');
        out.write('2');
        out.write('J');
    }

    // TODO: test it
    private void handleEscape(int[] escapeSequence) {
        if (escapeSequence[0] == '[') {
            if (escapeSequence[2] == 'A') {
                out.print("<UP>");
            } else if (escapeSequence[2] == 'B') {
                out.print("<DOWN>");
            } else if (escapeSequence[2] == 'C') {
                out.print("<FORWARD>");
                writeClearScreen();
            } else if (escapeSequence[2] == 'D') {
                out.print("<BACKWARD>");
                writeErasePrompt();
            }
        }
    }
    private static final String PROMPT = "\r\nconsole# ";

    public void run() {
        try {
            portOpen();
            while (true) {
                out.print(PROMPT);
                out.flush();
                String line = readLine().
                        trim();
                try {
                    if (line.length() > 0) {
                        parseCommand(line);
                    }
                } catch (Exception ex) {
                    if (Logger.BUILD_CRITICAL) {
                        Logger.log("Console.parseCommand: \"" + line + "\"", ex, true);
                    }
                }

            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("Console.run", ex, true);
            }
        }
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        portClose();
    }
}
