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
package org.tuckey.web.filters.urlrewriteviacontainer;

import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public abstract class ContainerTestBase extends TestCase {

    private String baseUrl = "http://127.0.0.1:8080";
    protected HttpClient client = new HttpClient();
    private File systemPropBaseReportsDir = new File("container-test", "reports");
    private String containerId = "test";

    protected void setUp() throws Exception {
        String containerId = System.getProperty("test.container.id");
        if (containerId != null) {
            this.containerId = containerId;
        }
        String systemPropBaseUrl = System.getProperty("test.base.url");
        if (systemPropBaseUrl != null) {
            baseUrl = systemPropBaseUrl;
        }
        String systemPropBaseReports = System.getProperty("test.base.reports");
        if (systemPropBaseReports != null) {
            systemPropBaseReportsDir = new File(systemPropBaseReports);
        }
        System.err.println("systemPropBaseReportsDir: " + systemPropBaseReportsDir);

        GetMethod method = new GetMethod(getBaseUrl() + "/rewrite-status/?conf=/WEB-INF/" + getConf());
        client.executeMethod(method);
        super.setUp();
    }

    protected String getBaseUrl() {
        return baseUrl + "/" + getApp();
    }

    protected File getSystemPropBaseReports() {
        return systemPropBaseReportsDir;
    }

    protected void recordRewriteStatus() throws IOException {
        GetMethod method = new GetMethod(baseUrl + "/" + getApp() + "/rewrite-status");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        File statusFile = new File(getSystemPropBaseReports(), containerId + "-" + getApp() + "-" + getConf() + "-rewrite-status.html");
        //noinspection ResultOfMethodCallIgnored
        statusFile.createNewFile();
        PrintWriter pw = new PrintWriter(statusFile);
        pw.print(method.getResponseBodyAsString());
        pw.close();
        System.out.println("status saved to " + statusFile.getAbsolutePath());
    }

    protected abstract String getApp();

    protected String getConf() {
        return "urlrewrite.xml";
    }

    public String getContainerId() {
        return containerId;
    }
}
