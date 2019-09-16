package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;

/**
 * 默认的 {@link ObjectWrapperFactory} 实现类，实际不会调用这个类的方法。
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory{
    @Override
    public boolean hasWrappereFor(Object object) {
        return false;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        throw new ReflectionException(this.getClass().getSimpleName() + "从不应该被调用类获取一个 ObjectWrapper 对象！！！");
    }
}
