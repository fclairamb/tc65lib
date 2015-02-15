package org.javacint.control.m2mp.data;

public class IdentificationRequest extends Message {

    public String ident;

    public IdentificationRequest(String ident) {
        this.ident = ident;
    }

    public String toString() {
        return "IdentificationRequest(" + ident + ")";
    }
}
