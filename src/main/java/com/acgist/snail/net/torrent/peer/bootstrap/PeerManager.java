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
import com.acgist.snail.system.evaluation.PeerEvaluator;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>Peer管理器</p>
 * <p>Peer加入放入两个队列，一个队列负责下载时使用：{@link #peers}，一个负责存档：{@link #storagePeers}。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerManager.class);

	private static final PeerManager INSTANCE = new PeerManager();
	
	/**
	 * <p>Peer队列，下载时Peer从中剔除，当Peer从下载队列中剔除时从新放回到队列中。</p>
	 * <p>key=InfoHashHex</p>
	 * <p>value=Peer：双端队列（尾部优先使用）</p>
	 */
	private final Map<String, Deque<PeerSession>> peers;
	/**
	 * Peer存档队列
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
	 * 查找PeerSession
	 */
	public PeerSession findPeerSession(String infoHashHex, String host) {
		final var list = list(infoHashHex);
		synchronized (list) {
			return findPeerSession(list, host);
		}
	}
	
	/**
	 * Peer存档队列拷贝
	 */
	public List<PeerSession> listPeers(String infoHashHex) {
		final var list = list(infoHashHex);
		if(list == null) {
			return List.of();
		}
		synchronized (list) {
			return new ArrayList<>(list);
		}
	}
	
	/**
	 * 删除任务对应的Peer队列
	 */
	public void remove(String infoHashHex) {
		synchronized (this.peers) {
			this.peers.remove(infoHashHex);
		}
		synchronized (this.storagePeers) {
			this.storagePeers.remove(infoHashHex);
		}
	}
	
	/**
	 * <p>新增Peer</p>
	 * <p>优先级高的Peer插入尾部优先使用。</p>
	 * <p>优先级计算：本地发现、主动连接、Peer评分、PEX可以连接</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * @param parent 任务下载统计
	 * @param host 地址
	 * @param port 端口
	 * @param source 来源
	 * 
	 * @return PeerSession
	 * 
	 * TODO：接入Peer调整Peer位置顺序
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
							source == PeerConfig.SOURCE_LSD || // 本地发现
							source == PeerConfig.SOURCE_CONNECT || // 主动连接
//							source == PeerConfig.SOURCE_HOLEPUNCH || // holepunch
							PeerEvaluator.getInstance().eval(peerSession) // Peer评分
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
	 * 放入一个劣质的Peer：插入头部
	 */
	public void inferior(String infoHashHex, PeerSession peerSession) {
		final var deque = deque(infoHashHex);
		synchronized (deque) {
			deque.offerFirst(peerSession);
		}
	}
	
	/**
	 * 放入一个优质的Peer：插入尾部
	 */
	public void preference(String infoHashHex, PeerSession peerSession) {
		final var deque = deque(infoHashHex);
		synchronized (deque) {
			deque.offerLast(peerSession);
		}
	}
	
	/**
	 * 从尾部选择一个Peer下载：选择可用状态的Peer
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
	 * <p>只发送给当前上传和下载的Peer</p>
	 */
	public void have(String infoHashHex, int index) {
		final var list = listActivePeer(infoHashHex);
		final AtomicInteger count = new AtomicInteger(0);
		list.stream()
			.forEach(session -> {
				final var peerConnect = session.peerConnect();
				final var peerLauncher = session.peerLauncher();
				if(peerConnect != null && peerConnect.available()) {
					count.incrementAndGet();
					peerConnect.have(index);
				} else if(peerLauncher != null && peerLauncher.available()) {
					count.incrementAndGet();
					peerLauncher.have(index);
				}
			});
		LOGGER.debug("发送have消息，通知Peer数量：{}", count.get());
	}
	
	/**
	 * <p>发送pex消息</p>
	 * <p>只发送给当前上传和下载的Peer</p>
	 */
	public void pex(String infoHashHex, List<PeerSession> optimize) {
		final byte[] bytes = PeerExchangeMessageHandler.buildMessage(optimize);
		if(bytes == null) {
			LOGGER.debug("发送pex消息失败：消息为空");
			return;
		}
		final var list = listActivePeer(infoHashHex);
		final AtomicInteger count = new AtomicInteger(0);
		list.stream()
			.forEach(session -> {
				final var peerConnect = session.peerConnect();
				final var peerLauncher = session.peerLauncher();
				if(peerConnect != null && peerConnect.available()) {
					count.incrementAndGet();
					peerConnect.pex(bytes);
				} else if(peerLauncher != null && peerLauncher.available()) {
					count.incrementAndGet();
					peerLauncher.pex(bytes);
				}
			});
		LOGGER.debug("发送pex消息，优质Peer数量：{}，通知Peer数量：{}", optimize.size(), count.get());
	}
	
	/**
	 * <p>发送uploadOnly消息</p>
	 * <p>只发送给当前上传和下载的Peer</p>
	 */
	public void uploadOnly(String infoHashHex) {
		final var list = listActivePeer(infoHashHex);
		final AtomicInteger count = new AtomicInteger(0);
		list.stream()
			.forEach(session -> {
				final var peerConnect = session.peerConnect();
				final var peerLauncher = session.peerLauncher();
				if(peerConnect != null && peerConnect.available()) {
					count.incrementAndGet();
					peerConnect.uploadOnly();
				} else if(peerLauncher != null && peerLauncher.available()) {
					count.incrementAndGet();
					peerLauncher.uploadOnly();
				}
			});
		LOGGER.debug("发送uploadOnly消息，通知Peer数量：{}", count.get());
	}
	
	/**
	 * 获取Peer队列
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
	 * 获取Peer存档队列
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
	 * <p>获取活动的Peer队列拷贝</p>
	 * <p>活动Peer：上传中、下载中</p>
	 */
	private List<PeerSession> listActivePeer(String infoHashHex) {
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
				return peer.equals(host);
			}).findFirst();
		if(optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

}
