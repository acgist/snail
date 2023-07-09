package com.acgist.snail.net.codec;

/**
 * 消息编码器接口
 *
 * @param <I> 输入消息类型
 * 
 * @author acgist
 */
public interface IMessageEncoder<I> {

    /**
     * 消息编码
     * 
     * @param message 原始消息
     * 
     * @return 编码消息
     */
    default I encode(I message) {
        return message;
    }
    
}
