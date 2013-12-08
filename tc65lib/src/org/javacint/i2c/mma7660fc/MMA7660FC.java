package org.javacint.i2c.mma7660fc;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import java.util.Timer;
import java.util.Vector;
import org.javacint.common.Bytes;
import org.javacint.common.TimerTaskProxy;
import org.javacint.i2c.I2CConnector;
import org.javacint.logging.Logger;

/**
 * MMA7660FC device handling.
 *
 * It is a 3-Axis Accelerometer with Digital Output
 *
 * @author Florent Clairambault / www.webingenia.com
 * @version 0.1
 */
public class MMA7660FC implements Runnable, InPortListener {

	private void bafroDetected(boolean bafro1, boolean bafro0) {
		int nb = (bafro1 ? 2 : 0) | (bafro0 ? 1 : 0);
		if ( listener != null ) {
			listener.baFroDetected(new BackFront(nb));
		}
	}

	private void polaDetected(boolean pola2, boolean pola1, boolean pola0) {
		int nb = (pola2 ? 4 : 0) | (pola1 ? 2 : 0) | (pola0 ? 1 : 0);
		if ( listener != null ) {
			listener.polaDetected(new Polarity(nb));
		}
	}
	private static final boolean LOG = Logger.BUILD_DEBUG;
	private final I2CConnector i2c;
	private final Timer timer;
	private final boolean log = true;
	private MMA7660FCListener listener;

	/**
	 * Set the event listener
	 *
	 * @param listener Event listener
	 */
	public void setListener(MMA7660FCListener listener) {
		this.listener = listener;
	}

	/**
	 * @param portValue
	 * @see InPortListener
	 */
	public void portValueChanged(int portValue) {
		try {
//			if ( Logger.BUILD_DEBUG && log ) {
//				Logger.log(this + ".portValueChanged(" + portValue + ");");
//			}
			if ( portValue == 0 ) {
				timer.schedule(new TimerTaskProxy(this), 0);
			}
		} catch (Exception ex) {
			if ( Logger.BUILD_CRITICAL ) {
				Logger.log(this + ".portValueChanged", ex, true);
			}
		}
	}

	public void run() {
		try {
			interrupt();
		} catch (Exception ex) {
			if ( Logger.BUILD_CRITICAL ) {
				Logger.log(this + ".run", ex, true);
			}
		}
	}

	private void interrupt() throws Exception {
		TiltStatus tilt = getTiltStatus();
		if ( tilt.shake ) {
			shakeDetected();
		}
		if ( tilt.tap ) {
			tapDetected();
		}

		if ( tilt.bafro1 || tilt.bafro0 ) {
			bafroDetected(tilt.bafro1, tilt.bafro0);
		}

		if ( tilt.pola2 || tilt.pola1 || tilt.pola0 ) {
			polaDetected(tilt.pola2, tilt.pola1, tilt.pola0);
		}
	}

	private void shakeDetected() throws Exception {
//		if ( Logger.BUILD_DEBUG && log ) {
//			Logger.log(this + ".shakeDetected();");
//		}

		int x, y, z;

		if ( setup.interrupts.SHINTX ) {
			x = getX();
//			Logger.log("shake/x: " + x);
		} else {
			x = -1;
		}
		if ( setup.interrupts.SHINTY ) {
			y = getY();
//			Logger.log("shake/y: " + y);
		} else {
			y = -1;
		}
		if ( setup.interrupts.SHINTZ ) {
			z = getZ();
//			Logger.log("shake/z: " + z);
		} else {
			z = -1;
		}

		if ( listener != null ) {
			listener.shakeDetected(x, y, z);
		}
	}

	private void tapDetected() {
//		if ( Logger.BUILD_DEBUG && log ) {
//			Logger.log(this + ".tapDetected();");
//		}
		if ( listener != null ) {
			listener.tapDetected();
		}
	}

	private Setup getDefaultSetup() {
		Setup s = new Setup();
		s.devId = 0x4C;
		s.interrupts.PDINT = true;
		s.interrupts.SHINTX = true;
		s.interrupts.SHINTY = true;
		s.interrupts.SHINTZ = true;
		s.interrupts.FBINT = true;
		s.interrupts.PLINT = true;
		return s;
	}

	public class Setup {

		public int devId;
		public InterruptSetup interrupts = new InterruptSetup();

		public String toString() {
			return "Setup{devId=" + devId + ", interrupts=" + interrupts + "}";
		}
	}
	private Setup setup = getDefaultSetup();

	/**
	 * MMA7660F movement sensor driver constructor
	 *
	 * @param connector I2C connetor
	 * @param timer Timer
	 * @param gpio GPIO number
	 * @throws Exception
	 */
	public MMA7660FC(I2CConnector connector, Timer timer, final int gpio) throws Exception {
		this.i2c = connector;
		this.timer = timer;

		InPort port = new InPort(new Vector() {

			{
				addElement("GPIO" + gpio);
			}
		});
		port.addListener(this);
	}
	public byte getY() throws Exception {
		return i2c.get(setup.devId, Register.YOUT);
	}

	public byte getZ() throws Exception {
		return i2c.get(setup.devId, Register.ZOUT);
	}

	public TiltStatus getTiltStatus() throws Exception {
		return new TiltStatus(i2c.get(setup.devId, Register.TILT));
	}
	public Setup getSetup() {
		return setup;
	}

	public void setSetup(Setup setup) {
		this.setup = setup;
	}

	public void apply() throws Exception {
		if ( LOG ) {
			Logger.log(this + ".apply();");
		}
		setup(setup);
	}

