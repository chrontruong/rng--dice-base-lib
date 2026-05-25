package com.io.begstd.dice.services.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BaseRandomService  implements IRandomService {
    private final Map<String, Function<Object[], Object>> extensions = new HashMap<>();

    public BaseRandomService() {
    }

    public void register(String event, Function<Object[], Object> extension) {
        this.extensions.put(event, extension);
    }

    public void clearExtension() {
        this.extensions.clear();
    }

    public <T> T random(String event, Class<T> clazz, Object... args) {
        T result = null;
        if (this.extensions.containsKey(event)) {
            //String userId = (String)args[0];
            result = (T)((Function)this.extensions.get(event)).apply(args);
        }

        if (result == null) {
            result = (T)this.securityRandom(event, clazz, args);
        }

        return result;
    }

    protected <T> T securityRandom(String event, Class<T> clazz, Object... args) {
        return null;
    }
}
