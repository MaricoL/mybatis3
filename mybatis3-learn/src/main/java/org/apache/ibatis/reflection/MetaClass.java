package org.apache.ibatis.reflection;

import org.apache.ibatis.reflection.invoker.GetFieldInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @author mrjimmylin
 * @description 类的元数据：基于 {@link Reflector} 和 {@link ReflectorFactory}
 * @date 2019/9/9 11:15
 */
public class MetaClass {

    private final ReflectorFactory reflectorFactory;
    private final Reflector reflector;

    private MetaClass(Class<?> type, ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
        this.reflector = reflectorFactory.findForClass(type);
    }


    // 创建指定类的 MetaClass 对象
    public static MetaClass forClass(Class<?> type, ReflectorFactory reflectorFactory) {
        return new MetaClass(type, reflectorFactory);
    }

    // 创建类的指定属性的 MetaClass 对象
    public MetaClass metaClassForProperty(String propertyName) {
        Class<?> getterType = reflector.getGetterType(propertyName);
        return MetaClass.forClass(getterType, reflectorFactory);
    }

    // 根据表达式获取属性名（是否要 下划线 转 驼峰）
    public String findProperty(String name, boolean useCamelCaseMapping) {
        // 是否要 下划线 转 驼峰
        if (useCamelCaseMapping) {
            name = name.replace("_", "");
        }
        return findProperty(name);
    }

    // 根据表达式获取属性名
    public String findProperty(String name) {
        StringBuilder sb = buildProperty(name, new StringBuilder());
        return sb.length() > 0 ? sb.toString() : null;
    }

    // 根据 name 构建属性名
    private StringBuilder buildProperty(String name, StringBuilder stringBuilder) {
        // 1. 先对 name 进行 分词处理
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        // 2. 判断是否有子节点（children字段）
        if (tokenizer.hasNext()) {
            String propertyName = reflector.findPropertyName(tokenizer.getName());
            if (propertyName != null) {
                stringBuilder.append(propertyName);
                stringBuilder.append(".");
                MetaClass metaClass = metaClassForProperty(propertyName);
                metaClass.buildProperty(tokenizer.getChildren(), stringBuilder);
            }
        } else {
            String propertyName = reflector.findPropertyName(tokenizer.getName());
            if (propertyName != null) {
                stringBuilder.append(propertyName);
            }
        }
        return stringBuilder;
    }

    // 判断指定属性是否有 getter 方法
    public boolean hasGetter(String propertyName) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(propertyName);
        if (tokenizer.hasNext()) {
            if (reflector.hasGetter(tokenizer.getName())) {
                MetaClass metaProp = metaClassForProperty(tokenizer);
                return metaProp.hasGetter(tokenizer.getChildren());
            }else{
                return false;
            }
        }else{
            return reflector.hasGetter(tokenizer.getName());
        }
    }

    // 判断指定属性是否有 setting 方法
    public boolean hasSetter(String propertyName) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(propertyName);
        if (tokenizer.hasNext()) {
            if (reflector.hasSetter(tokenizer.getName())) {
                MetaClass metaProp = metaClassForProperty(tokenizer);
                return metaProp.hasSetter(tokenizer.getChildren());
            }else{
                return false;
            }
        }else{
            return reflector.hasSetter(tokenizer.getName());
        }
    }

    // 根据属性名获取 getter 方法的返回类型
    public Class<?> getGetterType(String propertyName) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(propertyName);
        if (tokenizer.hasNext()) {
            MetaClass metaProp = metaClassForProperty(tokenizer);
            return metaProp.getGetterType(tokenizer.getChildren());
        }else{
            return getGetterType(tokenizer);
        }
    }

    // 根据属性名获取 setter 方法的参数类型
    public Class<?> getSetterType(String propertyName) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(propertyName);
        if (tokenizer.hasNext()) {
            MetaClass metaProp = metaClassForProperty(tokenizer);
            return metaProp.getSetterType(tokenizer.getChildren());
        }else{
            return reflector.getsetterType(tokenizer.getName());
        }
    }

    private MetaClass metaClassForProperty(PropertyTokenizer tokenizer) {
        Class<?> propType = getGetterType(tokenizer);
        return MetaClass.forClass(propType, reflectorFactory);

    }

    private Class<?> getGetterType(PropertyTokenizer tokenizer) {
        Class<?> getterType = reflector.getGetterType(tokenizer.getName());
        if (tokenizer.getIndex() != null && Collection.class.isAssignableFrom(getterType)) {
            // 如果 tokenizer 为 list[0].field，则需要先获得 list 是什么类型，才能继续获得 field
            Type returnType = getGenericGetterType(tokenizer.getName());
            if (returnType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnType = actualTypeArguments[0];
                    if (returnType instanceof Class) {
                        getterType = (Class<?>) returnType;
                    } else if (returnType instanceof ParameterizedType) {
                        getterType = (Class<?>) ((ParameterizedType) returnType).getRawType();
                    }
                }
            }
        }
        return getterType;
    }

    // 根据 属性名 获得其 getter 方法的返回类型
    private Type getGenericGetterType(String propertyName) {
        try {
            // 1. 获得 invoker
            Invoker invoker = reflector.getGetInvoker(propertyName);
            // 2. 如果 invoker 是 MethodInvoker，则说明是 getter 方法
            if (invoker instanceof MethodInvoker) {
                Field _method = MethodInvoker.class.getDeclaredField("method");
                _method.setAccessible(true);
                Method method = (Method) _method.get(invoker);
                return TypeParameterResolver.resolveReturnType(method, reflector.getType());
            }
            // 3. 如果 invoker 是 GetFieldInvoker，则说明是 字段 ，直接访问
            else if (invoker instanceof GetFieldInvoker) {
                Field _field = GetFieldInvoker.class.getDeclaredField("field");
                _field.setAccessible(true);
                Field field = (Field) _field.get(invoker);
                return TypeParameterResolver.resolveFieldType(field, reflector.getType());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Invoker getGetInvoker(String name) {
        return reflector.getGetInvoker(name);
    }

    public Invoker getSetInvoker(String name) {
        return reflector.getSetInvoker(name);
    }

}
