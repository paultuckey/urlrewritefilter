package org.tuckey.web.filters.urlrewrite.utils;

import junit.framework.TestCase;
import org.tuckey.web.testhelper.MockFilterConfig;

/**
 * 
 * 
 */
public class LogTest extends TestCase {

    public void testInitNull() {
        Log.setConfiguration(null);
    }

    public void testInitEmpty() {
        Log.setConfiguration(new MockFilterConfig());
    }

    public void testNull() {
        Log log = Log.getLog(null);
        log.debug("hi");
    }

    public void testDebug() {
        Log log = Log.getLog(null);
        Log.setLevel("DEBUG");
        assertFalse(log.isTraceEnabled());
        assertTrue(log.isDebugEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(log.isWarnEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
        log.debug("hi");
    }
}
