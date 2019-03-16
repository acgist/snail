package com.acgist.snail.net.tracker;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.AbstractUdpClient;
import com.acgist.snail.net.tracker.AbstractTrackerClient.Type;
import com.acgist.snail.net.tracker.impl.HttpTrackerClient;
import com.acgist.snail.net.tracker.impl.UdpTrackerClient;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.system.exception.NetException;

/**
 * tracker管理器
 */
public class TrackerClientManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerClientManager.class);
	private static final TrackerClientManager INSTANCE = new TrackerClientManager();
	
	private TrackerClientManager() {
		TRACKER_CLIENT_MAP = new HashMap<>();
	}

	public static final TrackerClientManager getInstance() {
		return INSTANCE;
	}
	
	private Map<Integer, AbstractTrackerClient> TRACKER_CLIENT_MAP;
	
	/**
	 * 获取tracker，如果已经存在返回tracker，否者注册tracker
	 */
	public AbstractTrackerClient tracker(String announceUrl) throws NetException {
		Optional<AbstractTrackerClient> optional = TRACKER_CLIENT_MAP.values().stream()
			.filter(client -> {
				return client.announceUrl().equals(announceUrl);
			}).findFirst();
		if(optional.isPresent()) {
			return optional.get();
		}
		AbstractTrackerClient client = buildClient(announceUrl);
		register(client);
		return client;
	}
	
	/**
	 * 设置udp的connectionId
	 */
	public void connectionId(int trackerId, long connectionId) {
		var client = TRACKER_CLIENT_MAP.get(trackerId);
		if(client != null && client.type == Type.udp) {
			UdpTrackerClient udpTrackerClient = (UdpTrackerClient) client;
			udpTrackerClient.connectionId(connectionId);
		}
	}

	/**
	 * 创建client
	 */
	private AbstractTrackerClient buildClient(String announceUrl) throws NetException {
		if(HttpProtocol.verify(announceUrl)) {
			return HttpTrackerClient.newInstance(announceUrl);
		} else if(AbstractUdpClient.verify(announceUrl)) {
			return UdpTrackerClient.newInstance(announceUrl);
		} else {
			throw new NetException("不支持的Tracker协议：" + announceUrl);
		}
	}
	
	/**
	 * client注册
	 */
	private void register(AbstractTrackerClient client) {
		LOGGER.info("注册tracker client，id：{}，announceUrl：{}", client.id, client.announceUrl());
		TRACKER_CLIENT_MAP.put(client.id(), client);
	}
	
}
