package org.javacint.demo;

import java.util.TimerTask;
import org.javacint.console.ConsoleBySetting;
import org.javacint.io.Streams;
import org.javacint.loading.Loader;
import org.javacint.loading.NamedRunnable;
import org.javacint.logging.Logger;
import org.javacint.otap.AutoUpdater;
import org.javacint.settings.Settings;
import org.javacint.task.Timers;
import org.javacint.watchdog.WatchdogEmbedded;
import org.javacint.watchdog.WatchdogManager;
import org.javacint.watchdog.WatchdogOnJavaGpio;

/**
 * Loader wrapper.
 */
public class StartupLoader extends TimerTask {

    private final String version;

    public StartupLoader(String version) {
        this.version = version;
    }

    public void run() {
        final Loader loader = new Loader();

        if (Logger.BUILD_NOTICE) {
            Logger.log("TC65LibDemo v" + version);
        }

        // We register the loader to the watchdog management class 
        // to make sure the program actually load in reasonnable time.
        loader.addRunnable(new NamedRunnable("Watchdog:start") {
            public void run() throws Exception {
                WatchdogManager wd = WatchdogManager.getInstance();
                wd.addStatusProvider(loader);
                wd.addWatchdogActors(new WatchdogEmbedded());
                wd.addWatchdogActors(new WatchdogOnJavaGpio(8, false));
                Timers.getFast().schedule(wd, 5000, 20000); // 20s is a reasonnable watchdog timer
            }
        });

        // We enable the loading mode of settings. In this mode, only the settings
        // consumers are provided.
        loader.addRunnable(new NamedRunnable("Settings:loading") {
            public void run() throws Exception {
                Settings.loading(true);
                Settings.addProvider(new BaseSettingsProvider());
            }
        });

        // All the settings consumers/providers must be added here (without being allowed to consume setting at this stage)

        // We load the console
        loader.addRunnable(new NamedRunnable("Console:loading") {
            public void run() throws Exception {
                Global.console = new ConsoleBySetting(Streams.serial(0, 115200));
                // Nothing prevents us from loading other console (one on an other port, one on a socket listening handler, one on a client socket, etc.)
            }
        });

        // Once we reach that stage, we can use settings
        loader.addRunnable(new NamedRunnable("Settings:loaded") {
            public void run() throws Exception {
                Settings.loading(false);
            }
        });

        // After that, all the settings providers can provider and use settings

        // We start the console
        loader.addRunnable(new NamedRunnable("Console:starting") {
            public void run() throws Exception {
                Global.console.start();
            }
        });

        loader.addRunnable(new NamedRunnable("AutoUpdater") {
            public void run() throws Exception {
                // We will try to find an update every 15 minutes
                AutoUpdater.schedule(version, 900 * 1000);
            }
        });

        loader.addRunnable(new NamedRunnable("Watchdog:started") {
            public void run() throws Exception {
                WatchdogManager.getInstance().removeStatusProvider(loader);
            }
        });
        Timers.getSlow().schedule(loader, 0);
    }
}
