package com.acgist.snail.system.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.tracker.TrackerClient;
import com.acgist.snail.net.tracker.TrackerClient.Type;
import com.acgist.snail.net.tracker.impl.HttpTrackerClient;
import com.acgist.snail.net.tracker.impl.UdpTrackerClient;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * Tracker Client管理器，所有的tracker都可以被使用
 */
public class TrackerClientManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerClientManager.class);
	
	private static final TrackerClientManager INSTANCE = new TrackerClientManager();

	private static final int MAX_CLIENT_SIZE = SystemConfig.getTrackerSize();
	
	private Map<Integer, TrackerClient> TRACKER_CLIENT_MAP;
	
	private TrackerClientManager() {
		TRACKER_CLIENT_MAP = new ConcurrentHashMap<>();
	}

	public static final TrackerClientManager getInstance() {
		return INSTANCE;
	}

	/**
	 * 获取可用的tracker client，传入announce的返回有用的，然后补充不足的的数量
	 */
	public List<TrackerClient> clients(String announceUrl, List<String> announceUrls) throws NetException {
		final List<TrackerClient> clients = register(announceUrl, announceUrls);
		final int size = clients.size();
		if(size < MAX_CLIENT_SIZE) {
			final var subjoin = clients(MAX_CLIENT_SIZE - size, clients);
			if(!subjoin.isEmpty()) {
				clients.addAll(subjoin);
			}
		}
		return clients;
	}
	
	public List<TrackerClient> clients(int size) {
		return clients(size, null);
	}
	
	/**
	 * 获取可用的tracker client，获取权重排在前面的tracker client
	 * @param size 返回可用client数量
	 * @param clients 已有的Client
	 */
	public List<TrackerClient> clients(int size, List<TrackerClient> clients) {
		return TRACKER_CLIENT_MAP.values().stream()
			.filter(client -> {
				return client.available() && (clients != null && !clients.contains(client));
			})
			.sorted()
			.limit(size)
			.collect(Collectors.toList());
	}
	
	/**
	 * 注册tracker client列表
	 */
	public List<TrackerClient> register(String announceUrl, List<String> announceUrls) throws NetException {
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
	public TrackerClient register(String announceUrl) throws NetException {
		if(StringUtils.isEmpty(announceUrl)) {
			return null;
		}
		synchronized (TRACKER_CLIENT_MAP) {
			Optional<TrackerClient> optional = TRACKER_CLIENT_MAP.values().stream()
				.filter(client -> {
					return client.exist(announceUrl);
				}).findFirst();
			if(optional.isPresent()) {
				return optional.get();
			}
			TrackerClient client = buildClientProxy(announceUrl);
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
	 * 创建Client
	 */
	private TrackerClient buildClientProxy(final String announceUrl) throws NetException {
		TrackerClient client = buildClient(announceUrl);
		if(client == null) {
			client = buildClient(UrlUtils.decode(announceUrl));
		}
		if(client == null) {
			throw new NetException("不支持的Tracker协议：" + announceUrl);
		}
		return client;
	}
	
	private TrackerClient buildClient(final String announceUrl) throws NetException {
		if(HttpProtocol.verify(announceUrl)) {
			return HttpTrackerClient.newInstance(announceUrl);
		} else if(UdpClient.verify(announceUrl)) {
			return UdpTrackerClient.newInstance(announceUrl);
		}
		return null;
	}
	
	/**
	 * client注册
	 */
	private void register(TrackerClient client) {
		LOGGER.debug("注册Tracker Client，ID：{}，announceUrl：{}", client.id(), client.announceUrl());
		TRACKER_CLIENT_MAP.put(client.id(), client);
	}

}
