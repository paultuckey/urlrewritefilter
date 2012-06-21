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
package org.tuckey.web.filters.urlrewrite.utils;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;


/**
 * Log copies the style of commons logging.  The only reason this exists is that many problems
 * were had with Log4j and commons-logging interfering with peoples installed configuration.
 * It is very easy to change this to slf4j by settting log level to "slf4j"
 * Note, this will fall back to system.log if not initialised with a context.
 *
 * @author Paul Tuckey
 * @version $Revision: 42 $ $Date: 2006-10-29 09:43:56 +1300 (Sun, 29 Oct 2006) $
 */
public class Log {

    private static Log localLog = Log.getLog(Log.class);

    // static vars
    private static ServletContext context = null;
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private static boolean usingSystemOut = false;
    private static boolean usingSystemErr = false;
    private static boolean usingSlf4j = false;

    private static boolean traceLevelEnabled = false;
    private static boolean debugLevelEnabled = false;
    private static boolean infoLevelEnabled = false;
    private static boolean warnLevelEnabled = false;
    private static boolean errorLevelEnabled = false;
    private static boolean fatalLevelEnabled = false;

    private Class clazz = null;

    private org.slf4j.Logger slf4jLogger = null;

    private Log(Class clazz) {
        this.clazz = clazz;
        // check for slf4j
        isUsingSlf4j();
    }

    public boolean isUsingSlf4j() {
        if (usingSlf4j && slf4jLogger == null) {
            this.slf4jLogger = org.slf4j.LoggerFactory.getLogger(clazz);
        }
        return usingSlf4j;
    }

    public boolean isUsingSystemOut() {
        return usingSystemOut;
    }

    public boolean isUsingSystemErr() {
        return usingSystemErr;
    }

    public boolean isTraceEnabled() {
        if (isUsingSlf4j()) return slf4jLogger.isTraceEnabled();
        return traceLevelEnabled;
    }

    public boolean isDebugEnabled() {
        if (isUsingSlf4j()) return slf4jLogger.isDebugEnabled();
        return traceLevelEnabled || debugLevelEnabled;
    }

    public boolean isInfoEnabled() {
        if (isUsingSlf4j()) return slf4jLogger.isInfoEnabled();
        return traceLevelEnabled || debugLevelEnabled || infoLevelEnabled;
    }

    public boolean isWarnEnabled() {
        if (isUsingSlf4j()) return slf4jLogger.isWarnEnabled();
        return traceLevelEnabled || debugLevelEnabled || infoLevelEnabled || warnLevelEnabled;
    }

    public boolean isErrorEnabled() {
        if (isUsingSlf4j()) return slf4jLogger.isErrorEnabled();
        return traceLevelEnabled || debugLevelEnabled || infoLevelEnabled || warnLevelEnabled || errorLevelEnabled;
    }

    public boolean isFatalEnabled() {
        if (isUsingSlf4j()) return slf4jLogger.isErrorEnabled(); // note: SLF4J has no fatal level
        return traceLevelEnabled || debugLevelEnabled || infoLevelEnabled || warnLevelEnabled || errorLevelEnabled || fatalLevelEnabled;
    }


    public void trace(Object o) {
        if (!isTraceEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.trace(String.valueOf(o));
            return;
        }
        write("TRACE", o);
    }

    public void trace(Object o, Throwable throwable) {
        if (!isTraceEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.trace(String.valueOf(o), throwable);
            return;
        }
        write("TRACE", o, throwable);
    }

    public void trace(Throwable throwable) {
        if (!isTraceEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.trace("", throwable);
            return;
        }
        write("TRACE", throwable, throwable);
    }

    public void debug(Object o) {
        if (!isDebugEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.debug(String.valueOf(o));
            return;
        }
        write("DEBUG", o);
    }

    public void debug(Object o, Throwable throwable) {
        if (!isDebugEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.debug(String.valueOf(o), throwable);
            return;
        }
        write("DEBUG", o, throwable);
    }

    public void debug(Throwable throwable) {
        if (!isDebugEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.debug("", throwable);
            return;
        }
        write("DEBUG", throwable, throwable);
    }

    public void info(Object o) {
        if (!isInfoEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.info(String.valueOf(o));
            return;
        }
        write("INFO", o);
    }

    public void info(Object o, Throwable throwable) {
        if (!isInfoEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.info(String.valueOf(o), throwable);
            return;
        }
        write("INFO", o, throwable);
    }

    public void info(Throwable throwable) {
        if (!isInfoEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.info("", throwable);
            return;
        }
        write("INFO", throwable, throwable);
    }

    public void warn(Object o) {
        if (!isWarnEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.warn(String.valueOf(o));
            return;
        }
        write("WARN", o);
    }

    public void warn(Object o, Throwable throwable) {
        if (!isWarnEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.warn(String.valueOf(o), throwable);
            return;
        }
        write("WARN", o, throwable);
    }

    public void warn(Throwable throwable) {
        if (!isWarnEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.warn("", throwable);
            return;
        }
        write("WARN", throwable, throwable);
    }

    public void error(Object o) {
        if (!isErrorEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.error(String.valueOf(o));
            return;
        }
        write("ERROR", o);
    }

    public void error(Object o, Throwable throwable) {
        if (!isErrorEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.error(String.valueOf(o), throwable);
            return;
        }
        write("ERROR", o, throwable);
    }

    public void error(Throwable throwable) {
        if (!isErrorEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.error("", throwable);
            return;
        }
        write("ERROR", throwable, throwable);
    }

