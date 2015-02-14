package org.javacint.control.m2mp.data;

public class ReceivedCommand extends Event {

    public String[] argv;
    public String id;

    public ReceivedCommand(String cmdId, String[] argv) {
        this.id = cmdId;
        this.argv = argv;
    }
}
