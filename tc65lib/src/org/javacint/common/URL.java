package org.javacint.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * URL management class.
 */
public class URL {

    /**
     * URL-encodes string
     *
     * @param s String to encode
     * @return URL-encoded string
     * @throws IOException when internal error occures
     */
    public static String encode(String s) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DataOutputStream dOut = new DataOutputStream(bOut);
        StringBuffer ret = new StringBuffer(); //return value
        dOut.writeUTF(s);
        ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
        bIn.read();
        bIn.read();
        int c = bIn.read();
        while (c >= 0) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '.' || c == '-' || c == '*' || c == '_' || c == '|' || c == '{' || c == '}' || c == ':') {
                ret.append((char) c);
            } else if (c == ' ') {
                ret.append('+');
            } else {
                if (c < 128) {
                    appendHex(ret, c);
                } else if (c < 224) {
                    appendHex(ret, c);
                    appendHex(ret, bIn.read());
                } else if (c < 240) {
                    appendHex(ret, c);
                    appendHex(ret, bIn.read());
                    appendHex(ret, bIn.read());
                }
            }
            c = bIn.read();
        }
        return ret.toString();
    }

    private static void appendHex(StringBuffer buff, int ch) {
        buff.append('%');
        if (ch < 16) {
            buff.append('0');
        }
        buff.append(Integer.toHexString(ch));
    }
}
