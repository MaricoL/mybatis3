package org.apache.ibatis.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description 参数名工具类
 * @author mrjimmylin
 * @date 2019/9/17 8:38
 */
public class ParamNameUtil {

    private ParamNameUtil() {
    }

    // 获得普通方法的参数列表
    public static List<String> getParamNames(Method method) {
        return getParameterNames(method);
    }

    // 获得构造方法的参数列表
    public static List<String> getParamNames(Constructor<?> constructor) {
        return getParameterNames(constructor);
    }

    private static List<String> getParameterNames(Executable executable) {
        return Arrays.stream(executable.getParameters())
                .map(Parameter::getName)
                .collect(Collectors.toList());
    }

}
