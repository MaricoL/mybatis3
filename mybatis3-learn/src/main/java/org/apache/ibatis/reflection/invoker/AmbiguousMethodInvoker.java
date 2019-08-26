package org.apache.ibatis.reflection.invoker;

import org.apache.ibatis.exceptions.ReflectionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AmbiguousMethodInvoker extends MethodInvoker {

    private String exceptionMessage;

    public AmbiguousMethodInvoker(Method method , String exceptionMessage) {
        super(method);
        this.exceptionMessage = exceptionMessage;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
        return new ReflectionException(exceptionMessage);
    }
}
