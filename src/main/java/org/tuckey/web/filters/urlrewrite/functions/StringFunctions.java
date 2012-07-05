package org.tuckey.web.filters.urlrewrite.functions;

import org.tuckey.web.filters.urlrewrite.substitution.SubstitutionContext;
import org.tuckey.web.filters.urlrewrite.substitution.SubstitutionFilterChain;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.regex.Pattern;


public class StringFunctions {

    private static Log log = Log.getLog(StringFunctions.class);
    private static final Pattern FIND_COLON_PATTERN = Pattern.compile("(?<!\\\\):");
    private static final Pattern FIND_ENCODING_PATTERN = Pattern.compile("^[0-9a-zA-Z-]+:");

    public static String toLower(final String s, SubstitutionFilterChain nextFilter, SubstitutionContext ctx) {
        return s == null ? null : nextFilter.substitute(s, ctx).toLowerCase();
    }

    public static String toUpper(final String s, SubstitutionFilterChain nextFilter, SubstitutionContext ctx) {
        return s == null ? null : nextFilter.substitute(s, ctx).toUpperCase();
    }

    public static String trim(final String str, SubstitutionFilterChain nextFilter, SubstitutionContext ctx) {
        if (str == null) {
            return null;
        }
        return nextFilter.substitute(str, ctx).trim();
    }

    public static String length(final String str, SubstitutionFilterChain nextFilter, SubstitutionContext ctx) {
        if (str == null) {
            return "0";
        }
        return String.valueOf(nextFilter.substitute(str, ctx).length());
    }

    /**
     * escape query string "as as" will return "as+as"
     * note, encoding can be specified after colon eg, "as:UTF-16"
     *
     * @param ctx
     * @param nextFilter
     */
    public static String escape(String subject, SubstitutionFilterChain nextFilter, SubstitutionContext ctx) {
        String encoding = "UTF-8";
        if (FIND_ENCODING_PATTERN.matcher(subject).find()) {
            encoding = subject.substring(0, subject.indexOf(':'));
            subject = subject.substring(subject.indexOf(':') + 1);
            if (!Charset.isSupported(encoding)) encoding = "UTF-8";
        }
        subject = nextFilter.substitute(subject, ctx);
        try {
            return URLEncoder.encode(subject, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error(e, e);
        }
        return "";
    }

    /**
     * escape string as a URI path segment "as as" will return "as%20as"
     * note, encoding can be specified after colon eg, "as:UTF-16"
     *
     * @param nextFilter
     * @param ctx
     */
    public static String escapePath(String subject, SubstitutionFilterChain nextFilter, SubstitutionContext ctx) {
        String encoding = "UTF-8";
        if (FIND_ENCODING_PATTERN.matcher(subject).find()) {
            encoding = subject.substring(0, subject.indexOf(':'));
            subject = subject.substring(subject.indexOf(':') + 1);
            if (!Charset.isSupported(encoding)) encoding = "UTF-8";
        }
        subject = nextFilter.substitute(subject, ctx);
        try {
            return org.tuckey.web.filters.urlrewrite.utils.URLEncoder.encodePathSegment(subject, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error(e, e);
        }
        return "";
    }

    /**
     * unescape query string "as+as" will return "as as"
     * note, encoding can be specified after colon eg, "as:UTF-16"
     *
     * @param nextFilter
     * @param ctx
     */
    public static String unescape(String subject, SubstitutionFilterChain nextFilter, SubstitutionContext ctx) {
        String encoding = "UTF-8";
        if (FIND_ENCODING_PATTERN.matcher(subject).find()) {
            encoding = subject.substring(0, subject.indexOf(':'));
            subject = subject.substring(subject.indexOf(':') + 1);
            if (!Charset.isSupported(encoding)) encoding = "UTF-8";
        }
        subject = nextFilter.substitute(subject, ctx);
        try {
            return URLDecoder.decode(subject, encoding);
        } catch (UnsupportedEncodingException e) {
            log.error(e, e);
        }
        return "";
    }

    /**
     * unescape path segment string "as+as%20as" will return "as+as as"
     * note, encoding can be specified after colon eg, "as:UTF-16"
     *
     * @param nextFilter
     * @param ctx
     */
    public static String unescapePath(String subject, SubstitutionFilterChain nextFilter, SubstitutionContext ctx) {
        String encoding = "UTF-8";
        if (FIND_ENCODING_PATTERN.matcher(subject).find()) {
            encoding = subject.substring(0, subject.indexOf(':'));
            subject = subject.substring(subject.indexOf(':') + 1);
            if (!Charset.isSupported(encoding)) encoding = "UTF-8";
        }
        subject = nextFilter.substitute(subject, ctx);
        try {
            return org.tuckey.web.filters.urlrewrite.utils.URLDecoder.decodePath(subject, encoding);
        } catch (URISyntaxException e) {
            log.error(e, e);
        }
        return "";
    }


    public static String replaceAll(String subject, SubstitutionFilterChain nextFilter, SubstitutionContext ctx) {
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
        subject = nextFilter.substitute(subject, ctx);
        return subject.replaceAll(replace, with);
    }

    public static String replaceFirst(String subject, SubstitutionFilterChain nextFilter, SubstitutionContext ctx) {
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
        subject = nextFilter.substitute(subject, ctx);
        return subject.replaceFirst(replace, with);
    }

}
