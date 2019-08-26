package org.apache.ibatis.reflection.type;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

/**
 * 通配符类型
 */
public class WildcardTypeTest {

    private List<? extends Number> listNum;
    private List<? super String> listStr;

    @Test
    public void Test1() throws NoSuchFieldException {
        Field listNum = WildcardTypeTest.class.getDeclaredField("listNum");
        Field listStr = WildcardTypeTest.class.getDeclaredField("listStr");

        ParameterizedType listNumParameterizedType = (ParameterizedType)listNum.getGenericType();
        ParameterizedType listStrParameterizedType = (ParameterizedType)listStr.getGenericType();

        WildcardType listNumWildcardType = (WildcardType)listNumParameterizedType.getActualTypeArguments()[0];
        WildcardType listStrWildcardType = (WildcardType)listStrParameterizedType.getActualTypeArguments()[0];

        Type[] upperBounds = listNumWildcardType.getUpperBounds();
        Type[] lowerBounds = listStrWildcardType.getLowerBounds();

        /* [class java.lang.Number] */
        System.out.println(Arrays.toString(upperBounds));
        /* [class java.lang.String] */
        System.out.println(Arrays.toString(lowerBounds));
    }
}
