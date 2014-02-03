package org.javacint.gsm;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import org.javacint.at.ATCommands;
import org.javacint.at.ATURCQueueHandler;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;

/**
 * Convert (mostly GSM related) URCs event to a nice java interface.
 */
public class GSMEventConverter implements ATCommandListener {

    private final GSMEventConsumer consumer;
    // Maybe this should be handled at the ATCommands level...
    private final ATURCQueueHandler handler = new ATURCQueueHandler(this);

    public GSMEventConverter(GSMEventConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Start the events handling.
     */
    public void start() {
        ATCommands.addListener(handler);
        ATCommands.sendUrc("AT+CMER=2,0,0,2");
    }

    /**
     * Stop the events handling.
     */
    public void stop() {
        ATCommands.removeListener(handler);
    }

    public void ATEvent(String event) {
        event = event.trim();
        if (event.startsWith("+CIEV: ")) {
            String data = event.substring(7).trim();
            String[] spl = Strings.split(',', data);
            String name = spl[0].trim();
            String value = spl[1].trim();

            if (consumer != null) {
                try {
                    consumer.gsmEventReceived(name, value);
                } catch (Exception ex) {
                    if (Logger.BUILD_CRITICAL) {
                        Logger.log(this + ".ATEvent", ex, true);
                    }
                }
            }
        }
    }

    public void RINGChanged(boolean bln) {
    }

    public void DCDChanged(boolean bln) {
    }

    public void DSRChanged(boolean bln) {
    }

    public void CONNChanged(boolean bln) {
    }
}
