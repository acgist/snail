package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;

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
 * @param <I> 输入消息泛型
 * @param <O> 输出消息泛型
 * 
 * @author acgist
 */
public abstract class MessageCodec<I, O> implements IMessageCodec<I> {

	/**
	 * <p>下一个消息处理器</p>
	 */
	protected final IMessageCodec<O> messageCodec;

	/**
	 * @param messageCodec 下一个消息处理器
	 */
	protected MessageCodec(IMessageCodec<O> messageCodec) {
		this.messageCodec = messageCodec;
	}

	@Override
	public final boolean done() {
		return false;
	}
	
	@Override
	public String encode(String message) {
		return this.messageCodec.encode(message);
	}
	
	@Override
	public ByteBuffer encode(ByteBuffer message) {
		return this.messageCodec.encode(message);
	}
	
	@Override
	public final void decode(I message) throws NetException {
		this.doDecode(message, null);
	}

	@Override
	public final void decode(I message, InetSocketAddress address) throws NetException {
		this.doDecode(message, address);
	}
	
	/**
	 * <p>消息解码</p>
	 * <p>解码完成必须执行{@link #doNext(Object, InetSocketAddress)}方法</p>
	 * 
	 * @param message 消息
	 * @param address 地址
	 * 
	 * @throws NetException 网络异常
	 */
	protected abstract void doDecode(I message, InetSocketAddress address) throws NetException;
	
	/**
	 * <p>执行下一个消息处理器</p>
	 * 
	 * @param message 消息
	 * @param address 地址
	 * 
	 * @throws NetException 网络异常
	 */
	protected void doNext(O message, InetSocketAddress address) throws NetException {
		if(address != null) {
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
	 * <p>消息最终处理器请实现{@linkplain IMessageCodec 消息处理器接口}</p>
	 */
	@Override
	public final void onMessage(I message) throws NetException {
		throw new NetException("请实现消息处理器接口");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>消息最终处理器请实现{@linkplain IMessageCodec 消息处理器接口}</p>
	 */
	@Override
	public final void onMessage(I message, InetSocketAddress address) throws NetException {
		throw new NetException("请实现消息处理器接口");
	}
	
}
