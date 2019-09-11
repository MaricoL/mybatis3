package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @description 对象包装器接口：基于 {@link org.apache.ibatis.reflection.MetaClass} 工具类，定义对指定对象的操作
 * @author mrjimmylin
 * @date 2019/9/9 16:55
 */
public interface ObjectWrapper {

    // 获得值 —— propertyTokenizer 相当于键
    Object get(PropertyTokenizer propertyTokenizer);

    // 设置值 —— propertyTokenizer 相当于 键，value 相当于 值
    void set(PropertyTokenizer propertyTokenizer, Object value);

    /**
     * {@link MetaClass#findProperty(String,boolean)}
     */
    String findProperty(String name, boolean useCamelCaseMapping);

    /**
     * {@link MetaClass#getGetterNames()}
     * @return 所有的 getter 方法所对应字段的名称
     */
    String[] getGetterNames();

    /**
     * {@link MetaClass#getSetterNames()}
     * @return 所有的 setter 方法所对应的字段的名称
     */
    String[] getSetterNames();

}
