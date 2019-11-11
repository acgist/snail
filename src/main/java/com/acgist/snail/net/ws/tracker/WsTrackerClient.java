package com.acgist.snail.net.ws.tracker;

import com.acgist.snail.net.ws.WebSocketClient;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * WebSocketTracekr客户端
 * 
 * @author acgist
 * @since 1.2.0
 */
public class WsTrackerClient extends WebSocketClient<WsTrackerMessageHandler> {

	private WsTrackerClient(String url, int connectTimeout, int receiveTimeout) throws NetException {
		super(url, connectTimeout, receiveTimeout, new WsTrackerMessageHandler());
	}
	
	public static final WsTrackerClient newInstance(String url) throws NetException {
		return new WsTrackerClient(url, SystemConfig.CONNECT_TIMEOUT, SystemConfig.RECEIVE_TIMEOUT);
	}

}
