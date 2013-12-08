package org.javacint.demo;

import java.io.IOException;
import java.util.TimerTask;
import org.javacint.apnauto.APNAutodetection;
import org.javacint.console.ConsiderUpdateCommand;
import org.javacint.console.ConsoleBySetting;
import org.javacint.console.DateCommand;
import org.javacint.console.FileNavigationCommandReceiver;
import org.javacint.console.GPSTestCommand;
import org.javacint.console.IDCommand;
import org.javacint.console.NTPTestCommand;
import org.javacint.console.SmsSenderCommand;
import org.javacint.console.UptimeCommand;
import org.javacint.console.VersionCommand;
import org.javacint.io.Streams;
import org.javacint.loading.Loader;
import org.javacint.loading.NamedRunnable;
import org.javacint.logging.Logger;
import org.javacint.otap.AutoUpdater;
import org.javacint.settings.Settings;
import org.javacint.sms.PingSMSConsumer;
import org.javacint.sms.SMSReceiver;
import org.javacint.sms.StandardFeaturesSMSConsumer;
import org.javacint.task.Timers;
import org.javacint.time.TimeClient;
import org.javacint.time.TimeRetriever;
import org.javacint.time.ntp.SntpClient;
import org.javacint.watchdog.WatchdogEmbedded;
import org.javacint.watchdog.WatchdogManager;
import org.javacint.watchdog.WatchdogOnJavaGpio;

/**
 * Loader wrapper.
 *
 * We add all the startup tasks to be able to monitor them and print it nicely.
 *
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
            public void run() {
                WatchdogManager.add(loader);
                WatchdogManager.add(new WatchdogEmbedded());
                WatchdogManager.add(new WatchdogOnJavaGpio(9, false));
                WatchdogManager.start(5000, 20000); // 20s is a reasonnable watchdog timer
            }
        });

        // We enable the loading mode of settings. In this mode, only the settings
        // consumers are provided.
        loader.addRunnable(new NamedRunnable("Settings:loading") {
            public void run() {
                Settings.loading(true);
                Settings.addProvider(new BaseSettingsProvider());
            }
        });

        // All the settings consumers/providers must be added here (without being allowed to consume setting at this stage)

        // We load the console
        loader.addRunnable(new NamedRunnable("Console:loading") {
            public void run() {
                try {
                    Global.console = new ConsoleBySetting(Streams.serial(0, 115200));
                    Global.console.addCommandReceiver(new DateCommand());
                    Global.console.addCommandReceiver(new NTPTestCommand());
                    Global.console.addCommandReceiver(new ConsiderUpdateCommand(version));
                    Global.console.addCommandReceiver(new IDCommand());
                    Global.console.addCommandReceiver(new GPSTestCommand());
                    Global.console.addCommandReceiver(new VersionCommand(version));
                    Global.console.addCommandReceiver(new UptimeCommand());
                    Global.console.addCommandReceiver(new FileNavigationCommandReceiver());
                    Global.console.addCommandReceiver(new SmsSenderCommand());
                } catch (Exception ex) {
                    if (Logger.BUILD_CRITICAL) {
                        Logger.log("Console:loading", ex, true);
                    }
                }
            }
        });

        loader.addRunnable(new NamedRunnable("SMS:loading") {
            public void run() {
                SMSReceiver.addConsumer(new StandardFeaturesSMSConsumer());
                SMSReceiver.addConsumer(new SMSHandler());
                SMSReceiver.addConsumer(new PingSMSConsumer());
            }
        });

        // Once we reach that stage, we can use settings
        loader.addRunnable(new NamedRunnable("Settings:loaded") {
            public void run() {
                Settings.loading(false);
            }
        });

        // After that, all the settings consumers can receive settings changes 
        // and use settings

        // We start the console
        loader.addRunnable(new NamedRunnable("Console:starting") {
            public void run() {
                Global.console.start();
            }
        });

        loader.addRunnable(new NamedRunnable("APNAuto:starting") {
            public void run() {
                APNAutodetection auto = new APNAutodetection();
                auto.addDefaultParameters();
                auto.autoLoadRightGPRSSettings();
            }
        });

        // Here we are ready to handle SMS and we should read them already 
        // present in the chip
        loader.addRunnable(new NamedRunnable("SMS:starting") {
            public void run() {
                SMSReceiver.start();
            }
        });

        loader.addRunnable(new NamedRunnable("AutoUpdater:scheduled") {
            public void run() {
                // We will try to find an update every 15 minutes by comparing the version
                // defined in the JAD file as the jadurl's address.
                AutoUpdater.schedule(version, 60 * 1000);
            }
        });

        loader.addRunnable(new NamedRunnable("TimeUpdater:scheduled") {
            public void run() {
                // This will get the time every 24h
                new TimeRetriever(new SntpClient()).schedule();
            }
        });

        loader.addRunnable(new NamedRunnable("Watchdog:started") {
            public void run() {
                // We have finished loading so we don't need to monitor this anymore
                WatchdogManager.remove(loader);
            }
        });

        // And we start the loading task right now.
        Timers.getSlow().schedule(loader, 0);
    }
}
