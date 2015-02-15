package org.javacint.control.m2mp;

import org.javacint.control.m2mp.data.Event;

/**
 * Application layer interface
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public interface M2MPEventsListener {

    public void m2mpEvent(Event event);
}
