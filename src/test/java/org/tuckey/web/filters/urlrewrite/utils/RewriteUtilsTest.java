package org.tuckey.web.filters.urlrewrite.utils;

import com.google.common.base.Stopwatch;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RewriteUtilsTest {

    @Test
    public void parsingTest() throws Exception {
        String lastS = "";
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (int i = 0; i < 100_000; i++) {
            lastS = RewriteUtils.uriEncodeParts("^/красота\\+от\\+природы/Словарь\\+ингредиентов/э/экстракт-фиалки-трехцветной-viola-tricolor-extract$");

        }
        stopwatch.stop();
        System.out.println("#"+ stopwatch.elapsed(TimeUnit.MILLISECONDS));

        // runs around ~ 1800 millisec on i7-3840QM, linux JDK8
        System.out.println("last: " + lastS);

    }


}