package org.javacint.demo;

import javax.microedition.midlet.*;
import org.javacint.task.Timers;

public class StartupMidlet extends MIDlet {

    public void startApp() {
        Timers.getSlow().schedule(new StartupLoader(getAppProperty("MIDlet-Version")), 0);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        new ShutdownLoader(unconditional).run();
        notifyDestroyed();
    }

    public String toString() {
        return "Midlet";
    }
}
