package com.acgist.snail.downloader.torrent.bootstrap;

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
 * Peer组<br>
 * 每次剔除权重的一个PeerClient<br>
 */
public class PeerClientGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerClientGroup.class);
	
	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	
	private final BlockingQueue<PeerClient> peerClients;
	
	private final PeerSessionManager peerSessionManager;
	
	public PeerClientGroup(TorrentSession torrentSession) {
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
		this.peerClients = new LinkedBlockingQueue<>();
		this.peerSessionManager = PeerSessionManager.getInstance();
		optimize(); // 优化
	}
	
	/**
	 * 创建下载线程
	 */
	public void launchers() {
		final int size = SystemConfig.getPeerSize();
		synchronized (peerClients) {
			while(true) {
				if(this.peerClients.size() >= size) {
					break;
				}
				final boolean ok = buildPeerClient();
				if(!ok) {
					break;
				}
			}
		}
	}

	/**
	 * 优化下载Peer，权重最低的剔除，然后插入队列头部，然后启动队列最后一个Peer
	 */
	public void optimize() {
		synchronized (peerClients) {
			LOGGER.debug("优化Peer");
			final boolean ok = inferiorPeerClient();
			if(ok) {
				buildPeerClient();
			}
			if(taskSession.download()) {
				final int interval = SystemConfig.getPeerOptimizeInterval();
				SystemThreadContext.timer(interval, TimeUnit.SECONDS, () -> {
					optimize(); // 定时优化
				});
			}
		}
	}

	/**
	 * 资源释放
	 */
	public void release() {
		synchronized (peerClients) {
			peerClients.forEach(client -> {
				SystemThreadContext.submit(() -> {
					LOGGER.debug("Peer关闭：{}:{}", client.peerSession().host(), client.peerSession().port());
					client.release();
				});
			});
		}
	}
	
	/**
	 * 拿去最后一个session创建PeerClient
	 */
	private boolean buildPeerClient() {
		if(!taskSession.download()) {
			return false;
		}
		final PeerSession peerSession = peerSessionManager.pick(torrentSession.infoHashHex());
		if(peerSession != null) {
			final PeerClient client = new PeerClient(peerSession, torrentSession);
			final boolean ok = client.download();
			if(ok) {
				peerClients.add(client);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 劣质的PeerClient
	 */
	private boolean inferiorPeerClient() {
		if(peerClients.isEmpty()) {
			return false;
		}
		PeerClient peerClient = pickInferior();
		if(peerClient != null) {
			LOGGER.debug("剔除劣质PeerClient：{}:{}", peerClient.peerSession().host(), peerClient.peerSession().port());
			peerSessionManager.inferior(torrentSession.infoHashHex(), peerClient.peerSession());
			return true;
		}
		return false;
	}
	
	private PeerClient pickInferior() {
		final int size = peerClients.size();
		if(size < SystemConfig.getPeerSize()) {
			return null;
		}
		int index = 0;
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
			if(inferior == null) {
				inferior = tmp;
			} else if(tmp.mark() < inferior.mark()) {
				if(inferior != null) {
					peerClients.offer(inferior);
				}
				inferior = tmp;
			} else {
				peerClients.offer(tmp);
			}
		}
		return inferior;
	}

}
