/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.demo;

import org.javacint.logging.Logger;
import org.javacint.sms.SMSConsumer;
import org.javacint.sms.SimpleSMS;

/**
 * SMS handler.
 */
public class SMSHandler implements SMSConsumer {

    public boolean smsReceived(String from, String content) {
        Logger.log("SMSHandler: " + from + ", " + content);

        // If we have something that we are supposed to handle
        if (content.toLowerCase().trim().equals("hello")) {
            // We handle it
            SimpleSMS.send(from, "Hello back !");

            // And we stop the consuming chain here
            return true;
        }
        return false;
    }
}
