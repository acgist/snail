package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.context.exception.NetException;

/**
 * <p>消息接收代理接口</p>
 * 
 * @author acgist
 */
public interface IMessageReceiver {

	/**
	 * <p>消息接收</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 */
	default void onReceive(ByteBuffer buffer) throws NetException {
	}
	
	/**
	 * <p>收到消息</p>
	 * 
	 * @param buffer 消息
	 * @param socketAddress 地址
	 * 
	 * @throws NetException 网络异常
	 */
	default void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
	}
	
}
