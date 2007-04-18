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

    private String conf = UrlRewriteFilter.DEFAULT_WEB_CONF_PATH;
    private String dest = "urlrewrite-conf-overview.html";
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

        Conf confObj = new Conf(new FileInputStream(conf), conf);
        confObj.initialise();

        if (!confObj.isOk()) {
            throw new BuildException("conf is not ok");
        }
        log("loaded fine with " + confObj.getRules().size() + " rules");

        File reportFile = new File(dest);
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

    public void setConf(String conf) {
        this.conf = conf;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
}
