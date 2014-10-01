package com.vortexwolf.chan.common;

import java.util.HashMap;

public class Container {

    private HashMap<Class<?>, Object> _instances = new HashMap<Class<?>, Object>();

    public <T> void register(Class<T> from, T to) {
        this._instances.put(from, to);
    }

    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        T result = (T) this._instances.get(type);

        return result;
    }
}
