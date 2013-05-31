package org.javacint.at;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif

/**
 * Pooled ATCommand instance wrapper.
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
}
