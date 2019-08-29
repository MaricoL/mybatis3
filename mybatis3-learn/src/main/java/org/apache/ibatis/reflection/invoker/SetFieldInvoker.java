package org.apache.ibatis.reflection.invoker;

import org.apache.ibatis.reflection.Reflector;

import java.lang.reflect.Field;

public class SetFieldInvoker implements Invoker {

    private Field field;

    public SetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object obj, Object[] args) throws IllegalAccessException {
        try {
            field.set(obj, args);
        } catch (IllegalAccessException e) {
            if (Reflector.canControlMemberAccessible()) {
                field.setAccessible(true);
                field.set(obj, args);
            } else {
                throw e;
            }
        }
        return null;
    }

    @Override
    public Class<?> getTypes() {
        return null;
    }
}
