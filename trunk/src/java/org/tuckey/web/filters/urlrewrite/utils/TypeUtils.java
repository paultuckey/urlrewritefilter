package org.tuckey.web.filters.urlrewrite.utils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class TypeUtils {


    public static Class findClass(String param) {
        Class paramClass = null;
        if ("boolean".equals(param) || "bool".equals(param) || "z".equalsIgnoreCase(param)) paramClass = boolean.class;
        if ("byte".equals(param) || "b".equalsIgnoreCase(param)) paramClass = byte.class;
        if ("char".equals(param) || "c".equalsIgnoreCase(param)) paramClass = char.class;
        if ("short".equals(param) || "s".equalsIgnoreCase(param)) paramClass = short.class;
        if ("int".equals(param) || "i".equalsIgnoreCase(param)) paramClass = int.class;
        if ("long".equals(param) || "l".equalsIgnoreCase(param)) paramClass = long.class;
        if ("float".equals(param) || "f".equalsIgnoreCase(param)) paramClass = float.class;
        if ("double".equals(param) || "d".equalsIgnoreCase(param)) paramClass = double.class;

        if ("Boolean".equals(param) || "Bool".equals(param)) paramClass = Boolean.class;
        if ("Byte".equals(param)) paramClass = Byte.class;
        if ("Character".equalsIgnoreCase(param) || "C".equals(param)) paramClass = Character.class;
        if ("Short".equals(param)) paramClass = Short.class;
        if ("Integer".equals(param)) paramClass = Integer.class;
        if ("Long".equals(param)) paramClass = Long.class;
        if ("Float".equals(param)) paramClass = Float.class;
        if ("Double".equals(param)) paramClass = Double.class;

        if ("Class".equalsIgnoreCase(param)) paramClass = Class.class;
        if ("Number".equalsIgnoreCase(param)) paramClass = Number.class;
        if ("Object".equalsIgnoreCase(param)) paramClass = Object.class;
        if ("String".equalsIgnoreCase(param) || "str".equalsIgnoreCase(param)) paramClass = String.class;

        if ("HttpServletRequest".equalsIgnoreCase(param) || "req".equalsIgnoreCase(param) || "request".equalsIgnoreCase(param))
            paramClass = HttpServletRequest.class;
        if ("HttpServletResponse".equalsIgnoreCase(param) || "res".equalsIgnoreCase(param) || "response".equalsIgnoreCase(param))
            paramClass = HttpServletResponse.class;
        if ("ServletRequest".equalsIgnoreCase(param)) paramClass = ServletRequest.class;
        if ("ServletResponse".equalsIgnoreCase(param)) paramClass = ServletResponse.class;
        return paramClass;
    }



    public static Object getConvertedParam(Class runMethodParam, Object matchObj) {
        // see http://jakarta.apache.org/commons/beanutils/api/org/apache/commons/beanutils/MethodUtils.html
        // for how to handle methods better
        Object param = null;
        if (matchObj == null) {
            if (runMethodParam.isPrimitive()) {
                if (runMethodParam.equals(boolean.class)) param = Boolean.FALSE;
                else if (runMethodParam.equals(char.class)) param = new Character('\u0000');
                else if (runMethodParam.equals(byte.class)) param = new Byte((byte) 0);
                else if (runMethodParam.equals(short.class)) param = new Short((short) 0);
                else if (runMethodParam.equals(int.class)) param = new Integer(0);
                else if (runMethodParam.equals(long.class)) param = new Long(0L);
                else if (runMethodParam.equals(float.class)) param = new Float(0f);
                else if (runMethodParam.equals(double.class)) param = new Double(0d);
            }
        } else {
            if (runMethodParam.equals(Boolean.class) || runMethodParam.equals(boolean.class))
                param = Boolean.valueOf((String) matchObj);
            else if (runMethodParam.equals(Character.class) || runMethodParam.equals(char.class))
                param = new Character(((String) matchObj).charAt(0));
            else if (runMethodParam.equals(Byte.class) || runMethodParam.equals(byte.class))
                param = Byte.valueOf((String) matchObj);
            else if (runMethodParam.equals(Short.class) || runMethodParam.equals(short.class))
                param = Short.valueOf((String) matchObj);
            else if (runMethodParam.equals(Integer.class) || runMethodParam.equals(int.class))
                param = Integer.valueOf((String) matchObj);
            else if (runMethodParam.equals(Long.class) || runMethodParam.equals(long.class))
                param = Long.valueOf((String) matchObj);
            else if (runMethodParam.equals(Float.class) || runMethodParam.equals(float.class))
                param = Float.valueOf((String) matchObj);
            else if (runMethodParam.equals(Double.class) || runMethodParam.equals(double.class))
                param = Double.valueOf((String) matchObj);
            else if (matchObj instanceof Throwable &&
                    runMethodParam.isAssignableFrom(matchObj.getClass()))
                param = matchObj;
            else {
                try {
                    // last attempt
                    param = runMethodParam.cast(matchObj);
                } catch (ClassCastException e) {
                    // do nothing
                }
            }
        }
        return param;
    }

    public static String getMethodSignature(String methodStr, Class[] methodParams) {
        if (methodStr == null) return null;
        StringBuffer sb = new StringBuffer(methodStr);
        if (methodParams != null) {
            for (int i = 0; i < methodParams.length; i++) {
                Class runMethodParam = methodParams[i];
                if (runMethodParam == null) continue;
                if (i == 0) sb.append("(");
                if (i > 0) sb.append(", ");
                sb.append(runMethodParam.getName());
                if (i + 1 == methodParams.length) sb.append(")");
            }
        }
        return sb.toString();
    }



}
