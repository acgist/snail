package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>字符串消息处理器</p>
 * 
 * @author acgist
 */
public final class StringMessageCodec extends MessageCodec<ByteBuffer, String> {

	/**
	 * @param messageDecoder 消息处理器
	 */
	public StringMessageCodec(IMessageDecoder<String> messageDecoder) {
		super(messageDecoder);
	}

	@Override
	protected void doDecode(ByteBuffer buffer, InetSocketAddress address) throws NetException {
		final String message = StringUtils.ofByteBuffer(buffer);
		this.doNext(message, address);
	}

}
