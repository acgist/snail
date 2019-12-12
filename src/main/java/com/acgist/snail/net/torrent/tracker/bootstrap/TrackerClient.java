package com.acgist.snail.net.torrent.tracker.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Tracker客户端</p>
 * <p>基本协议：TCP（HTTP）、UDP、WS（WebSocket）</p>
 * <p>sid：Torrent和Tracker服务器对应的id</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class TrackerClient implements Comparable<TrackerClient> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerClient.class);
	
	/**
	 * <p>想要获取的Peer数量</p>
	 */
	protected static final int WANT_PEER_SIZE = 50;
	
	/**
	 * <p>权重</p>
	 * <p>查询成功会使权重增加、查询失败会使权重减少</p>
	 */
	protected int weight;
	/**
	 * <p>客户端ID</p>
	 * <p>ID与Tracker服务器一一对应</p>
	 */
	protected final Integer id;
	/**
	 * <p>协议类型</p>
	 */
	protected final Protocol.Type type;
	/**
	 * <p>刮檫地址</p>
	 */
	protected final String scrapeUrl;
	/**
	 * <p>声明地址</p>
	 */
	protected final String announceUrl;
	/**
	 * <p>失败次数</p>
	 * <p>请求处理成功后清零，超过{@linkplain TrackerConfig#MAX_FAIL_TIMES 最大失败次数}设置为不可用。</p>
	 */
	private int failTimes = 0;
	/**
	 * <p>失败原因</p>
	 */
	private String failMessage;
	/**
	 * <p>是否可用</p>
	 */
	private boolean available = true;
	
	public TrackerClient(String scrapeUrl, String announceUrl, Protocol.Type type) throws NetException {
		if(StringUtils.isEmpty(announceUrl)) {
			throw new NetException("Tracker声明地址错误（不支持）：" + announceUrl);
		}
		this.id = NumberUtils.build();
		this.type = type;
		this.weight = 0;
		this.scrapeUrl = scrapeUrl;
		this.announceUrl = announceUrl;
	}

	/**
	 * <p>是否可用</p>
	 */
	public boolean available() {
		return this.available;
	}
	
	/**
	 * <p>查找Peer</p>
	 * <p>查找到的结果放入Peer列表</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 */
	public void findPeers(Integer sid, TorrentSession torrentSession) {
		if(!available()) {
			return;
		}
		try {
			announce(sid, torrentSession); // 发送声明消息
			this.failTimes = 0;
			this.weight++;
		} catch (Exception e) {
			LOGGER.error("查找Peer异常：失败次数：{}，声明地址：{}", this.failTimes, this.announceUrl, e);
			this.weight--;
			if(++this.failTimes >= TrackerConfig.MAX_FAIL_TIMES) {
				LOGGER.warn("TrackerClient停用：失败次数：{}，声明地址：{}", this.failTimes, this.announceUrl, e);
				this.available = false;
				this.failMessage = e.getMessage();
			}
		}
	}
	
	/**
	 * <p>跟踪（声明）</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void announce(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>完成</p>
	 * <p>任务下载完成时发送</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void complete(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>停止</p>
	 * <p>任务暂停是发送</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void stop(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>刮檫</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @throws NetException 网络异常
	 */
	public abstract void scrape(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * <p>创建声明消息</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * @param event 事件
	 * 
	 * @return 声明消息
	 */
	protected Object buildAnnounceMessage(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event) {
		long download = 0L, left = 0L, upload = 0L;
		final var taskSession = torrentSession.taskSession();
		if(taskSession != null) {
			final var statistics = taskSession.statistics();
			download = statistics.downloadSize(); // 已下载
			left = taskSession.getSize() - download; // 剩余下载
			upload = statistics.uploadSize(); // 已上传
		}
		return this.buildAnnounceMessageEx(sid, torrentSession, event, download, left, upload);
	}
	
	/**
	 * <p>创建声明消息</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * @param event 事件
	 * @param download 已下载大小
	 * @param left 剩余大小
	 * @param upload 已上传大小
	 * 
	 * @return 声明消息
	 */
	protected abstract Object buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long download, long left, long upload);
	
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
	 * <p>判断当前TrackerClient的声明URL和声明URL是否一致</p>
	 */
	public boolean equal(String announceUrl) {
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
	 * <p>相等：声明URL一致</p>
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
