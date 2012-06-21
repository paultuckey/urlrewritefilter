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
package org.tuckey.web.filters.urlrewrite.annotation;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.util.SourcePosition;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Annotation processor for UrlRewrite. Will search compiled classes for annotations and generate XML.
 * <p/>
 * Processor for JDK 1.5.
 *
 * @deprecated use UrlRewriteAnnotationProcessor.
 */
public class HttpUrlAnnotationProcessor implements AnnotationProcessor {

    private AnnotationProcessorEnvironment environment;
    private AnnotationTypeDeclaration httpUrlDeclaration;
    private AnnotationTypeDeclaration httpExceptionHandlerDeclaration;
    private List<ProcessedHttpUrlAnnotation> processedAnnotations = new ArrayList<ProcessedHttpUrlAnnotation>();
    private List<ProcessedHttpExceptionAnnotation> httpExceptionHandlers = new ArrayList<ProcessedHttpExceptionAnnotation>();
    private Messager messager;
    private boolean showPositionsOfAnnotations = false;
    private boolean debug = false;

    public HttpUrlAnnotationProcessor(AnnotationProcessorEnvironment env) {
        environment = env;
        messager = env.getMessager();
        // get the type declaration for the annotations we are processing for
        httpUrlDeclaration = (AnnotationTypeDeclaration) environment.getTypeDeclaration(HttpUrl.class.getName());
        httpExceptionHandlerDeclaration = (AnnotationTypeDeclaration) environment.getTypeDeclaration(HttpExceptionHandler.class.getName());
    }

