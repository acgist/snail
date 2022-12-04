package com.acgist.snail.net.torrent.tracker;

import java.util.Objects;

import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Tracker信息</p>
 * <p>基本协议：HTTP、UDP</p>
 * 
 * @author acgist
 */
public abstract class TrackerSession implements Comparable<TrackerSession> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerSession.class);
	
	/**
	 * <p>想要获取Peer数量：{@value}</p>
	 */
	protected static final int WANT_PEER_SIZE = 50;
	
	/**
	 * <p>客户端ID</p>
	 * <p>对应Tracker服务器</p>
	 */
	protected final Integer id;
	/**
	 * <p>协议类型</p>
	 */
	protected final Protocol.Type type;
	/**
	 * <p>刮擦地址</p>
	 */
	protected final String scrapeUrl;
	/**
	 * <p>声明地址</p>
	 */
	protected final String announceUrl;
	/**
	 * <p>权重</p>
	 * <p>查询成功会使权重增加</p>
	 * <p>查询失败会使权重减少</p>
	 */
	protected int weight;
	/**
	 * <p>失败次数</p>
	 * <p>查询成功会使失败次数增加</p>
	 * <p>查询失败会使失败次数减少</p>
	 * 
	 * @see TrackerConfig#MAX_FAIL_TIMES
	 */
	protected int failTimes = 0;
	/**
	 * <p>是否可用</p>
	 */
	protected boolean available = true;
	
	/**
	 * <p>Tracker信息</p>
	 * 
	 * @param scrapeUrl 刮擦地址
	 * @param announceUrl 声明地址
	 * @param type 协议类型
	 * 
	 * @throws NetException 网络异常
	 */
	protected TrackerSession(String scrapeUrl, String announceUrl, Protocol.Type type) throws NetException {
		if(StringUtils.isEmpty(announceUrl)) {
			throw new NetException("Tracker声明地址错误：" + announceUrl);
		}
		this.id = NumberUtils.build();
		this.type = type;
		this.weight = 0;
		this.scrapeUrl = scrapeUrl;
		this.announceUrl = announceUrl;
	}

	/**
	 * <p>查找Peer</p>
	 * <p>查找到的结果放入Peer列表</p>
	 * 
	 * @param sid {@link TrackerLauncher#id()}
	 * @param torrentSession BT任务信息
	 */
	public void findPeers(Integer sid, TorrentSession torrentSession) {
		if(!this.available()) {
			return;
		}
		try {
			// 发送声明消息
			this.started(sid, torrentSession);
			this.weight++;
			this.failTimes = 0;
		} catch (Exception e) {
			this.weight--;
			this.failTimes++;
			if(this.failTimes >= TrackerConfig.MAX_FAIL_TIMES) {
				this.available = false;
				LOGGER.error("Tracker停用，失败次数：{}，声明地址：{}", this.failTimes, this.announceUrl, e);
			} else {
				LOGGER.debug("查找Peer异常，失败次数：{}，声明地址：{}", this.failTimes, this.announceUrl, e);
			}
		}
	}
	
	/**
	 * <p>声明：开始</p>
	 * 
	 * @param sid {@link TrackerLauncher#id()}
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void started(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>声明：完成</p>
	 * <p>任务完成时发送</p>
	 * 
	 * @param sid {@link TrackerLauncher#id()}
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void completed(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>声明：停止</p>
	 * <p>任务暂停时发送</p>
	 * 
	 * @param sid {@link TrackerLauncher#id()}
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void stopped(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>刮檫</p>
	 * 
	 * @param sid {@link TrackerLauncher#id()}
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void scrape(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>新建声明消息</p>
	 * 
	 * @param sid {@link TrackerLauncher#id()}
	 * @param torrentSession BT任务信息
	 * @param event 事件
	 * 
	 * @return 声明消息
	 */
	protected Object buildAnnounceMessage(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event) {
		// 剩余下载大小
		long left = 0L;
		// 已经上传大小
		long upload = 0L;
		// 已经下载大小
		long download = 0L;
		final var taskSession = torrentSession.taskSession();
		if(taskSession != null) {
			final var statistics = taskSession.statistics();
			upload = statistics.uploadSize();
			download = statistics.downloadSize();
			left = taskSession.getSize() - download;
		}
		return this.buildAnnounceMessageEx(sid, torrentSession, event, upload, download, left);
	}
	
	/**
	 * <p>新建声明消息</p>
	 * 
	 * @param sid {@link TrackerLauncher#id()}
	 * @param torrentSession BT任务信息
	 * @param event 事件
	 * @param upload 已经上传大小
	 * @param download 已经下载大小
	 * @param left 剩余下载大小
	 * 
	 * @return 声明消息
	 */
	protected abstract Object buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long upload, long download, long left);
	
	/**
	 * <p>获取客户端ID</p>
	 * 
	 * @return 客户端ID
	 */
	public Integer id() {
		return this.id;
	}
	
	/**
	 * <p>获取协议类型</p>
	 * 
	 * @return 协议类型
	 */
	public Protocol.Type type() {
		return this.type;
	}
	
	/**
	 * <p>获取刮擦地址</p>
	 * 
	 * @return 刮擦地址
	 */
	public String scrapeUrl() {
		return this.scrapeUrl;
	}
	
	/**
	 * <p>获取声明地址</p>
	 * 
	 * @return 声明地址
	 */
	public String announceUrl() {
		return this.announceUrl;
	}
	
	/**
	 * <p>判断是否可用</p>
	 * 
	 * @return 是否可用
	 */
	public boolean available() {
		return this.available;
	}
	
	/**
	 * <p>判断当前Tracker声明地址和声明地址是否相同</p>
	 * 
	 * @param announceUrl 声明地址
	 * 
	 * @return 是否相同
	 */
	public boolean equalsAnnounceUrl(String announceUrl) {
		return this.announceUrl.equals(announceUrl);
	}
	
	@Override
	public int compareTo(TrackerSession session) {
		return Integer.compare(this.weight, session.weight);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.announceUrl);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof TrackerSession session) {
			return StringUtils.equals(this.announceUrl, session.announceUrl);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.id, this.type, this.scrapeUrl, this.announceUrl);
	}
	
}
