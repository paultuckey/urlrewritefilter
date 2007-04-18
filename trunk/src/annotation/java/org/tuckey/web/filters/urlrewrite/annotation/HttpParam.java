package org.tuckey.web.filters.urlrewrite.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for matching parameters in HTTP requests.
 *
 * Usage:
 *
 * \@HttpUrl("^/search/(people|objects)/$")
 * public void someMethod(@HttpParam("fName") String firstName, @HttpParam String lastName)
 * for the request /search/people/?fName=bob&lastName=smith
 * will invoke the method with someMethod("bob", "smith", "people")
 * to be used in conjunction with @HttpUrl
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface HttpParam {

    /**
     * If not set will use the name of the parameter (case insensitive).
     * can be a expression ie, $1 (the first group of @HttpUrl regexp), %{header:user-agent} (the user agent header).
     */
    String value() default "[ unassigned ]";

}
