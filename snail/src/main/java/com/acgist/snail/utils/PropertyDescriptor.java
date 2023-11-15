package com.acgist.snail.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * 对象属性工具
 * 可以使用Java内省替换：Introspector
 * 
 * @author acgist
 */
public final class PropertyDescriptor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyDescriptor.class);

    /**
     * Getter前缀（Boolean）：{@value}
     */
    private static final String PREFIX_IS = "is";
    /**
     * Getter前缀：{@value}
     */
    private static final String PREFIX_GET = "get";
    /**
     * Setter前缀：{@value}
     */
    private static final String PREFIX_SET = "set";
    /**
     * 对象方法缓存
     */
    private static final Map<Class<?>, Map<String, Method>> CACHE_METHOD = new WeakHashMap<>();
    
    /**
     * 类型
     */
    private final Class<?> clazz;
    /**
     * 对象
     */
    private final Object instance;
    
    /**
     * @param instance 对象
     */
    private PropertyDescriptor(Object instance) {
        this.instance = instance;
        this.clazz    = instance.getClass();
    }
    
    /**
     * @param instance 对象
     * 
     * @return {@link PropertyDescriptor}
     */
    public static final PropertyDescriptor newInstance(Object instance) {
        return new PropertyDescriptor(instance);
    }
    
    /**
     * 忽略属性：
     *  1. 静态（static）
     *  2. 瞬时（transient）
     * 
     * @param field 属性
     * 
     * @return 是否忽略
     */
    public static final boolean ignoreProperty(Field field) {
        return
            // 静态属性
            Modifier.isStatic(field.getModifiers()) ||
            // 瞬时属性
            Modifier.isTransient(field.getModifiers());
    }
    
    /**
     * @param property 属性名称
     * 
     * @return Getter
     */
    public Method getter(String property) {
        final Map<String, Method> map = CACHE_METHOD.computeIfAbsent(this.clazz, key -> new HashMap<>());
        final String getMethod = PREFIX_GET + property;
        final Method result    = map.get(getMethod);
        if(result != null) {
            return result;
        }
        String methodName;
        final String isMethod  = PREFIX_IS + property;
        final Method[] methods = this.clazz.getMethods();
        for (Method method : methods) {
            methodName = method.getName();
            // 按照出现次数排序
            if(
                method.getParameterCount() == 0 &&
                (
                    getMethod.equalsIgnoreCase(methodName) ||
                    isMethod.equalsIgnoreCase(methodName)  ||
                    property.equalsIgnoreCase(methodName)
                )
            ) {
                map.put(getMethod, method);
                return method;
            }
        }
        return null;
    }

    /**
     * @param property 属性名称
     * 
     * @return 属性值
     */
    public Object get(String property) {
        final Method getter = this.getter(property);
        if(getter == null) {
            return null;
        }
        try {
            return getter.invoke(this.instance);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error("获取属性值异常：{} - {}", this.instance, property, e);
        }
        return null;
    }
    
    /**
     * @param property 属性名称
     * 
     * @return Setter
     */
    public Method setter(String property) {
        final Map<String, Method> map = CACHE_METHOD.computeIfAbsent(this.clazz, key -> new HashMap<>());
        final String setMethod = PREFIX_SET + property;
        final Method result    = map.get(setMethod);
        if(result != null) {
            return result;
        }
        String methodName;
        final Method[] methods = this.clazz.getMethods();
        for (Method method : methods) {
            methodName = method.getName();
            if(
                method.getParameterCount() == 1 &&
                (
                    setMethod.equalsIgnoreCase(methodName) ||
                    property.equalsIgnoreCase(methodName)
                )
            ) {
                map.put(setMethod, method);
                return method;
            }
        }
        return null;
    }

    /**
     * @param property 属性名称
     * @param value    属性值
     */
    public void set(String property, Object value) {
        final Method setter = this.setter(property);
        if(setter == null) {
            return;
        }
        try {
            setter.invoke(this.instance, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error("设置属性值异常：{} - {} - {}", this.instance, property, value, e);
        }
    }
    
    /**
     * @param property 属性名称
     * 
     * @return 属性类型
     */
    public Class<?> getPropertyType(String property) {
        Field[] fields;
        String fieldName;
        Class<?> superClazz = this.clazz;
        while(superClazz != null) {
            fields = superClazz.getDeclaredFields();
            for (final Field field : fields) {
                fieldName = field.getName();
                if(!PropertyDescriptor.ignoreProperty(field) && fieldName.equals(property)) {
                    return field.getType();
                }
            }
            superClazz = superClazz.getSuperclass();
        }
        return null;
    }
    
}
