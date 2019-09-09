package org.apache.ibatis.reflection.factory;

import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.Reflector;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultObjectFactory implements ObjectFactory, Serializable {

    private static final long serialVersionUID = -8855120656740914948L;

    @Override
    public <T> T create(Class<T> type) {
        return create(type, null, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<T> type, List<Class<?>> constructorArgsTypes, List<Object> constructorArgs) {
        // 1. 解析 type 类型
        Class<?> classToCreate = resolveInterface(type);
        // 2. 实例化类
        return (T)instantiateClass(classToCreate, constructorArgsTypes, constructorArgs);
    }


    @Override
    public <T> boolean isCollection(Class<T> type) {
        return Collections.class.isAssignableFrom(type);
    }


    // 实例化类
    private <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgsTypes, List<Object> constructorArgs) {

        try {
            Constructor<T> constructor;
            // 如果构造参数类型和构造函数的参数列表为空，则获取空的构造函数
            if (constructorArgsTypes == null || constructorArgs == null) {
                constructor = type.getDeclaredConstructor();
                try {
                    return  constructor.newInstance();
                } catch (IllegalAccessException e) {
                    if (Reflector.canControlMemberAccessible()) {
                        constructor.setAccessible(true);
                        return constructor.newInstance();
                    } else {
                        throw e;
                    }
                }
                // 否则，寻找有参的构造函数
            } else {
                constructor = type.getDeclaredConstructor(constructorArgsTypes.toArray(new Class[0]));
                try {
                    return constructor.newInstance(constructorArgs.toArray(new Object[0]));
                } catch (IllegalAccessException e) {
                    if (Reflector.canControlMemberAccessible()) {
                        constructor.setAccessible(true);
                        return constructor.newInstance();
                    } else {
                        throw e;
                    }
                }
            }

        } catch (Exception e) {
            String argTypes = Optional.ofNullable(constructorArgsTypes).orElseGet(Collections::emptyList)
                    .stream().map(Class::getSimpleName)
                    .collect(Collectors.joining(","));
            String argValues = Optional.ofNullable(constructorArgs).orElseGet(Collections::emptyList)
                    .stream().map(String::valueOf)
                    .collect(Collectors.joining(","));
            throw new ReflectionException("在实例化 " + type + "对象的时候发生错误！传入的参数类型为：" +
                    argTypes + "  传入的参数的值为：" + argValues + "  。" +
                    "出错原因：" + e);
        }
    }

    /**
     * 对常用的集合接口，返回对应的实现类
     * TODO: review
     * @param type
     * @return
     */
    protected Class<?> resolveInterface(Class<?> type) {
        Class<?> classToCreate;
        if (type == List.class || type == Collection.class || type == Iterable.class) {
            classToCreate = ArrayList.class;
        } else if (type == Map.class) {
            classToCreate = HashMap.class;
        } else if (type == SortedSet.class) {
            classToCreate = TreeSet.class;
        } else if (type == Set.class) {
            classToCreate = HashSet.class;
        } else {
            classToCreate = type;
        }
        return classToCreate;
    }
}
