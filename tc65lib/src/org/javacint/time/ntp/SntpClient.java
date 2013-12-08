package org.javacint.time.ntp;

import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import org.javacint.io.Streams;
import org.javacint.logging.Logger;
import org.javacint.time.TimeClient;

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
public class SntpClient implements TimeClient {

    private final String server;
    private static final boolean LOG = true;

    public SntpClient(String server) {
        this.server = server;
    }

    public long getTime() throws Exception {
        if (Logger.BUILD_DEBUG && LOG) {
            Logger.log("NTP server: " + server);
        }
        byte[] buf = new NtpMessage().toByteArray();
        DatagramConnection conn = Streams.udp(server, 123);
        try {
            Datagram dtgm = conn.newDatagram(buf, buf.length);

            // Set the transmit timestamp *just* before sending the packet
            // ToDo: Does this actually improve performance or not?
            NtpMessage.encodeTimestamp(dtgm.getData(), 40,
                    (System.currentTimeMillis() / 1000.0) + 2208988800.0);

            conn.send(dtgm);


            // Get response
            if (Logger.BUILD_DEBUG && LOG) {
                Logger.log("NTP request sent (" + dtgm.getLength() + " bytes), waiting for response...");
            }
            //dtgm = conn.newDatagram(1024);
            conn.receive(dtgm);
            if (Logger.BUILD_DEBUG && LOG) {
                Logger.log("NTP response received (" + dtgm.getLength() + " bytes)");
            }

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
            if (Logger.BUILD_DEBUG && LOG) {
                Logger.log(msg.toString());

                Logger.log("Dest. timestamp:     "
                        + NtpMessage.timestampToString(destinationTimestamp));

                Logger.log("Round-trip delay: " + (roundTripDelay * 1000) + " ms");

                Logger.log("Local clock offset: " + (localClockOffset * 1000) + " ms");
            }
            return (long) msg.getTime() / 1000;
        } finally {
            conn.close();
        }
    }
}