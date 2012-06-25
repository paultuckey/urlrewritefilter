package org.tuckey.web.filters.urlrewrite.utils;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/**
 * URL-encoding utility for each URL part according to the RFC specs
 * see the rfc at http://www.ietf.org/rfc/rfc2396.txt
 *
 * @author stephane
 */
public class URLEncoder {

    /**
     * mark = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
     */
    public final static BitSet MARK = new BitSet();

    static {
        MARK.set('-');
        MARK.set('_');
        MARK.set('.');
        MARK.set('!');
        MARK.set('~');
        MARK.set('*');
        MARK.set('\'');
        MARK.set('(');
        MARK.set(')');
    }

    /**
     * lowalpha = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" |
     * "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
     */
    public final static BitSet LOW_ALPHA = new BitSet();

    static {
        LOW_ALPHA.set('a');
        LOW_ALPHA.set('b');
        LOW_ALPHA.set('c');
        LOW_ALPHA.set('d');
        LOW_ALPHA.set('e');
        LOW_ALPHA.set('f');
        LOW_ALPHA.set('g');
        LOW_ALPHA.set('h');
        LOW_ALPHA.set('i');
        LOW_ALPHA.set('j');
        LOW_ALPHA.set('k');
        LOW_ALPHA.set('l');
        LOW_ALPHA.set('m');
        LOW_ALPHA.set('n');
        LOW_ALPHA.set('o');
        LOW_ALPHA.set('p');
        LOW_ALPHA.set('q');
        LOW_ALPHA.set('r');
        LOW_ALPHA.set('s');
        LOW_ALPHA.set('t');
        LOW_ALPHA.set('u');
        LOW_ALPHA.set('v');
        LOW_ALPHA.set('w');
        LOW_ALPHA.set('x');
        LOW_ALPHA.set('y');
        LOW_ALPHA.set('z');
    }

    /**
     * upalpha = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" |
     * "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
     */
    public final static BitSet UP_ALPHA = new BitSet();

    static {
        UP_ALPHA.set('A');
        UP_ALPHA.set('B');
        UP_ALPHA.set('C');
        UP_ALPHA.set('D');
        UP_ALPHA.set('E');
        UP_ALPHA.set('F');
        UP_ALPHA.set('G');
        UP_ALPHA.set('H');
        UP_ALPHA.set('I');
        UP_ALPHA.set('J');
        UP_ALPHA.set('K');
        UP_ALPHA.set('L');
        UP_ALPHA.set('M');
        UP_ALPHA.set('N');
        UP_ALPHA.set('O');
        UP_ALPHA.set('P');
        UP_ALPHA.set('Q');
        UP_ALPHA.set('R');
        UP_ALPHA.set('S');
        UP_ALPHA.set('T');
        UP_ALPHA.set('U');
        UP_ALPHA.set('V');
        UP_ALPHA.set('W');
        UP_ALPHA.set('X');
        UP_ALPHA.set('Y');
        UP_ALPHA.set('Z');
    }

    /**
     * alpha = lowalpha | upalpha
     */
    public final static BitSet ALPHA = new BitSet();

    static {
        ALPHA.or(LOW_ALPHA);
        ALPHA.or(UP_ALPHA);
    }

    /**
     * digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
     */
    public final static BitSet DIGIT = new BitSet();

    static {
        DIGIT.set('0');
        DIGIT.set('1');
        DIGIT.set('2');
        DIGIT.set('3');
        DIGIT.set('4');
        DIGIT.set('5');
        DIGIT.set('6');
        DIGIT.set('7');
        DIGIT.set('8');
        DIGIT.set('9');
    }

    /**
     * alphanum = alpha | digit
     */
    public final static BitSet ALPHANUM = new BitSet();

    static {
        ALPHANUM.or(ALPHA);
        ALPHANUM.or(DIGIT);
    }

    /**
     * unreserved = alphanum | mark
     */
    public final static BitSet UNRESERVED = new BitSet();

    static {
        UNRESERVED.or(ALPHANUM);
        UNRESERVED.or(MARK);
    }

    /**
     * pchar = unreserved | escaped | ":" | "@" | "&" | "=" | "+" | "$" | ","
     * <p/>
     * Note: we don't allow escaped here since we will escape it ourselves, so we don't want to allow them in the
     * unescaped sequences
     */
    public final static BitSet PCHAR = new BitSet();

    static {
        PCHAR.or(UNRESERVED);
        PCHAR.set(':');
        PCHAR.set('@');
        PCHAR.set('&');
        PCHAR.set('=');
        PCHAR.set('+');
        PCHAR.set('$');
        PCHAR.set(',');
    }

    /**
     * Encodes a string to be a valid path parameter URL, which means it can contain PCHAR* only (do not put the leading
     * ";" or it will be escaped.
     *
     * @throws UnsupportedEncodingException
     */
    public static String encodePathParam(final String pathParam, final String charset) throws UnsupportedEncodingException {
        return encodePathSegment(pathParam, charset);
    }

    /**
     * Encodes a string to be a valid path segment URL, which means it can contain PCHAR* only (do not put path
     * parameters or they will be escaped.
     *
     * @throws UnsupportedEncodingException
     */
    public static String encodePathSegment(final String pathSegment, final String charset) throws UnsupportedEncodingException {
        if (pathSegment == null) {
            return null;
        }
        // start at *3 for the worst case when everything is %encoded on one byte
        final StringBuffer encoded = new StringBuffer(pathSegment.length() * 3);
        final char[] toEncode = pathSegment.toCharArray();
        for (int i = 0; i < toEncode.length; i++) {
            char c = toEncode[i];
            if (PCHAR.get(c)) {
                encoded.append(c);
            } else {
                final byte[] bytes = String.valueOf(c).getBytes(charset);
                for (int j = 0; j < bytes.length; j++) {
                    byte b = bytes[j];
                    // make it unsigned
                    final int u8 = b & 0xFF;
                    encoded.append("%");
                    if (u8 < 16)
                        encoded.append("0");
                    encoded.append(Integer.toHexString(u8));
                }
			}
		}
		return encoded.toString();
	}
}
