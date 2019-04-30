package com.acgist.snail.system.manager;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.ltep.UtPeerExchangeMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;

/**
 * Peer管理器
 */
public class PeerSessionManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerSessionManager.class);

	private static final PeerSessionManager INSTANCE = new PeerSessionManager();
	
	/**
	 * Peer Map<br>
	 * key=infoHashHex<br>
	 * value=Peers：双端队列，新加入插入队尾，剔除的Peer插入对头
	 */
	private final Map<String, Deque<PeerSession>> peers;
	
	private PeerSessionManager() {
		peers = new ConcurrentHashMap<>();
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
	public PeerSession newPeerSession(String infoHashHex, StatisticsSession parent, String host, Integer port, byte source) {
		LOGGER.debug("添加PeerSession，HOST：{}，PORT：{}", host, port);
		var deque = deque(infoHashHex);
		synchronized (deque) {
			final Optional<PeerSession> optional = deque.stream().filter(peer -> {
				return peer.exist(host);
			}).findFirst();
			PeerSession peerSession;
			if(optional.isPresent()) {
				peerSession = optional.get();
			} else {
				peerSession = PeerSession.newInstance(parent, host, port);
				deque.offerLast(peerSession);
			}
			peerSession.source(source); // 设置来源
			return peerSession;
		}
	}
	
	/**
	 * 放入一个优化的Peer，插入头部
	 */
	public void inferior(String infoHashHex, PeerSession peerSession) {
		var deque = deque(infoHashHex);
		synchronized (deque) {
			deque.offerFirst(peerSession);
		}
	}
	
	/**
	 * 选择一个Peer下载
	 */
	public PeerSession pick(String infoHashHex) {
		var deque = deque(infoHashHex);
		synchronized (deque) {
			int index = 0;
			int size = deque.size();
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
			return null;
		}
	}
	
	/**
	 * 发送have消息
	 */
	public void have(String infoHashHex, int index) {
		var list = list(infoHashHex);
		list.forEach(session -> {
			var handler = session.peerMessageHandler();
			if(handler != null && handler.available()) {
				handler.have(index);
			}
		});
	}
	
	/**
	 * 发送pex消息
	 */
	public void exchange(String infoHashHex, List<PeerSession> optimize) {
		final byte[] bytes = UtPeerExchangeMessageHandler.buildMessage(optimize);
		if(bytes == null) {
			return;
		}
		optimize.clear(); // 清空
		var list = list(infoHashHex);
		list.forEach(session -> {
			var handler = session.peerMessageHandler();
			if(handler != null && handler.available()) {
				handler.exchange(bytes);
			}
		});
	}
	
	/**
	 * 获取对应的一个临时的PeerSession列表
	 */
	public List<PeerSession> list(String infoHashHex) {
		final var deque = deque(infoHashHex);
		synchronized (deque) {
			return new ArrayList<>(deque);
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
