package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * <p>消息接收代理接口</p>
 * 
 * @author acgist
 */
public interface IMessageReceiver {

	/**
	 * <p>判断是否没有使用</p>
	 * 
	 * @return 是否没有使用
	 */
	default boolean useless() {
		return false;
	}
	
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
