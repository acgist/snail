package com.acgist.snail.net.tracker;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.bean.Peer;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.UniqueCodeUtils;

/**
 * tracker协议<br>
 * 基本协议：TCP（HTTP）、UDP
 * DOC：https://wiki.theory.org/index.php/BitTorrentSpecification
 * UDP：https://www.libtorrent.org/udp_tracker_protocol.html
 */
public abstract class AbstractTrackerClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTrackerClient.class);
	
	/**
	 * 最大失败次数
	 */
	private static final int MAX_FAIL_TIMES = 5;
	
	protected final Long id; // id：transaction_id（连接时使用）
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
	
	
	public AbstractTrackerClient(String scrapeUrl, String announceUrl) {
		this.id = UniqueCodeUtils.buildLong();
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
	 * 查找Peer
	 */
	public List<Peer> findPeers(TorrentSession session) {
		List<Peer> list = null;
		try {
			list = announce(session);
			failTimes.set(0);
		} catch (Exception e) {
			if(failTimes.incrementAndGet() > MAX_FAIL_TIMES) {
				available.set(false);
			}
			LOGGER.info("查找Peer异常", e);
		}
		return list;
	}
	
	/**
	 * 跟踪
	 */
	public abstract List<Peer> announce(TorrentSession session);
	
	/**
	 * 刮檫
	 */
	public abstract void scrape(TorrentSession session);
	
}
