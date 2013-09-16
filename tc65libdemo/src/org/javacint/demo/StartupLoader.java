package org.javacint.demo;

import java.util.TimerTask;
import org.javacint.console.ConsiderUpdateCommand;
import org.javacint.console.ConsoleBySetting;
import org.javacint.console.DateCommand;
import org.javacint.console.IDCommand;
import org.javacint.console.NTPTestCommand;
import org.javacint.io.Streams;
import org.javacint.loading.Loader;
import org.javacint.loading.NamedRunnable;
import org.javacint.logging.Logger;
import org.javacint.otap.AutoUpdater;
import org.javacint.settings.Settings;
import org.javacint.sms.PingSMSConsumer;
import org.javacint.sms.SMSReceiver;
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
                WatchdogManager.add(loader);
                WatchdogManager.add(new WatchdogEmbedded());
                WatchdogManager.add(new WatchdogOnJavaGpio(8, false));
                WatchdogManager.start( 5000, 20000); // 20s is a reasonnable watchdog timer
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
                Global.console.addCommandReceiver(new DateCommand());
                Global.console.addCommandReceiver(new NTPTestCommand());
                Global.console.addCommandReceiver(new ConsiderUpdateCommand(version));
                Global.console.addCommandReceiver(new IDCommand());
                // Nothing prevents us from loading other console (one on an other port, one on a socket listening handler, one on a client socket, etc.)
            }
        });

        loader.addRunnable(new NamedRunnable("SMS:loading") {
            public void run() throws Exception {
                SMSReceiver smsr = SMSReceiver.getInstance();
                smsr.addConsumer(new SMSHandler());
                smsr.addConsumer(new PingSMSConsumer());
            }
        });

        // Once we reach that stage, we can use settings
        loader.addRunnable(new NamedRunnable("Settings:loaded") {
            public void run() throws Exception {
                Settings.loading(false);
            }
        });

        // After that, all the settings consumers can receive settings changes 
        // and use settings

        // We start the console
        loader.addRunnable(new NamedRunnable("Console:starting") {
            public void run() throws Exception {
                Global.console.start();
            }
        });

        // Here we are ready to handle SMS and we should read them already 
        // present in the chip
        loader.addRunnable(new NamedRunnable("SMS:starting") {
            public void run() throws Exception {
                SMSReceiver.getInstance().start();
            }
        });

        loader.addRunnable(new NamedRunnable("AutoUpdater:scheduled") {
            public void run() throws Exception {
                // We will try to find an update every 15 minutes
                AutoUpdater.schedule(version, 900 * 1000);
            }
        });

        loader.addRunnable(new NamedRunnable("Watchdog:started") {
            public void run() throws Exception {
                // We have finished loading so we don't need to monitor this anymore
                WatchdogManager.remove(loader);
            }
        });
        Timers.getSlow().schedule(loader, 0);
    }
}
