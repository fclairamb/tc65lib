package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.email.Email;
import org.javacint.email.EmailSender;
import org.javacint.task.Timers;

/**
 * Email command.
 */
public class EmailCommand implements ConsoleCommand {

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.equals("email")) {
            Email email = new Email();
            out.println("From: ");
            email.from = Console.readLine(is, out);
            out.println("");
            out.println("To: ");
            email.to = Console.readLine(is, out);
            out.println("");
            out.println("Subject: ");
            email.subject = Console.readLine(is, out);
            out.println("");
            out.println("Content:");
            {
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = Console.readLine(is, out)).length() > 0) {
                    sb.append(line);
                    sb.append('\n');
                    out.println("");
                }
                email.message = sb.toString();
            }
            out.println("");
            out.println("Server: ");
            String server = Console.readLine(is, out);
            EmailSender sender = new EmailSender(server, email);
            out.println("Sending...");
            Timers.getSlow().schedule(sender, 0);
            return true;
        } else if (command.equals("help")) {
            out.println("[HELP] email                            - Send an email");
        }
        return false;
    }
}
