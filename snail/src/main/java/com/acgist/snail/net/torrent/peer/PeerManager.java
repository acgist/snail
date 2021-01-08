package com.acgist.snail.net.torrent.peer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.net.torrent.peer.extension.PeerExchangeMessageHandler;
import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>Peer管理器</p>
 * <p>Peer放入两个队列：{@linkplain #peers 下载队列}、{@linkplain #storagePeers 存档队列}</p>
 * 
 * @author acgist
 */
public final class PeerManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerManager.class);

	private static final PeerManager INSTANCE = new PeerManager();
	
	public static final PeerManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>have消息队列</p>
	 */
	private final Map<String, List<Integer>> haves;
	/**
	 * <p>Peer下载队列</p>
	 * <p>下载时Peer从队列中剔除，当Peer使用结束后重新放回下载队列。</p>
	 * <p>InfoHashHex=Peer双端队列（尾部优先使用）</p>
	 */
	private final Map<String, Deque<PeerSession>> peers;
	/**
	 * <p>Peer存档队列</p>
	 * <p>InfoHashHex=Peer队列</p>
	 */
	private final Map<String, List<PeerSession>> storagePeers;
	
	private PeerManager() {
		this.haves = new ConcurrentHashMap<>();
		this.peers = new ConcurrentHashMap<>();
		this.storagePeers = new ConcurrentHashMap<>();
		this.register();
	}
	
	/**
	 * <p>注册Have消息服务</p>
	 */
	private void register() {
		LOGGER.debug("注册Have消息服务：定时任务");
		final int interval = SystemConfig.getHaveInterval();
		SystemThreadContext.timerFixedDelay(
			interval,
			interval,
			TimeUnit.SECONDS,
			this::flushHave
		);
	}
	
	/**
	 * <p>查找PeerSession</p>
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
	 * <p>Peer存档队列拷贝</p>
	 * 
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
	 * <p>判断是否找到Peer</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return true-找到；false-没有找到；
	 */
	public boolean hasPeerSession(String infoHashHex) {
		final var list = this.list(infoHashHex);
		synchronized (list) {
			return !list.isEmpty();
		}
	}
	
	/**
	 * <p>删除InfoHashHex对应的所有队列</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 */
	public void remove(String infoHashHex) {
		// 删除have消息队列
		this.haves.remove(infoHashHex);
		// 删除Peer下载队列
		this.peers.remove(infoHashHex);
		// 删除Peer存档队列
		this.storagePeers.remove(infoHashHex);
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
	 * @param source Peer来源
	 * 
	 * @return PeerSession
	 * 
	 * TODO：去掉无效Peer（端口判断）
	 */
	public PeerSession newPeerSession(String infoHashHex, IStatisticsSession parent, String host, Integer port, PeerConfig.Source source) {
		synchronized (this) {
			final var list = this.list(infoHashHex); // 存档队列
			synchronized (list) {
				PeerSession peerSession = this.findPeerSession(list, host, port);
				if(peerSession == null) {
					LOGGER.debug("添加PeerSession：{}-{}，来源：{}", host, port, source);
					peerSession = PeerSession.newInstance(parent, host, port);
					final var deque = this.deque(infoHashHex); // 下载队列
					synchronized (deque) {
						if(source.preference()) {
							deque.offerLast(peerSession); // 插入尾部：优先级高
						} else {
							deque.offerFirst(peerSession); // 插入头部：优先级低
						}
					}
					list.add(peerSession); // 存档
				}
				peerSession.source(source); // 设置来源
				return peerSession;
			}
		}
	}
	
	/**
	 * <p>添加劣质Peer：插入头部</p>
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
	 * <p>添加优质Peer：插入尾部</p>
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
	 * <p>选择一个可用的Peer</p>
	 * <p>从下载队列尾部挑选</p>
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
	 * <p>放入have消息队列</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * @param index Piece索引
	 */
	public void have(String infoHashHex, int index) {
		final var list = this.listHave(infoHashHex);
		synchronized (list) {
			list.add(index);
		}
	}

	/**
	 * <p>发送所有have消息</p>
	 */
	private void flushHave() {
		LOGGER.debug("发送所有have消息");
		final List<String> keys;
		synchronized (this.haves) {
			keys = new ArrayList<>(this.haves.keySet());
		}
		keys.forEach(this::flushHave);
	}
	
	/**
	 * <p>发送have消息</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 */
	public void flushHave(String infoHashHex) {
		Integer[] indexArray;
		final var haveList = this.listHave(infoHashHex);
		synchronized (haveList) {
			indexArray = haveList.toArray(Integer[]::new);
			haveList.clear(); // 清空数据
		}
		if(ArrayUtils.isNotEmpty(indexArray)) {
			final var sessions = this.listConnectPeerSession(infoHashHex);
			final AtomicInteger count = new AtomicInteger(0);
			sessions.stream()
				.forEach(session -> {
					final var peerConnect = session.peerConnect();
					if(peerConnect != null && peerConnect.available()) {
						count.incrementAndGet();
						peerConnect.have(indexArray);
					}
				});
			LOGGER.debug("发送have消息：{}-{}", indexArray.length, count.get());
		}
	}
	
	/**
	 * <p>发送pex消息</p>
	 * <p>只发送给当前连接的Peer</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 */
	public void pex(String infoHashHex) {
		final var list = this.listConnectPeerSession(infoHashHex);
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
		LOGGER.debug("发送pex消息，通知Peer数量：{}", count.get());
	}
	
	/**
	 * <p>发送uploadOnly消息</p>
	 * <p>只发送给当前连接的Peer</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 */
	public void uploadOnly(String infoHashHex) {
		final var list = this.listConnectPeerSession(infoHashHex);
		final AtomicInteger count = new AtomicInteger(0);
		list.stream()
			.forEach(session -> {
				final var peerConnect = session.peerConnect();
				if(peerConnect != null && peerConnect.available()) {
					count.incrementAndGet();
					peerConnect.uploadOnly();
				}
			});
		LOGGER.debug("发送uploadOnly消息，通知Peer数量：{}", count.get());
	}
	
	/**
	 * <p>获取have消息队列</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return have消息队列
	 */
	private List<Integer> listHave(String infoHashHex) {
		synchronized (this.haves) {
			return this.haves.computeIfAbsent(infoHashHex, key -> new ArrayList<>());
		}
	}
	
	/**
	 * <p>获取任务下载队列</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return 任务下载队列
	 */
	private Deque<PeerSession> deque(String infoHashHex) {
		synchronized (this.peers) {
			return this.peers.computeIfAbsent(infoHashHex, key -> new LinkedBlockingDeque<>());
		}
	}

	/**
	 * <p>获取任务存档队列</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return 任务存档队列
	 */
	private List<PeerSession> list(String infoHashHex) {
		synchronized (this.storagePeers) {
			return this.storagePeers.computeIfAbsent(infoHashHex, key -> new ArrayList<>());
		}
	}
	
	/**
	 * <p>获取存档队列中当前连接的Peer队列拷贝</p>
	 * <p>连接中的Peer：上传中、下载中</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return 连接的Peer队列拷贝
	 */
	private List<PeerSession> listConnectPeerSession(String infoHashHex) {
		final var list = this.list(infoHashHex);
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
	 * @param host Peer地址
	 * @param port Peer端口
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
