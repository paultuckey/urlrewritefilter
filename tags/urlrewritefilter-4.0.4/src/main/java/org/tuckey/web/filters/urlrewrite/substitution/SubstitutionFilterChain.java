package org.tuckey.web.filters.urlrewrite.substitution;

public interface SubstitutionFilterChain {

    public String substitute(String string, SubstitutionContext ctx);

}
