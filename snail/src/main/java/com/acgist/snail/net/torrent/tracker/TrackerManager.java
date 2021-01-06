package com.acgist.snail.net.torrent.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.message.ScrapeMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.session.TrackerSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>Tracker管理器</p>
 * <p>管理TrackerSession和TrackerLauncher</p>
 * 
 * @author acgist
 */
public final class TrackerManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerManager.class);
	
	private static final TrackerManager INSTANCE = new TrackerManager();
	
	public static final TrackerManager getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>任务Tracker最大数量</p>
	 */
	private static final int MAX_TRACKER_SIZE = SystemConfig.getTrackerSize();
	
	/**
	 * <p>TrackerSession Map</p>
	 * <p>{@link TrackerSession#id()}=Tracker信息</p>
	 */
	private final Map<Integer, TrackerSession> trackerSessions;
	/**
	 * <p>TrackerLauncher Map</p>
	 * <p>{@link TrackerLauncher#id()}=Tracker执行器</p>
	 */
	private final Map<Integer, TrackerLauncher> trackerLaunchers;
	
	private TrackerManager() {
		this.trackerSessions = new ConcurrentHashMap<>();
		this.trackerLaunchers = new ConcurrentHashMap<>();
		this.register();
	}

	/**
	 * <p>新建TrackerLauncher</p>
	 * 
	 * @param trackerSession TrackerSession
	 * @param torrentSession BT任务信息
	 * 
	 * @return TrackerLauncher
	 */
	public TrackerLauncher buildTrackerLauncher(TrackerSession trackerSession, TorrentSession torrentSession) {
		final TrackerLauncher launcher = TrackerLauncher.newInstance(trackerSession, torrentSession);
		LOGGER.debug("加载TrackerLauncher：{}-{}，announceUrl：{}", launcher.id(), trackerSession.id(), trackerSession.announceUrl());
		this.trackerLaunchers.put(launcher.id(), launcher);
		return launcher;
	}
	
	/**
	 * <p>删除TrackerLauncher</p>
	 * 
	 * @param id {@link TrackerLauncher#id()}
	 */
	public void removeTrackerLauncher(Integer id) {
		LOGGER.debug("删除TrackerLauncher：{}", id);
		this.trackerLaunchers.remove(id);
	}
	
	/**
	 * <p>处理announce信息</p>
	 * 
	 * @param message 消息
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
	 * 
	 * @param message 消息
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
	 * 
	 * @param trackerId {@link TrackerSession#id()}
	 * @param connectionId 连接ID
	 */
	public void connectionId(int trackerId, long connectionId) {
		final var trackerSession = this.trackerSessions.get(trackerId);
		// 只有UDP Tracker需要获取连接ID
		if(
			trackerSession != null &&
			trackerSession instanceof UdpTrackerSession &&
			trackerSession.type() == Protocol.Type.UDP
		) {
			// TODO：新强转写法
			final UdpTrackerSession udpTrackerSession = (UdpTrackerSession) trackerSession;
			udpTrackerSession.connectionId(connectionId);
		}
	}
	
	/**
	 * <p>获取TrackerSession列表</p>
	 * 
	 * @return TrackerSession列表
	 */
	public List<TrackerSession> sessions() {
		return new ArrayList<>(this.trackerSessions.values());
	}
	
	/**
	 * <p>获取TrackerSession列表</p>
	 * 
	 * @param announceUrl 声明地址
	 * 
	 * @return TrackerSession列表
	 * 
	 * @throws DownloadException 下载异常
	 * 
	 * @see #sessions(String, List)
	 */
	public List<TrackerSession> sessions(String announceUrl) throws DownloadException {
		return this.sessions(announceUrl, null);
	}
	
	/**
	 * <p>获取TrackerSession列表</p>
	 * 
	 * @param announceUrls 声明地址集合
	 * 
	 * @return TrackerSession列表
	 * 
	 * @throws DownloadException 下载异常
	 * 
	 * @see #sessions(String, List)
	 */
	public List<TrackerSession> sessions(List<String> announceUrls) throws DownloadException {
		return this.sessions(null, announceUrls);
	}
	
	/**
	 * <p>获取TrackerSession列表</p>
	 * 
	 * @param announceUrl 声明地址
	 * @param announceUrls 声明地址集合
	 * 
	 * @return TrackerSession列表
	 * 
	 * @throws DownloadException 下载异常
	 * 
	 * @see #sessions(String, List, boolean)
	 */
	public List<TrackerSession> sessions(String announceUrl, List<String> announceUrls) throws DownloadException {
		return this.sessions(announceUrl, announceUrls, false);
	}

	/**
	 * <p>获取TrackerSession列表</p>
	 * <p>非私有种子如果TrackerSession列表长度不足则使用系统TrackerSession填充</p>
	 * 
	 * @param announceUrl 声明地址
	 * @param announceUrls 声明地址集合
	 * @param privateTorrent 是否是私有种子
	 * 
	 * @return TrackerSession列表
	 * 
	 * @throws DownloadException 下载异常
	 */
	public List<TrackerSession> sessions(String announceUrl, List<String> announceUrls, boolean privateTorrent) throws DownloadException {
		final List<TrackerSession> sessions = this.buildTrackerSession(announceUrl, announceUrls);
		if(privateTorrent) {
			LOGGER.debug("私有种子：不补充Tracker");
			return sessions;
		}
		final int size = sessions.size();
		if(size < MAX_TRACKER_SIZE) {
			final var subjoin = this.sessions(MAX_TRACKER_SIZE - size, sessions);
			if(!subjoin.isEmpty()) {
				sessions.addAll(subjoin);
			}
		}
		return sessions;
	}
	
	/**
	 * <p>补充TrackerSession</p>
	 * <p>补充权重最大的客户端</p>
	 * 
	 * @param size 补充数量
	 * @param sessions 已有客户端
	 * 
	 * @return TrackerSession列表
	 */
	private List<TrackerSession> sessions(int size, List<TrackerSession> sessions) {
		return this.trackerSessions.values().stream()
			.filter(client -> client.available() && (sessions != null && !sessions.contains(client)))
			.sorted() // 排序
			.limit(size)
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>注册默认Tracker</p>
	 * 
	 * @return TrackerSession列表
	 */
	private void register() {
		try {
			this.buildTrackerSession(TrackerConfig.getInstance().announces());
		} catch (DownloadException e) {
			LOGGER.error("注册默认Tracker异常", e);
		}
	}
	
	/**
	 * <p>创建TrackerSession列表</p>
	 * 
	 * @param announceUrl 声明地址
	 * @param announceUrls 声明地址集合
	 * 
	 * @return TrackerSession列表
	 * 
	 * @throws DownloadException 下载异常
	 * 
	 * @see #buildTrackerSession(List)
	 */
	private List<TrackerSession> buildTrackerSession(String announceUrl, List<String> announceUrls) throws DownloadException {
		final List<String> announces = new ArrayList<>();
		if(StringUtils.isNotEmpty(announceUrl)) {
			announces.add(announceUrl);
		}
		if(CollectionUtils.isNotEmpty(announceUrls)) {
			announces.addAll(announceUrls);
		}
		return this.buildTrackerSession(announces);
	}

	/**
	 * <p>创建TrackerSession列表</p>
	 * 
	 * @param announceUrls 声明地址集合
	 * 
	 * @return TrackerSession列表
	 * 
	 * @throws DownloadException 下载异常
	 */
	private List<TrackerSession> buildTrackerSession(List<String> announceUrls) throws DownloadException {
		if(announceUrls == null) {
			announceUrls = new ArrayList<>();
		}
		return announceUrls.stream()
			.map(announceUrl -> {
				try {
					return this.buildTrackerSession(announceUrl.trim());
				} catch (DownloadException e) {
					LOGGER.error("注册TrackerSession异常：{}", announceUrl, e);
				} catch (Exception e) {
					LOGGER.error("注册TrackerSession异常：{}", announceUrl, e);
				}
				return null;
			})
			.filter(client -> client != null && client.available())
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>创建TrackerSession</p>
	 * <p>通过传入的声明地址创建TrackerSession，如果没有被创建则创建TrackerSession并返回，反之直接返回已经创建的TrackerSession。</p>
	 * 
	 * @param announceUrl 声明地址
	 * 
	 * @return TrackerSession
	 * 
	 * @throws DownloadException 下载异常
	 */
	private TrackerSession buildTrackerSession(String announceUrl) throws DownloadException {
		if(StringUtils.isEmpty(announceUrl)) {
			return null;
		}
		final Optional<TrackerSession> optional = this.trackerSessions.values().stream()
			.filter(client -> client.equalsAnnounceUrl(announceUrl))
			.findFirst();
		if(optional.isPresent()) {
			return optional.get();
		}
		final TrackerSession session = this.buildSessionProxy(announceUrl);
		this.trackerSessions.put(session.id(), session);
		LOGGER.debug("注册TrackerSession：ID：{}，AnnounceUrl：{}", session.id(), session.announceUrl());
		return session;
	}

	/**
	 * <p>创建TrackerSession代理</p>
	 * <p>如果第一次创建失败将链接使用URL解码后再次创建</p>
	 * 
	 * @param announceUrl 声明地址
	 * 
	 * @return TrackerSession
	 * 
	 * @throws DownloadException 下载异常
	 */
	private TrackerSession buildSessionProxy(final String announceUrl) throws DownloadException {
		TrackerSession session = this.buildSession(announceUrl);
		if(session == null) {
			session = this.buildSession(UrlUtils.decode(announceUrl)); // URL解码
		}
		if(session == null) {
			throw new DownloadException("创建TrackerSession失败（未知Tracker协议）：" + announceUrl);
		}
		return session;
	}

	/**
	 * <p>创建TrackerSession</p>
	 * 
	 * @param announceUrl 声明地址
	 * 
	 * @return TrackerSession
	 * 
	 * @throws DownloadException 下载异常
	 */
	private TrackerSession buildSession(final String announceUrl) throws DownloadException {
		if(Protocol.Type.HTTP.verify(announceUrl)) {
			try {
				return HttpTrackerSession.newInstance(announceUrl);
			} catch (NetException e) {
				throw new DownloadException(e);
			}
		} else if(Protocol.Type.UDP.verify(announceUrl)) {
			try {
				return UdpTrackerSession.newInstance(announceUrl);
			} catch (NetException e) {
				throw new DownloadException(e);
			}
		}
		return null;
	}
	
}
