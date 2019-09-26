package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.system.exception.NetException;

/**
 * <p>Client消息代理适配器</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class ClientMessageHandlerAdapter<T extends IMessageHandler> implements IMessageHandler {

	/**
	 * 消息代理
	 */
	protected final T handler;
	
	protected ClientMessageHandlerAdapter(T handler) {
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
