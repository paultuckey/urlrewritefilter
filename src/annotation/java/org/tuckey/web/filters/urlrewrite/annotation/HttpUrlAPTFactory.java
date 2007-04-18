package org.tuckey.web.filters.urlrewrite.annotation;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;


public class HttpUrlAPTFactory implements AnnotationProcessorFactory {

    /**
     * Returns a note annotation processor.
     *
     * @return An annotation processor for note annotations if requested,
     *         otherwise, returns the NO_OP annotation processor.
     */
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> declarations,
                                               AnnotationProcessorEnvironment env) {
        AnnotationProcessor result;
        if (declarations.isEmpty()) {
            result = AnnotationProcessors.NO_OP;
        } else {
            // Next Step - implement this class:
            result = new HttpUrlAnnotationProcessor(env);
        }
        return result;

    }

    /**
     * This factory only builds processors for the
     * {@link HttpUrl} annotation.
     *
     * @return a collection containing only the note annotation name.
     */
    public Collection<String> supportedAnnotationTypes() {
        Set<String> set = new HashSet<String>();
        set.add(HttpUrl.class.getName());
        set.add(HttpExceptionHandler.class.getName());
        set.add(HttpParam.class.getName());
        return set;
    }

    /**
     * No options are supported by this annotation processor.
     *
     * @return an empty list.
     */
    public Collection<String> supportedOptions() {
        return Collections.emptyList();
    }
}

