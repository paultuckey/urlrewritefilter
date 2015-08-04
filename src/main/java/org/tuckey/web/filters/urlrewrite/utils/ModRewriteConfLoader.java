package org.tuckey.web.filters.urlrewrite.utils;

import org.tuckey.web.filters.urlrewrite.Condition;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.NormalRule;
import org.tuckey.web.filters.urlrewrite.SetAttribute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loader to enable loading of mod_rewrite style configuration for UrlRewriteFilter.
 */
public class ModRewriteConfLoader {

    private static Log log = Log.getLog(ModRewriteConfLoader.class);

    private final Pattern LOG_LEVEL_PATTERN = Pattern.compile("RewriteLogLevel\\s+([0-9]+)\\s*$");
    private final Pattern LOG_TYPE_PATTERN = Pattern.compile("RewriteLog\\s+(.*)$");
    private final Pattern ENGINE_PATTERN = Pattern.compile("RewriteEngine\\s+([a-zA-Z0-9]+)\\s*$");
    private final Pattern CONDITION_PATTERN = Pattern.compile("RewriteCond\\s+(.*)$");
    private final Pattern RULE_PATTERN = Pattern.compile("RewriteRule\\s+(.*)$");

    public void process(InputStream is, Conf conf) throws IOException {
        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        while ((line = in.readLine()) != null) {
            buffer.append(line);
            buffer.append("\n");
        }
        process(buffer.toString(), conf);
    }

    public void process(String modRewriteStyleConf, Conf conf) {
        String[] lines = modRewriteStyleConf.split("\n");
        List conditionsBuffer = new ArrayList();
        StringBuffer notesBuffer = new StringBuffer();
        String logLevelStr = null;
        String logTypeStr = null;

        for (int i = 0; i < lines.length; i++) {
            String line = StringUtils.trimToNull(lines[i]);
            if (line == null) continue;
            log.debug("processing line: " + line);

            if (line.startsWith("#")) {
                log.debug("adding note line (line starting with #)");
                if (notesBuffer.length() > 0) notesBuffer.append("\n");
                String noteLine = StringUtils.trim(line.substring(1));
                notesBuffer.append(noteLine);

            } else if (line.startsWith("RewriteBase")) {
                log.info("RewriteBase not supported, ignored");

            } else if (line.startsWith("RewriteCond")) {
                Condition condition = processRewriteCond(line);
                if (condition != null) conditionsBuffer.add(condition);

            } else if (line.startsWith("RewriteEngine")) {
                processRewriteEngine(conf, line);

            } else if (line.startsWith("RewriteLock")) {
                log.error("RewriteLock not supported, ignored");

            } else if (line.startsWith("RewriteLogLevel")) {
                logLevelStr = parseLogLevel(logLevelStr, line);

            } else if (line.startsWith("RewriteLog")) {
                logTypeStr = parseLogType(logTypeStr, line);

            } else if (line.startsWith("RewriteMap")) {
                log.error("RewriteMap not supported, ignored");

            } else if (line.startsWith("RewriteOptions")) {
                log.error("RewriteOptions not supported, ignored");

            } else if (line.startsWith("RewriteRule")) {
                parseRule(conf, conditionsBuffer, notesBuffer, line);
                notesBuffer = new StringBuffer();
                conditionsBuffer = new ArrayList();
            }
        }
        if (logTypeStr != null || logLevelStr != null) {
            String logStr = (logTypeStr == null ? "" : logTypeStr) + (logLevelStr == null ? "" : ":" + logLevelStr);
            log.debug("setting log to: " + logStr);
            Log.setLevel(logStr);
        }
        if (conditionsBuffer.size() > 0) {
            log.error("conditions left over without a rule");
        }
    }

    private void parseRule(Conf conf, List conditionsBuffer, StringBuffer notesBuffer, String line) {
        NormalRule rule = processRule(line);
        for (int j = 0; j < conditionsBuffer.size(); j++) {
            Condition condition = (Condition) conditionsBuffer.get(j);
            rule.addCondition(condition);
        }
        if (notesBuffer.length() > 0) rule.setNote(notesBuffer.toString());
        conf.addRule(rule);
    }

