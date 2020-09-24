package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;

/**
 * <p>消息处理器接口</p>
 * 
 * @param <T> 输入消息泛型
 * 
 * @author acgist
 * @since 1.1.1
 */
public interface IMessageCodec<T> {

	/**
	 * <p>判断消息是否需要继续处理</p>
	 * <p>消息解码：{@link #decode(Object)}、{@link #decode(Object, InetSocketAddress)}</p>
	 * <p>消息处理：{@link #onMessage(Object)}、{@link #onMessage(Object, InetSocketAddress)}</p>
	 * 
	 * @return {@code true}-完成（消息处理）；{@code false}-继续（消息解码）；
	 */
	default boolean done() {
		return true;
	}
	
	/**
	 * <p>消息编码</p>
	 * 
	 * @param message 消息
	 * 
	 * @return 编码后的消息
	 */
	default String encode(String message) {
		return message;
	}

	/**
	 * <p>消息编码</p>
	 * 
	 * @param message 消息
	 */
	default void encode(ByteBuffer message) {
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
