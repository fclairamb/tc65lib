package org.javacint.io;

import org.javacint.at.ATCommands;
import org.javacint.logging.Logger;

public final class ATDataConnection extends Connection {

    public boolean open(ConnectionProfile paramConnectionProfile) {
        try {
            String connect;
            if (((connect = ATCommands.getATCommandData().send(paramConnectionProfile.getProfile() + '\r')).indexOf("OK") > 0) || (connect.indexOf("CONNECT") > 0)) {
                this.is = ATCommands.getATCommandData().getDataInputStream();
                this.os = ATCommands.getATCommandData().getDataOutputStream();
                return true;
            }
        } catch (Exception e) {
            Logger.log(paramConnectionProfile.getProfile() + e.getMessage(), getClass());
        }
        return false;
    }
}

/* Location:           C:\Documents and Settings\PyTh0n\libX700\lib\libX700.jar
 * Qualified Name:     atcore.a
 * JD-Core Version:    0.6.0
 */
