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

import org.tuckey.web.filters.urlrewrite.gzip.GzipFilter;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.utils.ModRewriteConfLoader;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXParseException;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Configuration object for urlrewrite filter.
 *
 * @author Paul Tuckey
 * @version $Revision: 43 $ $Date: 2006-10-31 17:29:59 +1300 (Tue, 31 Oct 2006) $
 */
public class Conf {

    private static Log log = Log.getLog(Conf.class);

    private final List errors = new ArrayList();
    private final List rules = new ArrayList(50);
    private final List catchElems = new ArrayList(10);
    private List outboundRules = new ArrayList(50);
    private boolean ok = false;
    private Date loadedDate = null;
    private int ruleIdCounter = 0;
    private int outboundRuleIdCounter = 0;
    private String fileName;
    private String confSystemId;

    protected boolean useQueryString;
    protected boolean useContext;

    private static final String NONE_DECODE_USING = "null";
    private static final String HEADER_DECODE_USING = "header";
    private static final String DEFAULT_DECODE_USING = "header,utf-8";

    protected String decodeUsing = DEFAULT_DECODE_USING;
    private boolean decodeUsingEncodingHeader;

    protected String defaultMatchType = null;

    private ServletContext context;
    private boolean docProcessed = false;
    private boolean engineEnabled = true;

    /**
     * Empty const for testing etc.
     */
    public Conf() {
        loadedDate = new Date();
    }

    /**
     * Constructor for use only when loading XML style configuration.
     *
     * @param fileName to display on status screen
     */
    public Conf(ServletContext context, final InputStream inputStream, String fileName, String systemId) {
        this(context, inputStream, fileName, systemId, false);
    }

    /**
     * Normal constructor.
     *
     * @param fileName            to display on status screen
     * @param modRewriteStyleConf true if loading mod_rewrite style conf
     */
    public Conf(ServletContext context, final InputStream inputStream, String fileName, String systemId,
                boolean modRewriteStyleConf) {
        // make sure context is setup before calling initialise()
        this.context = context;
        this.fileName = fileName;
        this.confSystemId = systemId;
        if (modRewriteStyleConf) {
            loadModRewriteStyle(inputStream);
        } else {
            loadDom(inputStream);
        }
        if (docProcessed) initialise();
        loadedDate = new Date();
    }

    protected void loadModRewriteStyle(InputStream inputStream) {
        ModRewriteConfLoader loader = new ModRewriteConfLoader();
        try {
            loader.process(inputStream, this);
            docProcessed = true; // fixed
        } catch (IOException e) {
            addError("Exception loading conf " + " " + e.getMessage(), e);
        }
    }

    /**
     * Constructor when run elements don't need to be initialised correctly, for docuementation etc.
     */
    public Conf(URL confUrl) {
        // make sure context is setup before calling initialise()
        this.context = null;
        this.fileName = confUrl.getFile();
        this.confSystemId = confUrl.toString();
        try {
            loadDom(confUrl.openStream());
        } catch (IOException e) {
            addError("Exception loading conf " + " " + e.getMessage(), e);
        }
        if (docProcessed) initialise();
        loadedDate = new Date();
    }

    /**
     * Constructor when run elements don't need to be initialised correctly, for docuementation etc.
     */
    public Conf(InputStream inputStream, String conffile) {
        this(null, inputStream, conffile, conffile);
    }

    /**
     * Load the dom document from the inputstream
     * <p/>
     * Note, protected so that is can be extended.
     *
     * @param inputStream stream of the conf file to load
     */
    protected synchronized void loadDom(final InputStream inputStream) {
        if (inputStream == null) {
            log.error("inputstream is null");
            return;
        }
        DocumentBuilder parser;

        /**
         * the thing that resolves dtd's and other xml entities.
         */
        ConfHandler handler = new ConfHandler(confSystemId);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        log.debug("XML builder factory is: " + factory.getClass().getName());
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        try {
            parser = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("Unable to setup XML parser for reading conf", e);
            return;
        }
        log.debug("XML Parser: " + parser.getClass().getName());

        parser.setErrorHandler(handler);
        parser.setEntityResolver(handler);

        try {
            log.debug("about to parse conf");
            Document doc = parser.parse(inputStream, confSystemId);
            processConfDoc(doc);

        } catch (SAXParseException e) {
            addError("Parse error on line " + e.getLineNumber() + " " + e.getMessage(), e);

        } catch (Exception e) {
            addError("Exception loading conf " + " " + e.getMessage(), e);
        }
    }

