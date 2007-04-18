package org.tuckey.web.filters.urlrewrite.utils;

import java.util.regex.Matcher;

/**
 * Simple wrapper for java.util.regex.Matcher.
 *
 * @see java.util.regex.Matcher
 */
public class RegexMatcher implements StringMatchingMatcher {

    private Matcher matcher;
    private boolean found = false;

    public RegexMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    /**
     * @see Matcher#find
     */
    public boolean find() {
        found = matcher.find();
        return found;
    }

    public boolean isFound() {
        return found;
    }

    private void reset() {
        matcher.reset();
        found = false;
        find();
    }

    public String replaceAll(String replacement) {
        String replaced = matcher.replaceAll(replacement);
        reset();
        return replaced;
    }

    public int groupCount() {
        return matcher.groupCount();
    }

    public String group(int groupId) {
        return matcher.group(groupId);
    }


}
