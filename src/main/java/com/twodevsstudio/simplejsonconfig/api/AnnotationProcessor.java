package com.twodevsstudio.simplejsonconfig.api;

import com.twodevsstudio.simplejsonconfig.def.Serializer;
import com.twodevsstudio.simplejsonconfig.interfaces.Autowired;
import com.twodevsstudio.simplejsonconfig.interfaces.Comment;
import com.twodevsstudio.simplejsonconfig.interfaces.Configuration;
import com.twodevsstudio.simplejsonconfig.utils.CustomLogger;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.reflect.Modifier.isStatic;

public class AnnotationProcessor {
    
    public AnnotationProcessor() {
    
    }
    
    public static Map<String, Comment> getFieldsComments(Object object) {
        
        Map<String, Comment> comments = new HashMap<>();
        
        for (Field declaredField : object.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            if (!declaredField.isAnnotationPresent(Comment.class)) {
                continue;
            }
            
            Comment comment = declaredField.getAnnotation(Comment.class);
            comments.put(declaredField.getName(), comment);
        }
        
        return comments;
    }
    
    public void processAnnotations(Object anyMainClass, File configsDirectory) {
        
        ConfigurationBuilder builder = new ConfigurationBuilder();
        
        builder.addUrls(ClasspathHelper.forPackage(anyMainClass.getClass().getPackage().getName(),
                anyMainClass.getClass().getClassLoader(), ClassLoader.getSystemClassLoader(),
                ClasspathHelper.contextClassLoader(), ClasspathHelper.staticClassLoader()
        ));
        
        builder.addScanners(new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new SubTypesScanner());
        
        Reflections reflections = new Reflections(builder);
        
        
        processConfiguration(configsDirectory, reflections);
        processAutowired(reflections);
        
    }
    
    @SneakyThrows
    public void processConfiguration(File configsDirectory, Reflections reflections) {
        
        Set<Class<?>> configurationClasses = reflections.getTypesAnnotatedWith(Configuration.class);
        
        for (Class<?> annotadedClass : configurationClasses) {
            
            Configuration configurationAnnotation = annotadedClass.getAnnotation(Configuration.class);
            String configName = configurationAnnotation.value();
            
            if (!isConfig(annotadedClass)) {
                CustomLogger.warning("Configuration " +
                                     configName +
                                     " could not be loaded. Class annotated as @Configuration does not extends " +
                                     Config.class.getName());
                
                continue;
            }
            
            Class<? extends Config> configClass = (Class<? extends Config>) annotadedClass;
            
            Constructor<? extends Config> constructor;
            Config config;
            
            try {
                constructor = configClass.getConstructor();
                constructor.setAccessible(true);
                config = constructor.newInstance();
            } catch (ReflectiveOperationException ignored) {
                CustomLogger.warning(configClass.getName() + ": Cannot find default constructor");
                continue;
            }
            
            String fileName = configName.endsWith(".json") ? configName : configName + ".json";
            
            File configFile = new File(configsDirectory, fileName);
            
            Field field = configClass.getSuperclass().getDeclaredField("configFile");
            field.setAccessible(true);
            field.set(config, configFile);
            
            initConfig(config, configFile);
            
        }
        
    }
    
    @SneakyThrows
    public void processAutowired(Object anyMainClassObject) {
        
        Reflections reflections = new Reflections(anyMainClassObject.getClass().getPackage().getName(),
                new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new SubTypesScanner()
        );
        
        processAutowired(reflections);
        
    }
    
    @SneakyThrows
    public void processAutowired(Reflections reflections) {
        
        for (Field field : reflections.getFieldsAnnotatedWith(Autowired.class)) {
            
            field.setAccessible(true);
            
            Class<?> type = field.getType();
            
            if (isConfig(type) && isStatic(field.getModifiers()) && field.get(null) == null) {
                
                Config config = Config.getConfig((Class<? extends Config>) type);
                field.set(null, config);
            }
            
        }
        
    }
    
    public boolean isConfig(@NotNull Class<?> clazz) {
        
        return clazz.getSuperclass() == Config.class;
    }
    
    private void initConfig(@NotNull Config config, @NotNull File configFile) {
        
        config.configFile = configFile;
        
        if (!configFile.exists()) {
            
            try {
                configFile.mkdirs();
                configFile.createNewFile();
                Serializer.getInst().saveConfig(config, configFile);
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            
        } else {
            
            try {
                config.reload();
            } catch (Exception exception) {
                CustomLogger.warning(config.getClass().getName() + ": Config file is corrupted");
                return;
            }
            
        }
        
        ConfigContainer.SINGLETONS.put(config.getClass(), config);
    }
}