    public void process() {
        Map<String, String> options = environment.getOptions();
        Set<String> keys = options.keySet();
        String saveRulesTo = null;
        for (String key : keys) {
            if (key.startsWith("-AsaveRulesTo=")) {
                saveRulesTo = key.substring("-AsaveRulesTo=".length());
            }
            if (key.startsWith("-AshowPositions=")) {
                showPositionsOfAnnotations = "true".equalsIgnoreCase(key.substring("-AshowPositions=".length()));
            }
            if (key.startsWith("-Adebug=")) {
                debug = "true".equalsIgnoreCase(key.substring("-Adebug=".length()));
            }
        }
        debugMsg("Processing");
        if (saveRulesTo == null) {
            messager.printError("ERROR: conf option must be specified");
            return;
        }

        File confFile = new File(saveRulesTo);
        PrintWriter pw;
        boolean delFile = false;
        try {
            if (!confFile.exists()) {
                checkDirsExistMkdir(confFile.getParentFile());
                confFile.createNewFile();
            }
            if (!confFile.canWrite()) throw new IOException("cannot write to " + confFile.getName());
            pw = environment.getFiler().createTextFile(Filer.Location.CLASS_TREE, "", confFile, null);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {

            // Get all declarations that use the HttpUrl annotation.
            debugMsg("Looking for @HttpUrl");
            Collection<Declaration> urlDeclarations = environment.getDeclarationsAnnotatedWith(httpUrlDeclaration);
            for (Declaration declaration : urlDeclarations) {
                ProcessedHttpUrlAnnotation pa = processHttpUrlAnnotation(declaration);
                if (pa == null) delFile = true;
                else processedAnnotations.add(pa);
            }

            // Get all declarations that use the HttpExceptionHandler annotation.
            debugMsg("Looking for @HttpExceptionHandler");
            Collection<Declaration> exceptionDeclarations = environment.getDeclarationsAnnotatedWith(httpExceptionHandlerDeclaration);
            for (Declaration declaration : exceptionDeclarations) {
                ProcessedHttpExceptionAnnotation phea = processHttpExceptionHandlerAnnotation(declaration);
                if (phea == null) delFile = true;
                else httpExceptionHandlers.add(phea);
            }

            if (processedAnnotations.size() > 0) {
                messager.printNotice("Got " + processedAnnotations.size() + " @HttpUrl annotations");
                Collections.sort(processedAnnotations);
            }
            if (httpExceptionHandlers.size() > 0) {
                messager.printNotice("Got " + httpExceptionHandlers.size() + " @HttpExceptionHandler annotations");
                Collections.sort(httpExceptionHandlers);
            }

            if (!delFile) {
                environment.getMessager().printNotice("Writing to " + confFile);
                outputRules(pw);
                outputExceptionHandlers(pw);

            } else {
                confFile.delete();
            }

        } catch (Throwable t) {
            delFile = true;
            t.printStackTrace();
        }
        if (delFile) {
            messager.printError("Error occured during processing deleting generated file.");
            confFile.delete();
        }

        pw.close();
    }

    private void outputRules(PrintWriter pw) {
        for (ProcessedHttpUrlAnnotation pa : processedAnnotations) {
            pw.println("<rule>");
            pw.println("    <name>" + pa.sourceRef + "</name>");
            if (!isBlank(pa.docComment)) {
                pw.println("    <note>");
                pw.println(padEachLine("        ", escapeXML(pa.docComment)));
                pw.println("    </note>");
            }
            pw.println("    <from>" + pa.value + "</from>");
            pw.println("    <run class=\"" + pa.className + "\" method=\"" + pa.methodName + pa.paramsFormatted +
                    "\" />");
            if (!pa.chainUsed) {
                pw.println("    <to>null</to>");
            }
            pw.println("</rule>");
            pw.flush();
        }
    }

    private void outputExceptionHandlers(PrintWriter pw) {
        for (ProcessedHttpExceptionAnnotation pa : httpExceptionHandlers) {
            pw.println("<catch class=\"" + pa.exceptionName + "\">");
            if (!isBlank(pa.docComment)) {
                pw.println("    <note>");
                pw.println(padEachLine("        ", escapeXML(pa.docComment)));
                pw.println("    </note>");
            }
            pw.println("    <run class=\"" + pa.className + "\" method=\"" + pa.methodName + pa.paramsFormatted + "\"/>");
            pw.println("</catch>");
            pw.flush();
        }
    }

    private ProcessedHttpUrlAnnotation processHttpUrlAnnotation(Declaration declaration) {
        HttpUrl httpUrl = declaration.getAnnotation(HttpUrl.class);
        return new ProcessedHttpUrlAnnotation(HttpUrl.class.getName(), declaration, httpUrl.value(), httpUrl.weight());
    }

    private ProcessedHttpExceptionAnnotation processHttpExceptionHandlerAnnotation(Declaration declaration) {
        SourcePosition position = declaration.getPosition();
        if (!(declaration instanceof MethodDeclaration)) {
            messager.printWarning(declaration.getPosition(), "@HttpExceptionHandler declared on a non-method " + position);
            return null;
        }
        MethodDeclaration methodDeclaration = (MethodDeclaration) declaration;
        HttpExceptionHandler httpExceptionHandler = declaration.getAnnotation(HttpExceptionHandler.class);
        String className = methodDeclaration.getDeclaringType().getQualifiedName();

        ProcessedHttpExceptionAnnotation ea = new ProcessedHttpExceptionAnnotation();
        ea.exceptionName = httpExceptionHandler.value(); //.getName();
        ea.methodName = declaration.getSimpleName();
        ea.docComment = declaration.getDocComment();
        ea.className = className;

        ea.setParams(methodDeclaration.getParameters());

        // out exceptionName might not be set
        if ("[ unassigned ]".equals(ea.exceptionName) && methodDeclaration.getParameters().size() > 0) {
            // use first param
            ea.exceptionName = methodDeclaration.getParameters().iterator().next().getType().toString();
        }

        if (showPositionsOfAnnotations) {
            messager.printNotice(position, "@HttpExceptionHandlerUrl value " + ea.value + " weight " + ea.weight);
        }
        return ea;
    }

    class ProcessedHttpUrlAnnotation implements Comparable<ProcessedHttpUrlAnnotation> {
        public int weight = 0;
        public String value;
        public boolean chainUsed;
        public String paramsFormatted;
        public String methodName;
        public String className;
        public String docComment;
        public String sourceRef;
        private static final String FILTER_CHAIN_CLASS_NAME = "javax.servlet.FilterChain";

        public ProcessedHttpUrlAnnotation() {
            // empty
        }

        public ProcessedHttpUrlAnnotation(String typeName, Declaration declaration, String value, int weight) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) declaration;
            String className = methodDeclaration.getDeclaringType().getQualifiedName();
            this.methodName = declaration.getSimpleName();
            this.docComment = declaration.getDocComment();
            this.className = className;
            this.value = value;
            this.weight = weight;
            this.setParams(methodDeclaration.getParameters());
            String typeNameShort = typeName.substring(typeName.lastIndexOf("."));
            SourcePosition positionInCode = declaration.getPosition();
            sourceRef = positionInCode.file().getName() + ":" + positionInCode.line();
            if (!(declaration instanceof MethodDeclaration)) {
                messager.printWarning(positionInCode, "@" + typeNameShort + " declared on a non-method " + positionInCode);
            }
            if (showPositionsOfAnnotations) {
                messager.printNotice(positionInCode, "@" + typeNameShort + " value " + value + " weight " + weight);
            }
        }

