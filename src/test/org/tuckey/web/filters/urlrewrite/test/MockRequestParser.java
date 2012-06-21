package org.tuckey.web.filters.urlrewrite.test;

import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * todo: IN PROGRESS
 *
 * Class to decode a request from one of three inputs.  Literally:
 * 
 * /blah.jsp
 *
 * or
 *
 * GET /blah.jsp HTTP/1.1
 *
 * or
 *
 * GET /blah.jsp HTTP/1.1
 * user-agent: Mozilla 1.2.3
 * cookie: a:aaa
 *
 */
public class MockRequestParser {

    private static Log log = Log.getLog(MockRequestParser.class);

    public MockRequest decodeRequest(String requestString) {
        return decodeRequest(requestString.split("\n"));
    }

    Pattern FIRST_LINE_PATTERN = Pattern.compile("^([A-Z]+) (.*) ([A-Z0-9/\\.]+)$");

    public MockRequest decodeRequest(String[] requestLines) {
        MockRequest request = new MockRequest();
        request.setRemoteAddr("127.0.0.1");
        request.setRemoteHost("localhost");

        String line1 = requestLines[0];
        Matcher line1Matcher = FIRST_LINE_PATTERN.matcher(line1);

        String requestPart;
        if (line1Matcher.matches()) {
            request.setMethod(line1Matcher.group(1));
            requestPart = line1Matcher.group(2);
            request.setScheme(line1Matcher.group(3));
        } else {
            requestPart = line1;
        }

        String requestUri = requestPart;
        int questionMarkIdx = requestPart.indexOf('?');
        if (questionMarkIdx != -1) {
            String queryString = requestPart.substring(questionMarkIdx + 1);
            setParams(request, queryString);
            requestUri = requestPart.substring(0, questionMarkIdx);
        }
        int semiColonIdx = requestUri.indexOf(';');
        if (semiColonIdx != -1) {
            String sessionId = requestUri.substring(semiColonIdx + 1);
            int equalsIdx = sessionId.indexOf('=');
            if ( equalsIdx != -1 ) {
                sessionId = sessionId.substring(equalsIdx+1);
            }
            request.setRequestedSessionId(sessionId);
            requestUri = requestUri.substring(0, semiColonIdx);
        }
        request.setRequestURI(requestUri);

        if (requestLines.length > 1) {
            boolean parsingBody = false;
            for (int i = 0; i < requestLines.length; i++) {
                if (i == 0) continue;
                String line = requestLines[i];
                if (!parsingBody) {
                    if (line.length() == 0) {
                        parsingBody = true;
                        continue;
                    }
                    int colonIdx = line.indexOf(':');
                    if (colonIdx < 1) {
                        log.debug("cannot parse line " + line);
                        continue;
                    }
                    String headerName = StringUtils.trimToNull(line.substring(0, colonIdx));
                    String headerValue = StringUtils.trimToNull(line.substring(colonIdx + 1));
                    if (headerName == null) {
                        log.debug("cannot parse line " + line);
                        continue;
                    }
                    request.setHeader(headerName, headerValue);
                    if ("host".equals(headerName.toLowerCase())) {
                        request.setServerName(headerValue);
                    }
                } else {
                    setParams(request, line);
                }
            }
        }
        return request;
    }

    private void setParams(MockRequest request, String queryString) {
        request.setQueryString(queryString);
        if (queryString.indexOf('&') != -1) {
            String[] paramSets = queryString.split("&");
            for (int i = 0; i < paramSets.length; i++) {
                addParamSet(request, paramSets[i]);
            }
        } else {
            addParamSet(request, queryString);
        }
    }

    private void addParamSet(MockRequest request, String paramSet) {
        if (paramSet.indexOf("=") != -1) {
            String[] nameVal = paramSet.split("=", 2);
            request.addParameter(nameVal[0], nameVal[1]);
        } else {
            log.info("cannot find value of request parameter " + paramSet);
        }
    }


}
