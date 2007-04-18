package org.tuckey.web.filters.urlrewrite;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.tuckey.web.filters.urlrewrite.utils.Log;


public class RuleExecutionOutput {

    private static Log log = Log.getLog(RuleExecutionOutput.class);

    private String replacedUrl;
    private boolean ruleMatched = false;
    private boolean stopFilterMatch = false;
    private RewriteMatch rewriteMatch;

    /**
     * Will perform the action defined by the rule ie, redirect or passthrough.
     *
     * @param ruleExecutionOutput
     */
    public static RewrittenUrl getRewritenUrl(short toType, boolean encodeToUrl, RuleExecutionOutput ruleExecutionOutput) {

        NormalRewrittenUrl rewrittenRequest = new NormalRewrittenUrl(ruleExecutionOutput);
        String toUrl = ruleExecutionOutput.getReplacedUrl();

        if (toType == NormalRule.TO_TYPE_REDIRECT) {
            if (log.isDebugEnabled()) {
                log.debug("needs to be redirected to " + toUrl);
            }
            rewrittenRequest.setRedirect(true);

        } else if (toType == NormalRule.TO_TYPE_PERMANENT_REDIRECT) {
            if (log.isDebugEnabled()) {
                log.debug("needs to be permanentely redirected to " + toUrl);
            }
            rewrittenRequest.setPermanentRedirect(true);

        } else if (toType == NormalRule.TO_TYPE_TEMPORARY_REDIRECT) {
            if (log.isDebugEnabled()) {
                log.debug("needs to be temporarily redirected to " + toUrl);
            }
            rewrittenRequest.setTemporaryRedirect(true);

        } else if (toType == NormalRule.TO_TYPE_PRE_INCLUDE) {
            if (log.isDebugEnabled()) {
                log.debug(toUrl + " needs to be pre included");
            }
            rewrittenRequest.setPreInclude(true);

        } else if (toType == NormalRule.TO_TYPE_POST_INCLUDE) {
            if (log.isDebugEnabled()) {
                log.debug(toUrl + " needs to be post included");
            }
            rewrittenRequest.setPostInclude(true);

        } else if (toType == NormalRule.TO_TYPE_FORWARD) {

            // pass the request to the "to" url
            if (log.isDebugEnabled()) {
                log.debug("needs to be forwarded to " + toUrl);
            }
            rewrittenRequest.setForward(true);
        }
        if (encodeToUrl) {
            rewrittenRequest.setEncode(true);
        } else {
            rewrittenRequest.setEncode(false);
        }

        return rewrittenRequest;
    }

    public RuleExecutionOutput(String replacedUrl, boolean ruleMatched, RewriteMatch lastRunMatch) {
        this.replacedUrl = replacedUrl;
        this.ruleMatched = ruleMatched;
        this.rewriteMatch = lastRunMatch;
    }

    public String getReplacedUrl() {
        return replacedUrl;
    }

    public boolean isRuleMatched() {
        return ruleMatched;
    }

    public boolean isStopFilterMatch() {
        return stopFilterMatch;
    }

    public void setStopFilterMatch(boolean stopFilterMatch) {
        this.stopFilterMatch = stopFilterMatch;
    }

    public void setReplacedUrl(String replacedUrl) {
        this.replacedUrl = replacedUrl;
    }

    public RewriteMatch getRewriteMatch() {
        return rewriteMatch;
    }


}
