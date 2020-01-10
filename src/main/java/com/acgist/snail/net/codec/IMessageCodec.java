package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * <p>消息处理器接口</p>
 * <table border="1">
 * 	<caption>消息处理器</caption>
 * 	<tr>
 * 		<th>处理器</th><th>功能</th><th>实现</th>
 * 	</tr>
 * 	<tr>
 * 		<td>中间处理器</td>
 * 		<td>编码解码</td>
 * 		<td>继承{@linkplain MessageCodec 消息处理器适配器}</td>
 * 	</tr>
 * 	<tr>
 * 		<td>最终处理器</td>
 * 		<td>消息消费</td>
 * 		<td>实现{@linkplain IMessageCodec 消息处理器接口}</td>
 * 	</tr>
 * </table>
 * <p>注意编码解码的逻辑顺序（防止多个处理器结合使用时出现错误）</p>
 * 
 * @param <T> 输入类型
 * 
 * @author acgist
 * @since 1.1.1
 */
public interface IMessageCodec<T> {

	/**
	 * <p>判断是否完成解码</p>
	 * <p>消息解码：{@link #decode(Object)}或者{@link #decode(Object, InetSocketAddress)}</p>
	 * <p>处理消息：{@link #onMessage(Object)}或者{@link #onMessage(Object, InetSocketAddress)}</p>
	 * 
	 * @return {@code true}-完成（处理消息）；{@code false}-没有完成（消息解码）；
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
	 * <p>处理消息</p>
	 * 
	 * @param message 消息
	 * 
	 * @throws NetException 网络异常
	 */
	default void onMessage(T message) throws NetException {
	}
	
	/**
	 * <p>处理消息</p>
	 * 
	 * @param message 消息
	 * @param address 地址
	 * 
	 * @throws NetException 网络异常
	 */
	default void onMessage(T message, InetSocketAddress address) throws NetException {
	}

}
