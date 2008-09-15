package org.tuckey.web.filters.urlrewrite.annotation;

import javax.annotation.processing.Processor;
import java.util.ServiceLoader;

/**
 * Quick and dirty class to check that "service" file is loaded in the current classpath.
 */
public class ProcessorServiceLoaderCheck {

    public static void main(String[] args) {
        System.out.println("Checking for services...");
        ServiceLoader<Processor> processorServices = ServiceLoader.load(Processor.class);
        for (Object o : processorServices) {
            System.out.println(o);
        }
        System.out.println("Done.");
    }
}
