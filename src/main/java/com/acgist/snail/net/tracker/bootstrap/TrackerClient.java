package com.acgist.snail.net.tracker.bootstrap;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UniqueCodeUtils;

/**
 * Tracker协议<br>
 * 基本协议：TCP（HTTP）、UDP<br>
 * http://www.bittorrent.org/beps/bep_0015.html<br>
 * http://www.bittorrent.org/beps/bep_0023.html<br>
 * sid：每一个torrent和tracker服务器对应的id
 */
public abstract class TrackerClient implements Comparable<TrackerClient> {

	public static final int TIMEOUT = 4; // 超时时间
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerClient.class);
	
	public enum Type {
		udp,
		http;
	}
	
	/**
	 * 最大失败次数
	 */
	private static final int MAX_FAIL_TIMES = SystemConfig.getTrackerMaxFailTimes();
	
	protected int weight; // 权重
	protected final Integer id; // trackerId：transaction_id（UDP连接时使用）
	protected final Type type; // 类型
	protected final String scrapeUrl; // 刮檫URL
	protected final String announceUrl; // 声明URL

	/**
	 * 是否可用
	 */
	private AtomicBoolean available = new AtomicBoolean(true);
	/**
	 * 失败次数，成功后清零，超过一定次数#{@link MAX_FAIL_TIMES}设置为不可用
	 */
	private AtomicInteger failTimes = new AtomicInteger(0);
	/**
	 * 失败原因
	 */
	private String failMessage;
	
	public TrackerClient(String scrapeUrl, String announceUrl, Type type) throws NetException {
		if(StringUtils.isEmpty(announceUrl)) {
			throw new NetException("不支持的Tracker announceUrl：" + announceUrl);
		}
		this.id = UniqueCodeUtils.buildInteger();
		this.type = type;
		this.weight = 0;
		this.scrapeUrl = scrapeUrl;
		this.announceUrl = announceUrl;
	}

	/**
	 * 是否可用，如果多次超时标记不可用状态
	 */
	public boolean available() {
		return available.get();
	}
	
	/**
	 * 查找Peer，查找结果直接设置到session
	 */
	public void findPeers(Integer sid, TorrentSession torrentSession) {
		if(!available()) { // 不可用直接返回null
			return;
		}
		try {
			announce(sid, torrentSession);
			failTimes.set(0);
			weight--;
		} catch (Exception e) {
			weight++;
			if(failTimes.incrementAndGet() > MAX_FAIL_TIMES) {
				available.set(false);
				failMessage = e.getMessage();
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
	 * 完成
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
	
	public Type type() {
		return this.type;
	}
	
	public String announceUrl() {
		return this.announceUrl;
	}
	
	public String failMessage() {
		return this.failMessage;
	}
	
	/**
	 * 判断是否存在
	 */
	public boolean exist(String announceUrl) {
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
	
	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(ObjectUtils.equalsClazz(this, object)) {
			TrackerClient client = (TrackerClient) object;
			return ObjectUtils.equalsBuilder(this.announceUrl)
				.equals(ObjectUtils.equalsBuilder(client.announceUrl));
		}
		return false;
	}
	
}
