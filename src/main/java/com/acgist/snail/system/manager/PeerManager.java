package com.acgist.snail.system.manager;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.ltep.UtPeerExchangeMessageHandler;
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
	 * key=infoHashHex<br>
	 * value=Peers：双端队列，新加入插入队尾，剔除的Peer插入对头
	 */
	private final Map<String, Deque<PeerSession>> peers;
	
	private PeerManager() {
		peers = new ConcurrentHashMap<>();
	}
	
	public static final PeerManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 获取所有Peer
	 */
	public Map<String, List<PeerSession>> peers() {
		synchronized (this.peers) {
			return peers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
				return new ArrayList<>(entry.getValue());
			}));
		}
	}
	
	/**
	 * 新增Peer，插入尾部
	 * @param infoHashHex 下载文件infoHashHex
	 * @param parent torrent下载统计
	 * @param host 地址
	 * @param port 端口
	 */
	public PeerSession newPeerSession(String infoHashHex, StatisticsSession parent, String host, Integer port, byte source) {
		var deque = deque(infoHashHex);
		synchronized (deque) {
			final Optional<PeerSession> optional = deque.stream().filter(peer -> {
				return peer.exist(host);
			}).findFirst();
			PeerSession peerSession;
			if(optional.isPresent()) {
				peerSession = optional.get();
			} else {
				LOGGER.debug("添加PeerSession，{}-{}，来源：{}", host, port, source);
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
	 * 发送PEX消息
	 */
	public void exchange(String infoHashHex, List<PeerSession> optimize) {
		final byte[] bytes = UtPeerExchangeMessageHandler.buildMessage(optimize);
		if(bytes == null) {
			return;
		}
		optimize.clear(); // 清空
		final var list = list(infoHashHex);
		LOGGER.debug("发送PEX消息，Peer数量：{}，通知Peer数量：{}", optimize.size(), list.size());
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
	
	/**
	 * 获取对应的Peer列表
	 */
	private Deque<PeerSession> deque(String infoHashHex) {
		synchronized (this.peers) {
			Deque<PeerSession> deque = this.peers.get(infoHashHex);
			if(deque == null) {
				deque = new LinkedBlockingDeque<>();
				this.peers.put(infoHashHex, deque);
			}
			return deque;
		}
	}

}
