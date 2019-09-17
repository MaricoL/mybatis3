package org.apache.ibatis.reflection;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author mrjimmylin
 * @description 参数名解析器
 * @date 2019/9/17 9:13
 */
public class ParamNameResolver {

    private static final String GENERIC_NAME_PREFIX = "param";

    // 索引 - 真正参数名 映射map
    private final SortedMap<Integer, String> names;

    // 是否有 @Param 注解
    private boolean hasParamAnnotation;

    // 获得方法参数的真正参数名
    public ParamNameResolver(Configuration config, Method method) {
        final Class<?>[] paramTypes = method.getParameterTypes();
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        final SortedMap<Integer, String> map = new TreeMap<>();

        int paramCount = paramAnnotations.length;
        // 从 @Param 注解中获得真正的属性名
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            // 如果是特殊的类型，则跳过
            if (isSpecialParameter(paramTypes[paramIndex])) {
                continue;
            }

            String name = null;
            Annotation[] annotations = paramAnnotations[paramIndex];
            for (Annotation annotation : annotations) {
                // 将标注了 @Param 注解中的 value 取出
                if (annotation instanceof Param) {
                    hasParamAnnotation = true;
                    name = ((Param) annotation).value();
                    break;
                }
            }
            // 如果 name 为空，就获得真实的参数名
            if (name == null) {
                if (config.isUseActualParamName()) {
                    name = getActualParamName(method, paramIndex);
                }
                if (name == null) {
                    // 如果还是为空，则使用 map 的顺序作为 name
                    name = String.valueOf(map.size());
                }
            }
            // 添加到 map 中
            map.put(paramIndex, name);
        }
        // 构建不可变对象
        names = Collections.unmodifiableSortedMap(map);
    }


    // 获得 参数名 --- 值 的映射
    public Object getNamedParams(Object[] args) {
        // 参数个数
        final int paramCount = names.size();
        // 如果 args 为空 或者 names 长度为0，则返回 null
        if (args == null || paramCount == 0) {
            return null;
        }
        // 如果没有 @Param 注解 并且 参数长度只有1个
        else if (!hasParamAnnotation && paramCount == 1) {
            return args[names.firstKey()];
        }
        // 否则，获取映射map
        else{
            final Map<String, Object> paramMap = new MapperMethod.ParamMap<>();
            int i = 0;
            for (Map.Entry<Integer, String> entry : names.entrySet()) {
                // 参数名 --- 值 映射
                paramMap.put(entry.getValue(), args[entry.getKey()]);
                final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i);
                if (!names.containsValue(genericParamName)) {
                    paramMap.put(genericParamName, args[entry.getKey()]);
                }
                i++;
            }
            return paramMap;
        }
    }

    private String getActualParamName(Method method, int paramIndex) {
        return ParamNameUtil.getParamNames(method).get(paramIndex);
    }


    private boolean isSpecialParameter(Class<?> paramType) {
        return RowBounds.class.isAssignableFrom(paramType) || ResultHandler.class.isAssignableFrom(paramType);
    }

}
