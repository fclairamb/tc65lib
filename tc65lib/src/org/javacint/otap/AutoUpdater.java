package org.javacint.otap;

import java.io.IOException;
import java.util.TimerTask;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import org.javacint.at.ATExecution;
import org.javacint.common.BufferedReader;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.task.Timers;

/**
 * Program auto-updating class.
 *
 * This class checks if the current version matches the target version. If not,
 * it triggers the OTAP process.
 *
 * The class retries at each scheduled period to fetch the version number. If it
 * could fetch it, it doesn't retry until the "timeBetweenUpdates" period has
 * expired. If the fetched version is superior to the current version, the OTAP
 * process is triggered.
 *
 */
public class AutoUpdater extends TimerTask {

    private final String version;
    protected final int timeBetweenUpdates;
    private String userAgent;
    protected static final String JAD_FIELD_VERSION = "MIDlet-Version:";
    protected long lastDone;
    private Runnable before;
    private static final boolean LOG = false;

    /**
     * Constructor.
     *
     * @param version Current version of the program ("Midlet-Version" is
     * usually stored in Global.version)
     * @param timeBetweenUpdates Time in milliseconds between updates
     *
     * Note: the scheduling should be more frequent than the time between
     * updates (like every hour) because fetching the version might fail.
     *
     * Sample usage: <pre>
     * AutoUpdater up = new AutoUpdate(Global.atc, Global.version, "http://server/version.txt");
     * Global.slowTimer.schedule ( up, 0, 3600 * 1000 );
     * </pre> This will make it check the version now and then every
     * [timeBetweenUpdates] period (in millisecond). If it fails, it will check
     * every hour until it has a response from the server.
     */
    public AutoUpdater(String version, int timeBetweenUpdates) {
        this.version = version;
        this.timeBetweenUpdates = timeBetweenUpdates;
    }

    /**
     * Schedule an auto update frequently.
     *
     * @param version Current version of the program
     * @param timeBetweenUpdates Time between each update check
     * @return AutoUpdater instance
     */
    public static AutoUpdater schedule(String version, int timeBetweenUpdates) {
        AutoUpdater au = new AutoUpdater(version, timeBetweenUpdates);
        Timers.getSlow().schedule(au, 0, timeBetweenUpdates / 3);
        return au;
    }

    /**
     * Schedule an auto-update attempt once.
     *
     * @param version Current version of the program
     * @return AutoUpdater instance
     */
    public static AutoUpdater schedule(String version) {
        AutoUpdater au = new AutoUpdater(version);
        Timers.getSlow().schedule(au, 0);
        return au;
    }

    public AutoUpdater setBefore(Runnable before) {
        this.before = before;
        return this;
    }

    /**
     * Constructor.
     *
     * The timeBeforeUpdate arg is set to a default value of 24h (24*3600*1000
     * milliseconds)
     *
     * @param version Current version of the program ("Midlet-Version" is
     * usually stored in Global.version)
     */
    public AutoUpdater(String version) {
        this(version, 24 * 3600 * 1000);
    }

    public void run() {
        if (Logger.BUILD_NOTICE && LOG) {
            Logger.log(this + ".run();");
        }
        try {
            if (userAgent == null) {
                userAgent = "tc65lib/" + ATExecution.getImei() + "/" + version + "/" + ATExecution.getIccid();
            }
            if (needsUpdate()) {
                if (before != null) {
                    try {
                        before.run();
                    } catch (Throwable ex) {
                    }
                }
                ATExecution.update();
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".run", ex, true);
            }
        }
    }

    protected boolean needsChecking() {
        return System.currentTimeMillis() - lastDone > timeBetweenUpdates;
    }

    /**
     * Tests if the device needs an update.
     *
     * @return If the device needs an update
     * @throws IOException
     */
    private boolean needsUpdate() throws IOException {
        if (!needsChecking()) {
            if (Logger.BUILD_DEBUG && LOG) {
                Logger.log(this + ".needsUpdate: Too short...");
            }
            return false;
        }

        // We get the URL at each run because it might change from one run to an other
        final String url = Settings.get(Settings.SETTING_JADURL);
        HttpConnection conn = (HttpConnection) Connector.open(url);
        conn.setRequestProperty("user-agent", userAgent);
        int rc = conn.getResponseCode();

        // If we get a wrong HTTP response code, we might as well just stop trying
        if (rc != HttpConnection.HTTP_OK) {
            if (Logger.BUILD_NOTICE) {
                Logger.log(this + ".needsUpdate: Could not fetch the version's file !", true);
            }
            cancel();
        }

        String remoteVersion;
        BufferedReader reader = new BufferedReader(conn.openInputStream());

        // If it's a jad file, we search for the version property
        if (url.endsWith(".jad")) {
            remoteVersion = jadFileGetVersion(reader);
        } else {// If it's not, we guess it's the 
            remoteVersion = reader.readLine();
        }
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ": remoteVersion=" + remoteVersion);
        }
        lastDone = System.currentTimeMillis();
        boolean needIt = compareVersions(remoteVersion, version) > 0;
        if (needIt) {
            if (Logger.BUILD_NOTICE) {
                Logger.log(this + " : We will update from " + version + " to " + remoteVersion, true);
            }
        } else {
            if (Logger.BUILD_NOTICE) {
                Logger.log(this + " : No update is required !");
            }
        }
        return needIt;
    }

    /**
     * Compares two versions of the code.
     *
     * @param v1 String of the version
     * @param v2 String of the version
     * @return > 0 if v1 > v2
     *
     * Note: The two versions SHOULD have the same length. If not, only the
     * first numbers are taken into account. Thus: "1.3" == "1.3.2.1"
     */
    private int compareVersions(String v1, String v2) {
        String va1[] = Strings.split('.', v1);
        String va2[] = Strings.split('.', v2);

        for (int i = 0; i < va1.length && i < va2.length; i++) {
            int i1 = Integer.parseInt(va1[i]);
            int i2 = Integer.parseInt(va2[i]);
            if (i1 != i2) {
                return i1 - i2;
            }
        }
        return 0;
    }

    public String toString() {
        return "AutoUpdater";
    }

    protected String jadFileGetVersion(BufferedReader reader) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(JAD_FIELD_VERSION)) {
                return line.substring(JAD_FIELD_VERSION.length()).trim();
            }
        }
        return null;
    }
}
