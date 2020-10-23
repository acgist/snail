package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;

import com.acgist.snail.context.exception.NetException;

/**
 * <p>消息处理器接口</p>
 * 
 * @param <T> 输入消息泛型
 * 
 * @author acgist
 */
public interface IMessageCodec<T> {

	/**
	 * <p>判断消息是否需要继续处理</p>
	 * <p>消息解码：{@link #decode(Object)}、{@link #decode(Object, InetSocketAddress)}</p>
	 * <p>消息处理：{@link #onMessage(Object)}、{@link #onMessage(Object, InetSocketAddress)}</p>
	 * 
	 * @return true-完成（消息处理）；false-继续（消息解码）；
	 */
	default boolean done() {
		return true;
	}
	
	/**
	 * <p>消息编码</p>
	 * <p>处理器可以自行实现编码，发送消息时重写发送方法调用。</p>
	 * 
	 * @param message 原始消息
	 * 
	 * @return 编码消息
	 * 
	 * @throws NetException 网络异常
	 */
	default T encode(T message) throws NetException {
		return message;
	}
	
	/**
	 * <p>消息解码</p>
	 * 
	 * @param message 消息
	 * 
	 * @throws NetException 网络异常
	 */
	default void decode(T message) throws NetException {
	}
	
	/**
	 * <p>消息解码</p>
	 * 
	 * @param message 消息
	 * @param address 地址
	 * 
	 * @throws NetException 网络异常
	 */
	default void decode(T message, InetSocketAddress address) throws NetException {
	}
	
	/**
	 * <p>消息处理</p>
	 * 
	 * @param message 消息
	 * 
	 * @throws NetException 网络异常
	 */
	default void onMessage(T message) throws NetException {
	}
	
	/**
	 * <p>消息处理</p>
	 * 
	 * @param message 消息
	 * @param address 地址
	 * 
	 * @throws NetException 网络异常
	 */
	default void onMessage(T message, InetSocketAddress address) throws NetException {
	}

}
