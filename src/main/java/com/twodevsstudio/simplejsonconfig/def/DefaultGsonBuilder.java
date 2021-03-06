package com.twodevsstudio.simplejsonconfig.def;

import com.google.gson.GsonBuilder;
import com.twodevsstudio.simplejsonconfig.def.strategies.SuperclassExclusionStrategy;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

@Getter( onMethod_ = @NotNull )
@Setter( onParam_ = @NotNull )
public class DefaultGsonBuilder {
    
    private GsonBuilder gsonBuilder;
    
    public DefaultGsonBuilder() {
        
        this.gsonBuilder = new GsonBuilder().setPrettyPrinting()
                .disableHtmlEscaping()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
                .serializeNulls()
                .addDeserializationExclusionStrategy(new SuperclassExclusionStrategy())
                .addSerializationExclusionStrategy(new SuperclassExclusionStrategy());
    }
    
}
