/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.i2c.mma7660fc;

import com.siemens.icm.io.InPortListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Timer;
import org.javacint.common.Bytes;
import org.javacint.common.Strings;
import org.javacint.common.TimerTaskProxy;
import org.javacint.console.ConsoleCommand;
import org.javacint.i2c.I2CConnector;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.settings.SettingsProvider;

/**
 * I2C Test command receiver.
 *
 * This allows to send particular I2C commands through the console.
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public final class I2CTestCommandReceiver implements ConsoleCommand, SettingsProvider, MMA7660FCListener {

    private final Timer timer;
    private int slaveId = 0;
    private final static String SETTING_I2C_BAUDRATE = "i2c.baudrate";
    private final static String SETTING_I2C_READDELAY = "i2c.readdelay";
    private final static String SETTING_I2C_WRITEDELAY = "i2c.writedelay";
    private final static String SETTING_I2C_SLAVEID = "i2c.slaveid";
    private final I2CConnector connector = new I2CConnector();
    private final MMA7660FC mma;

    public I2CTestCommandReceiver(Timer timer) throws IOException, Exception {
        this.timer = timer;
        Settings.addProvider(this);
        settingsChanged(new String[]{SETTING_I2C_BAUDRATE, SETTING_I2C_READDELAY, SETTING_I2C_WRITEDELAY, SETTING_I2C_SLAVEID});
        mma = new MMA7660FC(connector, timer, 10);
        mma.setListener(this);
        mma.apply();
    }

    public void setListener(MMA7660FCListener listener) {
        mma.setListener(listener);
    }

    private void handleMma(String command, InputStream is, PrintStream out) throws Exception {
        if (command.equals("x")) {
            out.println("x=" + mma.getX());
        } else if (command.equals("y")) {
            out.println("y=" + mma.getY());
        } else if (command.equals("z")) {
            out.println("z=" + mma.getZ());
        }
    }

    public boolean consoleCommand(String command, InputStream is, PrintStream out) {
        try {
            if (command.startsWith("i2c ")) {
                command = command.substring(4);

                if (command.startsWith("w ")) {
                    command = command.substring(2);
                    connector.write(slaveId, Bytes.
                            hexStringToByteArray(command));
                } else if (command.startsWith("r ")) {
                    command = command.substring(2);
                    int size = Integer.parseInt(command);
                    connector.read(slaveId, size);
                } else if (command.startsWith("g ")) {
                    command = command.substring(2);
                    String[] spl = Strings.split(' ', command);
                    int registerId = Integer.parseInt(spl[0]);
                    int size = 1;
                    if (spl.length > 1) {
                        size = Integer.parseInt(spl[1]);
                    }
                    connector.write(slaveId, (byte) registerId);
                    byte read[] = connector.read(slaveId, size);
                    out.println("I2C[" + (int) registerId + "]=" + Bytes.
                            byteArrayToHexString(read));
                } else if (command.equals("reset")) {
                    connector.write(0x06, new byte[]{});
                } else if (command.startsWith("mma ")) {
                    handleMma(command.substring(4), is, out);
                } /*
                 * else if ( command.equals("test") ) { testMma7660c(out); }
                 * else if ( command.equals("gpio") ) { InPort port = new
                 * InPort(new Vector() {
                 *
                 * {
                 * addElement("GPIO10"); } }); port.addListener(new
                 * TestWithGpio(timer, out)); }
                 */ else {
                    return false;
                }

                return true;
            } else if (command.equals("help")) {
                out.println("[HELP] i2c w 0102");
                out.println("[HELP] i2c r 2");
                out.println("[HELP] i2c g 6");
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".consoleCommand", ex);
            }
        }
        return false;
    }

    public void getDefaultSettings(Hashtable settings) {
        settings.put(SETTING_I2C_BAUDRATE, "400");
        settings.put(SETTING_I2C_READDELAY, "0");
        settings.put(SETTING_I2C_WRITEDELAY, "0");
        settings.put(SETTING_I2C_SLAVEID, "4C");
    }

    public void settingsChanged(String[] settings) {
//        Settings set = Settings.getInstance();

        for (int i = 0; i < settings.length; i++) {
            String setName = settings[i];
            try {
                if (setName.equals(SETTING_I2C_BAUDRATE)) {
                    connector.setBaudrate(Settings.getInt(setName));
                } else if (setName.equals(SETTING_I2C_READDELAY)) {
                    connector.setReadDelay(Settings.get(SETTING_I2C_READDELAY));
                } else if (setName.equals(SETTING_I2C_WRITEDELAY)) {
                    connector.setWriteDelay(Settings.get(SETTING_I2C_WRITEDELAY));
                } else if (setName.equals(SETTING_I2C_SLAVEID)) {
                    Integer.parseInt(Settings.get(SETTING_I2C_SLAVEID).
                            toUpperCase(), 16);
                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".settingsChanged:setName=\"" + setName + "\", value=\"" + Settings.
                            get(setName) + "\"", ex);
                }
            }
        }
    }

    public String toString() {
        return "I2CTest";
    }

    public void shakeDetected(int x, int y, int z) {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".shakeDetected( " + x + ", " + y + ", " + z + " );");
        }
    }

    public void tapDetected() {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".tapDetected();");
        }
    }

    public void baFroDetected(BackFront backFront) {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".baFroDetected( " + backFront + " );");
        }
    }

    public void polaDetected(Polarity polarity) {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".polaDetected( " + polarity + " );");
        }
    }

    private class TestWithGpio implements InPortListener, Runnable {

        private final Timer timer;
        private final PrintStream out;
//		private final int devId = 0x4C;

        public TestWithGpio(Timer timer, PrintStream out) {
            this.timer = timer;
            this.out = out;
        }

        public void portValueChanged(int portValue) {
            if (Logger.BUILD_DEBUG) {
                Logger.log("portVaueChanged(" + portValue + ");");
            }
            if (portValue == 0) {
                timer.schedule(new TimerTaskProxy(this), 0);
            }
        }

        public void run() {
            try {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log("GPIO 10 activated !");
                }

                int devId = 0x4C;
                synchronized (connector) {
                    int x = (int) connector.get(devId, (byte) 0x01);
                    int y = (int) connector.get(devId, (byte) 0x02);
                    int z = (int) connector.get(devId, (byte) 0x03);

                    out.println("Changed: " + x + " | " + y + " | " + z);
                }
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log("TestWithGpio.run", ex);
                }
            }
        }
    }

    private void testMma7660c(PrintStream out) throws Exception {
        int devId = 0x4C;
        synchronized (connector) {
            // We set standbye to setup the chip
            connector.write(devId, new byte[]{0x07, 0x00});

            // We set some interupts
            connector.write(devId, new byte[]{0x06, 0x07});

            // We set active mode
            connector.write(devId, new byte[]{0x07, 0x01});
        }

        long last = System.currentTimeMillis();
        for (int i = 0;
                i < 10; i++) {
            synchronized (connector) {
                int x = (int) connector.get(devId, (byte) 0x01);
                int y = (int) connector.get(devId, (byte) 0x02);
                int z = (int) connector.get(devId, (byte) 0x03);
                long time = System.currentTimeMillis();
                long diff = (time - last);
                last = time;
                out.println("[" + i + "]     " + x + " | " + y + " | " + z + " / " + diff);
            }
        }
    }
}
