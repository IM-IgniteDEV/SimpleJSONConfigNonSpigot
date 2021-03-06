package com.twodevsstudio.simplejsonconfig.def;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.twodevsstudio.simplejsonconfig.api.CommentProcessor;
import com.twodevsstudio.simplejsonconfig.interfaces.PostProcessable;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

@Getter
public class Serializer {
    private CommentProcessor commentProcessor = new CommentProcessor();
    private JsonParser jsonParser = new JsonParser();
    
    @Setter( onParam_ = @NotNull )
    private Gson gson;
    
    /**
     * You can use this class to serialize and deserialize your object to and from JSON format This class uses Google
     * json library to perform serialization and deserialization Fields with modifiers {final, static, transient} wont
     * be serialized In order to serialize your class you have to create default (empty) constructor
     * <p>
     * If some necessary operations have to be performed after deserialization (like assign transient fields) use the
     * {@code PostProcessable} interface, that'll provide you {@code gsonPostProcess()} method You can create your
     * business logic, and that method will be called after the deserialization process complete
     */
    private Serializer() {
        
        this.gson = new DefaultGsonBuilder().getGsonBuilder().create();
    }
    
    /**
     * Get the instance of {@code Serializer}
     *
     * @return The instance of {@code Serializer}
     */
    @Contract( pure = true )
    public static Serializer getInst() {
        
        return Serializer.SingletonHelper.INSTANCE;
    }
    
    /**
     * Serialize parameterized object to JSON format and save it into the file
     *
     * @param object Object that is going to be serialized
     * @param file   File where serialized object will be stored
     */
    public void saveConfig(Object object, @NotNull File file) {
        
        try {
            if (file.createNewFile()) {
                String json = gson.toJson(jsonParser.parse(gson.toJson(object)));
                try (PrintWriter out = new PrintWriter(file)) {
                    out.println(json);
                }
                
                commentProcessor.includeComments(file, object);
            } else {
                if (file.delete()) {
                    if (file.createNewFile()) {
                        String json = gson.toJson(jsonParser.parse(gson.toJson(object)));
                        try (PrintWriter out = new PrintWriter(file)) {
                            out.println(json);
                        }
                        
                        commentProcessor.includeComments(file, object);
                    }
                    
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Deserialize your object from JSON format It also call {@code gsonPostProcess()} method if the class implements
     * {@code PostProcessable} interface
     *
     * @param clazz The class of serialized object
     * @param file  It's the file where serialized object is stored
     * @param <T>   It's the return type of deserialized object
     *
     * @return Deserialized object of parameterized type or null when any exception occurs
     */
    @Nullable
    public <T> T loadConfig(Class<T> clazz, @NotNull File file) {
        
        T deserializedObject;
        try {
            file = commentProcessor.getFileWithoutComments(file);
            
            deserializedObject = gson.fromJson(new String(Files.readAllBytes(file.toPath())), clazz);
            if (deserializedObject.getClass().equals(clazz)) {
                if (deserializedObject instanceof PostProcessable) {
                    ((PostProcessable) deserializedObject).gsonPostProcess();
                }
                return deserializedObject;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static class SingletonHelper {
        
        private static final Serializer INSTANCE = new Serializer();
        
    }
    
    
}
