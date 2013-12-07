package org.javacint.common;

import java.io.ByteArrayOutputStream;
import java.util.Vector;
import org.javacint.logging.Logger;

/**
 * Bytes management class.
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class Bytes {

    public static void byteArrayToHexString(StringBuffer sb, byte buf[]) {
        byteArrayToHexString(sb, buf, 0, buf.length);
    }

    public static void byteArrayToHexString(StringBuffer sb, byte buf[], int offset, int length) {
        if (length > 256) {
            length = 256;
        }
        int max = offset + length;
        for (int i = offset; i < max; i++) {
            if (((int) buf[i] & 0xff) < 0x10) {
                sb.append("0");
            }
            sb.append(Long.toString((int) buf[i] & 0xff, 16).
                    toUpperCase());
        }
    }

    public static String byteArrayToHexString(byte[] buf) {
        return byteArrayToHexString(buf, 0, buf.length);
    }

    public static String byteArrayToHexString(byte[] buf, int offset, int length) {
        StringBuffer sb = new StringBuffer();

        byteArrayToHexString(sb, buf, offset, length);

        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String str) {
        byte[] array = new byte[str.length() / 2];

        for (int i = 0; i < str.length(); i += 2) {
            array[(i / 2)] = (byte) Integer.parseInt(str.substring(i, i + 2), 16);
        }

        return array;
    }

    /**
     * Convert an unsigned int (signed long) to an array of bytes
     *
     * @param l Long to convert
     * @param out Array fo bytes to put the unsigned int into
     * @param i Index to consider
     */
    public static void longToUInt32Bytes(long l, byte[] out, int i) {
        out[i++] = (byte) ((l & 0xFF000000L) >> 24);
        out[i++] = (byte) ((l & 0x00FF0000L) >> 16);
        out[i++] = (byte) ((l & 0x0000FF00L) >> 8);
        out[i++] = (byte) ((l & 0x000000FFL));
    }

    /**
     * Convert an unsigned short (signed int) to an array of bytes
     *
     * @param s Short to convert
     * @param out Array of bytes to put the short in
     * @param i Index where to insert the short
     */
    public static void intTo2Bytes(int s, byte[] out, int i) {
        out[i++] = (byte) ((s & 0xFF00L) >> 8);
        out[i++] = (byte) ((s & 0x00FFL));
    }

    /**
     * Convert some bytes to an unsigned short (signed int)
     *
     * @param data Array of bytes to get the short from
     * @param offset Position of the short within the byte array
     * @return The value of the unsigned short at the defined position
     */
    public static int bytesToShort(byte[] data, int offset) {
        try {
            int firstByte = (0x000000FF & ((int) data[offset]));
            int secondByte = (0x000000FF & ((int) data[offset + 1]));
            int ret = (firstByte << 8 | secondByte);
            return ret;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("Common.bytesToShort( " + Bytes.byteArrayToPrettyString(data) + ", " + offset + " )", ex, true);
            }
            return -1;
        }
    }

    public static long bytesToLong(byte[] data, int offset) {
        long l = 0;
        for (int i = 0; i < 4; i++) {
            l *= 256;
            l += byteToInt(data[offset + i]);
        }
        return l;
    }

    /**
     * Convert an int to a byte
     *
     * @param l Int to convert
     * @param out Array of bytes to put the unsigned int into
     * @param i Index where to insert the byte into the array
     */
    public static void intTo1Byte(int l, byte[] out, int i) {
        out[i] = (byte) ((l & 0x00FFL));
    }

    /**
     * Get the unsigned value of a byte into an int
     *
     * @param b Byte to get the value from
     * @return Unsigned value of the byte
     */
    public static int byteToInt(byte b) {
        int i = (int) b;
        if (i < 0) {
            i += 256;
        }
        return i;
    }

    /**
     * Convert a float into an array of bytes
     *
     * @param f Float to convert
     * @param out Array to insert the bytes within
     * @param i Index where to inset the value of the bytes
     */
    public static void floatToBytes(float f, byte[] out, int i) {
        int j = Float.floatToIntBits(f);

        out[i++] = (byte) ((j & 0xFF000000L) >> 24);
        out[i++] = (byte) ((j & 0x00FF0000L) >> 16);
        out[i++] = (byte) ((j & 0x0000FF00L) >> 8);
        out[i++] = (byte) ((j & 0x000000FFL));
    }

    public static int getBit(byte[] data, int pos, int offset) {
        try {
            int posByte = pos / 8;
            int posBit = pos % 8;
            byte valByte = data[(posByte + offset)];
            int valInt = valByte >> (8 - (posBit + 1)) & 0x0001;
            return valInt;
        } catch (Exception ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("Common.getBit( " + Bytes.byteArrayToPrettyString(data) + ", " + pos + " )", ex, true);
            }
            return -1;
        }
    }

    public static boolean isBitSet(byte b, int bit) {
        if (bit < 0) {
            bit += 128;
        }
        return (b & (1 << bit)) != 0;
    }

    /**
     * Convert a byte to a 2 chars hex string
     *
     * @param b Byte to convert
     * @return Hex string
     */
    public static String byteToHex(byte b) {
        int i = (int) b;
        if (i < 0) {
            i += 256;
        }
        String s = Integer.toHexString(i).
                toUpperCase();
        if (s.length() < 2) {
            s = "0" + s;
        }
        return s;
    }

    /**
     * Give a pretty representation of an array of bytes
     *
     * @param data Array of byte to show
     * @return pretty display of the array of bytes
     *
     * This used for debug logging
     */
    public static String byteArrayToPrettyString(byte[] data) {
        StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        sb.append(data.length);
        for (int i = 0; i < data.length; i++) {
            sb.append(" 0x").
                    append(byteToHex(data[i]));
        }

        sb.append(" ]");

        return sb.toString();
    }

    /**
     * Calculate the LRC of the selected frame
     *
     * @param data Data to calculate the LRC from
     * @param start Index to calculate the LRC from
     * @param end Index to calculate the LRC to
     * @return Value of the LRC
     */
    public static byte calculateLrc(byte[] data, int start, int end) {
        byte total = 0;
        for (int i = start; i < end; i++) {
            total += data[i];
        }
        total *= -1;
        return total;
    }

    public static boolean checkLrc(byte[] data, int start, int end) {
        byte total = 0;
        for (int i = start; i < end; i++) {
            total += data[i];
        }
        return total == 0;
    }

    /**
     * Convert an array of strings to an array of arrays of bytes
     *
     * @param str Array of strings
     * @return Array of arrays of bytes
     */
    public static byte[][] stringsToBytes(String[] str) {
        byte[][] bytes = new byte[str.length][];
        for (int i = 0; i < str.length; i++) {
            bytes[i] = str[i].getBytes();
        }
        return bytes;
    }

    /**
     * Convert an array of bytes to an array of strings
     *
     * @param bytes Array of bytes to convert
     * @return Array of strings
     */
    public static String[] bytesToStrings(byte[][] bytes) {
        String[] strings = new String[bytes.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = new String(bytes[i]);
        }
        return strings;
    }

    /**
     * Convert a vector of strings to an array of array of bytes
     *
     * @param vect Vector of strings
     * @return Array of arrays of bytes
     */
    public static byte[][] stringsToBytes(Vector vect) {
        byte[][] bytes = new byte[vect.size()][];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = ((String) vect.elementAt(i)).getBytes();
        }
        return bytes;
    }

//    public static byte[] append(byte[] src, byte[] add, int offset, int length) {
//        if (src == null) {
//            src = new byte[0];
//        }
//        byte[] dst = new byte[src.length + add.length];
//        System.arraycopy(src, 0, dst, 0, src.length);
//        System.arraycopy(add, 0, dst, src.length, add.length);
//        return dst;
//    }
//
//    public static byte[] extend(byte[] src, int newSize) {
//        byte[] dst = new byte[src.length + newSize];
//        System.arraycopy(src, 0, dst, 0, src.length);
//        return dst;
//    }
}
