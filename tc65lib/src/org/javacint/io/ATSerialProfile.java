package org.javacint.io;

public abstract class ATSerialProfile implements ConnectionProfile {

    protected int baudrate = 1;
    protected final int INPUT_BUFFER_SIZE = 256;

    public void setBaudrate(int baudrate) {
        this.baudrate = baudrate;
    }

    public int getBaudrate() {
        return this.baudrate;
    }
}
