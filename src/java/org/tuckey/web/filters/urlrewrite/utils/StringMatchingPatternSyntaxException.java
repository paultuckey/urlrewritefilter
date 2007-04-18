package org.tuckey.web.filters.urlrewrite.utils;

import java.util.regex.PatternSyntaxException;

/**
 * 
 * 
 */
public class StringMatchingPatternSyntaxException extends Exception {

    public StringMatchingPatternSyntaxException(PatternSyntaxException e) {
        super(e);
    }
}
