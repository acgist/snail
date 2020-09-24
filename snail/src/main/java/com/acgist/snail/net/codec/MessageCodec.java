package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.exception.NetException;

/**
 * <p>消息处理器适配器</p>
 * <table border="1">
 * 	<caption>消息处理器</caption>
 * 	<tr>
 * 		<th>处理器</th><th>功能</th><th>实现</th>
 * 	</tr>
 * 	<tr>
 * 		<td>中间处理器</td>
 * 		<td>消息编码、消息解码</td>
 * 		<td>继承{@linkplain MessageCodec 消息处理器适配器}</td>
 * 	</tr>
 * 	<tr>
 * 		<td>最终处理器</td>
 * 		<td>消息消费、消息处理</td>
 * 		<td>实现{@linkplain IMessageCodec 消息处理器接口}</td>
 * 	</tr>
 * </table>
 * <p>注意编码解码的逻辑顺序（防止多个处理器结合使用时出现错误）</p>
 * 
 * @param <T> 输入消息泛型
 * @param <X> 输出消息泛型
 * 
 * @author acgist
 * @since 1.1.1
 */
public abstract class MessageCodec<T, X> implements IMessageCodec<T> {

	/**
	 * <p>下一个消息处理器</p>
	 */
	protected final IMessageCodec<X> messageCodec;

	protected MessageCodec(IMessageCodec<X> messageCodec) {
		this.messageCodec = messageCodec;
	}

	@Override
	public boolean done() {
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>必须执行{@linkplain #messageCodec 下一个消息处理器}的{@link #encode(String)}方法</p>
	 */
	@Override
	public String encode(String message) {
		return this.messageCodec.encode(message);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>必须执行{@linkplain #messageCodec 下一个消息处理器}的{@link #encode(ByteBuffer)}方法</p>
	 */
	@Override
	public void encode(ByteBuffer message) {
		this.messageCodec.encode(message);
	}
	
	@Override
	public void decode(T message) throws NetException {
		this.decode(message, null, false);
	}

	@Override
	public void decode(T message, InetSocketAddress address) throws NetException {
		this.decode(message, address, true);
	}
	
	/**
	 * <p>消息解码</p>
	 * <p>必须执行{@linkplain #messageCodec 下一个消息处理器}的{@link #doNext(Object, InetSocketAddress, boolean)}方法</p>
	 * 
	 * @param message 消息
	 * @param address 地址
	 * @param haveAddress 是否包含地址
	 * 
	 * @throws NetException 网络异常
	 */
	protected abstract void decode(T message, InetSocketAddress address, boolean haveAddress) throws NetException;
	
	/**
	 * <p>执行下一个消息处理器</p>
	 * 
	 * @param message 消息
	 * @param address 地址
	 * @param haveAddress 是否含有地址
	 * 
	 * @throws NetException 网络异常
	 */
	protected void doNext(X message, InetSocketAddress address, boolean haveAddress) throws NetException {
		if(haveAddress) {
			if(this.messageCodec.done()) {
				this.messageCodec.onMessage(message, address);
			} else {
				this.messageCodec.decode(message, address);
			}
		} else {
			if(this.messageCodec.done()) {
				this.messageCodec.onMessage(message);
			} else {
				this.messageCodec.decode(message);
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated 消息最终处理器请实现{@linkplain IMessageCodec 消息处理器接口}
	 */
	@Override
	@Deprecated(since = "1.0.0")
	public void onMessage(T message) throws NetException {
		IMessageCodec.super.onMessage(message);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated 消息最终处理器请实现{@linkplain IMessageCodec 消息处理器接口}
	 */
	@Override
	@Deprecated(since = "1.0.0")
	public void onMessage(T message, InetSocketAddress address) throws NetException {
		IMessageCodec.super.onMessage(message, address);
	}
	
}
