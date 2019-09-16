package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.util.List;

/**
 * @author mrjimmylin
 * @description 对象包装器接口：基于 {@link org.apache.ibatis.reflection.MetaClass} 工具类，定义对指定对象的操作
 * @date 2019/9/9 16:55
 */

public interface ObjectWrapper {

    // 获得值 —— propertyTokenizer 相当于键
    Object get(PropertyTokenizer propertyTokenizer);

    // 设置值 —— propertyTokenizer 相当于 键，value 相当于 值
    void set(PropertyTokenizer propertyTokenizer, Object value);

    /**
     * {@link MetaClass#findProperty(String, boolean)}
     * @return 属性名
     */
    String findProperty(String name, boolean useCamelCaseMapping);

    /**
     * {@link MetaClass#getGetterNames()}
     */
    String[] getGetterNames();

    /**
     * {@link MetaClass#getSetterNames()}
     */
    String[] getSetterNames();

    /**
     * {@link MetaClass#getSetterType(String)}
     */
    Class<?> getSetterType(String name);

    /**
     * {@link MetaClass#getGetterNames()}
     */
    Class<?> getGetterType(String name);

    /**
     * {@link MetaClass#hasSetter(String)}
     */
    boolean hasSetter(String name);

    /**
     * {@link MetaClass#hasGetter(String)}
     */
    boolean hasGetter(String name);


    /**
     * 是否为集合
     */
    boolean isCollection();

    /**
     * 添加元素到集合
     */
    void add(Object object);

    /**
     * 添加多个元素到集合
     */
    <E> void addAll(List<E> element);

    MetaObject instantiatePropertyValue(String name, PropertyTokenizer propertyTokenizer, ObjectFactory objectFactory);
}