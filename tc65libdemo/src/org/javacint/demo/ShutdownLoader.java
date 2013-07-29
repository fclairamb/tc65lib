package org.javacint.demo;

import java.util.TimerTask;
import org.javacint.loading.Loader;
import org.javacint.loading.NamedRunnable;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.watchdog.WatchdogManager;

/**
 * Shutdown loader.
 */
public class ShutdownLoader extends TimerTask {

    private final boolean uncond;

    public ShutdownLoader(boolean uncond) {
        this.uncond = uncond;
    }

    public void run() {

        Loader loader = new Loader();

        Logger.log("Midlet.destroy( " + uncond + " );");

        loader.addRunnable(new NamedRunnable("Settings:save") {
            public void run() throws Exception {
                Settings.save();
            }
        });

        loader.run();
    }
}
