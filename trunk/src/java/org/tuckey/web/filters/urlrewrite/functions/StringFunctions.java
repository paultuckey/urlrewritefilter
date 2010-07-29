package org.tuckey.web.filters.urlrewrite.functions;

import org.tuckey.web.filters.urlrewrite.utils.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;


public class StringFunctions {

    private static Log log = Log.getLog(StringFunctions.class);
    private static final Pattern FIND_COLON_PATTERN = Pattern.compile("(?<!\\\\):");

    public static String toLower(final String s) {
        return s == null ? null : s.toLowerCase();
    }

    public static String toUpper(final String s) {
        return s == null ? null : s.toUpperCase();
    }

    public static String trim(final String str) {
        if (str == null) {
            return null;
        }
        return str.trim();
    }

    public static String length(final String str) {
        if (str == null) {
            return "0";
        }
        return String.valueOf(str.length());
    }

    /**
     * escape string "as as" will return "as+as"
     * note, encoding can be specified after colon eg, "UTF-16:as"
     */
    public static String escape(String subject) {
        try {
            if (FIND_COLON_PATTERN.matcher(subject).find()) {
                String encoding = subject.substring(0, subject.indexOf(':'));
                return URLEncoder.encode(subject.substring(subject.indexOf(':') + 1), encoding);
            }
        } catch (UnsupportedEncodingException e) {
            log.debug("String to escape contained unknown encoding, falling back to UTF-8");
        }
        try {
            return URLEncoder.encode(subject, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e, e);
        }
        return "";
    }

    /**
     * unescape string "as+as" will return "as as"
     * note, encoding can be specified after colon eg, "UTF-16:as"
     */
    public static String unescape(String subject) {
        try {
            if (FIND_COLON_PATTERN.matcher(subject).find()) {
                String encoding = subject.substring(0, subject.indexOf(':'));
                return URLDecoder.decode(subject.substring(subject.indexOf(':') + 1), encoding);
            }
        } catch (UnsupportedEncodingException e) {
            log.debug("String to unescape contained unknown encoding, falling back to UTF-8");
        }
        try {
            return URLDecoder.decode(subject, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e, e);
        }
        return "";
    }


    public static String replaceAll(String subject) {
        String replace = "";
        String with = "";
        if (FIND_COLON_PATTERN.matcher(subject).find()) {
            replace = subject.substring(subject.indexOf(':') + 1);
            subject = subject.substring(0, subject.indexOf(':'));
            if (FIND_COLON_PATTERN.matcher(replace).find()) {
                with = replace.substring(replace.indexOf(':') + 1);
                replace = replace.substring(0, replace.indexOf(':'));
            }
        }
        return subject.replaceAll(replace, with);
    }

    public static String replaceFirst(String subject) {
        String replace = "";
        String with = "";
        if (FIND_COLON_PATTERN.matcher(subject).find()) {
            replace = subject.substring(subject.indexOf(':') + 1);
            subject = subject.substring(0, subject.indexOf(':'));
            if (FIND_COLON_PATTERN.matcher(replace).find()) {
                with = replace.substring(replace.indexOf(':') + 1);
                replace = replace.substring(0, replace.indexOf(':'));
            }
        }
        return subject.replaceFirst(replace, with);
    }

}
