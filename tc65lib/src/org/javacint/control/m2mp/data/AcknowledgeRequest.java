package org.javacint.control.m2mp.data;

public class AcknowledgeRequest extends Message {

    public byte nb;

    public AcknowledgeRequest(byte nb) {
        this.nb = nb;
    }

    public String toString() {
        return "AcknowledgeRequest(" + nb + ")";
    }
}
