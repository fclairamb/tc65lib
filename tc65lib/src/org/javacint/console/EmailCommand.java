/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.console;

import java.io.InputStream;
import java.io.PrintStream;
import org.javacint.common.BufferedReader;
import org.javacint.email.Email;
import org.javacint.email.EmailSender;
import org.javacint.task.Timers;

/**
 * Email command.
 */
public class EmailCommand implements ConsoleCommand {

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        if (command.equals("email")) {
            BufferedReader reader = new BufferedReader(is);
            Email email = new Email();
            out.print("From: ");
            email.from = reader.readLine();
            out.print("To: ");
            email.to = reader.readLine();
            out.print("Subject: ");
            email.subject = reader.readLine();
            out.println("Content:");
            {
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = reader.readLine()).length() > 0) {
                    sb.append(line);
                }
                email.message = sb.toString();
            }
            out.print("Server: ");
            String server = reader.readLine();
            EmailSender sender = new EmailSender(server, email);
            out.println("Sending...");
            Timers.getSlow().schedule(sender, 0);
            return true;
        }
        return false;
    }
}