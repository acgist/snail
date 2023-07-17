package com.acgist.snail.net;

import java.nio.channels.Channel;

/**
 * 通道代理
 * 
 * @param <T> 通道代理类型
 * 
 * @author acgist
 */
public interface IChannelHandler<T extends Channel> {

    /**
     * 通道代理
     * 
     * @param channel 通道
     */
    void handle(T channel);
    
}
