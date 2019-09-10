package org.apache.ibatis.reflection.factory;


import java.util.List;
import java.util.Properties;

/**
 * 根据指定类的对象，创建指定类对象的工厂接口
 */
public interface ObjectFactory {

    // 设置属性
    default void setProperties(Properties properties){

    }


    // 根据指定类的对象，使用默认构造函数创建对象
    <T> T create(Class<T> type);


    // 根据指定类型的对象，使用指定的构造函数创建对象
    <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

    // 判断指定类是否是集合
    <T> boolean isCollection(Class<T> type);
}
