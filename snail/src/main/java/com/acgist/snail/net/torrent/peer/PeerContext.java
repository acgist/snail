package com.acgist.snail.net.torrent.peer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.context.IContext;
import com.acgist.snail.context.IStatisticsSession;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.peer.extension.PeerExchangeMessageHandler;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.CollectionUtils;

/**
 * Peer上下文
 * 
 * @author acgist
 */
public final class PeerContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerContext.class);

	private static final PeerContext INSTANCE = new PeerContext();
	
	public static final PeerContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * have消息队列
	 * have消息不是每个Piece下载完成立即发出，使用定时任务减小交互次数。
	 */
	private final Map<String, List<Integer>> haves;
	/**
	 * Peer下载队列
	 * Peer使用时从下载队列中取出，使用结束后重新放回下载队列。
	 * InfoHashHex=Peer双端队列（尾部优先使用）
	 */
	private final Map<String, Deque<PeerSession>> activePeers;
	/**
	 * Peer存档列表
	 * InfoHashHex=Peer存档列表
	 */
	private final Map<String, List<PeerSession>> archivePeers;
	
	private PeerContext() {
		this.haves = new ConcurrentHashMap<>();
		this.activePeers = new ConcurrentHashMap<>();
		this.archivePeers = new ConcurrentHashMap<>();
	}
	
	/**
	 * 查找Peer信息
	 * 
	 * @param infoHashHex InfoHashHex
	 * @param host Peer地址
	 * @param port Peer端口
	 * 
	 * @return Peer信息
	 */
	public PeerSession findPeerSession(String infoHashHex, String host, Integer port) {
		final var list = this.list(infoHashHex);
		synchronized (list) {
			return this.findPeerSession(list, host, port);
		}
	}
	
	/**
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return Peer存档队列
	 */
	public List<PeerSession> listPeerSession(String infoHashHex) {
		final var list = this.list(infoHashHex);
		synchronized (list) {
			return new ArrayList<>(list);
		}
	}
	
	/**
	 * 判断是否找到Peer
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return 是否找到Peer
	 */
	public boolean isNotEmpty(String infoHashHex) {
		final var list = this.list(infoHashHex);
		synchronized (list) {
			return !list.isEmpty();
		}
	}
	
	/**
	 * 删除InfoHashHex所有队列
	 * 
	 * @param infoHashHex InfoHashHex
	 */
	public void remove(String infoHashHex) {
		LOGGER.debug("删除Peer队列：{}", infoHashHex);
		this.haves.remove(infoHashHex);
		this.activePeers.remove(infoHashHex);
		this.archivePeers.remove(infoHashHex);
	}
	
	/**
	 * 添加Peer
	 * 高优先级Peer插入尾部优先使用
	 * 
	 * @param infoHashHex InfoHashHex
	 * @param parent 任务下载统计
	 * @param host 地址
	 * @param port 端口
	 * @param source Peer来源
	 * 
	 * @return PeerSession
	 */
	public PeerSession newPeerSession(String infoHashHex, IStatisticsSession parent, String host, Integer port, PeerConfig.Source source) {
		synchronized (this) {
			final var list = this.list(infoHashHex);
			synchronized (list) {
				PeerSession peerSession = this.findPeerSession(list, host, port);
				if(peerSession == null) {
					LOGGER.debug("添加PeerSession：{}-{}-{}", host, port, source);
					peerSession = PeerSession.newInstance(parent, host, port);
					final var deque = this.deque(infoHashHex);
					synchronized (deque) {
						if(source.preference()) {
							// 插入尾部：优先级高
							deque.offerLast(peerSession);
						} else {
							// 插入头部：优先级低
							deque.offerFirst(peerSession);
						}
					}
					list.add(peerSession);
				}
				peerSession.source(source);
				return peerSession;
			}
		}
	}
	
	/**
	 * 添加劣质Peer：插入头部
	 * 
	 * @param infoHashHex InfoHashHex
	 * @param peerSession Peer信息
	 */
	public void inferior(String infoHashHex, PeerSession peerSession) {
		final var deque = this.deque(infoHashHex);
		synchronized (deque) {
			deque.offerFirst(peerSession);
		}
	}
	
	/**
	 * 添加优质Peer：插入尾部
	 * 
	 * @param infoHashHex InfoHashHex
	 * @param peerSession Peer信息
	 */
	public void preference(String infoHashHex, PeerSession peerSession) {
		final var deque = this.deque(infoHashHex);
		synchronized (deque) {
			deque.offerLast(peerSession);
		}
	}
	
	/**
	 * 挑选优质Peer信息
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return Peer信息
	 */
	public PeerSession pick(String infoHashHex) {
		final var deque = this.deque(infoHashHex);
		synchronized (deque) {
			int index = 0;
			final int size = deque.size();
			PeerSession peerSession = null;
			while(true) {
				if(++index > size) {
					break;
				}
				// 注意不要直接删除：防止重复进入下载队列添加不必要的验证
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
	 * 添加have消息
	 * 
	 * @param infoHashHex InfoHashHex
	 * @param index Piece索引
	 */
	public void have(String infoHashHex, int index) {
		final var list = this.haves(infoHashHex);
		synchronized (list) {
			list.add(index);
		}
	}

	/**
	 * 发送have消息
	 * 
	 * @param infoHashHex InfoHashHex
	 */
	public void have(String infoHashHex) {
		Integer[] indexArray;
		final var list = this.haves(infoHashHex);
		synchronized (list) {
			indexArray = list.toArray(Integer[]::new);
			list.clear();
		}
		if(ArrayUtils.isEmpty(indexArray)) {
			LOGGER.debug("发送have消息：没有数据");
			return;
		}
		final var sessions = this.listConnectPeerSession(infoHashHex);
		sessions.forEach(session -> {
			final var peerConnect = session.peerConnect();
			if(peerConnect != null && peerConnect.available()) {
				peerConnect.have(indexArray);
			}
		});
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("发送have消息：{}-{}-{}", infoHashHex, sessions.size(), indexArray.length);
		}
	}
	
	/**
	 * 发送PEX消息
	 * 
	 * @param infoHashHex InfoHashHex
	 */
	public void pex(String infoHashHex) {
		final var sessions = this.listConnectPeerSession(infoHashHex);
		// 优质Peer：下载数据
		final var optimize = sessions.stream()
			.filter(session -> session.getStatistics().getDownloadSize() > 0)
			.collect(Collectors.toList());
		final byte[] message = PeerExchangeMessageHandler.buildMessage(optimize);
		if(ArrayUtils.isEmpty(message)) {
			LOGGER.debug("发送PEX消息失败：没有数据");
			return;
		}
		sessions.forEach(session -> {
			final var peerConnect = session.peerConnect();
			if(peerConnect != null && peerConnect.available()) {
				peerConnect.pex(message);
			}
		});
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("发送PEX消息：{}-{}-{}", infoHashHex, sessions.size(), optimize.size());
		}
	}
	
	/**
	 * 发送uploadOnly消息
	 * 
	 * @param infoHashHex InfoHashHex
	 */
	public void uploadOnly(String infoHashHex) {
		final var sessions = this.listConnectPeerSession(infoHashHex);
		sessions.forEach(session -> {
			final var peerConnect = session.peerConnect();
			if(peerConnect != null && peerConnect.available()) {
				peerConnect.uploadOnly();
			}
		});
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("发送uploadOnly消息：{}-{}", infoHashHex, sessions.size());
		}
	}
	
	/**
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return have消息队列
	 */
	private List<Integer> haves(String infoHashHex) {
		synchronized (this.haves) {
			return this.haves.computeIfAbsent(infoHashHex, key -> new ArrayList<>());
		}
	}
	
	/**
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return Peer下载队列
	 */
	private Deque<PeerSession> deque(String infoHashHex) {
		synchronized (this.activePeers) {
			return this.activePeers.computeIfAbsent(infoHashHex, key -> new LinkedBlockingDeque<>());
		}
	}

	/**
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return Peer存档队列
	 */
	private List<PeerSession> list(String infoHashHex) {
		synchronized (this.archivePeers) {
			return this.archivePeers.computeIfAbsent(infoHashHex, key -> new ArrayList<>());
		}
	}
	
	/**
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return 存档队列连接中的Peer队列
	 */
	private List<PeerSession> listConnectPeerSession(String infoHashHex) {
		final var list = this.list(infoHashHex);
		if(CollectionUtils.isEmpty(list)) {
			return List.of();
		}
		synchronized (list) {
			return list.stream()
				.filter(PeerSession::available)
				.filter(PeerSession::connected)
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * 查找Peer信息
	 * 
	 * @param list 队列
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return Peer信息
	 */
	private PeerSession findPeerSession(List<PeerSession> list, String host, Integer port) {
		return list.stream()
			.filter(peer -> peer.equals(host, port))
			.findFirst()
			.orElse(null);
	}

}
