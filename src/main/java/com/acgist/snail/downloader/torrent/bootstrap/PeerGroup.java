package com.acgist.snail.downloader.torrent.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
public class PeerGroup {

//	private static final Logger LOGGER = LoggerFactory.getLogger(PeerGroup.class);
	
	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	
	private final List<PeerLauncher> peerLaunchers;
	
	private final PeerSessionManager peerSessionManager;
	
	public PeerGroup(TorrentSession torrentSession) {
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
		this.peerLaunchers = Collections.synchronizedList(new ArrayList<>());
		this.peerSessionManager = PeerSessionManager.getInstance();
	}
	
	/**
	 * 初始化下载线程
	 */
	public void launchers() {
		final int size = SystemConfig.getPeerSize();
		synchronized (this) {
			while(true) {
				if(this.peerLaunchers.size() > size) {
					break;
				}
				buildPeerLauncher();
			}
		}
	}
	
	/**
	 * 拿去最后一个session创建launcher
	 */
	private void buildPeerLauncher() {
		final PeerSession peerSession = peerSessionManager.pick(torrentSession.infoHashHex());
		if(peerSession != null) {
			// TODO
			return;
		}
	}
	
	/**
	 * 劣质的launcher
	 */
	private void inferiorLauncher() {
		// TODO
		PeerLauncher launcher = null;
		peerSessionManager.inferior(torrentSession.infoHashHex(), launcher.peerSession());
	}

	/**
	 * 优化下载Peer，权重最低的剔除，然后插入队列头部，然后启动队列最后一个Peer
	 */
	public void optimize() {
		inferiorLauncher();
		buildPeerLauncher();
		if(taskSession.download()) {
			// TODO：暂停，然后开始导致多个重复线程
			final int interval = SystemConfig.getPeerOptimizeInterval();
			SystemThreadContext.timer(interval, TimeUnit.SECONDS, () -> {
				optimize(); // 定时优化
			});
		}
	}

	/**
	 * 资源释放
	 */
	public void release() {
		peerLaunchers.forEach(launcher -> launcher.release());
	}

}
