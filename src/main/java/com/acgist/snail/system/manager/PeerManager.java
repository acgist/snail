package com.acgist.snail.system.manager;

import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;

/**
 * Peer管理器
 */
public class PeerManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerManager.class);

	private static final PeerManager INSTANCE = new PeerManager();
	
	/**
	 * Peer Map<br>
	 * key=infoHashHex
	 * value=Peers：双端队列，新加入插入队尾，剔除的Peer插入对头
	 */
	private Map<String, Deque<PeerSession>> peers = new ConcurrentHashMap<>();
	
	private PeerManager() {
	}
	
	public static final PeerManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 新增Peer，插入尾部
	 * @param infoHashHex 下载文件infoHashHex
	 * @param parent torrent下载统计
	 * @param host 地址
	 * @param port 端口
	 */
	public PeerSession newPeer(String infoHashHex, StatisticsSession parent, String host, Integer port) {
		synchronized (peers) {
			var deque = peers.get(infoHashHex);
			if(deque == null) {
				deque = new LinkedBlockingDeque<>();
				peers.put(infoHashHex, deque);
			}
			Optional<PeerSession> optional = deque.stream().filter(peer -> {
				return peer.exist(host, port);
			}).findFirst();
			if(optional.isPresent()) {
				return optional.get();
			}
			LOGGER.debug("添加Peer，HOST：{}，PORT：{}", host, port);
			PeerSession peerSession = new PeerSession(parent, host, port);
			deque.offerLast(peerSession);
			return peerSession;
		}
	}
	
	/**
	 * 放入一个优化的Peer，插入头部
	 */
	public void inferior(String infoHashHex, PeerSession peerSession) {
		synchronized (peers) {
			var deque = peers.get(infoHashHex);
			if(deque != null) {
				deque.offerFirst(peerSession);
			}
		}
	}
	
	/**
	 * 选择一个Peer下载
	 */
	public PeerSession pick(String infoHashHex) {
		synchronized (peers) {
			var deque = peers.get(infoHashHex);
			if(deque != null) {
				return deque.pollLast();
			}
			return null;
		}
	}

}
