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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Used for javadoc style output of conf file.
 * todo: doc for urlrewrite links to javadocs for actions 
 *
 * @author Paul Tuckey
 * @version $Revision: 49 $ $Date: 2006-12-08 10:09:07 +1300 (Fri, 08 Dec 2006) $
 */
public class UrlRewriteDocTask extends Task {

    private File conf = new File(UrlRewriteFilter.DEFAULT_WEB_CONF_PATH);
    private File dest = new File("urlrewrite-conf-overview.html");
    private String logLevel = "INFO";

    public void execute() throws BuildException {
        try {
            Log.setLevel("SYSOUT:" + logLevel);
            show();

        } catch (FileNotFoundException e) {
            throw new BuildException(e);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private void show() throws IOException {
        // make sure we don't have claspath issues when we load runs
        Run.setLoadClass(false);
        CatchElem.setLoadClass(false);

        Conf confObj = new Conf(new FileInputStream(conf), conf.getName());
        confObj.initialise();

        if (!confObj.isOk()) {
            throw new BuildException("conf is not ok");
        }
        log("loaded fine with " + confObj.getRules().size() + " rules");

        File reportFile = dest;
        if (reportFile.exists()) {
            reportFile.delete();
        }
        FileWriter writer;
        writer = new FileWriter(reportFile);

        Status status = new Status(confObj);
        status.displayStatusOffline();

        writer.write(status.getBuffer().toString());
        writer.close();
    }

    public void setConf(File conf) {
        this.conf = conf;
    }

    public void setDest(File dest) {
        this.dest = dest;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
}
