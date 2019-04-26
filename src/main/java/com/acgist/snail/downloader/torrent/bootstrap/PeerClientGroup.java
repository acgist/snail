package com.acgist.snail.downloader.torrent.bootstrap;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
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
 * <p>PeerClient组</p>
 * <p>
 * 对正在进行下载的PeerClient管理：<br>
 * <ul>
 * 	<li>创建PeerClient。</li>
 * 	<li>定时替换下载最慢的PeerClient。</li>
 * </ul>
 * </p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerClientGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerClientGroup.class);
	
	private static final Duration INTERVAL = Duration.ofSeconds(SystemConfig.getPeerOptimizeInterval());
	
	private long lastOptimizeTime = System.currentTimeMillis();
	
	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	/**
	 * PeerClient下载队列
	 */
	private final BlockingQueue<PeerClient> peerClients;
	private final PeerSessionManager peerSessionManager;
	/**
	 * 优选的Peer，每次优化时挑选出来可以进行下载的Peer，在优化后发送ut_pex消息发送给连接的Peer，发送完成后清空
	 */
	private final List<PeerSession> optimize = new ArrayList<>();
	
	private PeerClientGroup(TorrentSession torrentSession) {
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
		this.peerClients = new LinkedBlockingQueue<>();
		this.peerSessionManager = PeerSessionManager.getInstance();
		optimizeTimer(); // 优化
	}
	
	public static final PeerClientGroup newInstance(TorrentSession torrentSession) {
		return new PeerClientGroup(torrentSession);
	}
	
	/**
	 * <p>创建下载线程（异步生成）</p>
	 * 
	 * @param size 指定生成数量
	 */
	public void launchers(int size) {
		synchronized (peerClients) {
			for (int index = 0; index < size; index++) {
				torrentSession.submit(() -> {
					buildPeerClient();
				});
			}
		}
	}
	
	/**
	 * <p>定时优化线程</p>
	 */
	public void optimizeTimer() {
		optimize(); // 优化PeerClient
		if(taskSession.download()) {
			SystemThreadContext.timer(INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
				optimizeTimer(); // 定时优化
			});
		}		
	}

	/**
	 * <p>优化PeerClient</p>
	 * <p>
	 * 挑选权重最低的PeerClient，剔除下载队列，将剔除的Peer插入到Peer队列头部，然后重新生成一个PeerClient。
	 * </p>
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
		peerSessionManager.exchange(torrentSession.infoHashHex(), optimize);
	}

	/**
	 * <p>资源释放</p>
	 * <p>释放所有正在下载的PeerClient。</p>
	 */
	public void release() {
		LOGGER.debug("释放PeerClientGroup");
		synchronized (peerClients) {
			peerClients.forEach(client -> {
				SystemThreadContext.submit(() -> {
					client.release();
				});
			});
		}
	}
	
	/**
	 * <p>新建PeerClient加入下载队列</p>
	 * <p>从Peer列表尾部拿出一个Peer创建下载</p>
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
	 * <p>剔除劣质PeerClient</p>
	 * <p>选择劣质PeerClient，释放资源，然后放入Peer队列头部。</p>
	 * 
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
	 * <p>选择劣质PeerClient</p>
	 * <p>
	 * 挑选权重最低的PeerClient作为劣质Peer，如果其中含有不可用的PeerClient，直接剔除该PeerClient，
	 * 但是依旧需要循环完所有的PeerClient，清除权重进行新一轮的权重计算。
	 * </p>
	 */
	private PeerClient pickInferiorPeerClient() {
		final int size = peerClients.size();
		if(size < SystemConfig.getPeerSize()) {
			return null;
		}
		int index = 0;
		int mark = 0, minMark = 0;
		PeerClient tmp = null; // 临时
		PeerClient inferior = null; // 劣质PeerClient
		while(true) {
			if(index++ >= size) {
				break;
			}
			tmp = peerClients.poll();
			if(tmp == null) {
				break;
			}
			mark = tmp.mark(); // 清空权重
			if(mark > 0) {
				optimize.add(tmp.peerSession());
			}
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
