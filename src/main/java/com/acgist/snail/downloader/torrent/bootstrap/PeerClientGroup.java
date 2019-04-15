package com.acgist.snail.downloader.torrent.bootstrap;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.PeerClient;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.manager.PeerSessionManager;

/**
 * Peer组<br>
 * 每次剔除权重的一个PeerClient<br>
 */
public class PeerClientGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerClientGroup.class);
	
	private static final Duration INTERVAL = Duration.ofSeconds(SystemConfig.getPeerOptimizeInterval());
	
	private long lastOptimizeTime = System.currentTimeMillis();
	
	/**
	 * 线程池
	 */
	private final ExecutorService executor;
	
	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	private final BlockingQueue<PeerClient> peerClients;
	private final PeerSessionManager peerSessionManager;
	
	public PeerClientGroup(TorrentSession torrentSession) {
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
		this.peerClients = new LinkedBlockingQueue<>();
		this.peerSessionManager = PeerSessionManager.getInstance();
		this.executor = SystemThreadContext.newExecutor(10, 10, 100, 60L, SystemThreadContext.SNAIL_THREAD_PEER);
		optimizeTimer(); // 优化
	}
	
	/**
	 * 创建下载线程
	 */
	public void launchers(int size) {
		synchronized (peerClients) {
			for (int index = 0; index < size; index++) {
				executor.submit(() -> {
					buildPeerClient();
				});
			}
		}
	}
	
	/**
	 * 定时优化线程
	 */
	public void optimizeTimer() {
		synchronized (peerClients) {
			optimize();
			if(taskSession.download()) {
				SystemThreadContext.timer(INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
					optimizeTimer(); // 定时优化
				});
			}		
		}
	}

	/**
	 * 优化下载Peer，权重最低的剔除，然后插入队列头部，然后启动队列最后一个Peer
	 */
	public void optimize() {
		synchronized (peerClients) {
			final long now = System.currentTimeMillis();
			if(now - lastOptimizeTime >= INTERVAL.toMillis()) {
				lastOptimizeTime = now;
			} else {
				return;
			}
			LOGGER.debug("优化PeerClient");
			final boolean ok = inferiorPeerClient();
			if(ok) {
				launchers(1);
			}
		}
	}

	/**
	 * 资源释放
	 */
	public void release() {
		LOGGER.debug("释放PeerClientGroup");
		synchronized (peerClients) {
			peerClients.forEach(client -> {
				SystemThreadContext.submit(() -> {
					LOGGER.debug("Peer关闭：{}:{}", client.peerSession().host(), client.peerSession().port());
					client.release();
				});
			});
			executor.shutdownNow();
		}
	}
	
	/**
	 * 拿去最后一个session创建PeerClient
	 */
	private void buildPeerClient() {
		if(!taskSession.download()) {
			return;
		}
		if(this.peerClients.size() >= SystemConfig.getPeerSize()) {
			return;
		}
		final PeerSession peerSession = peerSessionManager.pick(torrentSession.infoHashHex());
		if(peerSession != null) {
			final PeerClient client = new PeerClient(peerSession, torrentSession);
			// TODO：验证是否含有对应未下载的Piece
			final boolean ok = client.download();
			if(ok) {
				peerClients.add(client);
			}
		}
	}
	
	/**
	 * 劣质的PeerClient
	 * @return true-剔除成功；false-剔除失败
	 */
	private boolean inferiorPeerClient() {
		if(peerClients.isEmpty()) {
			return false;
		}
		final PeerClient peerClient = pickInferiorPeerClient();
		if(peerClient != null) {
			LOGGER.debug("剔除劣质PeerClient：{}:{}", peerClient.peerSession().host(), peerClient.peerSession().port());
			peerClient.release();
			peerSessionManager.inferior(torrentSession.infoHashHex(), peerClient.peerSession());
			return true;
		}
		return false;
	}
	
	/***
	 * 选择劣质PeerClient
	 */
	private PeerClient pickInferiorPeerClient() {
		final int size = peerClients.size();
		if(size < SystemConfig.getPeerSize()) {
			return null;
		}
		int index = 0;
		int mark = 0, minMark = 0;
		PeerClient tmp = null; // 临时
		PeerClient inferior = null; // 劣质Client
		while(true) {
			if(index++ >= size) {
				break;
			}
			tmp = peerClients.poll();
			if(tmp == null) {
				break;
			}
			mark = tmp.mark(); // 清空分数
			if(inferior != null && !inferior.available()) { // 如果当前挑选的是不可用的PeerClient不执行后面操作
				continue;
			}
			if(inferior == null) {
				inferior = tmp;
				minMark = mark;
			} else if(!tmp.available()) {
				inferior = tmp;
			} else if(mark < minMark) {
				peerClients.offer(inferior);
				inferior = tmp;
				minMark = mark;
			} else {
				peerClients.offer(tmp);
			}
		}
		return inferior;
	}

}
