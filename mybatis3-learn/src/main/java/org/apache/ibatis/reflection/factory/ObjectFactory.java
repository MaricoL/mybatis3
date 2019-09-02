package org.apache.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;

// 创建指定类对象的工厂接口
public interface ObjectFactory {

    default void setProperties(Properties properties) {
        // NOP
    }

    <T> T create(Class<T> type);

    <T> T create(Class<T> type, List<Class<?>> constructorArgsTypes, List<Object> constructorArgs);

    <T> boolean isCollection(Class<?> type);
}

