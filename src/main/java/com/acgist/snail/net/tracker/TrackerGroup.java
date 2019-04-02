package com.acgist.snail.net.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.PeerGroup;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.manager.TrackerClientManager;
import com.acgist.snail.system.manager.TrackerSessionManager;
import com.acgist.snail.utils.CollectionUtils;

/**
 * tracker分组<br>
 * 定时循环
 */
public class TrackerGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerGroup.class);
	
//	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	/**
	 * Peer组
	 */
	private final PeerGroup peerGroup;
	/**
	 * tracker
	 */
	private final List<TrackerLauncher> trackerLaunchers;
	
	public TrackerGroup(TorrentSession torrentSession) throws DownloadException {
//		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
		this.trackerLaunchers = new ArrayList<>();
		this.peerGroup = new PeerGroup(torrentSession);
	}

	/**
	 * 设置Peer
	 */
	public void peer(StatisticsSession statistics, Map<String, Integer> peers) {
		if(CollectionUtils.isEmpty(peers)) {
			return;
		}
		peers.forEach((host, port) -> {
			peerGroup.put(statistics, host, port);
		});
	}

	/**
	 * 开始加载tracker
	 */
	public void loadTracker() throws DownloadException {
		var torrent = torrentSession.torrent();
		if(torrent == null) {
			throw new DownloadException("无效种子文件");
		}
		try {
			List<TrackerClient> clients = TrackerClientManager.getInstance().clients(torrent.getAnnounce(), torrent.getAnnounceList());
			clients.stream()
			.map(client -> {
				LOGGER.debug("添加Tracker Client，ID：{}，announce：{}", client.id, client.announceUrl);
				return TrackerSessionManager.getInstance().build(client, torrentSession);
			})
			.forEach(launcher -> this.trackerLaunchers.add(launcher));
			this.trackerLaunchers.forEach(launcher -> {
				SystemThreadContext.submitTracker(launcher);
			});
		} catch (NetException e) {
			throw new DownloadException(e);
		}
	}

	/**
	 * 释放资源
	 */
	public void release() {
		trackerLaunchers.forEach(launcher -> launcher.release());
		peerGroup.release();
	}
	
}
