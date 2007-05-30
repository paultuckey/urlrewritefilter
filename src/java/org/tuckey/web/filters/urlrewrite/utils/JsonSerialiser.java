package org.tuckey.web.filters.urlrewrite.utils;

import com.metaparadigm.jsonrpc.JSONSerializer;
import com.metaparadigm.jsonrpc.MarshallException;
import com.metaparadigm.jsonrpc.SerializerState;


public class JsonSerialiser {

    private static Log log = Log.getLog(JsonSerialiser.class);

    public static String toJsonString(Object result) {
        Object jsonResult = null;
        JSONSerializer serializer = new JSONSerializer();
        try {
            serializer.registerDefaultSerializers();
        } catch (Exception e) {
            log.error(e, e);
        }
        serializer.setMarshallClassHints(true);
        serializer.setMarshallNullAttributes(true);

        SerializerState state = new SerializerState();
        try {
            jsonResult = serializer.marshall(state, result);
        } catch (MarshallException e) {
            log.error(e, e);
        }
        return jsonResult == null ? null : jsonResult.toString();
    }

}
