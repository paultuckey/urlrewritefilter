package org.tuckey.web.filters.urlrewrite.functions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * todo: turn into a css vars filter plugin
 * Quick and dirty limited implementation of css Variables in Java
 * <p/>
 * http://disruptive-innovations.com/zoo/cssvariables/
 * http://trac.webkit.org/browser/trunk/LayoutTests/fast/css/variables
 */
public class CssVarFunction {

    /*

 set variables, these bay be overriden by request attributes

@variables {
    gadgetBodyLinkColor: #c0c0c0;
}

h1 {
    border-color: var(gadgetBodyLinkColor);
}

     */


    static Pattern VARIABLE_SET_PATTERN = Pattern.compile("\\s*([a-zA-Z0-9-]+)\\s*\\:(.*);");
    static Pattern VARIABLE_REF_PATTERN = Pattern.compile("var\\(([a-zA-Z0-9-]+)\\)");
    static Pattern VARIABLES_BLOCK_START_PATTERN = Pattern.compile("@variables");

    public static void parse(InputStream cssFile, Map variables, OutputStream os) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(cssFile));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        Map defaultVariables = new HashMap();

        if (variables == null) variables = new HashMap();
        String line;
        boolean readingVars = false;
        boolean emitNewLine = false;
        while ((line = br.readLine()) != null) {

            if (emitNewLine && !readingVars) bw.newLine();
            emitNewLine = true;

            if (!readingVars && VARIABLES_BLOCK_START_PATTERN.matcher(line).find()) readingVars = true;

            if (readingVars) {
                Matcher m = VARIABLE_SET_PATTERN.matcher(line);
                while (m.find()) {
                    defaultVariables.put(m.group(1), m.group(2));
                }
            }
            if (!readingVars) {
                Matcher m2 = VARIABLE_REF_PATTERN.matcher(line);
                StringBuffer sb = new StringBuffer();
                while (m2.find()) {
                    String var = m2.group(1);
                    String varValue = (String) variables.get(var);
                    if (varValue == null) varValue = (String) defaultVariables.get(var);
                    if (varValue == null) varValue = "";
                    m2.appendReplacement(sb, varValue);
                }
                m2.appendTail(sb);
                line = sb.toString();

                bw.write(line);
            }
            if (readingVars && line.matches("}")) {
                readingVars = false;
                emitNewLine = false;
            }
        }
        bw.flush();
    }

    public static String parse(String css, Map variables) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(css.getBytes());
        try {
            parse(bais, variables, baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toString();
    }

}

