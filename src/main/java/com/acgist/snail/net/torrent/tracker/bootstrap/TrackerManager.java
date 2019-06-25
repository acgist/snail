package com.acgist.snail.net.torrent.tracker.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.bootstrap.TrackerLauncher;
import com.acgist.snail.net.torrent.tracker.bootstrap.impl.HttpTrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.impl.UdpTrackerClient;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.system.config.ProtocolConfig;
import com.acgist.snail.system.config.ProtocolConfig.Protocol;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>Tracker管理器</p>
 * <p>管理TrackerClient和TrackerLauncher。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerManager.class);
	
	private static final TrackerManager INSTANCE = new TrackerManager();

	private static final int MAX_TRACKER_SIZE = SystemConfig.getTrackerSize();
	
	/**
	 * Tracker客户端，key={@link TrackerClient#id()}。
	 */
	private final Map<Integer, TrackerClient> trackerClients;
	/**
	 * Tracker执行器，key={@link TrackerLauncher#id()}。
	 */
	private final Map<Integer, TrackerLauncher> trackerLaunchers;
	
	private TrackerManager() {
		this.trackerClients = new ConcurrentHashMap<>();
		this.trackerLaunchers = new ConcurrentHashMap<>();
	}

	public static final TrackerManager getInstance() {
		return INSTANCE;
	}

	/**
	 * 新建TrackerLauncher
	 */
	public TrackerLauncher newTrackerLauncher(TrackerClient client, TorrentSession torrentSession) {
		final TrackerLauncher launcher = TrackerLauncher.newInstance(client, torrentSession);
		this.trackerLaunchers.put(launcher.id(), launcher);
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
		final TrackerLauncher trackerLauncher = this.trackerLaunchers.get(id);
		if(trackerLauncher != null) {
			trackerLauncher.announce(message);
		} else {
			LOGGER.warn("不存在的TrackerLauncher，AnnounceMessage：{}", message);
		}
	}
	
	/**
	 * 删除TrackerLauncher
	 */
	public void release(Integer id) {
		this.trackerLaunchers.remove(id);
	}
	
	/**
	 * 所有的TrackerClient
	 */
	public List<TrackerClient> clients() {
		return new ArrayList<>(this.trackerClients.values());
	}

	/**
	 * 获取可用的TrackerClient，传入announce的返回有用的，然后补充不足的的数量。
	 */
	public List<TrackerClient> clients(String announceUrl, List<String> announceUrls) throws DownloadException {
		final List<TrackerClient> clients = register(announceUrl, announceUrls);
		final int size = clients.size();
		if(size < MAX_TRACKER_SIZE) {
			final var subjoin = clients(MAX_TRACKER_SIZE - size, clients);
			if(!subjoin.isEmpty()) {
				clients.addAll(subjoin);
			}
		}
		return clients;
	}
	
	/**
	 * 获取可用的Tracker Client，获取权重排在前面的Tracker Client。
	 * 
	 * @param size 返回可用client数量
	 * @param clients 已有的Client
	 */
	private List<TrackerClient> clients(int size, List<TrackerClient> clients) {
		return this.trackerClients.values().stream()
			.filter(client -> {
				return client.available() && (clients != null && !clients.contains(client));
			})
			.sorted()
			.limit(size)
			.collect(Collectors.toList());
	}
	
	/**
	 * 设置udp的connectionId
	 */
	public void connectionId(int trackerId, long connectionId) {
		final var client = this.trackerClients.get(trackerId);
		if(client != null && client.type() == Protocol.udp) {
			final UdpTrackerClient udpTrackerClient = (UdpTrackerClient) client;
			udpTrackerClient.connectionId(connectionId);
		}
	}

	/**
	 * 注册{@link TrackerConfig}配置的默认Tracker。
	 */
	public List<TrackerClient> register() throws DownloadException {
		return register(TrackerConfig.getInstance().announces());
	}
	
	/**
	 * 注册TrackerClient。
	 */
	private List<TrackerClient> register(String announceUrl, List<String> announceUrls) throws DownloadException {
		final List<String> announces = new ArrayList<>();
		if(StringUtils.isNotEmpty(announceUrl)) {
			announces.add(announceUrl);
		}
		if(CollectionUtils.isNotEmpty(announceUrls)) {
			announces.addAll(announceUrls);
		}
		return register(announces);
	}

	/**
	 * 注册TrackerClient。
	 */
	private List<TrackerClient> register(List<String> announces) throws DownloadException {
		if(CollectionUtils.isEmpty(announces)) {
			throw new DownloadException("AnnounceUrl不合法");
		}
		return announces.stream()
			.map(announce -> announce.trim())
			.map(announce -> {
				try {
					return register(announce);
				} catch (DownloadException e) {
					LOGGER.error("TrackerClient注册异常：{}", announce, e);
				}
				return null;
			})
			.filter(client -> client != null)
			.filter(client -> client.available())
			.collect(Collectors.toList());
	}
	
	/**
	 * 注册TrackerClient，如果已经注册直接返回。
	 */
	private TrackerClient register(String announceUrl) throws DownloadException {
		if(StringUtils.isEmpty(announceUrl)) {
			return null;
		}
		synchronized (this.trackerClients) {
			final Optional<TrackerClient> optional = this.trackerClients.values().stream()
				.filter(client -> {
					return client.equals(announceUrl);
				}).findFirst();
			if(optional.isPresent()) {
				return optional.get();
			}
			final TrackerClient client = buildClientProxy(announceUrl);
			this.trackerClients.put(client.id(), client);
			LOGGER.debug("注册TrackerClient，ID：{}，AnnounceUrl：{}", client.id(), client.announceUrl());
			return client;
		}
	}

	/**
	 * 创建Client代理，如果第一次创建失败将链接使用URL解码后再次创建。
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

	/**
	 * 创建Client
	 */
	// TODO:wss
	private TrackerClient buildClient(final String announceUrl) throws DownloadException {
		if(HttpProtocol.verify(announceUrl)) {
			try {
				return HttpTrackerClient.newInstance(announceUrl);
			} catch (NetException e) {
				throw new DownloadException(e);
			}
		} else if(ProtocolConfig.verifyUdp(announceUrl)) {
			try {
				return UdpTrackerClient.newInstance(announceUrl);
			} catch (NetException e) {
				throw new DownloadException(e);
			}
		}
		return null;
	}
	
}
