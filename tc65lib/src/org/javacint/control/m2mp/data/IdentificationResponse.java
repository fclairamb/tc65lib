package org.javacint.control.m2mp.data;

public class IdentificationResponse extends Message {

    public boolean identified;

    public IdentificationResponse(boolean response) {
        this.identified = response;
    }

    public String toString() {
        return "IdentificationResponse(" + identified + ")";
    }
}
