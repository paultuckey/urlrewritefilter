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
package org.tuckey.web.filters.urlrewrite;

import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * An item that will allow exceptions during "run" invocation to be caught.
 */
public class CatchElem implements Runnable {

    private static Log log = Log.getLog(CatchElem.class);

    private String classStr;
    private String error = null;
    private boolean valid = false;
    private boolean initialised = false;
    private Class exceptionClass;
    private ArrayList runs = new ArrayList();

    /**
     * For testing and documentation we don't want to load the classes.
     */
    private static boolean loadClass = true;

    public static void setLoadClass(boolean loadClass) {
        CatchElem.loadClass = loadClass;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isInitialised() {
        return initialised;
    }

    /**
     * @return true on success
     */
    public boolean initialise(ServletContext servletContext) {
        initialised = true;
        boolean ok = true;
        if (log.isDebugEnabled()) {
            log.debug("looking for class " + classStr);
        }
        if ( loadClass ) {
            try {
                exceptionClass = Class.forName(classStr);
            } catch (ClassNotFoundException e) {
                setError("could not find " + classStr + " got a " + e.toString(), e);
                return false;
            } catch (NoClassDefFoundError e) {
                setError("could not find " + classStr + " got a " + e.toString(), e);
                return false;
            }
        }
        // now initialise runs
        for (int i = 0; i < runs.size(); i++) {
            final Run run = (Run) runs.get(i);
            if (!run.initialise(servletContext, exceptionClass)) {
                ok = false;
            }
        }

        valid = ok;

        return valid;
    }

    public boolean matches(Throwable t) {
        return t != null && exceptionClass != null && exceptionClass.isInstance(t);
    }

    protected RewrittenUrl execute(final HttpServletRequest hsRequest, final HttpServletResponse hsResponse,
                           Throwable originalThrowable)
            throws IOException, ServletException, InvocationTargetException {

        // make sure the runs are handled
        int runsSize = runs.size();
        RewriteMatch lastRunMatch = null;
        if (runsSize > 0) {
            log.trace("performing runs");
            for (int i = 0; i < runsSize; i++) {
                Run run = (Run) runs.get(i);
                lastRunMatch = run.execute(hsRequest, hsResponse, originalThrowable);
            }
        }
        if ( lastRunMatch == null ) return null;
        return new RewrittenUrlClass(lastRunMatch);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        log.error(error);
    }

    public void setError(String error, Throwable t) {
        this.error = error;
        log.error(error, t);
    }

    public String getClassStr() {
        return classStr;
    }

    public void setClassStr(String classStr) {
        this.classStr = classStr;
    }


    public void addRun(final Run run) {
        runs.add(run);
    }

    public List getRuns() {
        return runs;
    }
}
