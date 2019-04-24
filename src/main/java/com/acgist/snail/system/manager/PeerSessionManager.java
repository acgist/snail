package com.acgist.snail.system.manager;

import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;

/**
 * Peer管理器
 */
public class PeerSessionManager {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(PeerSessionManager.class);

	private static final PeerSessionManager INSTANCE = new PeerSessionManager();
	
	/**
	 * Peer Map<br>
	 * key=infoHashHex<br>
	 * value=Peers：双端队列，新加入插入队尾，剔除的Peer插入对头
	 */
	private Map<String, Deque<PeerSession>> peers = new ConcurrentHashMap<>();
	
	private PeerSessionManager() {
	}
	
	public static final PeerSessionManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 新增Peer，插入尾部
	 * @param infoHashHex 下载文件infoHashHex
	 * @param parent torrent下载统计
	 * @param host 地址
	 * @param port 端口
	 */
	public PeerSession newPeerSession(String infoHashHex, StatisticsSession parent, String host, Integer port) {
		var deque = peers.get(infoHashHex);
		synchronized (peers) {
			if(deque == null) {
				deque = new LinkedBlockingDeque<>();
				peers.put(infoHashHex, deque);
			}
			final Optional<PeerSession> optional = deque.stream().filter(peer -> {
				return peer.exist(host);
			}).findFirst();
			if(optional.isPresent()) {
				return optional.get();
			}
			final PeerSession peerSession = PeerSession.newInstance(parent, host, port);
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
				int size = deque.size();
				int index = 0;
				PeerSession peerSession = null;
				while(true) {
					if(++index > size) {
						break;
					}
					peerSession = deque.pollLast();
					if(peerSession.usable()) { // 可用
						return peerSession;
					} else {
						deque.offerFirst(peerSession);
					}
				}
			}
			return null;
		}
	}
	
	private Deque<PeerSession> deque(String infoHashHex) {
		synchronized (peers) {
			Deque<PeerSession> deque = peers.get(infoHashHex);
			if(deque == null) {
				deque = new LinkedBlockingDeque<>();
				peers.put(infoHashHex, deque);
			}
			return deque;
		}
	}

}
