package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * <p>消息处理器适配器</p>
 * 
 * @param <T> 输入类型
 * @param <X> 输出类型
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
	 * <p>必须调用{@link #doNext(Object, InetSocketAddress, boolean)}执行{@linkplain #messageCodec 下一个消息处理器}</p>
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
	
	@Override
	public String encode(String message) {
		return this.messageCodec.encode(message);
	}
	
	@Override
	public void encode(ByteBuffer message) {
		this.messageCodec.encode(message);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>消息最终处理器请实现{@linkplain IMessageCodec 消息处理器接口}</p>
	 */
	@Override
	@Deprecated
	public void onMessage(T message) throws NetException {
		IMessageCodec.super.onMessage(message);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>消息最终处理器请实现{@linkplain IMessageCodec 消息处理器接口}</p>
	 */
	@Override
	@Deprecated
	public void onMessage(T message, InetSocketAddress address) throws NetException {
		IMessageCodec.super.onMessage(message, address);
	}
	
}
