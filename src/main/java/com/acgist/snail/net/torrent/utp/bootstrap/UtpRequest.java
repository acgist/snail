package com.acgist.snail.net.torrent.utp.bootstrap;

import java.nio.ByteBuffer;

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.system.exception.NetException;

/**
 * UTP请求
 * 
 * @author acgist
 * @since 1.2.0
 */
public class UtpRequest {

	private final UtpWindowData windowData;
	private final IMessageCodec<ByteBuffer> messageCodec;
	
	private UtpRequest(UtpWindowData windowData, IMessageCodec<ByteBuffer> messageCodec) {
		this.windowData = windowData;
		this.messageCodec = messageCodec;
	}
	
	public static final UtpRequest newInstance(UtpWindowData windowData, IMessageCodec<ByteBuffer> messageCodec) {
		return new UtpRequest(windowData, messageCodec);
	}
	
	/**
	 * 执行请求
	 */
	public void execute() throws NetException {
		this.messageCodec.decode(this.windowData.buffer());
	}
	
}
