package org.tuckey.web.filters.urlrewrite;

/**
 * Inteface for something that contains Run's, so we can initialise cleanly.
 */
public interface Runnable {

    public void addRun(final Run run);

}
