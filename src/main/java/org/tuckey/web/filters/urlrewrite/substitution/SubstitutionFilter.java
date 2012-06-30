package org.tuckey.web.filters.urlrewrite.substitution;

public interface SubstitutionFilter {
    public String substitute(String string, SubstitutionContext ctx, SubstitutionFilterChain nextFilter);
}
