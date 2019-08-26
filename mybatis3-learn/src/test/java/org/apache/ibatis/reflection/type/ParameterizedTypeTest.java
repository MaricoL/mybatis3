package org.apache.ibatis.reflection.type;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;


/**
 * 参数化类型
 */
public class ParameterizedTypeTest {

    private Map<String,Integer> map;
    private Map.Entry<String,Integer> mapEntry;

    @Test
    public void Test1() throws NoSuchFieldException {
        Field map = ParameterizedTypeTest.class.getDeclaredField("map");
        Field mapEntry = ParameterizedTypeTest.class.getDeclaredField("mapEntry");

        Type mapType = map.getGenericType();
        Type mapEntryType = mapEntry.getGenericType();

        /* sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl */
        System.out.println(mapType.getClass().getName());
        System.out.println(mapEntryType.getClass().getName());

        /* ParameterizedType 表示参数化类型，如：List<T> Set<T> */
        ParameterizedType mapParameterizedType = (ParameterizedType) mapType;
        ParameterizedType mapEntryParameterizedType = (ParameterizedType) mapEntryType;

        // 1. getActualTypeArguments() —— 获得泛型中的实际类型
        Type[] types = mapParameterizedType.getActualTypeArguments();
        /* [class java.lang.String, class java.lang.Integer] */
        System.out.println(Arrays.toString(types));

        // 2. getRawType() —— 获得原始类型（即：字段的类型）
        Type rawType = mapParameterizedType.getRawType();
        /* interface java.util.Map */
        System.out.println(rawType);

        // 3. getOwnerType() —— 获得该类型的外部类类型，如果没有则返回 null
        Type ownerType = mapEntryParameterizedType.getOwnerType();
        /* interface java.util.Map */
        System.out.println(ownerType);
    }
}
