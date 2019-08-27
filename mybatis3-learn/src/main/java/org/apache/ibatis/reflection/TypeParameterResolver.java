package org.apache.ibatis.reflection;

import sun.net.www.content.text.Generic;

import java.lang.reflect.*;
import java.util.Arrays;

public class TypeParameterResolver {


    // 解析方法返回值类型
    public static Type resolveReturnType(Method method, Type srcType) {
        Type returnType = method.getGenericReturnType();
        Class<?> declaringClass = method.getDeclaringClass();
        return resolveType(returnType, srcType, declaringClass);
    }

    // 解析参数列表中的参数类型
    public static Type[] resolveParamType(Method method, Type srcType) {
        Type[] parameterTypes = method.getGenericParameterTypes();
        Class<?> declaringClass = method.getDeclaringClass();
        for (Type parameterType : parameterTypes) {
            parameterType = resolveType(parameterType, srcType, declaringClass);
        }
        return parameterTypes;
    }



    // 解析类型
    private static Type resolveType(Type type, Type srcType, Class<?> declaringClass) {
        if (type instanceof TypeVariable) {
//            resolveTypeVar((TypeVariable) type, srcType, declaringClass);
            return null;
        } else if (type instanceof ParameterizedType) {
            return resolveParameterizedType((ParameterizedType) type, srcType, declaringClass);
        } else if (type instanceof GenericArrayType) {
            return resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);
        }else{
            return type;
        }
    }

    private static Type resolveGenericArrayType(GenericArrayType type, Type srcType, Class<?> declaringClass) {
        Type componentType = type.getGenericComponentType();
        Type resolvedComponentType = null;
        if (componentType instanceof TypeVariable) {
            resolvedComponentType = resolveTypeVar((TypeVariable) componentType, srcType, declaringClass);
        } else if (componentType instanceof ParameterizedType) {
            resolvedComponentType = resolveParameterizedType((ParameterizedType) componentType, srcType, declaringClass);
        } else if (componentType instanceof GenericArrayType) {
            resolvedComponentType = resolveGenericArrayType((GenericArrayType) componentType, srcType, declaringClass);
        }
        if (resolvedComponentType instanceof Class) {
           return Array.newInstance((Class<?>)resolvedComponentType, 0).getClass();
        }else{
            return new GenericArrayTypeImpl(resolvedComponentType);
        }
    }

    private static Type resolveTypeVar(TypeVariable componentType, Type srcType, Class<?> declaringClass) {

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
                actualTypeArgument = resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);;
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

    static class GenericArrayTypeImpl implements GenericArrayType{
        private Type genericComponentType;

        public GenericArrayTypeImpl(Type genericComponentType) {
            this.genericComponentType = genericComponentType;
        }

        @Override
        public Type getGenericComponentType() {
            return this.genericComponentType;
        }
    }
}
