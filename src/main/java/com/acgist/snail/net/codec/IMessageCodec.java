package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * <p>消息处理器接口</p>
 * <p>最终处理器：消息消费</p>
 * <p>中间处理器：编码、解码</p>
 * <p>实现{@linkplain IMessageCodec 消息处理器接口}的类属于最终处理器</p>
 * <p>继承{@linkplain MessageCodec 消息处理器适配器}的类属于中间处理器</p>
 * <p>注：一定要注意编码和解码的逻辑顺序（防止多个处理器结合使用时出现错误）</p>
 * 
 * @param <T> 输入类型
 * 
 * @author acgist
 * @since 1.1.1
 */
public interface IMessageCodec<T> {

	/**
	 * <p>解码是否完成</p>
	 * <p>继续执行：{@link #decode(Object)}或者{@link #decode(Object, InetSocketAddress)}</p>
	 * <p>完成执行：{@link #onMessage(Object)}或者{@link #onMessage(Object, InetSocketAddress)}</p>
	 * 
	 * @return true-完成；false-继续；
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
	 * 
	 * @throws NetException 网络异常
	 */
	default void decode(T message) throws NetException {
	}
	
	/**
	 * 消息解码
	 * 
	 * @param message 消息
	 * @param address 地址
	 * 
	 * @throws NetException 网络异常
	 */
	default void decode(T message, InetSocketAddress address) throws NetException {
	}
	
	/**
	 * 处理消息
	 * 
	 * @param message 消息
	 * 
	 * @throws NetException 网络异常
	 */
	default void onMessage(T message) throws NetException {
	}
	
	/**
	 * 处理消息
	 * 
	 * @param message 消息
	 * @param address 地址
	 * 
	 * @throws NetException 网络异常
	 */
	default void onMessage(T message, InetSocketAddress address) throws NetException {
	}

}
