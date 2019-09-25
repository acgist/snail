package com.acgist.snail.net.codec.impl;

import java.net.InetSocketAddress;

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>多行消息</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class MultilineMessageCodec extends MessageCodec<String, String> {

	/**
	 * 换行符
	 */
	private final String split;
	/**
	 * 多行结束符
	 */
	private final String endRegex;
	private final StringBuffer message;
	
	public MultilineMessageCodec(String split, String endRegex, IMessageCodec<String> messageCodec) {
		super(messageCodec);
		this.split = split;
		this.endRegex = endRegex;
		this.message = new StringBuffer();
	}

	@Override
	protected void decode(String message, InetSocketAddress address, boolean hasAddress) throws NetException {
		if(message.matches(this.endRegex)) {
			this.message.append(message);
			this.doNext(this.message.toString(), address, hasAddress);
			this.message.setLength(0);
		} else {
			this.message.append(message).append(this.split);
		}
	}

}
