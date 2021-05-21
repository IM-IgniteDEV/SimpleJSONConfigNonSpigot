package com.twodevsstudio.simplejsonconfig;

import com.twodevsstudio.simplejsonconfig.api.AnnotationProcessor;
import com.twodevsstudio.simplejsonconfig.exceptions.InstanceOverrideException;

import java.io.File;

public enum SimpleJSONConfig {
    INSTANCE;
    
    private AnnotationProcessor annotationProcessor = new AnnotationProcessor();
    private File directory;
    private Object object;
    
    public void register(Object object, File configsDirectory) {
        
        if (this.object != null) {
            throw new InstanceOverrideException();
        }
        
        this.object = object;
        this.directory = configsDirectory;
        
        annotationProcessor.processAnnotations(object, configsDirectory);
        annotationProcessor.processAutowired(object);
    }
}
