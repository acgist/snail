package com.acgist.snail.utils;

/**
 * 可修改的Optional
 * 
 * @param <T> 数据类型
 * 
 * @author acgist
 */
public final class ModifyOptional<T> {

    /**
     * 数据
     */
    private T t;
    
    private ModifyOptional() {
    }
    
    /**
     * @param t 数据
     */
    private ModifyOptional(T t) {
        this.t = t;
    }

    /**
     * @param <T> 数据类型
     * 
     * @return {@link ModifyOptional}
     */
    public static final <T> ModifyOptional<T> newInstance() {
        return new ModifyOptional<>();
    }
    
    /**
     * @param <T> 数据类型
     * 
     * @param t 数据
     * 
     * @return {@link ModifyOptional}
     */
    public static final <T> ModifyOptional<T> newInstance(T t) {
        return new ModifyOptional<>(t);
    }
    
    /**
     * @param t 数据
     */
    public void set(T t) {
        this.t = t;
    }
    
    /**
     * @return 数据
     */
    public T get() {
        return this.t;
    }
    
    /**
     * @param defaultValue 默认数据
     * 
     * @return 数据
     */
    public T get(T defaultValue) {
        return this.isEmpty() ? defaultValue : this.get();
    }
    
    /**
     * 删除旧的数据
     * 
     * @return 旧的数据
     */
    public T delete() {
        final T old = this.t;
        this.t = null;
        return old;
    }

    /**
     * @return 数据是否为空
     */
    public boolean isEmpty() {
        return this.t == null;
    }
    
    /**
     * @return 数据是否非空
     */
    public boolean isPresent() {
        return !this.isEmpty();
    }
    
}
