package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.util.List;
import java.util.Map;

/**
 * 实现 {@link ObjectWrapper} 接口，为 {@link BeanWrapper} 和 {@link MapWrapper} 提供公共的方法
 */
public abstract class BaseWrapper implements ObjectWrapper {

    protected static final Object[] NO_ARGUMENTS = new Object[0];

    // MetaClass 对象
    protected final MetaObject metaObject;

    protected BaseWrapper(MetaObject metaObject) {
        this.metaObject = metaObject;
    }

    // 获得属性的值
    protected Object resolveCollection(PropertyTokenizer propertyTokenizer, Object object) {
        if ("".equals(propertyTokenizer.getName())) {
            return object;
        } else {
            return metaObject.getValue(propertyTokenizer.getName());
        }
    }

    // 获得集合中指定位置的元素
    protected Object getCollectionValue(PropertyTokenizer propertyTokenizer, Object collection) {
        if (collection instanceof Map) {
            return ((Map) collection).get(propertyTokenizer.getIndex());
        } else {
            int i = Integer.parseInt(propertyTokenizer.getIndex());
            if (collection instanceof List) {
                return ((List) collection).get(i);
            } else if (collection instanceof Object[]) {
                return ((Object[]) collection)[i];
            } else if (collection instanceof char[]) {
                return ((char[]) collection)[i];
            } else if (collection instanceof boolean[]) {
                return ((boolean[]) collection)[i];
            } else if (collection instanceof byte[]) {
                return ((byte[]) collection)[i];
            } else if (collection instanceof double[]) {
                return ((double[]) collection)[i];
            } else if (collection instanceof float[]) {
                return ((float[]) collection)[i];
            } else if (collection instanceof int[]) {
                return ((int[]) collection)[i];
            } else if (collection instanceof long[]) {
                return ((long[]) collection)[i];
            } else if (collection instanceof short[]) {
                return ((short[]) collection)[i];
            } else {
                throw new ReflectionException(collection + "的 " + propertyTokenizer.getName() + " 属性不是 List 或 Array！");
            }
        }
    }

    protected void setCollectionValue(PropertyTokenizer propertyTokenizer, Object collection, Object value) {
        if (collection instanceof Map) {
            ((Map) collection).put(propertyTokenizer.getIndex(), value);
        } else {
            int i = Integer.parseInt(propertyTokenizer.getIndex());
            if (collection instanceof List) {
                ((List) collection).set(i, value);
            } else if (collection instanceof Object[]) {
                ((Object[]) collection)[i] = value;
            } else if (collection instanceof char[]) {
                ((char[]) collection)[i] = (Character) value;
            } else if (collection instanceof boolean[]) {
                ((boolean[]) collection)[i] = (Boolean) value;
            } else if (collection instanceof byte[]) {
                ((byte[]) collection)[i] = (Byte) value;
            } else if (collection instanceof double[]) {
                ((double[]) collection)[i] = (Double) value;
            } else if (collection instanceof float[]) {
                ((float[]) collection)[i] = (Float) value;
            } else if (collection instanceof int[]) {
                ((int[]) collection)[i] = (Integer) value;
            } else if (collection instanceof long[]) {
                ((long[]) collection)[i] = (Long) value;
            } else if (collection instanceof short[]) {
                ((short[]) collection)[i] = (Short) value;
            } else {
                throw new ReflectionException(collection + "的 " + propertyTokenizer.getName() + " 属性不是 List 或 Array！");
            }
        }
    }
}
