package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.util.List;

public class BeanWrapper extends BaseWrapper {

    protected final Object object;
    protected final MetaClass metaClass;

    public BeanWrapper(MetaObject metaObject, Object object) {
        super(metaObject);
        this.object = object;
        this.metaClass = MetaClass.forClass(object.getClass(), metaObject.getReflectorFactory());
    }

    // 获得指定属性的值
    @Override
    public Object get(PropertyTokenizer propertyTokenizer) {
        if (propertyTokenizer.getIndex() != null) {
            Object collection = resolveCollection(propertyTokenizer, object);
            return getCollectionValue(propertyTokenizer, collection);
        } else {
            return getBeanProperty(propertyTokenizer, object);
        }
    }


    // 设置属性值
    @Override
    public void set(PropertyTokenizer propertyTokenizer, Object value) {
        if (propertyTokenizer.getIndex() != null) {
            Object collection = resolveCollection(propertyTokenizer, object);
            setCollectionValue(propertyTokenizer, collection, value);
        } else {
            setBeanProperty(propertyTokenizer, object, value);
        }
    }


    // 获得属性值 —— 属性名中没有[]的那种
    private Object getBeanProperty(PropertyTokenizer propertyTokenizer, Object object) {
        try {
            Invoker getMethod = metaClass.getGetInvoker(propertyTokenizer.getName());
            try {
                return getMethod.invoke(object, NO_ARGUMENTS);
            } catch (Throwable t) {
                throw ExceptionUtil.unwrapThrowable(t);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new ReflectionException("无法在 " + object.getClass() + " 类中获得 " + propertyTokenizer.getName() + " 属性" +
                    "原因是：" + throwable.getCause(), throwable);
        }
    }

    // 设置属性值 —— 属性名中没有[]的那种
    private void setBeanProperty(PropertyTokenizer propertyTokenizer, Object object, Object value) {
        try {
            Invoker setMethod = metaClass.getSetInvoker(propertyTokenizer.getName());
            try {
                setMethod.invoke(object, new Object[]{value});
            } catch (Throwable throwable) {
                throw ExceptionUtil.unwrapThrowable(throwable);
            }
        } catch (Throwable throwable) {
            throw new ReflectionException("无法在 " + object.getClass() + " 类中设置 " + propertyTokenizer.getName() + " 属性" +
                    "原因是：" + throwable.getCause(), throwable);
        }
    }


    @Override
    public String findProperty(String name, boolean useCamelCaseMapping) {
        return null;
    }

    @Override
    public String[] getGetterNames() {
        return new String[0];
    }

    @Override
    public String[] getSetterNames() {
        return new String[0];
    }

    @Override
    public Class<?> getSetterType(String name) {
        return null;
    }

    // 获得指定属性的 getter 方法的返回值
    @Override
    public Class<?> getGetterType(String name) {
        return null;
    }

    @Override
    public boolean hasSetter(String name) {
        return false;
    }

    @Override
    public boolean hasGetter(String name) {
        return false;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public void add(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E> void addAll(List<E> element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MetaObject instantiatePropertyValue(String name, PropertyTokenizer propertyTokenizer, ObjectFactory objectFactory) {
        MetaObject metaValue;
        Class<?> setterType = getSetterType(propertyTokenizer.getName());
        Object newObject = objectFactory.create(setterType);
        metaValue = MetaObject.forObject(newObject, this.metaObject.getObjectFactory(), this.metaObject.getObjectWrapperFactory(), this.metaObject.getReflectorFactory());
        set(propertyTokenizer, newObject);
        return metaValue;
    }
}
