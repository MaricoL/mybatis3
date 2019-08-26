package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;

/**
 * 调用者接口
 */
public interface Invoker {

    Object invoke(Object obj, Object[] args) throws IllegalAccessException, InvocationTargetException;


    Class<?> getTypes();
}