        public int compareTo(ProcessedHttpUrlAnnotation other) {
            if (this.weight < other.weight) return 1;
            if (this.weight > other.weight) return -1;
            if (this.className != null && other.className != null) {
                int comp = this.className.compareTo(other.className);
                if (comp != 0) return comp;
            }
            if (this.methodName != null && other.methodName != null) {
                return this.methodName.compareTo(other.methodName);
            }
            return 0;
        }

        void setParams(Collection<ParameterDeclaration> params) {
            paramsFormatted = "(";
            chainUsed = false;
            if (params.size() > 0) {
                int i = 1;
                for (ParameterDeclaration paramDeclaration : params) {
                    String paramType = paramDeclaration.getType().toString();
                    if (FILTER_CHAIN_CLASS_NAME.equals(paramType)) {
                        chainUsed = true;
                    }
                    paramsFormatted += (i == 1 ? "" : ", ") + paramType;

                    HttpParam httpParam = paramDeclaration.getAnnotation(HttpParam.class);
                    if (httpParam != null) {
                        paramsFormatted += " ";
                        if (!"[ unassigned ]".equals(httpParam.value())) {
                            paramsFormatted += httpParam.value();
                        } else {
                            paramsFormatted += paramDeclaration.getSimpleName();
                        }
                    }
                    i++;
                }
            }
            paramsFormatted += ")";
        }
    }

    class ProcessedHttpExceptionAnnotation extends ProcessedHttpUrlAnnotation {
        public String exceptionName;

        public int compareTo(ProcessedHttpExceptionAnnotation other) {
            int comp = super.compareTo(other);
            if (comp == 0) comp = exceptionName.compareTo(other.exceptionName);
            return comp;
        }

    }


    /**
     * a very very basic xml escaper.
     *
     * @param s string to escape
     * @return the escaped string
     */
    private static String escapeXML(String s) {
        if (s == null) {
            return null;
        }
        final int length = s.length();
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':
                    b.append("&amp;");
                    break;
                case '<':
                    b.append("&lt;");
                    break;
                case '>':
                    b.append("&gt;");
                    break;
                default:
                    b.append(c);
                    break;
            }
        }
        return b.toString();
    }

    private static String padEachLine(String padWith, String str) {
        StringBuffer out = new StringBuffer();
        String[] lines = str.split("\n");
        int i = 0;
        while (i < lines.length) {
            String line = lines[i];
            out.append(padWith);
            out.append(line);
            i++;
            if (i < lines.length) out.append('\n');
        }
        return out.toString();
    }

    private static boolean isBlank(final String str) {
        return str == null || "".equals(str) || "".equals(str.trim());
    }

    private static void checkDirsExistMkdir(File dir) {
        if (!dir.getParentFile().exists()) {
            checkDirsExistMkdir(dir.getParentFile());
        }
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private void debugMsg(String msg) {
        if (!debug) return;
        messager.printNotice("Debug: " + msg);
    }

}
