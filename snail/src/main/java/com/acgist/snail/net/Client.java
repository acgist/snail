package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;

/**
 * <p>客户端</p>
 * <p>发送方法全部调用消息代理发送方法：否者子类重写将会导致发送失败</p>
 * 
 * @param <T> 消息代理类型
 * 
 * @author acgist
 */
public abstract class Client<T extends IMessageSender> implements IMessageSender {

	/**
	 * <p>客户端名称</p>
	 */
	protected final String name;
	/**
	 * <p>消息代理</p>
	 */
	protected final T handler;
	
	/**
	 * @param name 客户端名称
	 * @param handler 消息代理
	 */
	protected Client(String name, T handler) {
		this.name = name;
		this.handler = handler;
	}

	@Override
	public boolean available() {
		if(this.handler == null) {
			return false;
		} else {
			return this.handler.available();
		}
	}

	@Override
	public void send(String message) throws NetException {
		this.handler.send(message);
	}

	@Override
	public void send(String message, String charset) throws NetException {
		this.handler.send(message, charset);
	}
	
	@Override
	public void send(byte[] message) throws NetException {
		this.handler.send(message);
	}

	@Override
	public void send(ByteBuffer buffer) throws NetException {
		this.handler.send(buffer);
	}
	
	@Override
	public void send(ByteBuffer buffer, int timeout) throws NetException {
		this.handler.send(buffer, timeout);
	}
	
	@Override
	public InetSocketAddress remoteSocketAddress() {
		return this.handler.remoteSocketAddress();
	}

	@Override
	public void close() {
		if(this.handler != null) {
			this.handler.close();
		}
	}
	
}
