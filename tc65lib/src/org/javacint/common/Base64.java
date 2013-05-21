package org.javacint.common;

import org.javacint.logging.Logger;

/**
 * Utility class for Base64 encoding/decoding
 *
 * @author <a href="mailto:nagydani@cs.elte.hu">Daniel A. Nagy</a>
 */
public final class Base64 {

    private static final byte[] cTable = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'
    };

    public static char encode(int nb) {
        return (char) cTable[nb];
    }

    /**
     * Do the encoding
     *
     * @param s unencoded data
     * @param off offset
     * @param len length
     * @return base64-encoded data
     */
    public static String encode(byte[] s, int off, int len) {
        int i, j = 0, l = off + len - 2;
        byte[] e = new byte[4 * ((len + 2) / 3)];
        for (i = off; i < l; i += 3) {
            e[j++] = cTable[(s[i] >> 2) & 63];
            e[j++] = cTable[((s[i] & 3) << 4) | ((s[i + 1] >> 4) & 15)];
            e[j++] = cTable[((s[i + 1] & 15) << 2) | ((s[i + 2] >> 6) & 3)];
            e[j++] = cTable[s[i + 2] & 63];
        }
        if ((i - off) < len) {
            e[j] = cTable[(s[i] >> 2) & 63];
            e[j + 3] = '=';
            if ((len + off - i) == 1) {
                e[j + 1] = cTable[(s[i] & 3) << 4];
                e[j + 2] = '=';
            } else {
                e[j + 1] = cTable[((s[i] & 3) << 4) | ((s[i + 1] >> 4) & 15)];
                e[j + 2] = cTable[(s[i + 1] & 15) << 2];
            }
        }
        return new String(e);
    }

    /**
     * Do the encoding
     *
     * @param s unencoded data
     * @return base64-encoded data
     */
    public static String encode(byte[] s) {
        return encode(s, 0, s.length);
    }
    private static final byte[] dTable = {
        -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,
        -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,
        -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, 62, -2, -2, -2, 63,
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -2, -2, -2, -1, -2, -2,
        -2, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -2, -2, -2, -2, -2,
        -2, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -2, -2, -2, -2, -2
    };

    public static byte decode(char c) {
        if (c > 127) {
            return -2;
        }
        return dTable[c];
    }

    /**
     * Do the decoding
     *
     * @param s base64-encoded data
     * @return decoded data
     */
    public static byte[] decode(String s) {
        int i, j = 0, l = 0;
        byte[] d, e = {0, 0, 0, 0};
        byte c;
        for (i = 0; i < s.length(); i++) {
            if ((c = decode(s.charAt(i))) > -2) {
                j++;
                switch (j) {
                    case 1:
                    case 2:
                        if (c < 0) {
                            j = 0;
                        }
                        break;
                    case 3:
                        if (c < 0) {
                            j = 0;
                            l++;
                        }
                        ;
                        break;
                    case 4:
                        if (c < 0) {
                            l += 2;
                        } else {
                            l += 3;
                        }
                        j = 0;
                }
            }
        }
        d = new byte[l];
        j = 0;
        l = 0;
        for (i = 0; i < s.length(); i++) {
            if ((c = decode(s.charAt(i))) > -2) {
                e[j] = c;
                j++;
                switch (j) {
                    case 1:
                    case 2:
                        if (c < 0) {
                            j = 0;
                        }
                        break;
                    case 3:
                        if (c < 0) {
                            j = 0;
                            d[l++] = (byte) ((e[0] << 2) | ((e[1] >> 4) & 3));
                        }
                        break;
                    case 4:
                        d[l++] = (byte) ((e[0] << 2) | ((e[1] >> 4) & 3));
                        d[l++] = (byte) (((e[1] & 15) << 4) | (e[2] >> 2));
                        if (c > -1) {
                            d[l++] = (byte) (((e[2] & 3) << 6) | c);
                        }
                        j = 0;
                }
            }
        }
        return d;
    }
}
