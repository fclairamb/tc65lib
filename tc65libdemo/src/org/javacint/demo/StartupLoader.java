package org.javacint.demo;

import java.util.TimerTask;
import org.javacint.apnauto.APNAutodetection;
import org.javacint.at.ATExecution;
import org.javacint.console.AddNetworkConsoleCommand;
import org.javacint.console.ConsiderUpdateCommand;
import org.javacint.console.Console;
import org.javacint.console.DateCommand;
import org.javacint.console.EmailCommand;
import org.javacint.console.FileNavigationCommandReceiver;
import org.javacint.console.GPSTestCommand;
import org.javacint.console.IDCommand;
import org.javacint.console.NTPTestCommand;
import org.javacint.console.PinManagementCommandReceiver;
import org.javacint.console.SmsSenderCommand;
import org.javacint.console.UptimeCommand;
import org.javacint.console.VersionCommand;
import org.javacint.io.Connections;
import org.javacint.loading.Loader;
import org.javacint.loading.NamedRunnable;
import org.javacint.logging.Logger;
import org.javacint.otap.AutoUpdater;
import org.javacint.settings.Settings;
import org.javacint.sms.PingSMSConsumer;
import org.javacint.sms.SMSReceiver;
import org.javacint.sms.SimpleSMS;
import org.javacint.sms.StandardFeaturesSMSConsumer;
import org.javacint.task.Timers;
import org.javacint.time.TimeRetriever;
import org.javacint.time.ntp.SntpClient;
import org.javacint.watchdog.WatchdogEmbedded;
import org.javacint.watchdog.WatchdogManager;
import org.javacint.watchdog.WatchdogOnJavaGpio;
import org.javacint.watchdog.WatchdogStatusProvider;

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

        loader.addRunnable(new NamedRunnable("init") {
            public void run() {
                ATExecution.setSsync(0);
            }
        });

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
                    Global.console = new Console(Connections.serial(0, 115200));
                    Global.console.addCommandReceiver(new DateCommand());
                    Global.console.addCommandReceiver(new NTPTestCommand());
                    Global.console.addCommandReceiver(new ConsiderUpdateCommand(version));
                    Global.console.addCommandReceiver(new IDCommand());
                    Global.console.addCommandReceiver(new GPSTestCommand());
                    Global.console.addCommandReceiver(new VersionCommand(version));
                    Global.console.addCommandReceiver(new UptimeCommand());
                    Global.console.addCommandReceiver(new FileNavigationCommandReceiver());
                    Global.console.addCommandReceiver(new SmsSenderCommand());
                    Global.console.addCommandReceiver(new AddNetworkConsoleCommand(Global.console));
                    Global.console.addCommandReceiver(new PinManagementCommandReceiver());
                    Global.console.addCommandReceiver(new EmailCommand());
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
                ATExecution.setSsync(2);
            }
        });

        if (false) { // This is some watchdog testing code
            loader.addRunnable(new NamedRunnable("Uptime SMS Sender") {
                private final long start = System.currentTimeMillis();
                private final String dest = "+33686955405";

                public void run() {
                    Timers.getSlow().schedule(new TimerTask() {
                        public void run() {
                            SimpleSMS.send(dest, "Uptime is " + (System.currentTimeMillis() - start) / 1000 + "s");
                        }
                    }, 0, 900000);
                    WatchdogManager.add(new WatchdogStatusProvider() {
                        private boolean sent;

                        public String getWorkingStatus() {
                            if ((System.currentTimeMillis() - start) > (3600 * 1000)) {
                                if (!sent) {
                                    sent = true;
                                    SimpleSMS.send(dest, "We need to restart now !");
                                }
                                return "1h have passed, We want to restart now";
                            }

                            return null;
                        }
                    });
                }
            });
        }
        // And we start the loading task right now.
        Timers.getSlow().schedule(loader, 0);
    }
}
