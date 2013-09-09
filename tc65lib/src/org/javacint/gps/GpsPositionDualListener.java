package org.javacint.gps;

/**
 * GPS Position dual listener.
 *
 * Listener to redirect the reception of gps location to two other listeners.
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class GpsPositionDualListener implements GpsPositionListener {

	private final GpsPositionListener a, b;

	public GpsPositionDualListener(GpsPositionListener a, GpsPositionListener b) {
		this.a = a;
		this.b = b;
	}

	public void positionReceived(GpsPosition pos) {
		a.positionReceived(pos);
		b.positionReceived(pos);
	}

	public void positionAdditionnalReceived(String type, String value) {
		a.positionAdditionnalReceived(type, value);
		b.positionAdditionnalReceived(type, value);
	}
}
