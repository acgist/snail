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
import com.acgist.snail.pojo.message.ScrapeMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>Tracker管理器</p>
 * <p>管理TrackerClient和TrackerLauncher</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TrackerManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerManager.class);
	
	private static final TrackerManager INSTANCE = new TrackerManager();

	/**
	 * 任务Tracker最大数量
	 */
	private static final int MAX_TRACKER_SIZE = SystemConfig.getTrackerSize();
	
	/**
	 * Tracker客户端Map：{@link TrackerClient#id()}=Tracker客户端
	 */
	private final Map<Integer, TrackerClient> trackerClients;
	/**
	 * Tracker执行器Map：{@link TrackerLauncher#id()}=Tracker执行器
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
	 * <p>新建TrackerLauncher</p>
	 */
	public TrackerLauncher newTrackerLauncher(TrackerClient client, TorrentSession torrentSession) {
		final TrackerLauncher launcher = TrackerLauncher.newInstance(client, torrentSession);
		this.trackerLaunchers.put(launcher.id(), launcher);
		return launcher;
	}
	
	/**
	 * <p>删除TrackerLauncher</p>
	 */
	public void release(Integer id) {
		this.trackerLaunchers.remove(id);
	}
	
	/**
	 * <p>处理announce信息</p>
	 */
	public void announce(AnnounceMessage message) {
		if(message == null) {
			return;
		}
		final Integer id = message.getId();
		final TrackerLauncher trackerLauncher = this.trackerLaunchers.get(id);
		if(trackerLauncher != null) {
			trackerLauncher.announce(message);
		} else {
			LOGGER.debug("TrackerLauncher不存在（AnnounceMessage）：{}", message);
		}
	}
	
	/**
	 * <p>处理scrape消息</p>
	 */
	public void scrape(ScrapeMessage message) {
		if(message == null) {
			return;
		}
		final Integer id = message.getId();
		final TrackerLauncher trackerLauncher = this.trackerLaunchers.get(id);
		if(trackerLauncher != null) {
			LOGGER.debug("Tracker刮檫消息：{}，做种：{}，完成：{}，下载：{}", id, message.getSeeder(), message.getCompleted(), message.getLeecher());	
		} else {
			LOGGER.debug("TrackerLauncher不存在（ScrapeMessage）：{}", message);
		}
	}
	
	/**
	 * <p>处理连接ID消息</p>
	 */
	public void connectionId(int trackerId, long connectionId) {
		final var client = this.trackerClients.get(trackerId);
		if(client != null && client.type() == Protocol.Type.UDP) {
			final UdpTrackerClient udpTrackerClient = (UdpTrackerClient) client;
			udpTrackerClient.connectionId(connectionId);
		}
	}
	
	/**
	 * @return TrackerClient列表拷贝
	 */
	public List<TrackerClient> clients() {
		return new ArrayList<>(this.trackerClients.values());
	}
	
	/**
	 * <p>默认不是私有种子</p>
	 * 
	 * @see #clients(String, List, boolean)
	 */
	public List<TrackerClient> clients(String announceUrl, List<String> announceUrls) throws DownloadException {
		return this.clients(announceUrl, announceUrls, false);
	}

	/**
	 * <p>获取可用的TrackerClient列表</p>
	 * <p>通过传入的声明地址获取TrackerClient，如果声明地址没有被注册为TrackerClient则自动注册。</p>
	 * <p>如果获取的数量不满足单个任务最大数量，并且不是私有种子时，将会使用系统的TrackerClient补充。</p>
	 * 
	 * @param announceUrl 声明地址
	 * @param announceUrls 声明地址集合
	 * @param privateTorrent 是否是私有种子
	 * 
	 * @return 可用的TrackerClient列表
	 */
	public List<TrackerClient> clients(String announceUrl, List<String> announceUrls, boolean privateTorrent) throws DownloadException {
		final List<TrackerClient> clients = register(announceUrl, announceUrls);
		if(privateTorrent) {
			LOGGER.debug("私有种子：不补充Tracker");
			return clients;
		}
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
	 * <p>补充TrackerClient</p>
	 * <p>补充权重最大的客户端</p>
	 * 
	 * @param size 补充数量
	 * @param clients 已有客户端
	 */
	private List<TrackerClient> clients(int size, List<TrackerClient> clients) {
		return this.trackerClients.values().stream()
			.filter(client -> {
				return client.available() && (clients != null && !clients.contains(client));
			})
			.sorted() // 排序
			.limit(size)
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>注册{@linkplain TrackerConfig#announces() 默认Tracker}</p>
	 */
	public List<TrackerClient> register() throws DownloadException {
		return register(TrackerConfig.getInstance().announces());
	}
	
	/**
	 * <p>注册TrackerClient</p>
	 * 
	 * @param announceUrl 声明地址
	 * @param announceUrls 声明地址集合
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
	 * <p>注册TrackerClient</p>
	 * 
	 * @param announceUrls 声明地址集合
	 */
	private List<TrackerClient> register(List<String> announceUrls) throws DownloadException {
		if(announceUrls == null) {
			announceUrls = new ArrayList<>();
		}
		synchronized (this.trackerClients) {
			return announceUrls.stream()
				.map(announceUrl -> announceUrl.trim())
				.map(announceUrl -> {
					try {
						return register(announceUrl);
					} catch (DownloadException e) {
						LOGGER.error("TrackerClient注册异常：{}", announceUrl, e);
					}
					return null;
				})
				.filter(client -> client != null)
				.filter(client -> client.available())
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * <p>注册TrackerClient</p>
	 * <p>如果已经注册直接返回</p>
	 * 
	 * @param announceUrl 声明地址
	 */
	private TrackerClient register(String announceUrl) throws DownloadException {
		if(StringUtils.isEmpty(announceUrl)) {
			return null;
		}
		final Optional<TrackerClient> optional = this.trackerClients.values().stream()
			.filter(client -> {
				return client.equals(announceUrl);
			}).findFirst();
		if(optional.isPresent()) {
			return optional.get();
		}
		final TrackerClient client = buildClientProxy(announceUrl);
		this.trackerClients.put(client.id(), client);
		LOGGER.debug("注册TrackerClient：ID：{}，AnnounceUrl：{}", client.id(), client.announceUrl());
		return client;
	}

	/**
	 * <p>创建TrackerClient代理</p>
	 * <p>如果第一次创建失败将链接使用URL解码后再次创建</p>
	 */
	private TrackerClient buildClientProxy(final String announceUrl) throws DownloadException {
		TrackerClient client = buildClient(announceUrl);
		if(client == null) {
			client = buildClient(UrlUtils.decode(announceUrl)); // URL解码
		}
		if(client == null) {
			throw new DownloadException("创建TrackerClient失败（Tracker协议不支持）：" + announceUrl);
		}
		return client;
	}

	/**
	 * <p>创建TrackerClient</p>
	 * 
	 * TODO：ws客户度
	 */
	private TrackerClient buildClient(final String announceUrl) throws DownloadException {
		if(Protocol.Type.HTTP.verify(announceUrl)) {
			try {
				return HttpTrackerClient.newInstance(announceUrl);
			} catch (NetException e) {
				throw new DownloadException(e);
			}
		} else if(Protocol.Type.UDP.verify(announceUrl)) {
			try {
				return UdpTrackerClient.newInstance(announceUrl);
			} catch (NetException e) {
				throw new DownloadException(e);
			}
		}
		return null;
	}
	
}
