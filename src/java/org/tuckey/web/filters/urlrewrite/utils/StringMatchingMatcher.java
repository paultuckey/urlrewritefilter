package org.tuckey.web.filters.urlrewrite.utils;

/**
 * A generic interface for StringMatching, method signature matches java.util.Matcher.
 *
 * @see java.util.regex.Matcher
 */
public interface StringMatchingMatcher {

    public boolean find();

    public boolean isFound();

    public String replaceAll(String replacement);

    int groupCount();

    String group(int groupId);
}
