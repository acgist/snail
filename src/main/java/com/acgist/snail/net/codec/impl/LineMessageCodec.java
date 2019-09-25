package com.acgist.snail.net.codec.impl;

import java.net.InetSocketAddress;

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>行消息</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public class LineMessageCodec extends MessageCodec<String, String> {

	/**
	 * 换行符
	 */
	private final String split;
	
	public LineMessageCodec(String split, IMessageCodec<String> messageCodec) {
		super(messageCodec);
		this.split = split;
	}

	@Override
	protected void decode(String messages, InetSocketAddress address, boolean hasAddress) throws NetException {
		String message;
		final int length = this.split.length();
		if(messages.contains(this.split)) {
			int index = messages.indexOf(this.split);
			while(index >= 0) {
				message = messages.substring(0, index);
				this.doNext(message, address, hasAddress);
				messages = messages.substring(index + length);
				index = messages.indexOf(this.split);
			}
		}
	}
	
	@Override
	public String encode(String message) {
		return this.messageCodec.encode(message) + this.split;
	}
	
}
