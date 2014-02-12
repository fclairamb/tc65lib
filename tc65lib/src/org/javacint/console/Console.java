package org.javacint.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.StreamConnection;
import org.javacint.at.ATCommands;
import org.javacint.at.ATExecution;
import org.javacint.common.Strings;
import org.javacint.common.sorting.Sorter;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;

/**
 * Console management.
 *
 * This class is not static because you could have as many console instances as
 * you want. You can put them on each serial port, on a TCP or UDP server socket
 * and on any StreamConnection implementation.
 */
public class Console implements Runnable {

    private Thread thread = new Thread(this, "con");
    private final StreamConnection stream;
    private InputStream in;
    private PrintStream out;
    private final Vector commandsReceiver = new Vector();

    /**
     * Add a command receiver to the console.
     *
     * @param receiver Command receiver to add
     */
    public void addCommandReceiver(ConsoleCommand receiver) {
        synchronized (commandsReceiver) {
            commandsReceiver.addElement(receiver);
        }
    }

    /**
     * Copy all the command receivers of one console to an other console.
     *
     * Please not that the command receives are the same instance.
     *
     * @param src Source console.
     */
    public void copyCommandReceivers(Console src) {
        synchronized (commandsReceiver) {
            for (Enumeration en = src.commandsReceiver.elements(); en.hasMoreElements();) {
                commandsReceiver.addElement(en.nextElement());
            }
        }
    }

    /**
     * Remove a command receiver to the console.
     *
     * @param receiver Command receiver to remove
     */
    public void removeCommandReceiver(ConsoleCommand receiver) {
        synchronized (commandsReceiver) {
            if (commandsReceiver.contains(receiver)) {
                commandsReceiver.removeElement(receiver);
            }
        }
    }

    /**
     * Console constructor
     *
     * @param stream StreamConnection used to receive and send humanly readable
     * commands.
     */
    public Console(StreamConnection stream) {
        this.stream = stream;
    }

    private void writeLine(String line) throws IOException {
        out.println(line);
    }

    public boolean parseCommand(String line) throws Exception {
        addHistory(line);
        out.println();
        synchronized (commandsReceiver) {
            for (Enumeration en = commandsReceiver.elements(); en.hasMoreElements();) {
                try {
                    ConsoleCommand commandReceiver = (ConsoleCommand) en.nextElement();
                    if (commandReceiver.consoleCommand(line, in, out)) {
                        return true;
                    }
                } catch (Throwable ex) {
                    if (Logger.BUILD_CRITICAL) {
                        out.println("Command receiver " + en + " failed: " + ex.getClass() + " : " + ex.getMessage());
                    }
                }
            }
        }

        if (line.startsWith("AT")) {
            String[] lines = Strings.split('\n', ATCommands.send(line));
            for (int i = 0; i < lines.length; ++i) {
                writeLine("[AT] " + lines[i]);
            }
        } else if (line.startsWith("#AT")) {
            line = line.substring(1);
            String[] lines = Strings.split('\n', ATCommands.sendLong(line));
            for (int i = 0; i < lines.length; ++i) {
                writeLine("[AT slow] " + lines[i]);
            }
        } else if (line.equals("restart")) {
            ATExecution.restart();
        } else if (line.equals("help")) {
            writeLine("[HELP] help                             - This help");
            writeLine("[HELP] conf list                        - List all configuration settings");
            writeLine("[HELP] conf <key>=<value>               - Define a configuration setting");
            writeLine("[HELP] conf <key>                       - Get a configuration setting");
            writeLine("[HELP] conf save                        - Save the configuration settings");
            writeLine("[HELP] restart                          - Restart the device");
            writeLine("[HELP] AT***                            - Send AT commads");
            writeLine("[HELP] stats                            - Get some system stats");
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
            return false;
        }
        return true;
    }

    private void portOpen() throws IOException {
        in = stream.openInputStream();
        out = new PrintStream(stream.openOutputStream());
    }

    private void portClose() {
        if (in != null) {
            try {
                in.close();
            } catch (Exception ex) {
            }
            in = null;
        }

        if (out != null) {
            try {
                out.close();
            } catch (Exception ex) {
            }
            out = null;
        }
    }
    private final Vector history = new Vector();

    private void addHistory(String line) {
        history.addElement(line);
        if (history.size() > 30) {
            history.removeElementAt(0);
        }
    }
    private static final char CTRL_Z = 0x1a;

    /**
     * Simulate a console line reading input.
     *
     * It echoes each chars and returns the string on CR.
     *
     * @param in InputStream for parsing raw keyboard input
     * @param out PrintStream for displaying output
     * @return The line read from the inputstream
     *
     * The output can be null, in that case it means we won't have an echo
     *
     * TODO: There are some problems with this method that we should fix.
     */
    public static String readLine(InputStream in, PrintStream out) {
        try {
            final StringBuffer buffer = new StringBuffer(64);
            while (true) {
                int c = in.read();
                if (c == '\n' || c == '\r' || c == CTRL_Z) { // If we have an end of line
                    String str = buffer.toString();
                    buffer.setLength(0);
                    if (out != null) {
                        out.write(c);
                    }

                    return str;
                } else if (c == 127) { // If we have backspace
                    if (buffer.length() > 0) { // If there's chars to remove
                        if (out != null) {
                            out.write(c);
                        }
                        buffer.setLength(buffer.length() - 1);
                    }
                } else if (c == 13) {
                    return "";
                } else if (c == 27) {
                    // I'm not really sure about this part
                    handleEscape(getEscapeSequence(in), out);
                } else { // We add chars to the buffer
                    buffer.append((char) c);
                    if (out != null) {
                        out.write(c);
                    }
                }
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("Console.readLine", ex);
            }
            return null;
        }
    }

    public String readLine() {
        return readLine(in, out);
    }

    private static int[] getEscapeSequence(InputStream in) throws IOException {
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
    }

    // TODO: test it
    private static void writeClearScreen(PrintStream out) {
        out.write(27);
        out.write('[');
        out.write('2');
        out.write('J');
    }

    // TODO: test it
    private static void handleEscape(int[] escapeSequence, PrintStream out) {
        if (escapeSequence[0] == '[') {
            if (escapeSequence[2] == 'A') {
                out.print("<UP>");
            } else if (escapeSequence[2] == 'B') {
                out.print("<DOWN>");
            } else if (escapeSequence[2] == 'C') {
                out.print("<FORWARD>");
                writeClearScreen(out);
            } else if (escapeSequence[2] == 'D') {
                out.print("<BACKWARD>");
                writeClearScreen(out);
            }
        }
    }
    private static final String PROMPT = "\r\nconsole# ";

    public void run() {
        try {
            portOpen();
            while (true) {
                String line = readLine().trim();
                try {
                    if (line.length() > 0) {
                        parseCommand(line);
                    }
                } catch (Exception ex) {
                    if (Logger.BUILD_CRITICAL) {
                        Logger.log("Console.parseCommand: \"" + line + "\"", ex, true);
                    }
                }
                out.print(PROMPT);
                out.flush();
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
