package org.apache.ibatis.reflection;

import java.util.Arrays;

public class ArrayUtil {


    // 返回对象的哈希值
    public static int hashCode(Object object) {

        if (object == null) {
            return 0;
        }

        final Class<?> clazz = object.getClass();

        // 如果不是数组，直接返回哈希值
        if (!clazz.isArray()) {
            return clazz.hashCode();
        }

        // 获得数组类型
        Class<?> componentType = clazz.getComponentType();
        if (long.class.equals(componentType)) {
            return Arrays.hashCode((long[]) object);
        }else if (int.class.equals(componentType)) {
            return Arrays.hashCode((int[]) object);
        } else if (short.class.equals(componentType)) {
            return Arrays.hashCode((short[]) object);
        } else if (char.class.equals(componentType)) {
            return Arrays.hashCode((char[]) object);
        } else if (byte.class.equals(componentType)) {
            return Arrays.hashCode((byte[]) object);
        } else if (boolean.class.equals(componentType)) {
            return Arrays.hashCode((boolean[]) object);
        } else if (float.class.equals(componentType)) {
            return Arrays.hashCode((float[]) object);
        } else if (double.class.equals(componentType)) {
            return Arrays.hashCode((double[]) object);
        } else {
            return Arrays.hashCode((Object[]) object);
        }
    }


    // 判断两个数组是否相同
    public static boolean equals(Object thisObj, Object thatObj) {
        if (thisObj == null) {
            return thatObj == null;
        } else if (thatObj == null) {
            return false;
        }

        Class<?> clazz = thisObj.getClass();
        if (!clazz.equals(thatObj.getClass())) {
            return false;
        }

        if (!clazz.isArray()) {
            return thisObj.equals(thatObj);
        }

        final Class<?> componentType = clazz.getComponentType();
        if (long.class.equals(componentType)) {
            return Arrays.equals((long[]) thisObj, (long[]) thatObj);
        } else if (int.class.equals(componentType)) {
            return Arrays.equals((int[]) thisObj, (int[]) thatObj);
        } else if (short.class.equals(componentType)) {
            return Arrays.equals((short[]) thisObj, (short[]) thatObj);
        } else if (char.class.equals(componentType)) {
            return Arrays.equals((char[]) thisObj, (char[]) thatObj);
        } else if (byte.class.equals(componentType)) {
            return Arrays.equals((byte[]) thisObj, (byte[]) thatObj);
        } else if (boolean.class.equals(componentType)) {
            return Arrays.equals((boolean[]) thisObj, (boolean[]) thatObj);
        } else if (float.class.equals(componentType)) {
            return Arrays.equals((float[]) thisObj, (float[]) thatObj);
        } else if (double.class.equals(componentType)) {
            return Arrays.equals((double[]) thisObj, (double[]) thatObj);
        } else {
            return Arrays.equals((Object[]) thisObj, (Object[]) thatObj);
        }
    }

    public static String toString(Object object) {
        if (object == null) {
            return "nul";
        }

        final Class<?> clazz = object.getClass();
        if (!clazz.isArray()) {
            return object.toString();
        }

        final Class<?> componentType = object.getClass().getComponentType();
        if (long.class.equals(componentType)) {
            return Arrays.toString((long[]) object);
        } else if (int.class.equals(componentType)) {
            return Arrays.toString((int[]) object);
        } else if (short.class.equals(componentType)) {
            return Arrays.toString((short[]) object);
        } else if (char.class.equals(componentType)) {
            return Arrays.toString((char[]) object);
        } else if (byte.class.equals(componentType)) {
            return Arrays.toString((byte[]) object);
        } else if (boolean.class.equals(componentType)) {
            return Arrays.toString((boolean[]) object);
        } else if (float.class.equals(componentType)) {
            return Arrays.toString((float[]) object);
        } else if (double.class.equals(componentType)) {
            return Arrays.toString((double[]) object);
        } else {
            return Arrays.toString((Object[]) object);
        }
    }
}
