package org.apache.ibatis.reflection;


import org.apache.ibatis.exceptions.ReflectionException;
import org.apache.ibatis.reflection.invoker.AmbiguousMethodInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.*;

public class Reflector {

    // 当前所需要解析类的Class对象
    private final Class<?> type;
    //    private final String[] readablePropertyNames;
//    private final String[] writablePropertyNames;
    private final Map<String, Invoker> setMethods = new HashMap<>();
    private final Map<String, Invoker> getMethods = new HashMap<>();
    private final Map<String, Class<?>> setTypes = new HashMap<>();
    private final Map<String, Class<?>> getTypes = new HashMap<>();
    private Constructor<?> defaultConstructor;

    public Reflector(Class<?> clazz) {
        this.type = clazz;
        // 默认无参构造方法
        addDefaultConstructor(clazz);
        // 根据getter方法获得属性名-getter方法调用者映射map
        addGetMethods(clazz);
        // 根据setter方法获得属性名-getter方法调用者映射map
        addSetMethods(clazz);
    }

    // 解析默认无参构造函数
    private void addDefaultConstructor(Class<?> clazz) {
        Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterTypes().length == 0)
                .findAny()
                .ifPresent(constructor -> this.defaultConstructor = constructor);
    }

    // 解析getter方法（所有以get 或 is开头的方法）：初始化 getMethods 和 getTypes
    private void addGetMethods(Class<?> clazz) {
        // 属性与其 getter 方法的映射map
        Map<String, List<Method>> conflictingGetters = new HashMap<>();
        // 1. 获得 clazz 的所有方法，包括父类和接口的
        Method[] methods = getClassMethods(clazz);
        // 2. 遍历 methods
        // 过滤出所有的 getter 方法：参数列表长度为0，并且方法名以get或is开头
        Arrays.stream(methods)
                .filter(m -> m.getParameterTypes().length == 0 && PropertyNamer.isGetter(m.getName()))
                .forEach(m -> addMethodConflict(conflictingGetters, PropertyNamer.methodToProperty(m.getName()), m));
        // 解决冲突的getter方法，最终一个属性仅保留一个getter方法
        resolveGetterConflicts(conflictingGetters);
    }

    // 解析setter方法（所有以 set 开头的方法）：初始化 setMethods 和 setTypes
    private void addSetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingSetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        Arrays.stream(methods)
                .filter(m -> m.getParameterTypes().length == 1 && PropertyNamer.isSetter(m.getName()))
                .forEach(m -> addMethodConflict(conflictingSetters, PropertyNamer.methodToProperty(m.getName()), m));
        // 解决冲突的setter方法，最终一个属性仅保留一个setter方法
        resolveSetterConflicts(conflictingSetters);
    }

    // 解决冲突的setter方法（与 resolveGetterConflicts() 方法处理稍有不同）
    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
        for (String propertyName : conflictingSetters.keySet()) {
            // 当前属性的 setter 方法
            List<Method> setters = conflictingSetters.get(propertyName);
            // 当前属性对应的 getter 方法 的返回值类型
            Class<?> getterType = getTypes.get(propertyName);
            // 当前属性对应的 getter 方法是否模糊不清
            boolean isGetterAmbiguous = getMethods.get(propertyName) instanceof AmbiguousMethodInvoker;
            // 当前属性对应的 setter 方法是否模糊不清
            boolean isSetterAmbiguous = false;
            // 如果有模糊不清的 setter 方法的时候，返回 match 和 setter 方法的参数列表的类型
            // 以便打印错误信息
            Map<String, Class<?>> ambiguousClassMap = new HashMap<>();
            // 最匹配的 setter 方法
            Method match = null;
            // 遍历 setters 方法集合，找到与当前属性最匹配的 setter 方法
            for (Method setter : setters) {
                // 如果当前对象的 getter 方法不模糊 并且
                // getter 方法的返回值类型与 当前遍历的 setter 方法 的参数列表的第一个参数类型一致
                // 则 优先匹配
                if (!isGetterAmbiguous && getterType.equals(setter.getParameterTypes()[0])) {
                    match = setter;
                    break;
                }
                // 第一次 match 为 null，则赋上初值
                if (match == null) {
                    match = setter;
                    continue;
                }

                // 如果 setter 方法没有模糊不清，则继续匹配更适合的 setter 方法
                if (!isSetterAmbiguous) {
                    match = pickBetterSetter(match, setter, ambiguousClassMap);
                    isSetterAmbiguous = match == null;
                }

            }
            addSetMethod(propertyName, match, isSetterAmbiguous, ambiguousClassMap);

        }

    }

    // 匹配更适合的 Setter 方法
    private Method pickBetterSetter(Method match, Method setter, Map<String, Class<?>> ambiguousClassMap) {
        // 继续判断是否有更适合的 setter 方法，根据 setter 方法参数列表中的第一个参数类型来判断
        Class<?> setter1 = match.getParameterTypes()[0];
        Class<?> setter2 = setter.getParameterTypes()[0];
        if (setter2.isAssignableFrom(setter1)) {
            // setter2 是 setter1 的父类，取子类 setter1，因此无需改变
        } else if (setter1.isAssignableFrom(setter2)) {
            // setter1 是 setter2 的父类，取子类 setter2
            match = setter;
        } else {
            ambiguousClassMap.putIfAbsent("matchParameterType", setter1);
            ambiguousClassMap.putIfAbsent("setterParameterType", setter2);
        }
        return null;
    }

    // 将 propertyName-setterMethod 映射关系添加到 setMethods 和 setTypes 中
    private void addSetMethod(String propertyName, Method match, boolean isSetterAmbiguous, Map<String, Class<?>> ambiguousClassMap) {
        MethodInvoker invoker = isSetterAmbiguous ? new AmbiguousMethodInvoker(match,
                MessageFormat.format("在类''{0}''中，''{1}}'' 字段的setter方法参数类型模糊不清，\n" +
                        "分别为''{2}'' , ''{3}'' ", match.getDeclaringClass(), propertyName, ambiguousClassMap.get("matchParameterType"), ambiguousClassMap.get("setterParameterType")))
                : new MethodInvoker(match);
        setMethods.put(propertyName, invoker);
        Type[] types = TypeParameterResolver.resolveParamType(match, this.type);
        setTypes.put(propertyName, typeToClass(types[0]));
    }


    // 解决冲突的getter方法
    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
        for (Map.Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
            boolean isAmbiguous = false;
            Method winner = null;
            String propertyName = entry.getKey();
            for (Method candidate : entry.getValue()) {
                if (winner == null) {
                    winner = candidate;
                    continue;
                }
                // 根据返回值类型进行判断，取子类的方法
                Class<?> winnerReturnType = winner.getReturnType();
                Class<?> candidateReturnType = candidate.getReturnType();
                // 返回值相同，只有 boolean 才可能发生，取方法名开头为 is 的方法
                if (candidateReturnType.equals(winnerReturnType)) {
                    if (!boolean.class.equals(candidateReturnType)) {
                        isAmbiguous = true;
                        break;
                    } else if (candidate.getName().startsWith("is")) {
                        winner = candidate;
                    }
                } else if (candidateReturnType.isAssignableFrom(winnerReturnType)) {
                    // 如果 candidateReturnType 是 winnerReturnType 的父类，则保持不变，还是取 winner
                } else if (winnerReturnType.isAssignableFrom(candidateReturnType)) {
                    // 如果 winnerReturnType 是 candidateReturnType 的父类，则取子类 candidate
                    winner = candidate;
                } else {
                    isAmbiguous = true;
                    break;
                }
            }
            // 2. 添加到 getMethods 和 getTypes 中
            addGetMethod(propertyName, winner, isAmbiguous);
        }


    }


    // 将 propertyName-getterMethod 映射关系添加到 getMethods 和 getTypes 中
    private void addGetMethod(String propertyName, Method method, boolean isAmbiguous) {
        MethodInvoker methodInvoker = isAmbiguous ? new AmbiguousMethodInvoker(method, MessageFormat.format("非法的getter方法：在 ''{0}'' 类中存在模糊不清的属性''{1}''", method.getDeclaringClass().getName(), propertyName))
                : new MethodInvoker(method);
        getMethods.put(propertyName, methodInvoker);
        // 解析方法的返回值类型
        Type returnType = TypeParameterResolver.resolveReturnType(method, this.type);
        getTypes.put(propertyName, typeToClass(returnType));
    }

    // Type -> Class
    private Class<?> typeToClass(Type src) {
        Class<?> result = null;
        // 普通类型直接使用 Class
        if (src instanceof Class) {
            result = (Class<?>) src;
            // 泛型类型使用 ParameterizedType
        } else if (src instanceof ParameterizedType) {
            result = (Class<?>) ((ParameterizedType) src).getRawType();
            // 泛型数组，获得其具体类型
        } else if (src instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) src).getGenericComponentType();
            // 普通类型直接使用 Class
            if (componentType instanceof Class) {
                result = Array.newInstance((Class<?>) componentType, 0).getClass();
            } else {
                // 递归该方法，返回类
                Class<?> componentClass = typeToClass(componentType);
                result = Array.newInstance(componentClass, 0).getClass();
            }
        }
        // 都不符合，返回 Object 类
        if (result == null) {
            return Object.class;
        }
        return result;
    }

    // 获得 clazz 的所有方法，包括父类和接口的
    private Method[] getClassMethods(Class<?> clazz) {
        // 每个方法签名和该方法的映射map
        Map<String, Method> uniqueMethods = new HashMap<>();
        // 循环，找到类父类，类父类的父类，直到父类为Object
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            // 记录当前 currentClass 定义的方法
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());
            // 记录当前 currentClass 所实现接口中定义的方法
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                addUniqueMethods(uniqueMethods, anInterface.getMethods());
            }

            currentClass = currentClass.getSuperclass();
        }

        Collection<Method> values = uniqueMethods.values();
        return values.toArray(new Method[0]);
    }

    // 将方法和其方法签名都添加到uniqueMethods中，形成映射关系
    private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] declaredMethods) {
        for (Method method : declaredMethods) {
            //  如果方法不是 桥接方法
            if (!method.isBridge()) {
                // 获得方法签名
                String methodSignature = getSignature(method);
                uniqueMethods.putIfAbsent(methodSignature, method);
            }
        }
    }

    // 获得方法签名，格式：返回值#方法名:参数1类型,参数2类型,参数3类型.。。。
    private String getSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        if (returnType != null) {
            sb.append(returnType).append("#");
        }
        sb.append(method.getName());
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(i == 0 ? ":" : ",").append(parameterTypes[i].getName());
        }
        return sb.toString();
    }

    private void addMethodConflict(Map<String, List<Method>> conflictingGetters, String propertyName, Method m) {
        if (isValidPropertyName(propertyName)) {
            List<Method> list = conflictingGetters.computeIfAbsent(propertyName, k -> new ArrayList<>());
            list.add(m);
        }
    }

    // 属性名是否有效
    private boolean isValidPropertyName(String name) {
        return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
    }

    public Constructor<?> getDefaultConstructor() {
        return Optional.ofNullable(this.defaultConstructor)
                .orElseThrow(() -> new ReflectionException("没有无参构造函数！！！"));
    }

    // 判断，是否可以修改可访问性
    public static boolean canControlMemberAccessible() {
        try {
            SecurityManager securityManager = System.getSecurityManager();
            if (null != securityManager) {
                securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
            }
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }
}
