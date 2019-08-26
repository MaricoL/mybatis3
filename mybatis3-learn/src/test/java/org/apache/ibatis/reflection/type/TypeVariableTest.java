package org.apache.ibatis.reflection.type;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;

/**
 * 泛型的类型变量
 */
public class TypeVariableTest<T extends Number & Serializable> {

    private List<T> list;
    private T t;

    @Test
    public void Test1() throws NoSuchFieldException {
        Field listField = TypeVariableTest.class.getDeclaredField("list");
        Field tField = TypeVariableTest.class.getDeclaredField("t");

        Type listType = listField.getGenericType();
        Type tType = tField.getGenericType();

        /* java.util.List<T> */
        System.out.println(listType);
        /* sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl */
        System.out.println(listType.getClass().getName());

        ParameterizedType listParameterizedType = (ParameterizedType) listType;

        // 获得
        Type[] type = listParameterizedType.getActualTypeArguments();
        /* T */
        System.out.println(type[0]);
        /* sun.reflect.generics.reflectiveObjects.TypeVariableImpl */
        System.out.println(type[0].getClass().getName());

        TypeVariable tTypeVariable = (TypeVariable) tType;


        // 1. getBounds() —— 获得泛型类型参数的上限类型
        Type[] bounds = tTypeVariable.getBounds();
        /*[class java.lang.Number, interface java.io.Serializable] */
        System.out.println(Arrays.toString(bounds));

        // 2. getGenericDeclaration() —— 获得声明该泛型类型的变量实体类型
        GenericDeclaration genericDeclaration = tTypeVariable.getGenericDeclaration();
        /* class org.apache.ibatis.reflection.type.TypeVariableTest */
        System.out.println(genericDeclaration);

        // 3. 获得泛型变量在源码中定义的名称
        String name = tTypeVariable.getName();
        /* T */
        System.out.println(name);
    }
}
