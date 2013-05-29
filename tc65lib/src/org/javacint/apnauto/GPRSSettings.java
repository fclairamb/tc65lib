/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.apnauto;

import org.javacint.common.Strings;
import org.javacint.logging.Logger;

/**
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public final class GPRSSettings {

    private final static String EMPTY = "";
    public final static String DEFAULT_TARGET = "8.8.8.8:53";
    private String[] values;
    private int index_carrier = -1,
            index_apn = -1,
            index_user = -1,
            index_pass = -1,
            index_dns = -1,
            index_mcc = -1,
            index_target = -1,
            index_mnc = 1;

    private String get(int index) {
        return get(index, EMPTY);
    }

    private String get(int index, String alt) {
        if (index == -1 || index >= values.length) {
            return alt;
        } else {
            return values[index];
        }
    }

    public String getCarrier() {
        return get(index_carrier);
    }

    public String getApn() {
        return get(index_apn);
    }

    public String getUser() {
        return get(index_user);
    }

    public String getPass() {
        return get(index_pass);
    }

    public String getDns() {
        return get(index_dns);
    }

    public String getMCC() {
        return get(index_mcc);
    }

    public String getMNC() {
        return get(index_mnc);
    }

    public String getTarget() {
        return get(index_target, DEFAULT_TARGET);
    }

    public void parse(String line) {
        if (line == null) {
            return;
        }
        values = Strings.split('|', line);
    }

    public GPRSSettings() {
    }

    public void setColumns(String cols) {
        String[] spl = Strings.split('|', cols);
        int l = spl.length;
        for (int i = 0; i < l; i++) {
            String col = spl[i];
            if (col.equals("c")) {
                index_carrier = i;
            } else if (col.equals("a")) {
                index_apn = i;
            } else if (col.equals("u")) {
                index_user = i;
            } else if (col.equals("p")) {
                index_pass = i;
            } else if (col.equals("d")) {
                index_dns = i;
            } else if (col.equals("m")) {
                index_mcc = i;
            } else if (col.equals("n")) {
                index_mnc = i;
            } else if (col.equals("t")) {
                index_target = i;
            } else if (Logger.BUILD_CRITICAL) {
                Logger.log("APNS_FILE: Column \"" + col + "\" (" + i + ") is unknown!");
            }
        }
    }

    public String toString() {
        return getCarrier() + " --> " + getApn() + ", " + getUser() + ", " + getPass();
    }
    private static final char QT = '"';
    private static final char SE = ',';

    public String toSjnet() {
        return QT + "gprs" + QT + SE
                + QT + getApn() + QT + SE
                + QT + getUser() + QT + SE
                + QT + getPass() + QT + SE
                + QT + getDns() + QT + SE
                + "0";
    }
}
