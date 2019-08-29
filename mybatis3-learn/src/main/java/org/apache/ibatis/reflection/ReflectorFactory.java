package org.apache.ibatis.reflection;

/**
 * 用于 创建 和 缓存 {@link Reflector} 对象
 */
public interface ReflectorFactory {

    boolean isClassCacheEnabled();

    void setClassCacheEnabled(boolean classCacheEnabled);

    // 传进 class 对象，构建其 Reflector 对象
    Reflector findForClass(Class<?> clazz);
}
