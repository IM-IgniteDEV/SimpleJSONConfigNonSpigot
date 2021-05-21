package com.twodevsstudio.simplejsonconfig.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
public final class CustomLogger {
    
    public static String LOG_PREFIX = "&7[&aSimpleJSONConfig&7] &a»» &7";
    
    public static String WARNING_PREFIX = "&7[&eWARNING&7] &a»» &e";
    
    public static String ERROR_PREFIX = "&7[&4ERROR&7] &c»» &4";
    
    public static void log(@NotNull String message) {
        
        System.out.println(LOG_PREFIX + message);
    }
    
    public static void warning(@NotNull String message) {
        
        System.out.println(WARNING_PREFIX + message);
    }
    
    public static void warning(@NotNull Throwable throwable) {
        
        System.out.println(WARNING_PREFIX + throwable.getMessage());
        throwable.getCause().printStackTrace();
    }
    
    public static void error(@NotNull String message) {
        
        System.out.println(ERROR_PREFIX + message);
    }
    
    
    public static void error(@NotNull Throwable throwable) {
        
        System.out.println(ERROR_PREFIX + throwable.getMessage());
        throwable.getCause().printStackTrace();
    }
    
}
