package com.acgist.snail.net;

/**
 * <p>通道代理</p>
 * 
 * @param <T> 通道代理类型
 * 
 * @author acgist
 */
public interface IChannelHandler<T> {

	/**
	 * <p>通道代理</p>
	 * 
	 * @param channel 通道
	 */
	void handle(T channel);
	
}
