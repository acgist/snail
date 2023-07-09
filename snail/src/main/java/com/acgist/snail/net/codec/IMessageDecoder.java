package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;

import com.acgist.snail.net.NetException;

/**
 * 消息解码器接口
 * 
 * @param <I> 输入消息类型
 * 
 * @author acgist
 */
public interface IMessageDecoder<I> {
    
    /**
     * 判断消息是否解码完成
     * 
     * @return 是否解码完成
     * 
     * @see #decode(Object)
     * @see #decode(Object, InetSocketAddress)
     * @see #onMessage(Object)
     * @see #onMessage(Object, InetSocketAddress)
     */
    default boolean done() {
        return true;
    }
    
    /**
     * 消息解码
     * 
     * @param message 消息
     * 
     * @throws NetException 网络异常
     */
    default void decode(I message) throws NetException {
    }
    
    /**
     * 消息解码
     * 
     * @param message 消息
     * @param address 地址
     * 
     * @throws NetException 网络异常
     */
    default void decode(I message, InetSocketAddress address) throws NetException {
    }
    
    /**
     * 消息处理
     * 
     * @param message 消息
     * 
     * @throws NetException 网络异常
     */
    default void onMessage(I message) throws NetException {
    }
    
    /**
     * 消息处理
     * 
     * @param message 消息
     * @param address 地址
     * 
     * @throws NetException 网络异常
     */
    default void onMessage(I message, InetSocketAddress address) throws NetException {
    }

}
