package org.apache.ibatis.reflection;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author mrjimmylin
 * @description 参数名解析器
 * @date 2019/9/17 9:13
 */
public class ParamNameResolver {

    // 索引 - 真正参数名 映射map
    private final SortedMap<Integer, String> maps;

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
        maps = Collections.unmodifiableSortedMap(map);
    }

    private String getActualParamName(Method method, int paramIndex) {
        return ParamNameUtil.getParamNames(method).get(paramIndex);
    }


    private boolean isSpecialParameter(Class<?> paramType) {
        return RowBounds.class.isAssignableFrom(paramType) || ResultHandler.class.isAssignableFrom(paramType);
    }

}
