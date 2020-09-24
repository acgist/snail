package com.acgist.snail.net.codec.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>字符串消息处理器</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class StringMessageCodec extends MessageCodec<ByteBuffer, String> {

	public StringMessageCodec(IMessageCodec<String> messageCodec) {
		super(messageCodec);
	}

	@Override
	protected void decode(ByteBuffer buffer, InetSocketAddress address, boolean haveAddress) throws NetException {
		final String message = StringUtils.ofByteBuffer(buffer); // 读取字符串
		this.doNext(message, address, haveAddress);
	}

}
