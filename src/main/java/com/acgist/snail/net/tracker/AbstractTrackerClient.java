package com.acgist.snail.net.tracker;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UniqueCodeUtils;

/**
 * tracker协议<br>
 * 基本协议：TCP（HTTP）、UDP<br>
 * DOC：https://wiki.theory.org/index.php/BitTorrentSpecification<br>
 * UDP：https://www.libtorrent.org/udp_tracker_protocol.html<br>
 */
public abstract class AbstractTrackerClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTrackerClient.class);
	
	public enum Type {
		udp,
		http;
	}
	
	/**
	 * announce事件
	 */
	public enum Event {
		
		none(0), // none
		completed(1), // 完成
		started(2), // 开始
		stopped(3); // 停止
		
		private int event;

		private Event(int event) {
			this.event = event;
		}

		public int event() {
			return this.event;
		}
		
	}
	
	/**
	 * 动作
	 */
	public enum Action {
		
		connect(0), // 连接
		announce(1), // 获取信息
		scrape(2), // 刷新信息
		error(3); // 错误
		
		private int action;

		private Action(int action) {
			this.action = action;
		}
		
		public int action() {
			return this.action;
		}
		
	}
	
	/**
	 * 最大失败次数
	 */
	private static final int MAX_FAIL_TIMES = 5;
	
	protected final Integer id; // trackerId：transaction_id（连接时使用）
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
	
	public AbstractTrackerClient(String scrapeUrl, String announceUrl, Type type) throws NetException {
		if(StringUtils.isEmpty(announceUrl)) {
			throw new NetException("不支持的Tracker announceUrl：" + announceUrl);
		}
		this.id = UniqueCodeUtils.buildInteger();
		this.type = type;
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
	public void findPeers(TorrentSession session) {
		if(!available()) { // 不可用直接返回null
			return;
		}
		try {
			announce(session);
			failTimes.set(0);
		} catch (Exception e) {
			if(failTimes.incrementAndGet() > MAX_FAIL_TIMES) {
				available.set(false);
				failMessage = e.getMessage();
			}
			LOGGER.info("查找Peer异常", e);
		}
	}
	
	/**
	 * 跟踪：开始
	 */
	public abstract void announce(TorrentSession session) throws NetException;
	
	/**
	 * 完成
	 */
	public abstract void complete(TorrentSession session);
	
	/**
	 * 停止
	 */
	public abstract void stop(TorrentSession session);
	
	/**
	 * 刮檫
	 */
	public abstract void scrape(TorrentSession session) throws NetException;
	
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
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(obj == this) {
			return true;
		}
		if(obj instanceof AbstractTrackerClient) {
			AbstractTrackerClient client = (AbstractTrackerClient) obj;
			return
				client.id.equals(this.id) ||
				client.announceUrl.equals(this.announceUrl);
		}
		return false;
	}
	
}
