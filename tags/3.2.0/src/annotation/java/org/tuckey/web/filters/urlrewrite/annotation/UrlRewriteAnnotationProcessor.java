/**
 * Copyright (c) 2005-2008, Paul Tuckey
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


import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Annotation processor for UrlRewrite. Will search classes for annotations and generate XML fragment.
 * JDK 1.6+
 * <p/>
 *
 * @since 3.2
 */
@SupportedAnnotationTypes("org.tuckey.web.filters.urlrewrite.*")
public class UrlRewriteAnnotationProcessor extends AbstractProcessor {

    private List<ProcessedHttpUrlAnnotation> processedAnnotations = new ArrayList<ProcessedHttpUrlAnnotation>();
    private List<ProcessedHttpJsonAnnotation> processedJsonAnnotations = new ArrayList<ProcessedHttpJsonAnnotation>();
    private List<ProcessedHttpExceptionAnnotation> httpExceptionHandlers = new ArrayList<ProcessedHttpExceptionAnnotation>();
    private Messager messager;
    private Elements elementUtils;
    private boolean showPositions = false;
    private boolean debug = false;
    private boolean errorDuringProcessing = false;
    private String dest = null;
    private String rpcBase = "/rpc/";

    public UrlRewriteAnnotationProcessor() {
        // needed
    }

    public Set<String> getSupportedOptions() {
        HashSet<String> options = new HashSet<String>();
        options.add("urlrewriteDest");
        options.add("urlrewriteShowPositions");
        options.add("urlrewriteDebug");
        options.add("urlrewriteRpcBase");
        return options;
    }

    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.messager = processingEnv.getMessager();
        this.elementUtils = processingEnv.getElementUtils();

        Map<String, String> options = processingEnv.getOptions();
        Set<String> keys = options.keySet();
        for (String key : keys) {
            if (key.equalsIgnoreCase("urlrewriteDest")) {
                dest = options.get(key);

            }   else if (key.equalsIgnoreCase("urlrewriteShowPositions")) {
                showPositions = "true".equalsIgnoreCase(options.get(key));

            }   else if (key.equalsIgnoreCase("urlrewriteDebug")) {
                debug = "true".equalsIgnoreCase(options.get(key));

            }   else if (key.equalsIgnoreCase("urlrewriteRpcBase")) {
                rpcBase = options.get(key);
            }
        }
        debugMsg("init");
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (isBlank(dest)) {
            if (roundEnv.processingOver())
                infoMsg(getClass().getSimpleName() + ": -AurlrewriteDest not specified, annotations ignored");
            return true;
        }
        debugMsg("process");

        Set<? extends Element> urlDeclarations = roundEnv.getElementsAnnotatedWith(HttpUrl.class);
        for (Element element : urlDeclarations) {
            processedAnnotations.add(new ProcessedHttpUrlAnnotation(element));
        }

        Set<? extends Element> jsonDeclarations = roundEnv.getElementsAnnotatedWith(HttpJson.class);
        for (Element element : jsonDeclarations) {
            processedJsonAnnotations.add(new ProcessedHttpJsonAnnotation(element));
        }

        Set<? extends Element> exceptionDeclarations = roundEnv.getElementsAnnotatedWith(HttpExceptionHandler.class);
        for (Element element : exceptionDeclarations) {
            httpExceptionHandlers.add(new ProcessedHttpExceptionAnnotation(element));
        }

