/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.io;

/**
 *
 * @author florent
 */
public class SerialProfile implements ConnectionProfile {

    private final int port, speed;

    public SerialProfile(int port, int speed) {
        this.port = port;
        this.speed = speed;
    }

    public String getProfile() {
        return "comm:com" + port + ";baudrate=" + speed + ";blocking=on;autocts=off;autorts=off";
    }
}