    /**
     * Process dom document and populate Conf object.
     * <p/>
     * Note, protected so that is can be extended.
     */
    protected void processConfDoc(Document doc) {
        Element rootElement = doc.getDocumentElement();

        if ("true".equalsIgnoreCase(getAttrValue(rootElement, "use-query-string"))) setUseQueryString(true);
        if ("true".equalsIgnoreCase(getAttrValue(rootElement, "use-context"))) {
            log.debug("use-context set to true");
            setUseContext(true);
        }
        setDecodeUsing(getAttrValue(rootElement, "decode-using"));
        setDefaultMatchType(getAttrValue(rootElement, "default-match-type"));

        NodeList rootElementList = rootElement.getChildNodes();
        for (int i = 0; i < rootElementList.getLength(); i++) {
            Node node = rootElementList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE &&
                    ((Element) node).getTagName().equals("rule")) {
                Element ruleElement = (Element) node;
                // we have a rule node
                NormalRule rule = new NormalRule();

                processRuleBasics(ruleElement, rule);
                procesConditions(ruleElement, rule);
                processRuns(ruleElement, rule);

                Node toNode = ruleElement.getElementsByTagName("to").item(0);
                rule.setTo(getNodeValue(toNode));
                rule.setToType(getAttrValue(toNode, "type"));
                rule.setToContextStr(getAttrValue(toNode, "context"));
                rule.setToLast(getAttrValue(toNode, "last"));
                rule.setQueryStringAppend(getAttrValue(toNode, "qsappend"));
                if ("true".equalsIgnoreCase(getAttrValue(toNode, "encode"))) rule.setEncodeToUrl(true);

                processSetAttributes(ruleElement, rule);

                addRule(rule);

            } else if (node.getNodeType() == Node.ELEMENT_NODE &&
                    ((Element) node).getTagName().equals("class-rule")) {
                Element ruleElement = (Element) node;

                ClassRule classRule = new ClassRule();
                if ("false".equalsIgnoreCase(getAttrValue(ruleElement, "enabled"))) classRule.setEnabled(false);
                if ("false".equalsIgnoreCase(getAttrValue(ruleElement, "last"))) classRule.setLast(false);
                classRule.setClassStr(getAttrValue(ruleElement, "class"));
                classRule.setMethodStr(getAttrValue(ruleElement, "method"));

                addRule(classRule);

            } else if (node.getNodeType() == Node.ELEMENT_NODE &&
                    ((Element) node).getTagName().equals("outbound-rule")) {

                Element ruleElement = (Element) node;
                // we have a rule node
                OutboundRule rule = new OutboundRule();

                processRuleBasics(ruleElement, rule);
                if ("true".equalsIgnoreCase(getAttrValue(ruleElement, "encodefirst"))) rule.setEncodeFirst(true);

                procesConditions(ruleElement, rule);
                processRuns(ruleElement, rule);

                Node toNode = ruleElement.getElementsByTagName("to").item(0);
                rule.setTo(getNodeValue(toNode));
                rule.setToLast(getAttrValue(toNode, "last"));
                if ("false".equalsIgnoreCase(getAttrValue(toNode, "encode"))) rule.setEncodeToUrl(false);

                processSetAttributes(ruleElement, rule);

                addOutboundRule(rule);

            } else if (node.getNodeType() == Node.ELEMENT_NODE &&
                    ((Element) node).getTagName().equals("catch")) {

                Element catchXMLElement = (Element) node;
                // we have a rule node
                CatchElem catchElem = new CatchElem();

                catchElem.setClassStr(getAttrValue(catchXMLElement, "class"));

                processRuns(catchXMLElement, catchElem);

                catchElems.add(catchElem);

            }
        }
        docProcessed = true;
    }

    private void processRuleBasics(Element ruleElement, RuleBase rule) {
      if ("false".equalsIgnoreCase(getAttrValue(ruleElement, "enabled"))) rule.setEnabled(false);

      String ruleMatchType = getAttrValue(ruleElement, "match-type");
      if (StringUtils.isBlank(ruleMatchType)) ruleMatchType = defaultMatchType;
      rule.setMatchType(ruleMatchType);

      Node nameNode = ruleElement.getElementsByTagName("name").item(0);
      rule.setName(getNodeValue(nameNode));

      Node noteNode = ruleElement.getElementsByTagName("note").item(0);
      rule.setNote(getNodeValue(noteNode));

      Node fromNode = ruleElement.getElementsByTagName("from").item(0);
      rule.setFrom(getNodeValue(fromNode));
      if ("true".equalsIgnoreCase(getAttrValue(fromNode, "casesensitive"))) rule.setFromCaseSensitive(true);
  }

    private static void processSetAttributes(Element ruleElement, RuleBase rule) {
        NodeList setNodes = ruleElement.getElementsByTagName("set");
        for (int j = 0; j < setNodes.getLength(); j++) {
            Node setNode = setNodes.item(j);
            if (setNode == null) continue;
            SetAttribute setAttribute = new SetAttribute();
            setAttribute.setValue(getNodeValue(setNode));
            setAttribute.setType(getAttrValue(setNode, "type"));
            setAttribute.setName(getAttrValue(setNode, "name"));
            rule.addSetAttribute(setAttribute);
        }
    }

    private static void processRuns(Element ruleElement, Runnable runnable) {
        NodeList runNodes = ruleElement.getElementsByTagName("run");
        for (int j = 0; j < runNodes.getLength(); j++) {
            Node runNode = runNodes.item(j);
            if (runNode == null) continue;
            Run run = new Run();
            processInitParams(runNode, run);
            run.setClassStr(getAttrValue(runNode, "class"));
            run.setMethodStr(getAttrValue(runNode, "method"));
            run.setJsonHandler("true".equalsIgnoreCase(getAttrValue(runNode, "jsonhandler")));
            run.setNewEachTime("true".equalsIgnoreCase(getAttrValue(runNode, "neweachtime")));
            runnable.addRun(run);
        }

        // gzip element is just a shortcut to run: org.tuckey.web.filters.urlrewrite.gzip.GzipFilter
        NodeList gzipNodes = ruleElement.getElementsByTagName("gzip");
        for (int j = 0; j < gzipNodes.getLength(); j++) {
            Node runNode = gzipNodes.item(j);
            if (runNode == null) continue;
            Run run = new Run();
            run.setClassStr(GzipFilter.class.getName());
            run.setMethodStr("doFilter(ServletRequest, ServletResponse, FilterChain)");
            processInitParams(runNode, run);
            runnable.addRun(run);
        }
    }

    private static void processInitParams(Node runNode, Run run) {
        if (runNode.getNodeType() == Node.ELEMENT_NODE) {
            Element runElement = (Element) runNode;
            NodeList initParamsNodeList = runElement.getElementsByTagName("init-param");
            for (int k = 0; k < initParamsNodeList.getLength(); k++) {
                Node initParamNode = initParamsNodeList.item(k);
                if (initParamNode == null) continue;
                if (initParamNode.getNodeType() != Node.ELEMENT_NODE) continue;
                Element initParamElement = (Element) initParamNode;
                Node paramNameNode = initParamElement.getElementsByTagName("param-name").item(0);
                Node paramValueNode = initParamElement.getElementsByTagName("param-value").item(0);
                run.addInitParam(getNodeValue(paramNameNode), getNodeValue(paramValueNode));
            }
        }
    }

    private static void procesConditions(Element ruleElement, RuleBase rule) {
        NodeList conditionNodes = ruleElement.getElementsByTagName("condition");
        for (int j = 0; j < conditionNodes.getLength(); j++) {
            Node conditionNode = conditionNodes.item(j);
            if (conditionNode == null) continue;
            Condition condition = new Condition();
            condition.setValue(getNodeValue(conditionNode));
            condition.setType(getAttrValue(conditionNode, "type"));
            condition.setName(getAttrValue(conditionNode, "name"));
            condition.setNext(getAttrValue(conditionNode, "next"));
            condition.setCaseSensitive("true".equalsIgnoreCase(getAttrValue(conditionNode, "casesensitive")));
            condition.setOperator(getAttrValue(conditionNode, "operator"));
            rule.addCondition(condition);
        }
    }

    private static String getNodeValue(Node node) {
        if (node == null) return null;
        NodeList nodeList = node.getChildNodes();
        if (nodeList == null) return null;
        Node child = nodeList.item(0);
        if (child == null) return null;
        if ((child.getNodeType() == Node.TEXT_NODE)) {
            String value = ((Text) child).getData();
            return value.trim();
        }
        return null;
    }

    private static String getAttrValue(Node n, String attrName) {
        if (n == null) return null;
        NamedNodeMap attrs = n.getAttributes();
        if (attrs == null) return null;
        Node attr = attrs.getNamedItem(attrName);
        if (attr == null) return null;
        String val = attr.getNodeValue();
        if (val == null) return null;
        return val.trim();
    }

    /**
     * Initialise the conf file.  This will run initialise on each rule and condition in the conf file.
     */
    public void initialise() {
        if (log.isDebugEnabled()) {
            log.debug("now initialising conf");
        }

        initDecodeUsing(decodeUsing);

        boolean rulesOk = true;
        for (int i = 0; i < rules.size(); i++) {
            final Rule rule = (Rule) rules.get(i);
            if (!rule.initialise(context)) {
                // if we failed to initialise anything set the status to bad
                rulesOk = false;
            }
        }
        for (int i = 0; i < outboundRules.size(); i++) {
            final OutboundRule outboundRule = (OutboundRule) outboundRules.get(i);
            if (!outboundRule.initialise(context)) {
                // if we failed to initialise anything set the status to bad
                rulesOk = false;
            }
        }
        for (int i = 0; i < catchElems.size(); i++) {
            final CatchElem catchElem = (CatchElem) catchElems.get(i);
            if (!catchElem.initialise(context)) {
                // if we failed to initialise anything set the status to bad
                rulesOk = false;
            }
        }
        if (rulesOk) {
            ok = true;
        }
        if (log.isDebugEnabled()) {
            log.debug("conf status " + ok);
        }
    }

    private void initDecodeUsing(String decodeUsingSetting) {
        decodeUsingSetting = StringUtils.trimToNull(decodeUsingSetting);
        if (decodeUsingSetting == null) decodeUsingSetting = DEFAULT_DECODE_USING;

        if ( decodeUsingSetting.equalsIgnoreCase(HEADER_DECODE_USING)) { // is 'header'
            decodeUsingEncodingHeader = true;
            decodeUsingSetting = null;

        }   else if ( decodeUsingSetting.startsWith(HEADER_DECODE_USING + ",")) { // is 'header,xxx'
            decodeUsingEncodingHeader = true;
            decodeUsingSetting = decodeUsingSetting.substring((HEADER_DECODE_USING + ",").length());

        }
        if (NONE_DECODE_USING.equalsIgnoreCase(decodeUsingSetting)) {
            decodeUsingSetting = null;
        }
        if ( decodeUsingSetting != null ) {
            try {
                URLDecoder.decode("testUrl", decodeUsingSetting);
                this.decodeUsing = decodeUsingSetting;
            } catch (UnsupportedEncodingException e) {
                addError("unsupported 'decodeusing' " + decodeUsingSetting + " see Java SDK docs for supported encodings");
            }
        }   else {
            this.decodeUsing = null;
        }
    }

    /**
     * Destory the conf gracefully.
     */
    public void destroy() {
        for (int i = 0; i < rules.size(); i++) {
            final Rule rule = (Rule) rules.get(i);
            rule.destroy();
        }
    }

    /**
     * Will add the rule to the rules list.
     *
     * @param rule The Rule to add
     */
    public void addRule(final Rule rule) {
        rule.setId(ruleIdCounter++);
        rules.add(rule);
    }

    /**
     * Will add the rule to the rules list.
     *
     * @param outboundRule The outbound rule to add
     */
    public void addOutboundRule(final OutboundRule outboundRule) {
        outboundRule.setId(outboundRuleIdCounter++);
        outboundRules.add(outboundRule);
    }

    /**
     * Will get the List of errors.
     *
     * @return the List of errors
     */
    public List getErrors() {
        return errors;
    }

    /**
     * Will get the List of rules.
     *
     * @return the List of rules
     */
    public List getRules() {
        return rules;
    }

    /**
     * Will get the List of outbound rules.
     *
     * @return the List of outbound rules
     */
    public List getOutboundRules() {
        return outboundRules;
    }

    /**
     * true if the conf has been loaded ok.
     *
     * @return boolean
     */
    public boolean isOk() {
        return ok;
    }

    private void addError(final String errorMsg, final Exception e) {
        errors.add(errorMsg);
        log.error(errorMsg, e);
    }

    private void addError(final String errorMsg) {
        errors.add(errorMsg);
    }

    public Date getLoadedDate() {
        return (Date) loadedDate.clone();
    }

    public String getFileName() {
        return fileName;
    }


    public boolean isUseQueryString() {
        return useQueryString;
    }

    public void setUseQueryString(boolean useQueryString) {
        this.useQueryString = useQueryString;
    }

    public boolean isUseContext() {
        return useContext;
    }

    public void setUseContext(boolean useContext) {
        this.useContext = useContext;
    }

    public String getDecodeUsing() {
        return decodeUsing;
    }

    public void setDecodeUsing(String decodeUsing) {
        this.decodeUsing = decodeUsing;
    }

    public void setDefaultMatchType(String defaultMatchType) {
        if (RuleBase.MATCH_TYPE_WILDCARD.equalsIgnoreCase(defaultMatchType)) {
            this.defaultMatchType = RuleBase.MATCH_TYPE_WILDCARD;
        } else {
            this.defaultMatchType = RuleBase.DEFAULT_MATCH_TYPE;
        }
    }

    public String getDefaultMatchType() {
        return defaultMatchType;
    }

    public List getCatchElems() {
        return catchElems;
    }

    public boolean isDecodeUsingCustomCharsetRequired() {
        return decodeUsing != null;
    }

    public boolean isEngineEnabled() {
        return engineEnabled;
    }

    public void setEngineEnabled(boolean engineEnabled) {
        this.engineEnabled = engineEnabled;
    }

    public boolean isLoadedFromFile() {
        return fileName != null;
    }

    public boolean isDecodeUsingEncodingHeader() {
        return decodeUsingEncodingHeader;
    }
}
