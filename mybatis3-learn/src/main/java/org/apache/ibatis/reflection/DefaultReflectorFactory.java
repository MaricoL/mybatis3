package org.apache.ibatis.reflection;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的 {@link ReflectorFactory} 工厂对象
 */
public class DefaultReflectorFactory implements ReflectorFactory{

    // 默认为 true ， 开启 Class -- Reflector 关系映射缓存
    private boolean classCacheEnabled = true;

    // Class -- Reflector 关系映射缓存map
    private ConcurrentHashMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<>();

    @Override
    public boolean isClassCacheEnabled() {
        return classCacheEnabled;
    }

    @Override
    public void setClassCacheEnabled(boolean classCacheEnabled) {

        this.classCacheEnabled = classCacheEnabled;
    }

    // 如果开启缓存，则先从 reflectorMap 中寻找
    @Override
    public Reflector findForClass(Class<?> clazz) {
        if (classCacheEnabled) {
            return reflectorMap.computeIfAbsent(clazz, Reflector::new);
        }
        return new Reflector(clazz);
    }
}
