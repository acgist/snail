package com.acgist.snail.net.torrent.utp.bootstrap;

import java.nio.ByteBuffer;

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>UTP请求</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class UtpRequest {

	/**
	 * 窗口数据
	 */
	private final UtpWindowData windowData;
	/**
	 * 消息处理
	 */
	private final IMessageCodec<ByteBuffer> messageCodec;
	
	private UtpRequest(UtpWindowData windowData, IMessageCodec<ByteBuffer> messageCodec) {
		this.windowData = windowData;
		this.messageCodec = messageCodec;
	}
	
	public static final UtpRequest newInstance(UtpWindowData windowData, IMessageCodec<ByteBuffer> messageCodec) {
		return new UtpRequest(windowData, messageCodec);
	}
	
	/**
	 * <p>执行请求</p>
	 * 
	 * @throws NetException 网络异常
	 */
	public void execute() throws NetException {
		this.messageCodec.decode(this.windowData.buffer());
	}
	
}
