package org.apache.ibatis.reflection;


import org.apache.ibatis.exceptions.ReflectionException;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Reflector {

    // 当前所需要解析类的Class对象
    private final Class<?> type;
//    private final String[] readablePropertyNames;
//    private final String[] writablePropertyNames;
//    private final Map<String, Invoker> setMethods = new HashMap<>();
//    private final Map<String, Invoker> getMethods = new HashMap<>();
//    private final Map<String, Class<?>> setTypes = new HashMap<>();
//    private final Map<String, Class<?>> getTypes = new HashMap<>();
    private Constructor<?> defaultConstructor;

    public Reflector(Class<?> clazz) {
        this.type = clazz;
        addDefaultConstructor(clazz);
    }

    private void addDefaultConstructor(Class<?> clazz) {
        Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterTypes().length == 0)
                .findAny()
                .ifPresent(constructor -> this.defaultConstructor = constructor);
    }


    public Constructor<?> getDefaultConstructor() {
        return Optional.ofNullable(this.defaultConstructor)
                .orElseThrow(() -> new ReflectionException("没有无参构造函数！！！"));
    }
}
