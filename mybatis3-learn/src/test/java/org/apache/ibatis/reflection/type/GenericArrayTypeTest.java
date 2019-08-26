package org.apache.ibatis.reflection.type;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 泛型数组类型
 */
public class GenericArrayTypeTest<T> {
    private List<String>[] listArray;
    private T[] t;


    @Test
    public void Test1() throws NoSuchFieldException {
        Field listArray = GenericArrayTypeTest.class.getDeclaredField("listArray");
        Field t = GenericArrayTypeTest.class.getDeclaredField("t");

        Type listArrayType = listArray.getGenericType();
        Type tType = t.getGenericType();

        /* java.util.List<java.lang.String>[] */
        System.out.println(listArrayType);
        /* T[] */
        System.out.println(tType);

        /* sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl */
        System.out.println(listArrayType.getClass().getName());
        /* sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl */
        System.out.println(tType.getClass().getName());

        GenericArrayType listGenericArrayType = (GenericArrayType) listArrayType;
        GenericArrayType tTypeArrayType = (GenericArrayType) tType;

        // 1.
        Type _listArrayType = listGenericArrayType.getGenericComponentType();
        Type _tType = tTypeArrayType.getGenericComponentType();
        /* java.util.List<java.lang.String> */
        System.out.println(_listArrayType);
        /* T */
        System.out.println(_tType);
    }
}
