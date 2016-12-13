package org.tuckey.web.filters.urlrewrite.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import java.net.URI;

public final class RewriteUtils {

    private static Log log = Log.getLog(RewriteUtils.class);


    private RewriteUtils() {
    }

    /**
     * Not very efficient but good enough(100_000 rules ~4 sec), but we need to encode each character..
     */
    public static String uriEncodeParts(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return value;
        }
        final int length = value.length();
        final StringBuilder builder = new StringBuilder(length << 1);
        for (int i = 0; i < length; i++) {
            final char c = value.charAt(i);
            if (CharMatcher.ASCII.matches(c)) {
                builder.append(String.valueOf(c));
            } else {
                builder.append(encodeCharacter(c));
            }
        }
        return builder.toString();
    }


    private static String encodeCharacter(final char c) {
        try {
            return URI.create(String.valueOf(c)).toASCIIString();
        } catch (Exception e) {
            log.error("Error parsing character: ", e);
        }
        return String.valueOf(c);
    }


    public static String encodeRedirect(final String target) {
        boolean allAscii = CharMatcher.ASCII.matchesAllOf(target);
        if (!allAscii) {
            try {
                return URI.create(target).toASCIIString();
            } catch (Exception e) {
                log.error("Invalid target uri: " + target, e);
            }
        }
        return target;
    }
}
