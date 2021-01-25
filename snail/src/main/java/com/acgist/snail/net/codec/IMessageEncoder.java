package com.acgist.snail.net.codec;

/**
 * <p>消息编码器接口</p>
 *
 * @param <I> 输入消息类型
 * 
 * @author acgist
 */
public interface IMessageEncoder<I> {

	/**
	 * <p>消息编码</p>
	 * 
	 * @param message 原始消息
	 * 
	 * @return 编码消息
	 */
	default I encode(I message) {
		return message;
	}
	
}
