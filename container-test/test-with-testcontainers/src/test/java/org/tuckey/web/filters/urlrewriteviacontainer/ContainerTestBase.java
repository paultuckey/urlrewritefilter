/**
 * Copyright (c) 2005-2023, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * - Neither the name tuckey.org nor the names of its contributors
 * may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.time.Duration;


@Testcontainers
public abstract class ContainerTestBase {

    protected HttpClient client = new HttpClient();
    private File systemPropBaseReportsDir = new File("container-test", "reports");
    private String containerId = "test";

    String webappPath = Paths.get("..", "example-webapp", "target", "webapp.war")
            .toAbsolutePath().toString();

    @Container
    public GenericContainer<?> container = new GenericContainer<>("tomcat:10.1.9")
            .withReuse(true)
            .withExposedPorts(8080)
            .withFileSystemBind(webappPath, "/usr/local/tomcat/webapps/webapp.war");

    ToStringConsumer toStringConsumer = new ToStringConsumer();

    public void setUp() throws Exception {
        container.start();

        container.followOutput(toStringConsumer, OutputFrame.OutputType.STDOUT);
        container.followOutput(toStringConsumer, OutputFrame.OutputType.STDERR);

        System.out.println(container.getContainerId());
        System.out.println("HOST " + container.getHost());
        System.out.println("PORT " + container.getFirstMappedPort());
        Thread.sleep(2000); //no work //.waitingFor(Wait.forHttp("/webapp/test/test.jsp").forStatusCode(200));
        assert (container.isRunning());

        String containerId = System.getProperty("test.container.id");
        if (containerId != null) {
            this.containerId = containerId;
        }
//        String systemPropBaseUrl = System.getProperty("test.base.url");
//        if (systemPropBaseUrl != null) {
//            baseUrl = systemPropBaseUrl;
//        }
        String systemPropBaseReports = System.getProperty("test.base.reports");
        if (systemPropBaseReports != null) {
            systemPropBaseReportsDir = new File(systemPropBaseReports);
        }
        System.err.println("systemPropBaseReportsDir: " + systemPropBaseReportsDir);

        GetMethod method = new GetMethod(getBaseUrl() + "/rewrite-status/?conf=/WEB-INF/" + getConf());
        client.executeMethod(method);
    }

    public void tearDown() throws InterruptedException {
        Thread.sleep(1);
        // useful for debugging
        //Thread.sleep(5 * 60 * 1000);
        // go to tomcat container then files /usr/local/tomcat/logs
    }

    protected String getBaseUrl() {
        return "http://" + container.getHost() + ":" + container.getFirstMappedPort() + "/" + getApp();
    }

    protected void recordRewriteStatus() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/rewrite-status");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        File statusFile = new File(containerId + "-" + getApp() + "-" + getConf() + "-rewrite-status.html");
        if (statusFile.exists() && !statusFile.delete()) {
            System.out.println("could not remove status at " + statusFile.getAbsolutePath());
        } else
        if (!statusFile.createNewFile()) {  // some containers don't let us do this
            System.out.println("could not create status at " + statusFile.getAbsolutePath());
        } else {
            PrintWriter pw = new PrintWriter(statusFile);
            pw.print(method.getResponseBodyAsString());
            pw.close();
            System.out.println("status saved to " + statusFile.getAbsolutePath());
        }
    }

    abstract protected String getApp();

    protected String getConf() {
        return "urlrewrite.xml";
    }

    public String getContainerId() {
        return containerId;
    }
}
