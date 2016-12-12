package org.tuckey.web.filters.urlrewrite.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class RewriteUtils {

    private static Log log = Log.getLog(RewriteUtils.class);

    private RewriteUtils() {
    }

    public static String uriEncodeParts(final String value) {

        try {
            final Spliterator<String> iterator = Splitter.on('/').split(value).spliterator();
            return StreamSupport.stream(iterator, false)
                    .map(RewriteUtils::safeEncode).collect(Collectors.joining("/"));

        } catch (Exception e) {
            log.error("Error converting url", e);
        }
        return value;
    }

    private static String safeEncode(final String part) {
       /* // NOTE: URI.create() stuff is expensive so, check if we need to encode anything first:
        if (CharMatcher.ASCII.matchesAllOf(part)) {
            return part;
        }*/
        // always encode:
        try {
            return URI.create(part).toASCIIString();
        } catch (Exception ignore) {
            //
        }
        return part;
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
