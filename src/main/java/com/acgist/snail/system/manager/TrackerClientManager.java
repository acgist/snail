package com.acgist.snail.system.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.AbstractUdpClient;
import com.acgist.snail.net.tracker.AbstractTrackerClient;
import com.acgist.snail.net.tracker.AbstractTrackerClient.Type;
import com.acgist.snail.net.tracker.impl.HttpTrackerClient;
import com.acgist.snail.net.tracker.impl.UdpTrackerClient;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * Tracker Client管理器，所有的tracker都可以被使用
 */
public class TrackerClientManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerClientManager.class);
	private static final TrackerClientManager INSTANCE = new TrackerClientManager();

	private static final int MAX_CLIENT_SIZE = 40; // TODO：默认返回的数量
	
	private TrackerClientManager() {
		TRACKER_CLIENT_MAP = new ConcurrentHashMap<>();
	}

	public static final TrackerClientManager getInstance() {
		return INSTANCE;
	}

	private Map<Integer, AbstractTrackerClient> TRACKER_CLIENT_MAP;

	/**
	 * 获取可用的tracker client，传入announce的返回有用的，然后补充不足的的数量
	 */
	public List<AbstractTrackerClient> clients(String announceUrl, List<String> announceUrls) throws NetException {
		List<AbstractTrackerClient> clients = register(announceUrl, announceUrls);
		final int size = clients.size();
		if(size < MAX_CLIENT_SIZE) {
			clients.addAll(clients(MAX_CLIENT_SIZE - size));
		}
		return clients;
	}
	
	/**
	 * 获取可用的tracker client，获取权重排在前面的tracker client
	 * @param size 返回可用client数量
	 */
	public List<AbstractTrackerClient> clients(int size) {
		return TRACKER_CLIENT_MAP.values().stream()
			.filter(client -> client.available())
			.sorted()
			.limit(size)
			.collect(Collectors.toList());
	}
	
	/**
	 * 注册tracker client列表
	 */
	public List<AbstractTrackerClient> register(String announceUrl, List<String> announceUrls) throws NetException {
		final List<String> announces = new ArrayList<>();
		if(StringUtils.isNotEmpty(announceUrl)) {
			announces.add(announceUrl);
		}
		if(CollectionUtils.isNotEmpty(announceUrls)) {
			announces.addAll(announceUrls);
		}
		if(announces.size() == 0) {
			throw new NetException("announceUrl不合法");
		}
		return announces.stream()
		.map(announce -> {
			try {
				return register(announce);
			} catch (NetException e) {
				LOGGER.error("tracker获取异常", e);
			}
			return null;
		})
		.filter(client -> client != null)
		.filter(client -> client.available())
		.collect(Collectors.toList());
	}

	/**
	 * 注册tracker client，如果已经注册直接返回
	 */
	public AbstractTrackerClient register(String announceUrl) throws NetException {
		if(StringUtils.isEmpty(announceUrl)) {
			return null;
		}
		synchronized (TRACKER_CLIENT_MAP) {
			Optional<AbstractTrackerClient> optional = TRACKER_CLIENT_MAP.values().stream()
				.filter(client -> {
					return client.exist(announceUrl);
				}).findFirst();
			if(optional.isPresent()) {
				return optional.get();
			}
			AbstractTrackerClient client = buildClient(announceUrl);
			register(client);
			return client;
		}
	}

	/**
	 * 设置udp的connectionId
	 */
	public void connectionId(int trackerId, long connectionId) {
		var client = TRACKER_CLIENT_MAP.get(trackerId);
		if(client != null && client.type() == Type.udp) {
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
		LOGGER.info("注册tracker client，id：{}，announceUrl：{}", client.id(), client.announceUrl());
		TRACKER_CLIENT_MAP.put(client.id(), client);
	}

}
