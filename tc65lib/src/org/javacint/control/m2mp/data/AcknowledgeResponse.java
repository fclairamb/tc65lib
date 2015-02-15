package org.javacint.control.m2mp.data;

public class AcknowledgeResponse extends Message {

    public byte nb;

    public AcknowledgeResponse(byte nb) {
        this.nb = nb;
    }

    public String toString() {
        return "AcknowledgeResponse(" + nb + ")";
    }
}
