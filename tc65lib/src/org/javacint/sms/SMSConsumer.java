/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.sms;

/**
 *
 * @author florent
 */
public interface SMSConsumer {

    public boolean smsReceived(String from, String content);
}
