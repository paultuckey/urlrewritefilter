package org.tuckey.web.filters.urlrewrite;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.tuckey.web.filters.urlrewrite.extend.RewriteRule;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class ClassRule implements Rule {

    private static Log log = Log.getLog(ClassRule.class);

    private String classStr;
    private RewriteRule localRule;
    private boolean initialised = false;
    private int id;
    private boolean enabled = true;
    private boolean valid = false;
    private boolean last = true;
    private List errors = new ArrayList();

    private static final String DEAULT_METHOD_STR = "matches";
    private String methodStr = DEAULT_METHOD_STR;


    /**
     * The parameter types used for run methods.
     */
    private static Class[] methodParameterTypesHttp = new Class[2];

    static {
        methodParameterTypesHttp[0] = HttpServletRequest.class;
        methodParameterTypesHttp[1] = HttpServletResponse.class;
    }

    /**
     * For second try.
     */
    private static Class[] methodParameterTypes = new Class[2];

    static {
        methodParameterTypes[0] = ServletRequest.class;
        methodParameterTypes[1] = ServletResponse.class;
    }

    private Method destroyMethod;
    private Method initMethod;
    private Method matchesMethod;


    public RewrittenUrl matches(final String url, final HttpServletRequest hsRequest,
                                final HttpServletResponse hsResponse, final RuleChain chain)
            throws IOException, ServletException {
        return matches(url, hsRequest, hsResponse);
    }

    public RewrittenUrl matches(final String url, final HttpServletRequest hsRequest,
                                final HttpServletResponse hsResponse)
            throws ServletException, IOException {
        if (! initialised) return null;

        Object[] args = new Object[2];
        args[0] = hsRequest;
        args[1] = hsResponse;

        Object returnedObj;
        if (log.isDebugEnabled()) {
            log.debug("running " + classStr + "." + methodStr + "(HttpServletRequest, HttpServletResponse)");
        }
        if (matchesMethod == null) return null;
        try {
            returnedObj = matchesMethod.invoke(localRule, (Object[]) args);

        } catch (IllegalAccessException e) {
            if (log.isDebugEnabled()) log.debug(e);
            throw new ServletException(e);

        } catch (InvocationTargetException e) {
            if (log.isDebugEnabled()) log.debug(e);
            Throwable originalThrowable = e.getTargetException();
            if (originalThrowable == null) {
                originalThrowable = e.getCause();
                if (originalThrowable == null) {
                    throw new ServletException(e);
                }
            }
            if (originalThrowable instanceof Error) throw (Error) originalThrowable;
            if (originalThrowable instanceof RuntimeException) throw (RuntimeException) originalThrowable;
            if (originalThrowable instanceof ServletException) throw (ServletException) originalThrowable;
            if (originalThrowable instanceof IOException) throw (IOException) originalThrowable;
            throw new ServletException(originalThrowable);
        }

        if ( returnedObj != null && returnedObj instanceof RewriteMatch) {
            return new RewrittenUrlClass((RewriteMatch) returnedObj);
        }
        return null;
    }






    public boolean initialise(ServletContext context) {
        // check all the conditions
        initialised = true;

        Class ruleClass;
        try {
            ruleClass = Class.forName(classStr);
        } catch (ClassNotFoundException e) {
            addError("could not find " + classStr + " got a " + e.toString(), e);
            return false;
        } catch (NoClassDefFoundError e) {
            addError("could not find " + classStr + " got a " + e.toString(), e);
            return false;
        }
        if (ruleClass == null) {
            addError("had trouble finding " + classStr + " after Class.forName got a null object");
            return false;
        }

        Constructor constructor;
        try {
            constructor = ruleClass.getConstructor((Class[]) null);
        } catch (NoSuchMethodException e) {
            addError("could not get constructor for " + classStr, e);
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("looking for " + methodStr + " will try with several arguments");
        }
        try {
            matchesMethod = ruleClass.getMethod(methodStr, methodParameterTypes);
        } catch (NoSuchMethodException e) {
            try {
                matchesMethod = ruleClass.getMethod(methodStr, methodParameterTypesHttp);
            } catch (NoSuchMethodException e2) {
                addError("could not find " + methodStr + "(ServletRequest, ServletResponse) on " + classStr, e);
                addError("also tried " + methodStr + "(HttpServletRequest, HttpServletResponse)", e2);
            }
        }

        Method[] methods = ruleClass.getMethods();
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

        Object instance;
        log.debug("getting new instance of " + classStr);
        try {
            instance = constructor.newInstance((Object[]) null);
        } catch (InstantiationException e) {
            logInvokeException("constructor", e);
            return false;
        } catch (IllegalAccessException e) {
            logInvokeException("constructor", e);
            return false;
        } catch (InvocationTargetException e) {
            logInvokeException("constructor", e);
            return false;
        }
        if (initMethod != null) {
            log.debug("about to run init(ServletContext) on " + classStr);
            Object[] args = new Object[1];
            args[0] = context;
            try {
                initMethod.invoke(instance, args);
            } catch (IllegalAccessException e) {
                logInvokeException("init(ServletContext)", e);
                return false;
            } catch (InvocationTargetException e) {
                logInvokeException("init(ServletContext)", e);
                return false;
            }
        }

        localRule = (RewriteRule) instance;

        valid = true;
        return true;
    }

    private void logInvokeException(String methodStr, Exception e) {
        Throwable cause = e.getCause();
        if (cause == null) {
            addError("when invoking " + methodStr + " on " + classStr
                    + " got an " + e.toString(), e);
        } else {
            addError("when invoking " + methodStr + " on " + classStr
                    + " got an " + e.toString() + " caused by " + cause.toString(), cause);
        }
    }

    public void destroy() {
        if (localRule == null) return;
        localRule.destroy();
    }

    public String getName() {
        return classStr;
    }

    public String getDisplayName() {
        return "Class Rule " + classStr;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public void setClassStr(String classStr) {
        this.classStr = classStr;
    }

    public String getClassStr() {
        return classStr;
    }

    public void setMethodStr(String methodStr) {
        this.methodStr = StringUtils.trimToNull(methodStr);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return id;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isFilter() {
        return false;
    }

    public List getErrors() {
        return errors;
    }

    private void addError(String s, Throwable t) {
        log.error(getDisplayName() + " had error: " + s, t);
        errors.add(s);
    }

    private void addError(String s) {
        log.error(getDisplayName() + " had error: " + s);
        errors.add(s);
    }


}
