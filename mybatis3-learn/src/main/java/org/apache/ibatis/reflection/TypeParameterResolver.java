package org.apache.ibatis.reflection;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.*;
import java.util.Arrays;

public class TypeParameterResolver {


    // 解析方法返回值类型
    public static Type resolveReturnType(Method method, Type srcType) {
        Type returnType = method.getGenericReturnType();
        Class<?> declaringClass = method.getDeclaringClass();
        return resolveType(returnType, srcType, declaringClass);
    }

    // 解析类型
    private static Type resolveType(Type type, Type srcType, Class<?> declaringClass) {
        if (type instanceof TypeVariable) {
//            resolveTypeVar((TypeVariable) type, srcType, declaringClass);
            return null;
        } else if (type instanceof ParameterizedType) {
            return resolveParameterizedType((ParameterizedType) type, srcType, declaringClass);
        } else if (type instanceof GenericArrayType) {
//            resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);
            return null;
        }else{
            return type;
        }
    }

    private static Type resolveParameterizedType(ParameterizedType type, Type srcType, Class<?> declaringClass) {
        Class<?> rawType = (Class<?>) type.getRawType();
        Type[] actualTypeArguments = type.getActualTypeArguments();
        for (Type actualTypeArgument : actualTypeArguments) {
            if (actualTypeArgument instanceof TypeVariable) {
//                actualTypeArgument = resolveTypeVar((TypeVariable) type, srcType, declaringClass);
            } else if (actualTypeArgument instanceof ParameterizedType) {
                actualTypeArgument = resolveParameterizedType(type, srcType, declaringClass);
            } else if (actualTypeArgument instanceof GenericArrayType) {
//                actualTypeArgument = resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);;
            }
        }
        return new ParameterizedTypeImpl(rawType , actualTypeArguments , null);
    }

    static class ParameterizedTypeImpl implements ParameterizedType{
        private Class<?> rawType;
        private Type[] actualTypeArguments;
        private Type ownerType;

        public ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
            this.rawType = rawType;
            this.actualTypeArguments = actualTypeArguments;
            this.ownerType = ownerType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return this.actualTypeArguments;
        }

        @Override
        public Type getRawType() {
            return this.rawType;
        }

        @Override
        public Type getOwnerType() {
            return this.ownerType;
        }

        @Override
        public String toString() {
            return "ParameterizedTypeImpl{" +
                    "rawType=" + rawType +
                    ", actualTypeArguments=" + Arrays.toString(actualTypeArguments) +
                    ", ownerType=" + ownerType +
                    '}';
        }
    }
}
