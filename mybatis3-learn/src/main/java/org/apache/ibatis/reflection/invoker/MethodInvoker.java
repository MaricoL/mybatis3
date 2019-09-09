package org.apache.ibatis.reflection.invoker;

import org.apache.ibatis.reflection.Reflector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker implements Invoker {
    // 如果是 getter 方法，则为 方法返回值类型
    // 如果是 setter 方法，则为 形参类型
    private final Class<?> type;
    // method 方法对象
    private final Method method;

    public MethodInvoker(Method method) {
        this.method = method;
        if (method.getParameterTypes().length == 0) {
            type = method.getReturnType();
        } else {
            type = method.getParameterTypes()[0];
        }
    }

    // 调用 target 对象的 method 方法，方法参数列表为 args
    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            if (Reflector.canControlMemberAccessible()) {
                method.setAccessible(true);
                return method.invoke(target, args);
            }else{
                throw e;
            }
        }
    }

    @Override
    public Class<?> getTypes() {
        return null;
    }
}
