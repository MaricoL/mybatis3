package org.apache.ibatis.reflection;


import org.apache.ibatis.exceptions.ReflectionException;
import org.apache.ibatis.reflection.invoker.AmbiguousMethodInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.text.MessageFormat;
import java.util.*;

public class Reflector {

    // 当前所需要解析类的Class对象
    private final Class<?> type;
    //    private final String[] readablePropertyNames;
//    private final String[] writablePropertyNames;
//    private final Map<String, Invoker> setMethods = new HashMap<>();
    private final Map<String, Invoker> getMethods = new HashMap<>();
//    private final Map<String, Class<?>> setTypes = new HashMap<>();
    private final Map<String, Class<?>> getTypes = new HashMap<>();
    private Constructor<?> defaultConstructor;

    public Reflector(Class<?> clazz) {
        this.type = clazz;
        // 默认无参构造方法
        addDefaultConstructor(clazz);
        // 根据getter方法获得属性名-getter方法调用者映射map
        addGetMethods(clazz);
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
        // 解决冲突的getter方法，最终一个属性仅保留一个方法
        resolveGetterConflicts(conflictingGetters);

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
                }else {
                    isAmbiguous = true;
                    break;
                }
            }
            // 2. 添加到 getMethods 和 getTypes 中
            addGetMethod(propertyName , winner , isAmbiguous);
        }


    }

    // 将 propertyName-getterMethod 映射关系添加到 getMethods 和 getTypes 中
    private void addGetMethod(String propertyName, Method method, boolean isAmbiguous) {
        MethodInvoker methodInvoker = isAmbiguous ? new AmbiguousMethodInvoker(method, MessageFormat.format("非法的getter方法：在 ''{0}'' 类中存在模糊不清的属性''{1}''", method.getDeclaringClass().getName(), propertyName))
                : new MethodInvoker(method);
        getMethods.put(propertyName, methodInvoker);
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
