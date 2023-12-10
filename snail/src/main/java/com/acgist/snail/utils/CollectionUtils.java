package com.acgist.snail.utils;

import java.util.Collection;
import java.util.List;

/**
 * 集合工具
 * 
 * @author acgist
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }
    
    /**
     * @param list 集合
     * 
     * @return 是否为空
     */
    public static final boolean isEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }
    
    /**
     * @param list 集合
     * 
     * @return 是否非空
     */
    public static final boolean isNotEmpty(Collection<?> list) {
        return !CollectionUtils.isEmpty(list);
    }
    
    /**
     * @param <T> 泛型
     * 
     * @param list 集合
     * 
     * @return 首个元素
     */
    public static final <T> T getFirst(List<T> list) {
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * @param <T> 泛型
     * 
     * @param list 集合
     * 
     * @return 尾部元素
     */
    public static final <T> T getLast(List<T> list) {
        return CollectionUtils.isEmpty(list) ? null : list.get(list.size() - 1);
    }
    
}