    private String parseLogType(String logTypeStr, String line) {
        Matcher logTypeMatcher = LOG_TYPE_PATTERN.matcher(line);
        if (logTypeMatcher.matches()) {
            logTypeStr = StringUtils.trimToNull(logTypeMatcher.group(1));
            if (logTypeStr != null) {
                logTypeStr = logTypeStr.replaceAll("\"", "");
                log.debug("RewriteLog parsed as " + logTypeStr);
            }
        }
        return logTypeStr;
    }

    private String parseLogLevel(String logLevelStr, String line) {
        log.debug("found a RewriteLogLevel");
        Matcher logLevelMatcher = LOG_LEVEL_PATTERN.matcher(line);
        if (logLevelMatcher.matches()) {
            int logLevel = NumberUtils.stringToInt(logLevelMatcher.group(1));
            if (logLevel <= 1) logLevelStr = "FATAL";
            else if (logLevel == 2) logLevelStr = "ERROR";
            else if (logLevel == 3) logLevelStr = "INFO";
            else if (logLevel == 4) logLevelStr = "WARN";
            else if (logLevel >= 5) logLevelStr = "DEBUG";
            log.debug("RewriteLogLevel parsed as " + logLevel);

        } else {
            log.error("cannot parse " + line);
        }
        return logLevelStr;
    }

    private NormalRule processRule(String line) {
        NormalRule rule = new NormalRule();
        Matcher ruleMatcher = RULE_PATTERN.matcher(line);
        if (ruleMatcher.matches()) {
            String rulePartStr = StringUtils.trimToNull(ruleMatcher.group(1));
            if (rulePartStr != null) {
                log.debug("got rule " + rulePartStr);
                String[] ruleParts = rulePartStr.split(" ");
                int partCounter = 0;
                for (int j = 0; j < ruleParts.length; j++) {
                    String part = StringUtils.trimToNull(ruleParts[j]);
                    if (part == null) continue;
                    partCounter++;
                    log.debug("parsed rule part " + part);
                    if (partCounter == 1) {
                        rule.setFrom(part);
                    }
                    if (partCounter == 2 && !"-".equals(part)) {
                        rule.setTo(part);
                    }
                    if (part.startsWith("[") && part.endsWith("]")) {
                        processRuleFlags(rule, part);
                    }
                }
            } else {
                log.error("could not parse rule from " + line);
            }
        } else {
            log.error("cannot parse " + line);
        }
        return rule;
    }

    private void processRewriteEngine(Conf conf, String line) {
        boolean enabled = true;
        Matcher engineMatcher = ENGINE_PATTERN.matcher(line);
        if (engineMatcher.matches()) {
            String enabledStr = StringUtils.trim(engineMatcher.group(1));
            log.debug("RewriteEngine value parsed as '" + enabledStr + "'");
            if ("0".equalsIgnoreCase(enabledStr) ||
                    "false".equalsIgnoreCase(enabledStr) ||
                    "no".equalsIgnoreCase(enabledStr) ||
                    "off".equalsIgnoreCase(enabledStr)) enabled = false;
            log.debug("RewriteEngine as boolean '" + enabled + "'");
        } else {
            log.error("cannot parse " + line);
        }
        conf.setEngineEnabled(enabled);
    }

