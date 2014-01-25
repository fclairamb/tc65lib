package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Vector;
import org.javacint.logging.Logger;
import org.javacint.sms.SimpleSMS;

/**
 * sms sending console command.
 *
 * This sms allows to send SMS from the console by typing "sms send [recipient]
 * [message]"
 *
 */
public class SmsSenderCommand implements ConsoleCommand {

    public boolean consoleCommand(String line, InputStream in, PrintStream out) {
        try {
            if (line.startsWith("sms send ")) {
                Vector ret = new Vector();

                String content = line.substring(9);
                int p = content.indexOf(' ');
                String phone = content.substring(0, p);
                String message = content.substring(p + 1);

                ret.addElement("[SMS SEND] " + (SimpleSMS.send(phone, message) ? "OK" : "FAIL") + " ! ");

                return true;
            } else if (line.equals("help")) {
                out.println("[HELP] sms send <tel> <message>         - Send a message <message> to <tel>");
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".consoleCommand(\"" + line + "\")", ex, true);
            }
        }
        return false;
    }
}
