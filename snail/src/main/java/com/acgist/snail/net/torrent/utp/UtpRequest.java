package com.acgist.snail.net.torrent.utp;

import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.codec.IMessageCodec;

/**
 * <p>UTP请求</p>
 * 
 * @author acgist
 */
public final class UtpRequest {

	/**
	 * <p>请求数据</p>
	 */
	private final ByteBuffer buffer;
	/**
	 * <p>消息处理器</p>
	 */
	private final IMessageCodec<ByteBuffer> messageCodec;
	
	/**
	 * @param buffer 请求数据
	 * @param messageCodec 消息处理器
	 */
	private UtpRequest(ByteBuffer buffer, IMessageCodec<ByteBuffer> messageCodec) {
		this.buffer = buffer;
		this.messageCodec = messageCodec;
	}
	
	/**
	 * <p>创建UTP请求</p>
	 * 
	 * @param buffer 请求数据
	 * @param messageCodec 消息处理器
	 * 
	 * @return UTP请求
	 */
	public static final UtpRequest newInstance(ByteBuffer buffer, IMessageCodec<ByteBuffer> messageCodec) {
		return new UtpRequest(buffer, messageCodec);
	}
	
	/**
	 * <p>处理请求</p>
	 * 
	 * @throws NetException 网络异常
	 */
	public void execute() throws NetException {
		this.messageCodec.decode(this.buffer);
	}
	
}
