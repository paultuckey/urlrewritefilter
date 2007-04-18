package org.tuckey.web.filters.urlrewrite.utils;

/**
 * 
 * 
 */
public class WildcardPattern implements StringMatchingPattern {

    WildcardHelper wh;
    private String patternStr;

    public WildcardPattern(String patternStr) {
        this.wh = new WildcardHelper();
        this.patternStr = patternStr;
    }


    public StringMatchingMatcher matcher(String matchStr) {
        return new WildcardMatcher(wh, patternStr, matchStr);
    }
}