    private void processRuleFlags(NormalRule rule, String part) {
        String rawFlags = StringUtils.trimToNull(part.substring(1, part.length() - 1));
        if (rawFlags != null) {
            String[] flags = rawFlags.split(",");
            for (int k = 0; k < flags.length; k++) {
                String flag = flags[k];
                String flagValue = null;
                if (flag.indexOf("=") != -1) {
                    flagValue = flag.substring(flag.indexOf("=") + 1);
                    flag = flag.substring(0, flag.indexOf("="));
                }
                flag = flag.toLowerCase();
                /*
                # 'chain|C' (chained with next rule)
                This flag chains the current rule with the next rule (which itself can be chained with the following rule, and so on). This has the following effect: if a rule matches, then processing continues as usual - the flag has no effect. If the rule does not match, then all following chained rules are skipped. For instance, it can be used to remove the ``.www'' part, inside a per-directory rule set, when you let an external redirect happen (where the ``.www'' part should not occur!).
                 */
                if ("chain".equalsIgnoreCase(flag) || "C".equalsIgnoreCase(flag)) {
                    log.info("chain flag [C] not supported");
                }
                /*
                # 'cookie|CO=NAME:VAL:domain[:lifetime[:path]]' (set cookie)
                This sets a cookie in the client's browser. The cookie's name is specified by NAME and the value is VAL. The domain field is the domain of the cookie, such as '.apache.org', the optional lifetime is the lifetime of the cookie in minutes, and the optional path is the path of the cookie
                 */
                if ("cookie".equalsIgnoreCase(flag) || "CO".equalsIgnoreCase(flag)) {
                    SetAttribute set = new SetAttribute();
                    set.setType("cookie");
                    String cookieName = flagValue;
                    String cookieValue = null;
                    if (flagValue != null) {
                        int colon = flagValue.indexOf(":");
                        if (colon != -1) {
                            cookieValue = flagValue.substring(colon + 1);
                            cookieName = flagValue.substring(0, colon);
                        }
                    }
                    set.setName(cookieName);
                    // NOTE: The colon separated domain, lifetime, path are
                    // handled by SetAttribute.setValue()
                    set.setValue(cookieValue);
                    rule.addSetAttribute(set);
                }
                /*
                # 'env|E=VAR:VAL' (set environment variable)
                This forces an environment variable named VAR to be set to the value VAL, where VAL can contain regexp backreferences ($N and %N) which will be expanded. You can use this flag more than once, to set more than one variable. The variables can later be dereferenced in many situations, most commonly from within XSSI (via <!--#echo var="VAR"-->) or CGI ($ENV{'VAR'}). You can also dereference the variable in a later RewriteCond pattern, using %{ENV:VAR}. Use this to strip information from URLs, while maintaining a record of that information.
                */
                if ("env".equalsIgnoreCase(flag) || "E".equalsIgnoreCase(flag)) {
                    log.info("env flag [E] not supported");
                }
                /*
                # 'forbidden|F' (force URL to be forbidden)
                This forces the current URL to be forbidden - it immediately sends back a HTTP response of 403 (FORBIDDEN). Use this flag in conjunction with appropriate RewriteConds to conditionally block some URLs.
                */
                if ("forbidden".equalsIgnoreCase(flag) || "F".equalsIgnoreCase(flag)) {
                    SetAttribute set = new SetAttribute();
                    set.setType("status");
                    set.setValue("403");
                    rule.addSetAttribute(set);
                }
                /*
                # 'gone|G' (force URL to be gone)
                This forces the current URL to be gone - it immediately sends back a HTTP response of 410 (GONE). Use this flag to mark pages which no longer exist as gone.
                */
                if ("gone".equalsIgnoreCase(flag) || "G".equalsIgnoreCase(flag)) {
                    SetAttribute set = new SetAttribute();
                    set.setType("status");
                    set.setValue("410");
                    rule.addSetAttribute(set);
                }
                /*
                # 'last|L' (last rule)
                Stop the rewriting process here and don't apply any more rewrite rules. This corresponds to the Perl last command or the break command in C. Use this flag to prevent the currently rewritten URL from being rewritten further by following rules. For example, use it to rewrite the root-path URL ('/') to a real one, e.g., '/e/www/'.
                */
                if ("last".equalsIgnoreCase(flag) || "L".equalsIgnoreCase(flag)) {
                    rule.setToLast("true");
                }
                /*
                # 'next|N' (next round)
                Re-run the rewriting process (starting again with the first rewriting rule). This time, the URL to match is no longer the original URL, but rather the URL returned by the last rewriting rule. This corresponds to the Perl next command or the continue command in C. Use this flag to restart the rewriting process - to immediately go to the top of the loop.
                Be careful not to create an infinite loop!
                */
                if ("next".equalsIgnoreCase(flag) || "N".equalsIgnoreCase(flag)) {
                    log.info("next flag [N] not supported");
                }
                /*
                # 'nocase|NC' (no case)
                This makes the Pattern case-insensitive, ignoring difference between 'A-Z' and 'a-z' when Pattern is matched against the current URL.
                */
                if ("nocase".equalsIgnoreCase(flag) || "NC".equalsIgnoreCase(flag)) {
                    rule.setFromCaseSensitive(false);
                }
                /*
                # 'noescape|NE' (no URI escaping of output)
                This flag prevents mod_rewrite from applying the usual URI escaping rules to the result of a rewrite. Ordinarily, special characters (such as '%', '$', ';', and so on) will be escaped into their hexcode equivalents ('%25', '%24', and '%3B', respectively); this flag prevents this from happening. This allows percent symbols to appear in the output, as in

                RewriteRule /foo/(.*) /bar?arg=P1\%3d$1 [R,NE]
                which would turn '/foo/zed' into a safe request for '/bar?arg=P1=zed'.
                */
                if ("noescape".equalsIgnoreCase(flag) || "NE".equalsIgnoreCase(flag)) {
                    rule.setEncodeToUrl(false);
                }
                /*
                # 'nosubreq|NS' ( not for internal sub-requests)
                This flag forces the rewrite engine to skip a rewrite rule if the current request is an internal sub-request. For instance, sub-requests occur internally in Apache when mod_include tries to find out information about possible directory default files (index.xxx). On sub-requests it is not always useful, and can even cause errors, if the complete set of rules are applied. Use this flag to exclude some rules.
                To decide whether or not to use this rule: if you prefix URLs with CGI-scripts, to force them to be processed by the CGI-script, it's likely that you will run into problems (or significant overhead) on sub-requests. In these cases, use this flag.
                */
                if ("nosubreq".equalsIgnoreCase(flag) || "NS".equalsIgnoreCase(flag)) {
                    log.info("nosubreq flag [NS] not supported");
                }
                /*
                # 'proxy|P' (force proxy)
                This flag forces the substitution part to be internally sent as a proxy request and immediately (rewrite processing stops here) put through the proxy module. You must make sure that the substitution string is a valid URI (typically starting with http://hostname) which can be handled by the Apache proxy module. If not, you will get an error from the proxy module. Use this flag to achieve a more powerful implementation of the ProxyPass directive, to map remote content into the namespace of the local server.

                Note: mod_proxy must be enabled in order to use this flag.
                */
                if ("proxy".equalsIgnoreCase(flag) || "P".equalsIgnoreCase(flag)) {
                    rule.setToType("proxy");
                }
                /*
                # 'passthrough|PT' (pass through to next handler)
                This flag forces the rewrite engine to set the uri field of the internal request_rec structure to the value of the filename field. This flag is just a hack to enable post-processing of the output of RewriteRule directives, using Alias, ScriptAlias, Redirect, and other directives from various URI-to-filename translators. For example, to rewrite /abc to /def using mod_rewrite, and then /def to /ghi using mod_alias:

                RewriteRule ^/abc(.*) /def$1 [PT]
                Alias /def /ghi
                If you omit the PT flag, mod_rewrite will rewrite uri=/abc/... to filename=/def/... as a full API-compliant URI-to-filename translator should do. Then mod_alias will try to do a URI-to-filename transition, which will fail.

                Note: You must use this flag if you want to mix directives from different modules which allow URL-to-filename translators. The typical example is the use of mod_alias and mod_rewrite.
                */
                if ("passthrough".equalsIgnoreCase(flag) || "PT".equalsIgnoreCase(flag)) {
                    rule.setToType("forward");
                }
                /*
                # 'qsappend|QSA' (query string append)
                This flag forces the rewrite engine to append a query string part of the substitution string to the existing string, instead of replacing it. Use this when you want to add more data to the query string via a rewrite rule.
                */
                if ("qsappend".equalsIgnoreCase(flag) || "QSA".equalsIgnoreCase(flag)) {
                    rule.setQueryStringAppend("true");
                }
                /*
                # 'redirect|R [=code]' (force redirect)
                Prefix Substitution with http://thishost[:thisport]/ (which makes the new URL a URI) to force a external redirection. If no code is given, a HTTP response of 302 (MOVED TEMPORARILY) will be returned. If you want to use other response codes in the range 300-400, simply specify the appropriate number or use one of the following symbolic names: temp (default), permanent, seeother. Use this for rules to canonicalize the URL and return it to the client - to translate ``/~'' into ``/u/'', or to always append a slash to /u/user, etc.
                Note: When you use this flag, make sure that the substitution field is a valid URL! Otherwise, you will be redirecting to an invalid location. Remember that this flag on its own will only prepend http://thishost[:thisport]/ to the URL, and rewriting will continue. Usually, you will want to stop rewriting at this point, and redirect immediately. To stop rewriting, you should add the 'L' flag.
                */
                if ("redirect".equalsIgnoreCase(flag) || "R".equalsIgnoreCase(flag)) {
                    if ("301".equals(flagValue)) {
                        rule.setToType("permanent-redirect");
                    } else if ("302".equals(flagValue)) {
                        rule.setToType("temporary-redirect");
                    } else {
                        rule.setToType("redirect");
                    }
                }
                /*
                # 'skip|S=num' (skip next rule(s))
                This flag forces the rewriting engine to skip the next num rules in sequence, if the current rule matches. Use this to make pseudo if-then-else constructs: The last rule of the then-clause becomes skip=N, where N is the number of rules in the else-clause. (This is not the same as the 'chain|C' flag!)
                */
                if ("skip".equalsIgnoreCase(flag) || "S".equalsIgnoreCase(flag)) {
                    log.info("Skip flag [S] not supported");
                }
                /*
                # 'type|T=MIME-type' (force MIME type)
                Force the MIME-type of the target file to be MIME-type. This can be used to set up the content-type based on some conditions. For example, the following snippet allows .php files to be displayed by mod_php if they are called with the .phps extension:
                 */
                if ("type".equalsIgnoreCase(flag) || "T".equalsIgnoreCase(flag)) {
                    SetAttribute set = new SetAttribute();
                    set.setType("content-type");
                    set.setValue(flagValue);
                    rule.addSetAttribute(set);
                }

            }

        } else {
            log.error("cannot parse flags from " + part);
        }
    }