	private void setup(Setup setup) throws Exception {
		if ( LOG ) {
			Logger.log(this + ".setup( " + setup + " );");
		}
		// We prepare the setup
		setMode(SimplifiedMode.Standbye);

		// We do the setup
		setInterruptMode(setup.interrupts);

		// We activate the chip
		setMode(SimplifiedMode.Active);
	}

	private void setMode(byte mode) throws Exception {
		i2c.write(setup.devId, new byte[]{Register.MODE, mode});
	}

	private void setInterruptMode(InterruptSetup is) throws Exception {
		i2c.write(setup.devId, new byte[]{Register.INTSU, is.getByte()});
	}

	public byte getX() throws Exception {
		return i2c.get(setup.devId, Register.XOUT);
	}



	public class TiltStatus {

		public TiltStatus(byte s) {
			bafro0 = Bytes.isBitSet(s, TiltStatusBits.bafro0);
			bafro1 = Bytes.isBitSet(s, TiltStatusBits.bafro1);
			pola0 = Bytes.isBitSet(s, TiltStatusBits.pola0);
			pola1 = Bytes.isBitSet(s, TiltStatusBits.pola1);
			pola2 = Bytes.isBitSet(s, TiltStatusBits.pola2);
			tap = Bytes.isBitSet(s, TiltStatusBits.tap);
			alert = Bytes.isBitSet(s, TiltStatusBits.alert);
			shake = Bytes.isBitSet(s, TiltStatusBits.shake);
		}
		public final boolean bafro0;
		public final boolean bafro1;
		public final boolean pola0;
		public final boolean pola1;
		public final boolean pola2;
		public final boolean tap;
		public final boolean alert;
		public final boolean shake;
	}

	public class InterruptSetup {

		/**
		 * Front/Back position change
		 */
		public boolean FBINT;
		/**
		 * Up/Down/Right/Left position change
		 */
		public boolean PLINT;
		/**
		 * Tap detection
		 */
		public boolean PDINT;
		/**
		 * Exiting Auto-sleep
		 */
		public boolean ASINT;
		/**
		 * After every measurement
		 */
		public boolean GINT;
		/**
		 * Shake on the Z-axis
		 */
		public boolean SHINTZ;
		/**
		 * Shake on the Y-axis
		 */
		public boolean SHINTY;
		/**
		 * Shake on the X-axis
		 */
		public boolean SHINTX;

		public byte getByte() {
			return (byte) ((FBINT ? InterruptSetupBits.FBINT : 0)
					| (PLINT ? InterruptSetupBits.PLINT : 0)
					| (PDINT ? InterruptSetupBits.PDINT : 0)
					| (ASINT ? InterruptSetupBits.ASINT : 0)
					| (GINT ? InterruptSetupBits.GINT : 0)
					| (SHINTX ? InterruptSetupBits.SHINTX : 0)
					| (SHINTY ? InterruptSetupBits.SHINTY : 0)
					| (SHINTZ ? InterruptSetupBits.SHINTZ : 0));
		}

		public String toString() {
			return "InterruptSetup{value=" + Bytes.byteToInt(getByte()) + "}";
		}
	}

	private interface Register {

		/**
		 * 6-bit output value X
		 */
		public static final byte XOUT = 0x00;
		/**
		 * 6-bit output value Y
		 */
		public static final byte YOUT = 0x01;
		/**
		 * 6-bit output value Z
		 */
		public static final byte ZOUT = 0x02;
		/**
		 * Tilt Status
		 */
		public static final byte TILT = 0x03;
		/**
		 * Sampling Rate Status
		 */
		public static final byte SRST = 0x04;
		/**
		 * Sleep Count
		 */
		public static final byte SPCNT = 0x05;
		/**
		 * Interrupt Setup
		 */
		public static final byte INTSU = 0x06;
		/**
		 * Mode
		 */
		public static final byte MODE = 0x07;
		/**
		 * Auto-Wake/Sleep and Portrait/Landscape samples per seconds and
		 * Debounce Filter
		 */
		public static final byte SR = 0x08;
		/**
		 * Tap Detection
		 */
		public static final byte PDET = 0x09;
		/**
		 * Tap Debounce Count
		 */
		public static final byte PD = 0x0A;
	}

	private interface ModeSetupBits {

		/**
		 * Active mode
		 */
		public static final byte MODE = 1;
		public static final byte D1 = 2;
		/**
		 * Test mode
		 */
		public static final byte TON = 4;
		/**
		 * Auto-wake
		 */
		public static final byte AWE = 8;
		/**
		 * Auto-sleep
		 */
		public static final byte ASE = 16;
		/**
		 * prescaler divided by 16 (or by 1)
		 */
		public static final byte SCPS = 32;
		/**
		 * interrupt output is push-pull (or open-drain)
		 */
		public static final byte IPP = 64;
		/**
		 * Interrupt output is active high (or low)
		 */
		public static final byte IAH = (byte) 128;
	}





	private interface SimplifiedMode {

		public static final byte Standbye = 0;
		public static final byte Active = ModeSetupBits.MODE;
	}

	private interface InterruptSetupBits {

		public static final byte FBINT = 1;
		public static final byte PLINT = 2;
		public static final byte PDINT = 4;
		public static final byte ASINT = 8;
		public static final byte GINT = 16;
		public static final byte SHINTZ = 32;
		public static final byte SHINTY = 64;
		public static final byte SHINTX = (byte) 128;
	}

	private interface TiltStatusBits {

		public static final byte bafro0 = 1;
		public static final byte bafro1 = 2;
		public static final byte pola0 = 4;
		public static final byte pola1 = 8;
		public static final byte pola2 = 16;
		public static final byte tap = 32;
		public static final byte alert = 64;
		public static final byte shake = (byte) 128;
	}

	public String toString() {
		return "MMA7660FC";
	}
}
