package com.acgist.snail.net.peer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;

/**
 * PeerClient分组<br>
 * 每次剔除权重的一个PeerClient<br>
 */
public class PeerClientGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerClientGroup.class);
	
	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	/**
	 * 双端队列，新加入插入队尾，剔除的Peer插入对头
	 */
	private final Deque<PeerSession> peers;
	private final List<PeerLauncher> launchers;
	
	public PeerClientGroup(TorrentSession torrentSession) {
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
		this.peers = new LinkedBlockingDeque<>();
		this.launchers = Collections.synchronizedList(new ArrayList<>());
	}
	
	/**
	 * 初始化下载线程
	 */
	private void launchers() {
		final int size = SystemConfig.getPeerSize();
		if(this.launchers.size() > size) {
			return;
		}
		buildPeerLauncher();
	}
	
	/**
	 * 拿去最后一个session创建launcher
	 */
	private void buildPeerLauncher() {
		PeerSession peerSession = peers.pollLast();
		if(peerSession == null) {
			// TODO
			return;
		}
	}
	
	/**
	 * 获取劣质的launcher
	 */
	private PeerLauncher inferiorLauncher() {
		// TODO
		return null;
	}

	/**
	 * 优化下载Peer，权重最低的剔除，然后插入队列头部，然后启动队列最后一个Peer
	 */
	public void optimize() {
		PeerLauncher launcher = inferiorLauncher();
		peers.offerFirst(launcher.peerSession());
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
	 * 新增Peer
	 * @param parent torrent下载统计
	 * @param host 地址
	 * @param port 端口
	 */
	public void put(StatisticsSession parent, String host, Integer port) {
		synchronized (peers) {
			final boolean exist = peers.stream().anyMatch(peer -> {
				return peer.exist(host, port);
			});
			if(exist) {
				return;
			}
			LOGGER.debug("添加Peer，HOST：{}，PORT：{}", host, port);
			peers.offerLast(new PeerSession(parent, host, port));
			launchers();
		}
	}

}
