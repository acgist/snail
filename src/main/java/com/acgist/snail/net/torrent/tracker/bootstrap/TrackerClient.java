package com.acgist.snail.net.torrent.tracker.bootstrap;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.ProtocolConfig.Protocol;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Tracker客户端</p>
 * <p>基本协议：TCP（HTTP）、UDP</p>
 * <p>失败次数超过{@link #MAX_FAIL_TIMES}时为无效客户端，不可再使用。</p>
 * <p>每次成功查询都会使这个Tracker的权重增加。</p>
 * <p>sid：每一个Torrent和Tracker服务器对应的id。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class TrackerClient implements Comparable<TrackerClient> {

	public static final int TIMEOUT = 4; // 超时时间
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerClient.class);
	
	protected int weight; // 权重
	protected final Integer id; // ID
	protected final Protocol type; // 类型
	protected final String scrapeUrl; // 刮檫URL
	protected final String announceUrl; // 声明URL

	/**
	 * 是否可用
	 */
	private AtomicBoolean available = new AtomicBoolean(true);
	/**
	 * 失败次数，成功后清零，超过一定次数#{@link #MAX_FAIL_TIMES}设置为不可用
	 */
	private AtomicInteger failTimes = new AtomicInteger(0);
	/**
	 * 失败原因
	 */
	private String failMessage;
	
	public TrackerClient(String scrapeUrl, String announceUrl, Protocol type) throws NetException {
		if(StringUtils.isEmpty(announceUrl)) {
			throw new NetException("不支持的Tracker announceUrl：" + announceUrl);
		}
		this.id = NumberUtils.build();
		this.type = type;
		this.weight = 0;
		this.scrapeUrl = scrapeUrl;
		this.announceUrl = announceUrl;
	}

	/**
	 * 是否可用，如果多次超时标记不可用状态
	 */
	public boolean available() {
		return this.available.get();
	}
	
	/**
	 * 查找Peer，查找到的结果放入Peer列表。
	 */
	public void findPeers(Integer sid, TorrentSession torrentSession) {
		if(!available()) { // 不可用直接返回null
			return;
		}
		try {
			announce(sid, torrentSession);
			this.failTimes.set(0);
			this.weight++;
		} catch (Exception e) {
			this.weight--;
			if(this.failTimes.incrementAndGet() > TrackerConfig.MAX_FAIL_TIMES) {
				this.available.set(false);
				this.failMessage = e.getMessage();
				LOGGER.info("失败次数过多，停用Tracker Client，announceUrl：{}", this.announceUrl);
			}
			LOGGER.error("查找Peer异常，当前失败次数：{}", failTimes.get(), e);
		}
	}
	
	/**
	 * 跟踪：开始
	 */
	public abstract void announce(Integer sid, TorrentSession torrentSession) throws NetException;
	
	/**
	 * 完成，下载完成时推送，如果一开始时就已经完成不需要推送。
	 */
	public abstract void complete(Integer sid, TorrentSession torrentSession);
	
	/**
	 * 停止
	 */
	public abstract void stop(Integer sid, TorrentSession torrentSession);
	
	/**
	 * 刮檫
	 */
	public abstract void scrape(Integer sid, TorrentSession torrentSession) throws NetException;
	
	public Integer id() {
		return this.id;
	}
	
	public Protocol type() {
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
