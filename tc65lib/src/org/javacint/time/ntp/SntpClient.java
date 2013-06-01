package org.javacint.time.ntp;

import java.io.IOException;
import java.util.TimerTask;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import org.javacint.logging.Logger;

/**
 * NtpClient - an NTP client for Java. This program connects to an NTP server
 * and prints the response to the console.
 *
 * The local clock offset calculation is implemented according to the SNTP
 * algorithm specified in RFC 2030.
 *
 * Note that on windows platforms, the curent time-of-day timestamp is limited
 * to an resolution of 10ms and adversely affects the accuracy of the results.
 *
 *
 * This code is copyright (c) Adam Buckley 2004
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. A HTML version of the GNU General Public License can be seen at
 * http://www.gnu.org/licenses/gpl.html
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * @author Adam Buckley
 */
public class SntpClient extends TimerTask {

    private final String server;

    public SntpClient(String server) {
        this.server = server;
    }

    public void run() {
        try {
            byte[] buf = new NtpMessage().toByteArray();
            DatagramConnection conn = (DatagramConnection) Connector.open("udp://" + server + ":123");
            Datagram dtgm = conn.newDatagram(buf, buf.length);

            // Set the transmit timestamp *just* before sending the packet
            // ToDo: Does this actually improve performance or not?
            NtpMessage.encodeTimestamp(dtgm.getData(), 40,
                    (System.currentTimeMillis() / 1000.0) + 2208988800.0);

            conn.send(dtgm);


            // Get response
            if (Logger.BUILD_DEBUG) {
                Logger.log("NTP request sent, waiting for response...");
            }
            dtgm.reset();
            conn.receive(dtgm);


            // Immediately record the incoming timestamp
            double destinationTimestamp =
                    (System.currentTimeMillis() / 1000.0) + 2208988800.0;


            // Process response
            NtpMessage msg = new NtpMessage(dtgm.getData());

            // Corrected, according to RFC2030 errata
            double roundTripDelay = (destinationTimestamp - msg.originateTimestamp)
                    - (msg.transmitTimestamp - msg.receiveTimestamp);

            double localClockOffset =
                    ((msg.receiveTimestamp - msg.originateTimestamp)
                    + (msg.transmitTimestamp - destinationTimestamp)) / 2;


            // Display response
            if (Logger.BUILD_DEBUG) {
                Logger.log("NTP server: " + server);
                Logger.log(msg.toString());

                Logger.log("Dest. timestamp:     "
                        + NtpMessage.timestampToString(destinationTimestamp));

                Logger.log("Round-trip delay: " + (roundTripDelay * 1000) + " ms");

                Logger.log("Local clock offset: " + (localClockOffset * 1000) + " ms");
            }

            conn.close();
        } catch (IOException ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("SntpClient", ex, true);
            }
        }
    }
}