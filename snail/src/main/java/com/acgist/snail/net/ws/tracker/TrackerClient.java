package com.acgist.snail.net.ws.tracker;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.ws.WebSocketClient;

/**
 * <p>Tracker Client（WebSocket）</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class TrackerClient extends WebSocketClient<TrackerMessageHandler> {

	/**
	 * <p>Tracker客户端</p>
	 * 
	 * @param url Tracker地址
	 * @param connectTimeout 连接超时时间
	 * @param receiveTimeout 接收超时时间
	 * 
	 * @throws NetException 网络异常
	 */
	private TrackerClient(String url, int connectTimeout, int receiveTimeout) throws NetException {
		super(url, connectTimeout, receiveTimeout, new TrackerMessageHandler());
	}
	
	/**
	 * <p>创建Tracker客户端</p>
	 * 
	 * @param url Tracker地址
	 * 
	 * @return Tracker客户端
	 * 
	 * @throws NetException 网络异常
	 */
	public static final TrackerClient newInstance(String url) throws NetException {
		return new TrackerClient(url, SystemConfig.CONNECT_TIMEOUT, SystemConfig.RECEIVE_TIMEOUT);
	}

}
