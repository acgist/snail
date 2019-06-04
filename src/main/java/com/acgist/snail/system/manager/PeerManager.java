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

import com.acgist.snail.net.bt.peer.bootstrap.ltep.PeerExchangeMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.utils.NetUtils;

/**
 * Peer管理器
 */
public class PeerManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerManager.class);

	private static final PeerManager INSTANCE = new PeerManager();
	
	/**
	 * 使用的Peer，下载时Peer从中间剔除，选为劣质Peer时放回到列表中。<br>
	 * key=infoHashHex<br>
	 * value=Peers：双端队列，新加入插入队尾，剔除的Peer插入对头。
	 */
	private final Map<String, Deque<PeerSession>> peers;
	/**
	 * 记录所有的Peer
	 */
	private final Map<String, List<PeerSession>> storagePeers;
	
	private PeerManager() {
		peers = new ConcurrentHashMap<>();
		storagePeers = new ConcurrentHashMap<>();
	}
	
	public static final PeerManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 获取所有Peer
	 */
	public Map<String, List<PeerSession>> peers() {
		synchronized (this.storagePeers) {
			return storagePeers.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> {
					return new ArrayList<>(entry.getValue());
				}));
		}
	}
	
	/**
	 * 获取对应任务的一个临时的PeerSession列表
	 */
	public List<PeerSession> list(String infoHashHex) {
		final var list = this.storagePeers.get(infoHashHex);
		if(list == null) {
			return null;
		}
		synchronized (list) {
			return new ArrayList<>(list);
		}
	}
	
	/**
	 * 新增Peer，插入尾部。
	 * @param infoHashHex 下载文件infoHashHex
	 * @param parent torrent下载统计
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return PeerSession，如果是本机IP返回null。
	 */
	public PeerSession newPeerSession(String infoHashHex, StatisticsSession parent, String host, Integer port, byte source) {
		if(NetUtils.isLocalhost(host)) { // 本机
			return null;
		}
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
				this.storage(infoHashHex, peerSession);
			}
			peerSession.source(source); // 设置来源
			return peerSession;
		}
	}
	
	/**
	 * 放入一个劣质的Peer，插入头部。
	 */
	public void inferior(String infoHashHex, PeerSession peerSession) {
		var deque = deque(infoHashHex);
		synchronized (deque) {
			deque.offerFirst(peerSession);
		}
	}
	
	/**
	 * 选择一个Peer下载，选择可用状态的Peer。
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
				if(peerSession.available()) { // 可用
					return peerSession;
				} else {
					deque.offerFirst(peerSession);
				}
			}
			return null;
		}
	}
	
	/**
	 * <p>发送have消息</p>
	 * <p>只发送给当前上传和下载的Peer。</p>
	 */
	public void have(String infoHashHex, int index) {
		final var list = list(infoHashHex);
		if(list == null) {
			return;
		}
		list.stream()
		.filter(session -> session.uploading() || session.downloading())
		.forEach(session -> {
			var handler = session.peerLauncherMessageHandler();
			if(handler != null && handler.available()) {
				handler.have(index);
			}
		});
	}
	
	/**
	 * <p>发送PEX消息</p>
	 * <p>只发送给当前上传和下载的Peer。</p>
	 */
	public void exchange(String infoHashHex, List<PeerSession> optimize) {
		final byte[] bytes = PeerExchangeMessageHandler.buildMessage(optimize);
		if(bytes == null) {
			return;
		}
		final var list = list(infoHashHex);
		if(list == null) {
			return;
		}
		LOGGER.debug("发送PEX消息，Peer数量：{}，通知Peer数量：{}", optimize.size(), list.size());
		list.stream()
		.filter(session -> session.uploading() || session.downloading())
		.forEach(session -> {
			var handler = session.peerLauncherMessageHandler();
			if(handler != null && handler.available()) {
				handler.exchange(bytes);
			}
		});
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
	
	/**
	 * 添加对应的Peer列表
	 */
	private void storage(String infoHashHex, PeerSession peerSession) {
		List<PeerSession> list = this.storagePeers.get(infoHashHex);
		if(list == null) {
			list = new ArrayList<>();
			this.storagePeers.put(infoHashHex, list);	
		}
		synchronized (list) {
			list.add(peerSession);
		}
	}

}