    public void fatal(Object o) {
        if (!isFatalEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.error(String.valueOf(o)); // note: SLF4J has no fatal level
            return;
        }
        write("FATAL", o);
    }

    public void fatal(Object o, Throwable throwable) {
        if (!isFatalEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.error(String.valueOf(o), throwable); // note: SLF4J has no fatal level
            return;
        }
        write("FATAL", o, throwable);
    }

    public void fatal(Throwable throwable) {
        if (!isFatalEnabled()) {
            return;
        }
        if (isUsingSlf4j()) {
            slf4jLogger.error("", throwable); // note: SLF4J has no fatal level
            return;
        }
        write("FATAL", throwable, throwable);
    }

    /**
     * Will get an instance of log for a given class.
     *
     * @param aClass to log for
     * @return the log instance
     */
    public static Log getLog(Class aClass) {
        return new Log(aClass);
    }

    /**
     * Set the logging level (options are TRACE, DEBUG, INFO, WARN, ERROR, FATAL).
     *
     * @param level the level to log with
     */
    public static void setLevel(String level) {
        level = level == null ? null : level.toUpperCase();
        usingSystemOut = false;
        usingSystemErr = false;
        // check for log type on the front
        if ("SLF4J".equalsIgnoreCase(level)) {
            usingSlf4j = true;
        } else {
            // log need to parse level also
            if (level != null) {
                if (level.startsWith("SYSOUT:")) {
                    usingSystemOut = true;
                    level = level.substring("SYSOUT:".length());
                }
                if (level.startsWith("STDOUT:")) {
                    usingSystemOut = true;
                    level = level.substring("STDOUT:".length());
                }
                if (level.startsWith("STDERR:")) {
                    usingSystemErr = true;
                    level = level.substring("STDERR:".length());
                }
                if (level.startsWith("SYSERR:")) {
                    usingSystemErr = true;
                    level = level.substring("SYSERR:".length());
                }
            }
            setLevelInternal(level);
        }
    }

    /**
     * actually set the level.  setLevel is really setting type.
     */
    private static void setLevelInternal(String level) {
        // reset all level info
        traceLevelEnabled = false;
        debugLevelEnabled = false;
        infoLevelEnabled = false;
        warnLevelEnabled = false;
        errorLevelEnabled = false;
        fatalLevelEnabled = false;

        // set correct level
        boolean levelSelected = false;
        if ("TRACE".equalsIgnoreCase(level)) {
            traceLevelEnabled = true;
            levelSelected = true;
        }
        if ("DEBUG".equalsIgnoreCase(level)) {
            debugLevelEnabled = true;
            levelSelected = true;
        }
        if ("INFO".equalsIgnoreCase(level)) {
            infoLevelEnabled = true;
            levelSelected = true;
        }
        if ("WARN".equalsIgnoreCase(level)) {
            warnLevelEnabled = true;
            levelSelected = true;
        }
        if ("ERROR".equalsIgnoreCase(level)) {
            errorLevelEnabled = true;
            levelSelected = true;
        }
        if ("FATAL".equalsIgnoreCase(level)) {
            fatalLevelEnabled = true;
            levelSelected = true;
        }
        if (!levelSelected) {
            infoLevelEnabled = true;
        }
    }

    /**
     * Handles writing for throwable.
     *
     * @param level     log level to log for
     * @param throwable to log
     * @param o object to log
     */
    private void write(String level, Object o, Throwable throwable) {
        String msg = getMsg(level, o).toString();
        if (usingSystemOut || context == null) {
            System.out.println(msg);
            throwable.printStackTrace(System.out);
        } else if (usingSystemErr) {
            System.err.println(msg);
            throwable.printStackTrace(System.err);
        } else {
            context.log(msg, throwable);
        }
    }

    /**
     * Handles writing of log lines.
     *
     * @param level log level to log for
     * @param o     object to log (runs toString)
     */
    private void write(String level, Object o) {
        String msg = getMsg(level, o).toString();
        if (usingSystemOut || context == null) {
            System.out.println(msg);
        } else if (usingSystemErr) {
            System.err.println(msg);
        } else {
            context.log(msg);
        }
    }

    private StringBuffer getMsg(String level, Object o) {
        StringBuffer msg = new StringBuffer();
        if (clazz == null) {
            msg.append("null");
        } else {
            msg.append(clazz.getName());
        }
        msg.append(" ");
        msg.append(level);
        msg.append(": ");
        msg.append(o.toString());
        return msg;
    }

    /**
     * Resets log to default state.
     */
    public static void resetAll() {
        Log.context = null;
        setLevel(DEFAULT_LOG_LEVEL);
        Log.usingSystemOut = false;
        Log.usingSystemErr = false;
        Log.usingSlf4j = false;
    }

    /**
     * Will setup Log based on the filter config.  Uses init paramater "logLevel" to get the log level.
     * Defaults to "INFO".
     *
     * @param filterConfig the filter config to use
     */
    public static void setConfiguration(final FilterConfig filterConfig) {
        resetAll();

        if (filterConfig == null) {
            localLog.error("no filter config passed");
            return;
        }
        Log.context = filterConfig.getServletContext();

        String logLevelConf = filterConfig.getInitParameter("logLevel");

        if (logLevelConf != null) {
            logLevelConf = StringUtils.trim(logLevelConf);
        }

        setLevel(logLevelConf);
        localLog.debug("logLevel set to " + logLevelConf);
    }

}
