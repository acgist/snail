package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * <p>消息处理器：编码、解码、处理</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public interface IMessageCodec<T> {

	/**
	 * 是否完成
	 */
	default boolean done() {
		return true;
	}
	
	/**
	 * 消息编码
	 * 
	 * @param message 消息
	 * 
	 * @return 编码后的消息
	 */
	default String encode(String message) {
		return message;
	}

	/**
	 * 消息编码
	 * 
	 * @param message 消息
	 */
	default void encode(ByteBuffer message) {
	}
	
	/**
	 * 消息解码
	 * 
	 * @param message 消息
	 */
	default void decode(T message) throws NetException {}
	
	/**
	 * 消息解码
	 * 
	 * @param message 消息
	 * @param address 地址
	 */
	default void decode(T message, InetSocketAddress address) throws NetException {}
	
	/**
	 * 处理消息
	 * 
	 * @param message 消息
	 */
	default void onMessage(T message) throws NetException {}
	
	/**
	 * 处理消息
	 * 
	 * @param message 消息
	 * @param address 地址
	 */
	default void onMessage(T message, InetSocketAddress address) throws NetException {}

}
