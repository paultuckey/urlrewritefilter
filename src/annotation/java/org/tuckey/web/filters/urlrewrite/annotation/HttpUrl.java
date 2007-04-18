package org.tuckey.web.filters.urlrewrite.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for matching URL's in HTTP requests.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface HttpUrl {

    String value();

    int weight() default 0;

}
