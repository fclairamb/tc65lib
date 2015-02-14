package org.javacint.control.m2mp.data;

import java.util.Vector;
import org.javacint.common.Bytes;

public class NamedDataArray extends Message {

    public String name;
    public byte[][] data;

    public NamedDataArray(String name, byte[][] data) {
        this.name = name;
        this.data = data;
    }

    public NamedDataArray(String name, String[] data) {
        this.name = name;
        this.data = Bytes.stringsToBytes(data);
    }

    public NamedDataArray(String name, Vector data) {
        this.name = name;
        this.data = Bytes.stringsToBytes(data);
    }

    public String toString() {
        return "NamedDataArray(" + name + ", byte[][" + data.length + "] );";
    }
}
