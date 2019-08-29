package org.apache.ibatis.reflection.invoker;

import org.apache.ibatis.reflection.Reflector;

import java.lang.reflect.Field;

public class GetFieldInvoker implements Invoker {
    private Field field;

    public GetFieldInvoker(Field field) {
        this.field = field;
    }


    @Override
    public Object invoke(Object obj, Object[] args) throws IllegalAccessException {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            if (Reflector.canControlMemberAccessible()) {
                field.setAccessible(true);
                return field.get(obj);
            } else {
                throw e;
            }
        }

    }

    @Override
    public Class<?> getTypes() {
        return null;
    }
}
