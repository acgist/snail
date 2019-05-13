package com.acgist.snail.system.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bootstrap.TrackerLauncher;
import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.tracker.bootstrap.TrackerClient.Type;
import com.acgist.snail.net.tracker.bootstrap.impl.HttpTrackerClient;
import com.acgist.snail.net.tracker.bootstrap.impl.UdpTrackerClient;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * Tracker Client管理器，所有的tracker都可以被使用
 */
public class TrackerManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerManager.class);
	
	private static final TrackerManager INSTANCE = new TrackerManager();

	private static final int MAX_CLIENT_SIZE = SystemConfig.getTrackerSize();
	
	private final Map<Integer, TrackerClient> trackerClients;
	private final Map<Integer, TrackerLauncher> trackerLaunchers;
	
	private TrackerManager() {
		trackerClients = new ConcurrentHashMap<>();
		trackerLaunchers = new ConcurrentHashMap<>();
	}

	public static final TrackerManager getInstance() {
		return INSTANCE;
	}

	/**
	 * 所有的TrackerClient
	 */
	public List<TrackerClient> clients() {
		return new ArrayList<>(trackerClients.values());
	}
	
	/**
	 * 新建TrackerLauncher
	 */
	public TrackerLauncher newTrackerLauncher(TrackerClient client, TorrentSession torrentSession) {
		final TrackerLauncher launcher = TrackerLauncher.newInstance(client, torrentSession);
		trackerLaunchers.put(launcher.id(), launcher);
		return launcher;
	}
	
	/**
	 * 处理announce信息
	 */
	public void announce(final AnnounceMessage message) {
		if(message == null) {
			return;
		}
		final Integer id = message.getId();
		final TrackerLauncher trackerLauncher = trackerLaunchers.get(id);
		if(trackerLauncher != null) {
			trackerLauncher.announce(message);
		} else {
			LOGGER.warn("不存在的TrackerLauncher，AnnounceMessage：{}", message);
		}
	}
	
	/**
	 * 获取可用的tracker client，传入announce的返回有用的，然后补充不足的的数量
	 */
	public List<TrackerClient> clients(String announceUrl, List<String> announceUrls) throws DownloadException {
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
	private List<TrackerClient> clients(int size, List<TrackerClient> clients) {
		return trackerClients.values().stream()
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
	private List<TrackerClient> register(String announceUrl, List<String> announceUrls) throws DownloadException {
		final List<String> announces = new ArrayList<>();
		if(StringUtils.isNotEmpty(announceUrl)) {
			announces.add(announceUrl);
		}
		if(CollectionUtils.isNotEmpty(announceUrls)) {
			announces.addAll(announceUrls);
		}
		if(announces.size() == 0) {
			throw new DownloadException("announceUrl不合法");
		}
		return announces.stream()
		.map(announce -> {
			try {
				return register(announce);
			} catch (DownloadException e) {
				LOGGER.error("Tracker注册异常：{}", announce, e);
			}
			return null;
		})
		.filter(client -> client != null)
		.filter(client -> client.available())
		.collect(Collectors.toList());
	}

	/**
	 * 注册Tracker Client，如果已经注册直接返回
	 */
	private TrackerClient register(String announceUrl) throws DownloadException {
		if(StringUtils.isEmpty(announceUrl)) {
			return null;
		}
		synchronized (trackerClients) {
			final Optional<TrackerClient> optional = trackerClients.values().stream()
				.filter(client -> {
					return client.exist(announceUrl);
				}).findFirst();
			if(optional.isPresent()) {
				return optional.get();
			}
			final TrackerClient client = buildClientProxy(announceUrl);
			LOGGER.debug("注册Tracker Client，ID：{}，announceUrl：{}", client.id(), client.announceUrl());
			trackerClients.put(client.id(), client);
			return client;
		}
	}

	/**
	 * 设置udp的connectionId
	 */
	public void connectionId(int trackerId, long connectionId) {
		var client = trackerClients.get(trackerId);
		if(client != null && client.type() == Type.udp) {
			final UdpTrackerClient udpTrackerClient = (UdpTrackerClient) client;
			udpTrackerClient.connectionId(connectionId);
		}
	}

	/**
	 * 创建Client
	 */
	private TrackerClient buildClientProxy(final String announceUrl) throws DownloadException {
		TrackerClient client = buildClient(announceUrl);
		if(client == null) {
			client = buildClient(UrlUtils.decode(announceUrl));
		}
		if(client == null) {
			throw new DownloadException("不支持的Tracker协议：" + announceUrl);
		}
		return client;
	}
	
	private TrackerClient buildClient(final String announceUrl) throws DownloadException {
		if(HttpProtocol.verify(announceUrl)) {
			try {
				return HttpTrackerClient.newInstance(announceUrl);
			} catch (NetException e) {
				throw new DownloadException(e);
			}
		} else if(UdpClient.verify(announceUrl)) {
			try {
				return UdpTrackerClient.newInstance(announceUrl);
			} catch (NetException e) {
				throw new DownloadException(e);
			}
		}
		return null;
	}
	
}
