package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * 消息接收代理接口
 * 
 * @author acgist
 */
public interface IMessageReceiver {

    /**
     * @return 是否没有使用
     */
    default boolean useless() {
        return false;
    }
    
    /**
     * 消息接收
     * 
     * @param buffer 消息
     * 
     * @throws NetException 网络异常
     */
    default void onReceive(ByteBuffer buffer) throws NetException {
    }
    
    /**
     * 消息接收
     * 
     * @param buffer        消息
     * @param socketAddress 地址
     * 
     * @throws NetException 网络异常
     */
    default void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
    }
    
}
