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
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;
import org.tuckey.web.filters.urlrewrite.utils.TypeUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;


/**
 * Defines a function element, the ability to call a java method from within a to element value.
 *
 * @author Paul Tuckey
 * @version $Revision: 43 $ $Date: 2006-10-31 17:29:59 +1300 (Tue, 31 Oct 2006) $
 */
public class Function {

    private static Log log = Log.getLog(Function.class);

    /**
     * Weather or not the user wants the classStr created for each function.
     */
    private boolean newEachTime = false;

    private String name;
    private String classStr;

    private static final String DEAULT_METHOD_STR = "run";
    private String methodStr = DEAULT_METHOD_STR;

    /**
     * Used to identify the condition.
     */
    private int id = 0;

    private String error = null;

    private boolean valid = false;
    private boolean initialised = false;

    /**
     * The instance of the classStr to function.  Note, will be null if newEachTime is true.
     */
    private Object classInstance;

    /**
     * handles to the methods we are going to function.
     */
    private Constructor classConstructor;
    private Method initMethod;
    private Method runMethod;
    private Method destroyMethod;

    /**
     * The config that we pass to the objectwe are trying to function.
     */
    private RunConfig classConfig;

    private Hashtable initParams = new Hashtable();

    private static boolean loadClass = true;

    private static Class[] functionMethodSignature = {String.class};


    /**
     * Initialise the function, this will check specified classStr, constructor and methodStr exist.
     */
    public boolean initialise(ServletContext context) {
        log.debug("initialising function");
        classConfig = new RunConfig(context, initParams);
        initialised = true;
        valid = false;
        if (StringUtils.isBlank(classStr)) {
            setError("cannot initialise function " + id + " value is empty");
            return false;
        }
        if (methodStr == null) {
            setError("cannot initialise function " + id + " method is empty");
            return false;
        }
        log.debug("methodStr: " + methodStr);

        if (loadClass) {
            prepareFunctionObject();
        } else {
            valid = true;
        }
        return valid;
    }

