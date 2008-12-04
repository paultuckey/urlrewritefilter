/**
 * Copyright (c) 2005-2007, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   - Neither the name tuckey.org nor the names of its contributors
 *     may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.tuckey.web.filters.urlrewrite.annotation;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Annotation Processor Factory for UrlRewriteFilter annotations.
 *
 * @deprecated Use UrlRewriteAnnotationProcessor
 */
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
            result = new HttpUrlAnnotationProcessor(env);
        }
        return result;

    }

    /**
     * This factory only builds processors for all UrlRewrite annotations.
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
     * Options supported by this annotation processor.
     *
     * @return collection of options.
     */
    public Collection<String> supportedOptions() {
        Set<String> set = new HashSet<String>();
        set.add("-AsaveRulesTo");
        set.add("-AshowPositions");
        set.add("-Adebug");
        return set;
    }
}

