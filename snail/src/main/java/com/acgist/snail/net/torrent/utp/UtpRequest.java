package com.acgist.snail.net.torrent.utp;

import java.nio.ByteBuffer;

import com.acgist.snail.net.NetException;
import com.acgist.snail.net.codec.IMessageDecoder;

/**
 * <p>UTP请求</p>
 * 
 * @author acgist
 */
public final record UtpRequest(
	/**
	 * <p>请求数据</p>
	 */
	ByteBuffer buffer,
	/**
	 * <p>消息处理器</p>
	 */
	IMessageDecoder<ByteBuffer> messageDecoder
) {

	/**
	 * <p>新建UTP请求</p>
	 * 
	 * @param buffer 请求数据
	 * @param messageDecoder 消息处理器
	 * 
	 * @return {@link UtpRequest}
	 */
	public static final UtpRequest newInstance(ByteBuffer buffer, IMessageDecoder<ByteBuffer> messageDecoder) {
		return new UtpRequest(buffer, messageDecoder);
	}
	
	/**
	 * <p>处理请求</p>
	 * 
	 * @throws NetException 网络异常
	 */
	public void execute() throws NetException {
		this.messageDecoder.decode(this.buffer);
	}
	
}
