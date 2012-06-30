package org.tuckey.web.filters.urlrewrite.substitution;

import org.tuckey.web.filters.urlrewrite.ConditionMatch;
import org.tuckey.web.filters.urlrewrite.utils.StringMatchingMatcher;

import javax.servlet.http.HttpServletRequest;

public class SubstitutionContext {


    private HttpServletRequest hsRequest;
    private StringMatchingMatcher matcher;
    private ConditionMatch lastConditionMatch;
    private String replacePattern;

    public SubstitutionContext(HttpServletRequest hsRequest,
                               StringMatchingMatcher matcher, ConditionMatch lastConditionMatch,
                               String replacePattern) {
        super();
        this.hsRequest = hsRequest;
        this.matcher = matcher;
        this.lastConditionMatch = lastConditionMatch;
        this.replacePattern = replacePattern;
    }

    public HttpServletRequest getHsRequest() {
        return hsRequest;
    }

    public StringMatchingMatcher getMatcher() {
        return matcher;
    }

    public ConditionMatch getLastConditionMatch() {
        return lastConditionMatch;
    }

    public String getReplacePattern() {
        return replacePattern;
    }

}
