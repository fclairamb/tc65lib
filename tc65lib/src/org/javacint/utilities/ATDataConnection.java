package org.javacint.utilities;

import org.javacint.io.Connection;
import org.javacint.io.ConnectionProfile;

final class ATDataConnection extends Connection {

    public boolean open(ConnectionProfile paramConnectionProfile) {
        try {
            String connect;
            if (((connect = ATClass.getDataATCommand().send(paramConnectionProfile.getProfile() + '\r')).indexOf("OK") > 0) || (connect.indexOf("CONNECT") > 0)) {
                this.is = ATClass.getDataATCommand().getDataInputStream();
                this.os = ATClass.getDataATCommand().getDataOutputStream();
                return true;
            }
        } catch (Exception e) {
            Log.add2Log(paramConnectionProfile.getProfile() + e.getMessage(), getClass());
        }
        return false;
    }
}

/* Location:           C:\Documents and Settings\PyTh0n\libX700\lib\libX700.jar
 * Qualified Name:     atcore.a
 * JD-Core Version:    0.6.0
 */

