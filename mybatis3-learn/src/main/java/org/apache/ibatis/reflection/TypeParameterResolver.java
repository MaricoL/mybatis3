package org.apache.ibatis.reflection;


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

    // 解析字段类型
    public static Type resolveFieldType(Field field, Type srcType) {
        Type genericType = field.getGenericType();
        Class<?> declaringClass = field.getDeclaringClass();
        return resolveType(genericType, srcType, declaringClass);
    }



    // 解析类型
    private static Type resolveType(Type type, Type srcType, Class<?> declaringClass) {
        if (type instanceof TypeVariable) {
            resolveTypeVar((TypeVariable) type, srcType, declaringClass);
            return null;
        } else if (type instanceof ParameterizedType) {
            return resolveParameterizedType((ParameterizedType) type, srcType, declaringClass);
        } else if (type instanceof GenericArrayType) {
            return resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);
        }else{
            return type;
        }
    }

    // 解析带泛型的类型
    private static Type resolveParameterizedType(ParameterizedType type, Type srcType, Class<?> declaringClass) {
        Class<?> rawType = (Class<?>) type.getRawType();
        Type[] actualTypeArguments = type.getActualTypeArguments();
        for (Type actualTypeArgument : actualTypeArguments) {
            if (actualTypeArgument instanceof TypeVariable) {
                actualTypeArgument = resolveTypeVar((TypeVariable) type, srcType, declaringClass);
            } else if (actualTypeArgument instanceof ParameterizedType) {
                actualTypeArgument = resolveParameterizedType(type, srcType, declaringClass);
            } else if (actualTypeArgument instanceof GenericArrayType) {
                actualTypeArgument = resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);;
            }
        }
        return new ParameterizedTypeImpl(rawType , actualTypeArguments , null);
    }

    // 解析带泛型的泛型数组
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


    /**
     * 解析泛型参数类型，如：T、V等
     * 如：List<N>[] selectArrayOfList();
     * @param typeVar 泛型参数类型
     * @param srcType 调用该方法的类
     * @param declaringClass 定义该方法的实际类
     * @return 泛型参数实际类型
     */
    private static Type resolveTypeVar(TypeVariable typeVar, Type srcType, Class<?> declaringClass) {
        Type result = null;
        Class<?> clazz;
        // 1. 现将 srcType 转换成 Class 类型
        if (srcType instanceof Class) {
            clazz = (Class<?>) srcType;
        } else if (srcType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) srcType;
            clazz = (Class<?>) parameterizedType.getRawType();
        }else{
            throw new IllegalArgumentException("此方法的第二个参数类型必须为 Class 或 ParameterizedType 类型，但是传入的是 " + srcType.getClass() + " 类型！");
        }

        // 如果 调用该方法的类 与 定义该方法的类 相同，
        // 则说明 该方法是直接定义在 srcType 类中的，可以直接获取 实际类型
        if (clazz == declaringClass) {
            Type[] bounds = typeVar.getBounds();
            if (bounds.length > 0) {
                return bounds[0];
            }
            return Object.class;
        }

        // 如果不相等，则说明 该方法实际是被定义在 srcType 的 父类 或 父类的父类。。。中
        Type genericSuperclass = clazz.getGenericSuperclass();

        // 开始扫描 父类 中是否含有该方法返回类型的 实际类型
        result = scanSuperTypes(typeVar , srcType , declaringClass , clazz , genericSuperclass);
        if (result != null) {
            return result;
        }

        // 如果 父类 中没有，则继续扫描 接口 中是否含有该方法返回类型的 实际类型
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            result = scanSuperTypes(typeVar , srcType , declaringClass , clazz , genericInterface);
            if (result != null) {
                return result;
            }
        }
        return Object.class;
    }

    // 扫描 父类 中是否有 泛型类型参数的 实际类型
    private static Type scanSuperTypes(TypeVariable typeVar, Type srcType, Class<?> declaringClass, Class<?> clazz, Type superclass) {
        if (superclass instanceof ParameterizedType) {
            ParameterizedType parentAsType = (ParameterizedType) superclass;
            Class<?> parentAsClass = (Class<?>)parentAsType.getRawType();
            TypeVariable<?>[] parentTypeVars = parentAsClass.getTypeParameters();
            if (srcType instanceof ParameterizedType) {
                parentAsType = translateParentTypeVar((ParameterizedType) srcType, clazz, parentAsType);
            }
            if (declaringClass == parentAsClass) {
                for (int i = 0; i < parentTypeVars.length; i++) {
                    if (typeVar == parentTypeVars[0]) {
                        return parentAsType.getActualTypeArguments()[i];
                    }
                }
            }

            if (declaringClass.isAssignableFrom(parentAsClass)) {
                return resolveTypeVar(typeVar, parentAsType, declaringClass);
            }
        } else if (superclass instanceof Class && declaringClass.isAssignableFrom((Class<?>) superclass)) {
            return resolveTypeVar(typeVar, superclass, declaringClass);
        }
        return null;
    }

    private static ParameterizedType translateParentTypeVar(ParameterizedType srcType, Class<?> srcClass, ParameterizedType parentType) {
        Type[] parentTypeArgs = parentType.getActualTypeArguments();
        Type[] srcTypeArgs = srcType.getActualTypeArguments();
        TypeVariable<?>[] srcTypeVars = srcClass.getTypeParameters();
        Type[] newParentArgs = new Type[parentTypeArgs.length];
        boolean noChange = true;
        for (int i = 0; i < parentTypeArgs.length; i++) {
            if (parentTypeArgs[i] instanceof TypeVariable) {
                for (int j = 0; j < srcTypeVars.length; j++) {
                    if (srcTypeVars[j] == parentTypeArgs[i]) {
                        noChange = false;
                        newParentArgs[i] = srcTypeArgs[j];
                    }
                }
            }else{
                newParentArgs[i] = parentTypeArgs[i];
            }
        }
        return noChange ? parentType : new ParameterizedTypeImpl((Class<?>) parentType.getRawType(), newParentArgs , null);
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
