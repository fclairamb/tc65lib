package org.javacint.intsens;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif
import java.util.TimerTask;
import org.javacint.logging.Logger;

public abstract class ADCCheckTask extends TimerTask {

    private final int adcNb;
    private final int diff;
    private int lastValue = -1000;
    private ADC adc;

    public ADCCheckTask(int adcNb, int diff) {
        this.adcNb = adcNb;
        this.diff = diff;
    }

    public void run() {
        try {
            if (lastValue == -1000) {
                adc = new ADC(adcNb, 0);
            }

            int value = adc.getValue();

            if (Math.abs(value - lastValue) >= diff) {
                lastValue = value;
                changed(value);
            }
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log(this + ".run", ex, true);
            }

            // If we fail, it's best that we don't run anymore
            cancel();
        }
    }

    public String toString() {
        return "ADCCheckTask";
    }

    public abstract void changed(int temp);
}
