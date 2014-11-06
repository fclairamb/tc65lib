package org.javacint.call;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;

//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif

import org.javacint.at.ATCommands;
import org.javacint.at.ATCommandPooled;
import org.javacint.common.Strings;
import org.javacint.logging.Logger;
import org.javacint.gsm.GSMEventConsumer;

/**
 * Call management.
 */
public class CallManager implements ATCommandListener, ATCommandResponseListener {

    CallConsumer callConsumer;
    String callingPhoneNumber;
    int nbRings;
    boolean callInAction;

    public void start() {
        ATCommands.addListener(this);
    }

    public void ATEvent(String event) {
        if (event.startsWith("^SLCC: ")) {
            String args[] = Strings.split(',', event.substring(7));

            if (args[2].compareTo("4") == 0) { // Receiving a call
                String phoneNumber = args[6].substring(1, args[6].length() - 1);
                if (callConsumer != null) {
                    callingPhoneNumber = phoneNumber;
                    nbRings = 0;
                    callConsumer.phoneIsRinging(phoneNumber, nbRings++);
                }
            } else if (args[2].compareTo("3") == 0) { // Hearing the other phone ringing
                remotePhoneRinging();
            } else if (Logger.BUILD_DEBUG) {
                Logger.log("args[2]: " + args[2]);
            }
        } else if (event.startsWith("+CIEV: ")) {
            String data = event.substring(7).
                    trim();
            String[] spl = Strings.split(',', data);
            String name = spl[0].trim();
            String value = spl[1].trim();

            if (name.equals("sounder") && value.equals("1") && callConsumer != null) {
                try {
                    remotePhoneRinging();
                } catch (Exception ex) {
                    if (Logger.BUILD_CRITICAL) {
                        Logger.log(this + ".treatUrlLine: Call to _callConsumer failed", ex);
                    }
                }

            }
        } else if (event.equals("RING") && callConsumer != null) {
            callConsumer.phoneIsRinging(callingPhoneNumber, nbRings++);
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

    private void remotePhoneRinging() {
    }
    private ATCommandPooled callingAt;

    public void callNumber(String number) {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".callNumber( \"" + number + "\" )...", true);
        }
        try {
            //String ret = _atCalling.send( "ATD" + number + ";\r" );
            callInAction = true;
            callingAt = ATCommands.getATCommand();
            callingAt.atc().send("ATD" + number + ";", this);
            if (Logger.BUILD_DEBUG) {
//					Logger.log(this + ".callNumber:ATResponse: \"" + ret.replace('\n', '.').replace('\r', '.') + "\"", true);
            }

        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("CallManagement.callNumber", ex, true);
            }
        }
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".callNumber( \"" + number + "\" );", true);
        }
    }

    public void answerCall() {
        try {
            ATCommands.send("ATA");
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("GSMManagement.answerCall", ex, true);
            }
        }
    }

    public String toString() {
        return "CallManager";
    }

    public void ATResponse(String response) {
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".ATResponse( \"" + response + "\" )...");
        }
        if (callInAction) {
            try {
                ATCommands.send("ATH");
                callingAt.release();
            } catch (Exception ex) {
                if (Logger.BUILD_CRITICAL) {
                    Logger.log(this + ".ATResponse();", ex, true);
                }
            }
        }
        callInAction = false;
        if (Logger.BUILD_DEBUG) {
            Logger.log(this + ".ATResponse( \"" + response + "\" );");
        }
    }

    public void setCallConsumer(CallConsumer consumer) {
        this.callConsumer = consumer;
    }
}