        if (roundEnv.processingOver()) {
            if (processedAnnotations.size() > 0) {
                infoMsg("Got " + processedAnnotations.size() + " @HttpUrl annotations");
            }
            if (processedJsonAnnotations.size() > 0) {
                infoMsg("Got " + processedJsonAnnotations.size() + " @HttpJson annotations");
                processedAnnotations.addAll(processedJsonAnnotations);
            }
            Collections.sort(processedAnnotations);

            if (httpExceptionHandlers.size() > 0) {
                infoMsg("Got " + httpExceptionHandlers.size() + " @HttpExceptionHandler annotations");
                Collections.sort(httpExceptionHandlers);
            }
            try {
                File destFile = new File(dest);
                if (!destFile.exists()) {
                    checkDirsExistMkdir(destFile.getParentFile());
                    destFile.createNewFile();
                }
                if (!destFile.canWrite()) {
                    throw new IOException("cannot write to " + destFile.getName());
                }
                if (errorDuringProcessing) {
                    errorMsg("Error occured during processing deleting generated file.");
                    destFile.delete();

                } else {
                    PrintWriter pw = new PrintWriter(destFile);
                    infoMsg("Writing to " + destFile);
                    outputRules(pw);
                    outputExceptionHandlers(pw);
                    pw.close();

                }
            } catch (FileNotFoundException e) {
                errorMsg(e);
                e.printStackTrace();

            } catch (IOException e) {
                errorMsg(e);
                e.printStackTrace();
            }
        }
        return true;
    }

    private void outputRules(PrintWriter pw) {
        for (ProcessedHttpUrlAnnotation pa : processedAnnotations) {
            boolean jsonHandler = pa instanceof ProcessedHttpJsonAnnotation;
            pw.println("<rule>");
            pw.println("    <name>" + pa.className + "." + pa.methodName + "</name>");
            if (!isBlank(pa.docComment)) {
                pw.println("    <note>");
                pw.println(padEachLine("        ", escapeXML(pa.docComment)));
                pw.println("    </note>");
            }
            pw.println("    <from>" + pa.value + "</from>");
            pw.println("    <run class=\"" + pa.className + "\" method=\"" + pa.methodName + pa.paramsFormatted +
                    "\""+ (jsonHandler ? " handler=\"json\"" : "") +" />");
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

    class ProcessedUrlRewriteFilterAnnotation implements Comparable<ProcessedUrlRewriteFilterAnnotation> {

        public String methodName;
        public String className;
        public String docComment;
        public String paramsFormatted;

        public ExecutableElement init(Element declaration) {
            if (!ElementKind.METHOD.equals(declaration.getKind())) {
                errorMsg("declared on a non-method (type is " + declaration.getKind() + ")", declaration);
                return null;
            }
            ExecutableElement methodDeclaration = (ExecutableElement) declaration;
            className = methodDeclaration.getEnclosingElement().getSimpleName().toString();
            methodName = declaration.getSimpleName().toString();
            docComment = elementUtils.getDocComment(declaration);
            return methodDeclaration;
        }

        public int compareTo(ProcessedUrlRewriteFilterAnnotation other) {
            if (this.className != null && other.className != null) {
                int comp = this.className.compareTo(other.className);
                if (comp != 0) return comp;
            }
            if (this.methodName != null && other.methodName != null) {
                return this.methodName.compareTo(other.methodName);
            }
            return 0;
        }

        void setParams(Collection<? extends VariableElement> params) {
            paramsFormatted = "(";
            if (params.size() > 0) {
                int i = 1;
                for (VariableElement paramDeclaration : params) {
                    String paramType = paramDeclaration.asType().toString();
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

    class ProcessedHttpJsonAnnotation extends ProcessedHttpUrlAnnotation {
        public ProcessedHttpJsonAnnotation(Element declaration) {
            ExecutableElement methodDeclaration = init(declaration);
            if (methodDeclaration == null) return;

            HttpJson httpJson = declaration.getAnnotation(HttpJson.class);
            if ("[ unassigned ]".equals(httpJson.value()) ) {
                this.value = rpcBase + this.className + "/" + this.methodName;
            }   else {
                this.value = httpJson.value();
            }
            this.weight = httpJson.weight();
            setParams(methodDeclaration.getParameters());

            if (showPositions) {
                messager.printMessage(Diagnostic.Kind.NOTE, "@HttpJson value " + value + " weight " + weight, methodDeclaration);
            }
        }

    }

    class ProcessedHttpUrlAnnotation extends ProcessedUrlRewriteFilterAnnotation {
        public int weight = 0;
        public String value;
        public boolean chainUsed;
        public String sourceRef;
        private static final String FILTER_CHAIN_CLASS_NAME = "javax.servlet.FilterChain";

        public ProcessedHttpUrlAnnotation() {
            // empty
        }

        public ProcessedHttpUrlAnnotation(Element declaration) {
            ExecutableElement methodDeclaration = init(declaration);
            if (methodDeclaration == null) return;

            HttpUrl httpUrl = declaration.getAnnotation(HttpUrl.class);
            this.value = httpUrl.value();
            this.weight = httpUrl.weight();
            setParams(methodDeclaration.getParameters());

            if (showPositions) {
                messager.printMessage(Diagnostic.Kind.NOTE, "@HttpUrl value " + value + " weight " + weight, methodDeclaration);
            }
        }

        public int compareTo(ProcessedHttpUrlAnnotation other) {
            if (this.weight < other.weight) return 1;
            if (this.weight > other.weight) return -1;
            return super.compareTo(other);
        }

        protected void setParams(Collection<? extends VariableElement> params) {
            chainUsed = false;
            if (params.size() > 0) {
                for (VariableElement paramDeclaration : params) {
                    String paramType = paramDeclaration.asType().toString();
                    if (FILTER_CHAIN_CLASS_NAME.equals(paramType)) {
                        chainUsed = true;
                    }
                }
            }
            super.setParams(params);
        }

    }

    class ProcessedHttpExceptionAnnotation extends ProcessedUrlRewriteFilterAnnotation {
        public String exceptionName;

        public ProcessedHttpExceptionAnnotation(Element declaration) {
            ExecutableElement methodDeclaration = init(declaration);
            if (methodDeclaration == null) return;

            HttpExceptionHandler httpExceptionHandler = declaration.getAnnotation(HttpExceptionHandler.class);
            exceptionName = httpExceptionHandler.value();

            // out exceptionName might not be set
            if ("[ unassigned ]".equals(exceptionName) ) {
                // use first param
                exceptionName = methodDeclaration.getParameters().get(0).asType().toString();
            }
            setParams(methodDeclaration.getParameters());

            if (showPositions) {
                messager.printMessage(Diagnostic.Kind.NOTE, "@HttpExceptionHandlerUrl exceptionName " + exceptionName, methodDeclaration);
            }
        }

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
        messager.printMessage(Diagnostic.Kind.OTHER, getClass().getSimpleName() + " " + msg);
    }

    private void infoMsg(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private void errorMsg(String msg) {
        errorDuringProcessing = true;
        messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private void errorMsg(Exception e) {
        errorDuringProcessing = true;
        messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
    }

    private void errorMsg(String msg, Element element) {
        errorDuringProcessing = true;
        messager.printMessage(Diagnostic.Kind.ERROR, msg, element);
    }

}