    private Condition processRewriteCond(String rewriteCondLine) {
        log.debug("about to parse condition");
        Condition condition = new Condition();
        Matcher condMatcher = CONDITION_PATTERN.matcher(rewriteCondLine);
        if (condMatcher.matches()) {
            String conditionParts = StringUtils.trimToNull(condMatcher.group(1));
            if (conditionParts != null) {
                String[] condParts = conditionParts.split(" ");
                for (int i = 0; i < condParts.length; i++) {
                    String part = StringUtils.trimToNull(condParts[i]);
                    if (part == null) continue;
                    if (part.equalsIgnoreCase("%{HTTP_USER_AGENT}")) {
                        condition.setType("header");
                        condition.setName("user-agent");
                    } else if (part.equalsIgnoreCase("%{HTTP_REFERER}")) {
                        condition.setType("header");
                        condition.setName("referer");
                    } else if (part.equalsIgnoreCase("%{HTTP_COOKIE}")) {
                        condition.setType("header");
                        condition.setName("cookie");
                    } else if (part.equalsIgnoreCase("%{HTTP_FORWARDED}")) {
                        condition.setType("header");
                        condition.setName("forwarded");
                    } else if (part.equalsIgnoreCase("%{HTTP_PROXY_CONNECTION}")) {
                        condition.setType("header");
                        condition.setName("proxy-connection");
                    } else if (part.equalsIgnoreCase("%{HTTP_ACCEPT}")) {
                        condition.setType("header");
                        condition.setName("accept");
                    } else if (part.equalsIgnoreCase("%{HTTP_HOST}")) {
                        condition.setType("server-name");

                    } else if (part.equalsIgnoreCase("%{REMOTE_ADDR}")) {
                        condition.setType("remote-addr");
                    } else if (part.equalsIgnoreCase("%{REMOTE_HOST}")) {
                        condition.setType("remote-host");
                    } else if (part.equalsIgnoreCase("%{REMOTE_USER}")) {
                        condition.setType("remote-user");
                    } else if (part.equalsIgnoreCase("%{REQUEST_METHOD}")) {
                        condition.setType("method");
                    } else if (part.equalsIgnoreCase("%{QUERY_STRING}")) {
                        condition.setType("query-string");

                    } else if (part.equalsIgnoreCase("%{TIME_YEAR}")) {
                        condition.setType("year");
                    } else if (part.equalsIgnoreCase("%{TIME_MON}")) {
                        condition.setType("month");
                    } else if (part.equalsIgnoreCase("%{TIME_DAY}")) {
                        condition.setType("dayofmonth");
                    } else if (part.equalsIgnoreCase("%{TIME_WDAY}")) {
                        condition.setType("dayofweek");
                    } else if (part.equalsIgnoreCase("%{TIME_HOUR}")) {
                        condition.setType("hourofday");
                    } else if (part.equalsIgnoreCase("%{TIME_MIN}")) {
                        condition.setType("minute");
                    } else if (part.equalsIgnoreCase("%{TIME_SEC}")) {
                        condition.setType("second");

                    } else if (part.equalsIgnoreCase("%{PATH_INFO}")) {
                        condition.setType("path-info");
                    } else if (part.equalsIgnoreCase("%{AUTH_TYPE}")) {
                        condition.setType("auth-type");
                    } else if (part.equalsIgnoreCase("%{SERVER_PORT}")) {
                        condition.setType("port");
                    } else if (part.equalsIgnoreCase("%{REQUEST_URI}")) {
                        condition.setType("request-uri");
                    } else if (part.equalsIgnoreCase("%{REQUEST_FILENAME}")) {
                        condition.setType("request-filename");

                    } else if (part.equals("-f") || part.equals("-F")) {
                        condition.setOperator("isfile");
                    } else if (part.equals("-d")) {
                        condition.setOperator("isdir");
                    } else if (part.equalsIgnoreCase("-s")) {
                        condition.setOperator("isfilewithsize");
                    } else if (part.equals("!-f") || part.equals("!-F")) {
                        condition.setOperator("notfile");
                    } else if (part.equals("!-d")) {
                        condition.setOperator("notdir");
                    } else if (part.equalsIgnoreCase("!-s")) {
                        condition.setOperator("notfilewithsize");

                        //todo: bits below this comment
                    } else if (part.equalsIgnoreCase("%{REMOTE_PORT}")) {
                        log.error("REMOTE_PORT currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{REMOTE_IDENT}")) {
                        log.error("REMOTE_IDENT currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{SCRIPT_FILENAME}")) {
                        log.error("SCRIPT_FILENAME currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{DOCUMENT_ROOT}")) {
                        log.error("DOCUMENT_ROOT currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{SERVER_ADMIN}")) {
                        log.error("SERVER_ADMIN currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{SERVER_NAME}")) {
                        log.error("SERVER_NAME currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{SERVER_ADDR}")) {
                        log.error("SERVER_ADDR currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{SERVER_PROTOCOL}")) {
                        log.error("SERVER_PROTOCOL currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{SERVER_SOFTWARE}")) {
                        log.error("SERVER_SOFTWARE currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{TIME}")) {
                        log.error("TIME currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{API_VERSION}")) {
                        log.error("API_VERSION currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{THE_REQUEST}")) {
                        log.error("THE_REQUEST currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{IS_SUBREQ}")) {
                        log.error("IS_SUBREQ currently unsupported, ignoring");
                    } else if (part.equalsIgnoreCase("%{HTTPS}")) {
                        log.error("HTTPS currently unsupported, ignoring");
                        //todo: note https in mod_rewrite means "on" in URF land it means true

                    } else {
                        condition.setValue(part);
                    }

                }
            } else {
                log.error("could not parse condition from " + rewriteCondLine);
            }
        } else {
            log.error("cannot parse " + rewriteCondLine);
        }
        return condition;
    }


}
