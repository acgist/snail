package com.acgist.snail.net.torrent.tracker.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Tracker客户端</p>
 * <p>基本协议：TCP（HTTP）、UDP、WS（WebSocket）</p>
 * <p>sid：每一个Torrent和Tracker服务器对应的id。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class TrackerClient implements Comparable<TrackerClient> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerClient.class);
	
	/**
	 * 超时时间
	 */
	public static final int TIMEOUT = SystemConfig.CONNECT_TIMEOUT;
	/**
	 * 想要获取的Peer数量
	 */
	public static final int WANT_PEER_SIZE = 50;
	
	/**
	 * <p>权重</p>
	 * <p>查询成功会使权重增加，查询失败会使权重减少。</p>
	 */
	protected int weight;
	/**
	 * <p>ID</p>
	 * <p>UDP获取连接ID时使用</p>
	 */
	protected final Integer id;
	/**
	 * 协议类型
	 */
	protected final Protocol.Type type;
	/**
	 * 刮檫地址
	 */
	protected final String scrapeUrl;
	/**
	 * 声明地址
	 */
	protected final String announceUrl;
	/**
	 * 失败次数，成功后清零，超过一定次数#{@link TrackerConfig#MAX_FAIL_TIMES}设置为不可用。
	 */
	private int failTimes = 0;
	/**
	 * 失败原因
	 */
	private String failMessage;
	/**
	 * 是否可用
	 */
	private boolean available = true;
	
	public TrackerClient(String scrapeUrl, String announceUrl, Protocol.Type type) throws NetException {
		if(StringUtils.isEmpty(announceUrl)) {
			throw new NetException("不支持的Tracker声明地址：" + announceUrl);
		}
		this.id = NumberUtils.build();
		this.type = type;
		this.weight = 0;
		this.scrapeUrl = scrapeUrl;
		this.announceUrl = announceUrl;
	}

	/**
	 * 是否可用
	 */
	public boolean available() {
		return this.available;
	}
	
	/**
	 * 查找Peer：查找到的结果放入Peer列表
	 */
	public void findPeers(Integer sid, TorrentSession torrentSession) {
		if(!available()) {
			return;
		}
		try {
			announce(sid, torrentSession);
			this.failTimes = 0;
			this.weight++;
		} catch (Exception e) {
			LOGGER.error("查找Peer异常，失败次数：{}，地址：{}", this.failTimes, this.announceUrl, e);
			this.weight--;
			if(++this.failTimes >= TrackerConfig.MAX_FAIL_TIMES) {
				LOGGER.warn("TrackerClient停用，失败次数：{}，地址：{}", this.failTimes, this.announceUrl, e);
				this.available = false;
				this.failMessage = e.getMessage();
			}
		}
	}
	
	/**
	 * 跟踪（声明）
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void announce(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * 完成：下载完成时推送，如果一开始时就已经完成不需要推送。
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void complete(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * 停止
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void stop(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * 刮檫
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void scrape(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * 创建声明消息
	 * 
	 * @param <T> 消息类型
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * @param event 事件
	 * 
	 * @return 声明消息
	 */
	protected <T> T buildAnnounceMessage(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event) {
		long download = 0L, remain = 0L, upload = 0L;
		final var taskSession = torrentSession.taskSession();
		if(taskSession != null) {
			var statistics = taskSession.statistics();
			download = statistics.downloadSize();
			remain = taskSession.entity().getSize() - download;
			upload = statistics.uploadSize();
		}
		return this.buildAnnounceMessageEx(sid, torrentSession, event, download, remain, upload);
	}
	
	/**
	 * 创建声明消息
	 * 
	 * @param <T> 消息类型
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * @param event 事件
	 * @param download 已下载大小
	 * @param remain 剩余大小
	 * @param upload 已上传大小
	 * 
	 * TODO：子类泛型优化
	 * 
	 * @return 声明消息
	 */
	protected abstract <T> T buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long download, long remain, long upload);
	
	public Integer id() {
		return this.id;
	}
	
	public Protocol.Type type() {
		return this.type;
	}
	
	public String announceUrl() {
		return this.announceUrl;
	}
	
	public String failMessage() {
		return this.failMessage;
	}
	
	/**
	 * 判断当前TrackerClient的声明URL和声明URL是否一致。
	 */
	public boolean equals(String announceUrl) {
		return this.announceUrl.equals(announceUrl);
	}
	
	@Override
	public int compareTo(TrackerClient client) {
		return this.weight == client.weight ? 0 : this.weight > client.weight ? 1 : -1;
	}
	
	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(this.announceUrl);
	}
	
	/**
	 * 如果声明URL一致即为相等
	 */
	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(object instanceof TrackerClient) {
			final TrackerClient client = (TrackerClient) object;
			return StringUtils.equals(this.announceUrl, client.announceUrl);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.id, this.type, this.failTimes, this.announceUrl);
	}
	
}
