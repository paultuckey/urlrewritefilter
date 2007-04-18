package org.tuckey.web.filters.urlrewrite.utils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Simpe wrapper for java.util.regex.Pattern.
 *
 * @see java.util.regex.Pattern
 */
public class RegexPattern implements StringMatchingPattern {

    private Pattern pattern;

    public RegexPattern(String patternStr, boolean caseSensitive)
            throws StringMatchingPatternSyntaxException {
        try {
            if (caseSensitive) {
                pattern = Pattern.compile(patternStr);
            } else {
                pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            }
        } catch (PatternSyntaxException e) {
            throw new StringMatchingPatternSyntaxException(e);
        }
    }

    public StringMatchingMatcher matcher(String regex) {
        return new RegexMatcher(pattern.matcher(regex));
    }

}
