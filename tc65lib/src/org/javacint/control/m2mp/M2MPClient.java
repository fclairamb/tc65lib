package org.javacint.control.m2mp;

import org.javacint.control.m2mp.data.Event;

public interface M2MPClient {

    void start() throws Exception;

    void stop() throws Exception;

    void setListener(M2MPEventsListener listener);

    public void setIdent(String string);

    public void setCapabilities(String echoloc);

    public void setStatus(String name, String value);

    public void send(Event event);
}
