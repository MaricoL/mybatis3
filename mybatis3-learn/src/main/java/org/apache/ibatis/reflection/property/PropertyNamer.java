package org.apache.ibatis.reflection.property;

import org.apache.ibatis.reflection.ReflectionException;

import java.util.Locale;

public final class PropertyNamer {

    private PropertyNamer() {
    }

    // 根据方法名获得属性名
    public static String methodToProperty(String name) {
        // 从方法名中截取属性名
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new ReflectionException("无法解析该方法名：" + name + "。该方法名不是以 is 或 get 或 set开头！");
        }

        // 将属性名的首字母小写
        if (name.length() > 1) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }
        return name;
    }

    // 以 get 或 is 开头，说明是 getter 方法
    public static boolean isGetter(String name) {
        return name.startsWith("get") && name.length() > 3 || name.startsWith("is") && name.length() > 2;
    }

    // 以 set 开头，说明是 setter 方法
    public static boolean isSetter(String name) {
        return name.startsWith("set") && name.length() > 3;
    }
}
