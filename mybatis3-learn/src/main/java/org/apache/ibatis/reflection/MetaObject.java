package org.apache.ibatis.reflection;


import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 对象元数据，是对 {@link BaseWrapper} 的操作进一步增强
 */
public class MetaObject {

    // 原始 Object 对象
    private final Object originalObject;

    // 封装过的 Object 对象
    private final ObjectWrapper objectWrapper;
    private final ObjectFactory objectFactory;
    private final ObjectWrapperFactory objectWrapperFactory;
    private final ReflectorFactory reflectorFactory;

    private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
        this.originalObject = object;
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;
        this.reflectorFactory = reflectorFactory;

        // 根据 origin 的类型，创建不同的 ObjectWrapper 对象
        if (object instanceof ObjectWrapper) {
            this.objectWrapper = (ObjectWrapper) object;
        }
        // 如果 object 对象已经被包装过了
        else if (objectWrapperFactory.hasWrappereFor(object)) {
            this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
        }
        // 如果是 Map 类型
        else if (object instanceof Map) {
            this.objectWrapper = new MapWrapper(this, (Map) object);
        }
        // 如果是 Collection 类型
        else if (object instanceof Collection) {
            this.objectWrapper = new CollectionWrapper(this, (Collection) object);
        } else {
            this.objectWrapper = new BeanWrapper(this, object);
        }
    }

    // 创建 MetaObject 对象
    public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
        if (object == null) {
            return SystemMetaObject.NULL_META_OBJECT;
        } else {
            return new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
        }
    }


    // 根据指定的 属性名，创建 MetaObject 对象
    public MetaObject metaObjectForProperty(String name) {
        Object value = getValue(name);
        return new MetaObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    // 根据 属性值 获得对象
    public Object getValue(String name) {
        // 对 name 进行分词
        PropertyTokenizer propertyTokenizer = new PropertyTokenizer(name);
        if (propertyTokenizer.hasNext()) {
            MetaObject metaValue = metaObjectForProperty(propertyTokenizer.getIndexName());
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                return null;
            } else {
                return metaValue.getValue(propertyTokenizer.getChildren());
            }
        } else {
            return objectWrapper.get(propertyTokenizer);
        }
    }

    // 设置字段名为 name 的 属性值 value
    public void setValue(String name, Object value) {
        PropertyTokenizer propertyTokenizer = new PropertyTokenizer(name);
        if (propertyTokenizer.hasNext()) {
            MetaObject metaValue = metaObjectForProperty(propertyTokenizer.getName());
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                if (value == null) {
                    return;
                } else {
                    metaValue = objectWrapper.instantiatePropertyValue(name, propertyTokenizer, objectFactory);
                }
            }
            metaValue.setValue(propertyTokenizer.getChildren(), value);
        } else {
            objectWrapper.set(propertyTokenizer, value);
        }
    }

    public boolean isCollection() {
        return objectWrapper.isCollection();
    }

    public void add(Object element) {
        objectWrapper.add(element);
    }

    public <E> void addAll(List<E> list) {
        objectWrapper.addAll(list);
    }

    public Object getOriginalObject() {
        return originalObject;
    }

    public ObjectWrapper getObjectWrapper() {
        return objectWrapper;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    public ReflectorFactory getReflectorFactory() {
        return reflectorFactory;
    }

    public Class<?> getSetterType(String name) {
        return objectWrapper.getSetterType(name);
    }

    public Class<?> getGetterType(String name) {
        return objectWrapper.getGetterType(name);
    }

    public boolean hasGetter(String name) {
        return objectWrapper.hasGetter(name);
    }

    public boolean hasSetter(String name) {
        return objectWrapper.hasSetter(name);
    }
}
