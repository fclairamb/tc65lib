/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.at;

import com.siemens.icm.io.ATCommand;

/**
 *
 * @author florent
 */
public class ATCommandPooled {

    private final ATCommand atc;
    private final byte bitInstance;

    ATCommandPooled(ATCommand atc, byte bitInstance) {
        this.atc = atc;
        this.bitInstance = bitInstance;
    }

    ATCommand getATCommand() {
        return atc;
    }

    byte getBit() {
        return bitInstance;
    }

    public String send(String cmd) {
        return ATCommands.send(atc, cmd);
    }

    public String sendRaw(String cmd) {
        return ATCommands.sendRaw(atc, cmd);
    }

    public void release() {
        ATCommands.release(this);
    }

    protected void finalize() {
        // This is just in case you forget to actually use it
        release();
    }
}