    /**
     * Prepare the object for running by constructing and setting up method handles.
     */
    private void prepareFunctionObject() {
        if (log.isDebugEnabled()) {
            log.debug("looking for class " + classStr);
        }
        Class runClass;
        try {
            runClass = Class.forName(classStr);
            if (runClass == null) {
                setError("had trouble finding " + classStr + " after Class.forName got a null object");
                return;
            }
        } catch (ClassNotFoundException e) {
            setError("could not find " + classStr + " got a " + e.toString(), e);
            return;
        } catch (NoClassDefFoundError e) {
            setError("could not find " + classStr + " got a " + e.toString(), e);
            return;
        }
        try {
            classConstructor = runClass.getConstructor((Class[]) null);
        } catch (NoSuchMethodException e) {
            setError("could not get constructor for " + classStr, e);
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("looking for " + methodStr + " with specific params");
        }
        try {
            runMethod = runClass.getMethod(methodStr, functionMethodSignature);
        } catch (NoSuchMethodException e) {
            // do nothing
        }

        Method[] methods = runClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if ("destroy".equals(method.getName()) && method.getParameterTypes().length == 0) {
                log.debug("found destroy methodStr");
                destroyMethod = method;
            }
            if ("init".equals(method.getName()) && method.getParameterTypes().length == 1 &&
                    ServletConfig.class.getName().equals(method.getParameterTypes()[0].getName())) {
                log.debug("found init methodStr");
                initMethod = method;
            }
            if (initMethod != null && destroyMethod != null) break;
        }
        if (!newEachTime) {
            classInstance = fetchNewInstance();
        }
        valid = true;

    }

    private void invokeDestroy(Object runClassInstanceToDestroy) {
        if (runClassInstanceToDestroy != null && destroyMethod != null) {
            if (log.isDebugEnabled()) {
                log.debug("running " + classStr + ".destroy()");
            }
            try {
                destroyMethod.invoke(runClassInstanceToDestroy, (Object[]) null);
            } catch (IllegalAccessException e) {
                logInvokeException("destroy()", e);
            } catch (InvocationTargetException e) {
                logInvokeException("destroy()", e);
            }
        }
    }

    /**
     * Invokes the function method.
     * <p/>
     * Exceptions at invocation time are either rethrown as a ServletException or as thr original exception if we can
     * manage to do it.
     * <p/>
     * We don't log exceptions here, the container can do that.
     */
    private String invokeFunctionMethod(Object classInstanceToRun, String subject)
            throws ServletException, InvocationTargetException {
        if (log.isDebugEnabled()) {
            log.debug("running " + classStr + "." + getMethodSignature() + " ");
        }
        if (classInstanceToRun == null || runMethod == null) return null;
        Object returned;
        Object[] params = {subject};

        try {
            returned = runMethod.invoke(classInstanceToRun, (Object[]) params);

        } catch (IllegalAccessException e) {
            if (log.isDebugEnabled()) log.debug(e);
            throw new ServletException(e);

        }
        return returned == null ? null : returned.toString();
    }

    /**
     * Run the underlying destroy methodStr on the run classStr.
     */
    public void destroy() {
        initialised = false;
        valid = false;

        invokeDestroy(classInstance);

        // be paranoid and clean up all hooks to users classStr
        destroyMethod = null;
        runMethod = null;
        initMethod = null;
        classConfig = null;
        classConstructor = null;
        classInstance = null;

        methodStr = null;
        classStr = null;
        error = null;
    }

    /**
     *
     */
    public String execute(String subject, HttpServletRequest httpServletRequest)
            throws IOException, ServletException, InvocationTargetException {
        if (!initialised) {
            log.debug("not initialised skipping");
            return null;
        }
        if (!valid) {
            log.debug("not valid skipping");
            return null;
        }
        String returned;
        try {
            if (newEachTime) {
                Object newRunClassInstance = fetchNewInstance();
                returned = invokeFunctionMethod(newRunClassInstance, subject);
                invokeDestroy(newRunClassInstance);
            } else {
                returned = invokeFunctionMethod(classInstance, subject);
            }
        } catch (ServletException e) {
            httpServletRequest.setAttribute("javax.servlet.error.exception", e);
            throw e;
        }
        return returned;
    }

    private void logInvokeException(String methodStr, Exception e) {
        Throwable cause = e.getCause();
        if (cause == null) {
            setError("when invoking " + methodStr + " on " + classStr
                    + " got an " + e.toString(), e);
        } else {
            setError("when invoking " + methodStr + " on " + classStr
                    + " got an " + e.toString() + " caused by " + cause.toString(), cause);
        }
    }

    /**
     * Get a new instance of the classStr we want to run and init if required.
     *
     * @return the new instance
     */
    private Object fetchNewInstance() {
        Object obj;
        log.debug("getting new instance of " + classStr);
        try {
            obj = classConstructor.newInstance((Object[]) null);
        } catch (InstantiationException e) {
            logInvokeException("constructor", e);
            return null;
        } catch (IllegalAccessException e) {
            logInvokeException("constructor", e);
            return null;
        } catch (InvocationTargetException e) {
            logInvokeException("constructor", e);
            return null;
        }
        if (initMethod != null) {
            log.debug("about to run init(ServletConfig) on " + classStr);
            Object[] args = new Object[1];
            args[0] = classConfig;
            try {
                initMethod.invoke(obj, args);
            } catch (IllegalAccessException e) {
                logInvokeException("init(ServletConfig)", e);
                return null;
            } catch (InvocationTargetException e) {
                logInvokeException("init(ServletConfig)", e);
                return null;
            }
        }
        return obj;
    }

    public String getError() {
        return error;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isInitialised() {
        return initialised;
    }

    /**
     * The name of the classStr that will be run for each rule match.
     *
     * @return String eg, org.tuckey.YellowObject
     */
    public String getClassStr() {
        return classStr;
    }

    /**
     * The name of the methodStr that will be run for each rule match.
     *
     * @return String eg, setDate
     */
    public String getMethodStr() {
        return methodStr;
    }

    /**
     * The name of  the method signature ie, setDate(java.util.Date, int).  Includes fully qualified object names
     * for paramters.
     */
    public String getMethodSignature() {
        return TypeUtils.getMethodSignature(methodStr, functionMethodSignature);
    }

    public boolean isNewEachTime() {
        return newEachTime;
    }

    public void setNewEachTime(boolean newEachTime) {
        this.newEachTime = newEachTime;
    }

    /**
     * Gets a handle on the instance of the class run is running.
     * <p/>
     * If newEachTime is set to true this will always return null.
     */
    public Object getClassInstance() {
        return classInstance;
    }

    public void addInitParam(String name, String value) {
        if (name != null) {
            initParams.put(name, value);
        }
    }

    public String getInitParam(String paramName) {
        return (String) initParams.get(paramName);
    }

    public void setClassStr(String classStr) {
        this.classStr = classStr;
    }

    public void setMethodStr(String methodStr) {
        this.methodStr = methodStr;
    }

    public static void setLoadClass(boolean loadClazz) {
        loadClass = loadClazz;
    }

    public void setError(String error, Throwable t) {
        this.error = error;
        log.error(error, t);
    }

    public void setError(String error) {
        this.error = error;
        log.error(error);
    }

    public String getDisplayName() {
        return "Function " + id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
