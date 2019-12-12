package com.acgist.snail.net.torrent.peer.bootstrap;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.ltep.PeerExchangeMessageHandler;
import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>Peer管理器</p>
 * <p>Peer放入两个队列：{@linkplain #peers 下载队列}、{@linkplain #storagePeers 存档队列}</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class PeerManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerManager.class);

	private static final PeerManager INSTANCE = new PeerManager();
	
	/**
	 * <p>下载队列</p>
	 * <p>下载时Peer从队列中剔除，当Peer使用结束后重新放回下载队列。</p>
	 * <p>InfoHashHex=Peer双端队列（尾部优先使用）</p>
	 */
	private final Map<String, Deque<PeerSession>> peers;
	/**
	 * <p>Peer存档队列</p>
	 */
	private final Map<String, List<PeerSession>> storagePeers;
	
	private PeerManager() {
		this.peers = new ConcurrentHashMap<>();
		this.storagePeers = new ConcurrentHashMap<>();
	}
	
	public static final PeerManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>查找PeerSession</p>
	 */
	public PeerSession findPeerSession(String infoHashHex, String host) {
		final var list = list(infoHashHex);
		synchronized (list) {
			return findPeerSession(list, host);
		}
	}
	
	/**
	 * <p>存档队列拷贝</p>
	 */
	public List<PeerSession> listPeerSession(String infoHashHex) {
		final var list = list(infoHashHex);
		synchronized (list) {
			return new ArrayList<>(list);
		}
	}
	
	/**
	 * <p>判断是否找到Peer</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return {@code true}-找到；{@code false}-没有找到；
	 */
	public boolean havePeerSession(String infoHashHex) {
		return !list(infoHashHex).isEmpty();
	}
	
	/**
	 * <p>删除任务对应的所有队列</p>
	 */
	public void remove(String infoHashHex) {
		// 删除下载队列
		synchronized (this.peers) {
			this.peers.remove(infoHashHex);
		}
		// 删除存档队列
		synchronized (this.storagePeers) {
			this.storagePeers.remove(infoHashHex);
		}
	}
	
	/**
	 * <p>添加Peer</p>
	 * <p>优先级高的Peer插入尾部优先使用</p>
	 * <p>优先级计算：PEX、本地发现、主动连接</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * @param parent 任务下载统计
	 * @param host 地址
	 * @param port 端口
	 * @param source 来源
	 * 
	 * @return PeerSession
	 */
	public PeerSession newPeerSession(String infoHashHex, IStatisticsSession parent, String host, Integer port, byte source) {
		synchronized (this) {
			final var list = list(infoHashHex);
			final var deque = deque(infoHashHex);
			synchronized (list) {
				synchronized (deque) {
					PeerSession peerSession = findPeerSession(list, host);
					if(peerSession == null) {
						if(LOGGER.isDebugEnabled()) {
							LOGGER.debug("添加PeerSession：{}-{}，来源：{}", host, port, PeerConfig.source(source));
						}
						peerSession = PeerSession.newInstance(parent, host, port);
						if(
							source == PeerConfig.SOURCE_PEX || // PEX
							source == PeerConfig.SOURCE_LSD || // 本地发现
							source == PeerConfig.SOURCE_CONNECT // 主动连接
						) {
							deque.offerLast(peerSession); // 插入尾部：优先级高
						} else {
							deque.offerFirst(peerSession); // 插入头部：优先级低
						}
						list.add(peerSession); // 存档
					}
					peerSession.source(source); // 设置来源
					return peerSession;
				}
			}
		}
	}
	
	/**
	 * <p>下载队列添加劣质Peer：插入头部</p>
	 */
	public void inferior(String infoHashHex, PeerSession peerSession) {
		final var deque = deque(infoHashHex);
		synchronized (deque) {
			deque.offerFirst(peerSession);
		}
	}
	
	/**
	 * <p>下载队列添加优质Peer：插入尾部</p>
	 */
	public void preference(String infoHashHex, PeerSession peerSession) {
		final var deque = deque(infoHashHex);
		synchronized (deque) {
			deque.offerLast(peerSession);
		}
	}
	
	/**
	 * <p>从下载队列尾部选择一个可用的Peer</p>
	 */
	public PeerSession pick(String infoHashHex) {
		final var deque = deque(infoHashHex);
		synchronized (deque) {
			int index = 0;
			final int size = deque.size();
			PeerSession peerSession = null;
			while(true) {
				if(++index > size) {
					break;
				}
				peerSession = deque.pollLast();
				// 可用状态
				if(peerSession.available()) {
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
	 * <p>只发送给当前连接的Peer</p>
	 */
	public void have(String infoHashHex, int index) {
		final var list = listConnectPeer(infoHashHex);
		final AtomicInteger count = new AtomicInteger(0);
		list.stream()
			.forEach(session -> {
				final var peerConnect = session.peerConnect();
				if(peerConnect != null && peerConnect.available()) {
					count.incrementAndGet();
					peerConnect.have(index);
				}
			});
		LOGGER.debug("发送have消息：通知Peer数量：{}", count.get());
	}
	
	/**
	 * <p>发送pex消息</p>
	 * <p>只发送给当前连接的Peer</p>
	 */
	public void pex(String infoHashHex) {
		final var list = listConnectPeer(infoHashHex);
		// 优质Peer：下载数据
		final var optimize = list.stream()
			.filter(session -> session.statistics().downloadSize() > 0)
			.collect(Collectors.toList());
		final byte[] message = PeerExchangeMessageHandler.buildMessage(optimize);
		if(message == null) {
			LOGGER.debug("发送pex消息失败：消息为空");
			return;
		}
		final AtomicInteger count = new AtomicInteger(0);
		list.stream()
			.forEach(session -> {
				final var peerConnect = session.peerConnect();
				if(peerConnect != null && peerConnect.available()) {
					count.incrementAndGet();
					peerConnect.pex(message);
				}
			});
		LOGGER.debug("发送pex消息：通知Peer数量：{}", count.get());
	}
	
	/**
	 * <p>发送uploadOnly消息</p>
	 * <p>只发送给当前连接的Peer</p>
	 */
	public void uploadOnly(String infoHashHex) {
		final var list = listConnectPeer(infoHashHex);
		final AtomicInteger count = new AtomicInteger(0);
		list.stream()
			.forEach(session -> {
				final var peerConnect = session.peerConnect();
				if(peerConnect != null && peerConnect.available()) {
					count.incrementAndGet();
					peerConnect.uploadOnly();
				}
			});
		LOGGER.debug("发送uploadOnly消息：通知Peer数量：{}", count.get());
	}
	
	/**
	 * @return 任务下载队列
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
	 * @return 任务存档队列
	 */
	private List<PeerSession> list(String infoHashHex) {
		synchronized (this.storagePeers) {
			List<PeerSession> list = this.storagePeers.get(infoHashHex);
			if(list == null) {
				list = new ArrayList<>();
				this.storagePeers.put(infoHashHex, list);
			}
			return list;
		}
	}
	
	/**
	 * <p>获取存档队列中当前连接的Peer队列拷贝</p>
	 * <p>连接中的Peer：上传中、下载中</p>
	 */
	private List<PeerSession> listConnectPeer(String infoHashHex) {
		final var list = list(infoHashHex);
		if(CollectionUtils.isEmpty(list)) {
			return List.of();
		}
		synchronized (list) {
			return list.stream()
				.filter(session -> session.available())
				.filter(session -> session.connected())
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * <p>查找PeerSession</p>
	 * 
	 * @param list Peer队列
	 * @param host IP地址
	 */
	private PeerSession findPeerSession(List<PeerSession> list, String host) {
		final Optional<PeerSession> optional = list.stream()
			.filter(peer -> {
				return peer.equalsHost(host);
			}).findFirst();
		if(optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

}
