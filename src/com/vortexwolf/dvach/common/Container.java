package com.vortexwolf.dvach.common;

import java.util.HashMap;

public class Container {

    private HashMap<Class<?>, Object> _instances = new HashMap<Class<?>, Object>();

    public <T> void register(Class<T> from, T to) {
        _instances.put(from, to);
    }

    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        T result = (T) _instances.get(type);

        return result;
    }
}
