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

import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

/**
 * Assists with the setting of variable type names for condition types and rule to variables.
 *
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class TypeConverter {

    /**
     * Type of condition ie, header, port etc.
     */
    protected short type;

    /**
     * Error message from the regular expression compilation.
     */
    protected String error = null;

    // Type statics
    public static final short TYPE_TIME = 4;
    public static final short TYPE_TIME_YEAR = 5;
    public static final short TYPE_TIME_MONTH = 6;
    public static final short TYPE_TIME_DAY_OF_MONTH = 7;
    public static final short TYPE_TIME_DAY_OF_WEEK = 8;
    public static final short TYPE_TIME_AMPM = 9;
    public static final short TYPE_TIME_HOUR_OF_DAY = 10;
    public static final short TYPE_TIME_MINUTE = 11;
    public static final short TYPE_TIME_SECOND = 12;
    public static final short TYPE_TIME_MILLISECOND = 13;
    public static final short TYPE_ATTRIBUTE = 14;
    public static final short TYPE_AUTH_TYPE = 15;
    public static final short TYPE_CHARACTER_ENCODING = 16;
    public static final short TYPE_CONTENT_LENGTH = 17;
    public static final short TYPE_CONTENT_TYPE = 18;
    public static final short TYPE_CONTEXT_PATH = 19;
    public static final short TYPE_COOKIE = 20;
    public static final short TYPE_HEADER = 1;
    public static final short TYPE_LOCAL_PORT = 39;
    public static final short TYPE_METHOD = 21;
    public static final short TYPE_PARAMETER = 22;
    public static final short TYPE_PATH_INFO = 23;
    public static final short TYPE_PATH_TRANSLATED = 24;
    public static final short TYPE_PROTOCOL = 25;
    public static final short TYPE_QUERY_STRING = 26;
    public static final short TYPE_REMOTE_ADDR = 27;
    public static final short TYPE_REMOTE_HOST = 28;
    public static final short TYPE_REMOTE_USER = 29;
    public static final short TYPE_REQUESTED_SESSION_ID = 30;
    public static final short TYPE_REQUEST_URI = 31;
    public static final short TYPE_REQUEST_URL = 32;
    public static final short TYPE_SESSION_ATTRIBUTE = 33;
    public static final short TYPE_SESSION_IS_NEW = 34;
    public static final short TYPE_SERVER_PORT = 35;
    public static final short TYPE_SERVER_NAME = 36;
    public static final short TYPE_SCHEME = 37;
    public static final short TYPE_USER_IN_ROLE = 38;
    public static final short TYPE_EXCEPTION = 40;
    public static final short TYPE_REQUESTED_SESSION_ID_FROM_COOKIE = 41;
    public static final short TYPE_REQUESTED_SESSION_ID_FROM_URL = 42;
    public static final short TYPE_REQUESTED_SESSION_ID_VALID = 43;
    public static final short TYPE_REQUEST_FILENAME = 44;
    public static final short TYPE_SERVLET_CONTEXT = 45;

    /**
     * Will get the type code ie, method, port, header etc.
     *
     * @return String
     */
    public String getType() {
        switch (type) {

            case TYPE_TIME:
                return "time";
            case TYPE_TIME_YEAR:
                return "year";
            case TYPE_TIME_MONTH:
                return "month";
            case TYPE_TIME_DAY_OF_MONTH:
                return "dayofmonth";
            case TYPE_TIME_DAY_OF_WEEK:
                return "dayofweek";

            case TYPE_TIME_AMPM:
                return "ampm";
            case TYPE_TIME_HOUR_OF_DAY:
                return "hourofday";
            case TYPE_TIME_MINUTE:
                return "minute";
            case TYPE_TIME_SECOND:
                return "second";
            case TYPE_TIME_MILLISECOND:
                return "millisecond";

            case TYPE_ATTRIBUTE:
                return "attribute";
            case TYPE_AUTH_TYPE:
                return "auth-type";
            case TYPE_CHARACTER_ENCODING:
                return "character-encoding";
            case TYPE_CONTENT_LENGTH:
                return "content-length";
            case TYPE_CONTENT_TYPE:
                return "content-type";

            case TYPE_CONTEXT_PATH:
                return "context-path";
            case TYPE_COOKIE:
                return "cookie";
            case TYPE_HEADER:
                return "header";
            case TYPE_LOCAL_PORT:
                return "local-port";
            case TYPE_METHOD:
                return "method";
            case TYPE_PARAMETER:
                return "parameter";

            case TYPE_PATH_INFO:
                return "path-info";
            case TYPE_PATH_TRANSLATED:
                return "path-translated";
            case TYPE_PROTOCOL:
                return "protocol";
            case TYPE_QUERY_STRING:
                return "query-string";
            case TYPE_REMOTE_ADDR:
                return "remote-addr";

            case TYPE_REMOTE_HOST:
                return "remote-host";
            case TYPE_REMOTE_USER:
                return "remote-user";
            case TYPE_REQUESTED_SESSION_ID:
                return "requested-session-id";
            case TYPE_REQUESTED_SESSION_ID_FROM_COOKIE:
              return "requested-session-id-from-cookie";
            case TYPE_REQUESTED_SESSION_ID_FROM_URL:
              return "requested-session-id-from-url";
            case TYPE_REQUESTED_SESSION_ID_VALID:
              return "requested-session-id-valid";
            case TYPE_REQUEST_URI:
                return "request-uri";
            case TYPE_REQUEST_URL:
                return "request-url";
            case TYPE_SESSION_ATTRIBUTE:
                return "session-attribute";
            case TYPE_SESSION_IS_NEW:
                return "session-isnew";
            case TYPE_SERVER_PORT:
                return "port";
            case TYPE_SERVER_NAME:
                return "server-name";
            case TYPE_SCHEME:
                return "scheme";

            case TYPE_USER_IN_ROLE:
                return "user-in-role";
            case TYPE_EXCEPTION:
                return "exception";
            case TYPE_REQUEST_FILENAME:
                return "request-filename";
            case TYPE_SERVLET_CONTEXT:
            	return "context";
            default:
                return "";
        }
    }

    /**
     * Will set the type.
     *
     * @param strType the type
     */
    public void setType(final String strType) {
        if ("time".equals(strType)) {
            this.type = TYPE_TIME;
        } else if ("year".equals(strType)) {
            this.type = TYPE_TIME_YEAR;
        } else if ("month".equals(strType)) {
            this.type = TYPE_TIME_MONTH;
        } else if ("dayofmonth".equals(strType)) {
            this.type = TYPE_TIME_DAY_OF_MONTH;
        } else if ("dayofweek".equals(strType)) {
            this.type = TYPE_TIME_DAY_OF_WEEK;

        } else if ("ampm".equals(strType)) {
            this.type = TYPE_TIME_AMPM;
        } else if ("hourofday".equals(strType)) {
            this.type = TYPE_TIME_HOUR_OF_DAY;
        } else if ("minute".equals(strType)) {
            this.type = TYPE_TIME_MINUTE;
        } else if ("second".equals(strType)) {
            this.type = TYPE_TIME_SECOND;
        } else if ("millisecond".equals(strType)) {
            this.type = TYPE_TIME_MILLISECOND;

        } else if ("attribute".equals(strType)) {
            this.type = TYPE_ATTRIBUTE;
        } else if ("auth-type".equals(strType)) {
            this.type = TYPE_AUTH_TYPE;
        } else if ("character-encoding".equals(strType)) {
            this.type = TYPE_CHARACTER_ENCODING;
        } else if ("content-length".equals(strType)) {
            this.type = TYPE_CONTENT_LENGTH;
        } else if ("content-type".equals(strType)) {
            this.type = TYPE_CONTENT_TYPE;

        } else if ("context-path".equals(strType)) {
            this.type = TYPE_CONTEXT_PATH;
        } else if ("cookie".equals(strType)) {
            this.type = TYPE_COOKIE;
        } else if ("header".equals(strType) || StringUtils.isBlank(strType)) {
            this.type = TYPE_HEADER;
        } else if ("local-port".equals(strType)) {
            this.type = TYPE_LOCAL_PORT;
        } else if ("method".equals(strType)) {
            this.type = TYPE_METHOD;
        } else if ("parameter".equals(strType) || "param".equals(strType)) {
            this.type = TYPE_PARAMETER;

        } else if ("path-info".equals(strType)) {
            this.type = TYPE_PATH_INFO;
        } else if ("path-translated".equals(strType)) {
            this.type = TYPE_PATH_TRANSLATED;
        } else if ("protocol".equals(strType)) {
            this.type = TYPE_PROTOCOL;
        } else if ("query-string".equals(strType)) {
            this.type = TYPE_QUERY_STRING;
        } else if ("remote-addr".equals(strType)) {
            this.type = TYPE_REMOTE_ADDR;

        } else if ("remote-host".equals(strType)) {
            this.type = TYPE_REMOTE_HOST;
        } else if ("remote-user".equals(strType)) {
            this.type = TYPE_REMOTE_USER;
        } else if ("requested-session-id".equals(strType)) {
            this.type = TYPE_REQUESTED_SESSION_ID;
        } else if ("requested-session-id-from-cookie".equals(strType)) {
          this.type = TYPE_REQUESTED_SESSION_ID_FROM_COOKIE;
        } else if ("requested-session-id-from-url".equals(strType)) {
          this.type = TYPE_REQUESTED_SESSION_ID_FROM_URL;
        } else if ("requested-session-id-valid".equals(strType)) {
          this.type = TYPE_REQUESTED_SESSION_ID_VALID;
        } else if ("request-uri".equals(strType)) {
            this.type = TYPE_REQUEST_URI;
        } else if ("request-url".equals(strType)) {
            this.type = TYPE_REQUEST_URL;

        } else if ("session-attribute".equals(strType)) {
            this.type = TYPE_SESSION_ATTRIBUTE;
        } else if ("session-isnew".equals(strType)) {
            this.type = TYPE_SESSION_IS_NEW;
        } else if ("port".equals(strType)) {
            this.type = TYPE_SERVER_PORT;
        } else if ("server-name".equals(strType)) {
            this.type = TYPE_SERVER_NAME;
        } else if ("scheme".equals(strType)) {
            this.type = TYPE_SCHEME;

        } else if ("user-in-role".equals(strType)) {
            this.type = TYPE_USER_IN_ROLE;

        } else if ("exception".equals(strType)) {
            this.type = TYPE_EXCEPTION;

        } else if ("request-filename".equals(strType)) {
            this.type = TYPE_REQUEST_FILENAME;

        } else if ("context".equals(strType)) {
            this.type = TYPE_SERVLET_CONTEXT;

        }else {
            setError("Type " + strType + " is not valid");
        }
    }


    /**
     * Will get the description of the error.
     *
     * @return String
     */
    public final String getError() {
        return error;
    }

    protected void setError(String error) {
        this.error = error;
    }

    public int getTypeShort() {
        return type;
    }
}
