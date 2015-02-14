package org.javacint.control.m2mp;

public interface M2MPClient extends M2MPSender {

    void setListener(M2MPEventsListener listener);

    void start() throws Exception;

    void stop() throws Exception;

    public void setIdent(String string);

    public void setCapabilities(String echoloc);

    public void setStatus(String name, String value);
}
