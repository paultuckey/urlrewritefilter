/**
 * Copyright (c) 2005, Paul Tuckey
 * All rights reserved.
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
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
    public static boolean loadClass = true;

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
                if (exceptionClass == null) {
                    setError("had trouble finding " + classStr + " after Class.forName got a null object");
                    return false;
                }
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
        if ( t == null ) return false;
        if ( exceptionClass == null ) return false;
        return exceptionClass.isInstance(t);
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
