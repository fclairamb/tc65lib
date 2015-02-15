package org.javacint.control.m2mp.data;

public class NamedData extends Message {

    public final String name;
    public final byte[] data;

    public NamedData(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public NamedData(String name, String data) {
        this.name = name;
        this.data = data.getBytes();
    }

    public String toString() {
        return "NamedData( \"" + name + "\", byte[" + data.length + "] )";
    }
}
