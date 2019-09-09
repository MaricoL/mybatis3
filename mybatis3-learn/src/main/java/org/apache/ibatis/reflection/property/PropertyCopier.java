package org.apache.ibatis.reflection.property;

import org.apache.ibatis.reflection.Reflector;

import java.lang.reflect.Field;

/**
 * 属性复制器
 */
public final class PropertyCopier {

    private PropertyCopier() {

    }

    /**
     * 将 sourceBean 中的属性复制到 destinationBean中
     *
     * @param type            对象类型
     * @param sourceBean      原对象
     * @param destinationBean 目标对象
     */
    public static void copyBeanProperties(Class<?> type, Object sourceBean, Object destinationBean) {
        Class<?> parent = type;
        while (parent != null) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                try {
                    try {
                        field.set(destinationBean, field.get(sourceBean));
                    } catch (IllegalAccessException e) {
                        if (Reflector.canControlMemberAccessible()) {
                            field.setAccessible(true);
                            field.set(destinationBean, field.get(sourceBean));
                        } else {
                            throw e;
                        }
                    }
                } catch (IllegalAccessException ignored) {

                }
            }
            parent = parent.getSuperclass();
        }
    }
}
