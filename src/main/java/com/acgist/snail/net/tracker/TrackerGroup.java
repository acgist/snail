package com.acgist.snail.net.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.PeerClientGroup;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.manager.TrackerClientManager;
import com.acgist.snail.utils.CollectionUtils;

/**
 * tracker分组<br>
 * 定时循环
 */
public class TrackerGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerGroup.class);
	
	/**
	 * tracker信息
	 */
	private List<TrackerLauncher> trackers;
	/**
	 * Peer组
	 */
	private PeerClientGroup peerClientGroup;
	
	public TrackerGroup() {
		this.trackers = new ArrayList<>();
		this.peerClientGroup = new PeerClientGroup();
	}

	/**
	 * 设置Peer
	 */
	public void peer(StatisticsSession statistics, Map<String, Integer> peers) {
		if(CollectionUtils.isEmpty(peers)) {
			return;
		}
		peers.forEach((host, port) -> {
			peerClientGroup.put(statistics, host, port);
		});
	}

	/**
	 * 开始加载tracker
	 */
	public void loadTracker(TorrentSession session) throws DownloadException {
		var torrent = session.torrent();
		if(torrent == null) {
			throw new DownloadException("无效种子文件");
		}
		try {
			List<AbstractTrackerClient> clients = TrackerClientManager.getInstance().clients(torrent.getAnnounce(), torrent.getAnnounceList());
			if(LOGGER.isDebugEnabled()) {
				clients.forEach(client -> {
					LOGGER.debug("添加Tracker Client，ID：{}，announce：{}", client.id, client.announceUrl);
				});
			}
			this.trackers = clients.stream()
			.map(client -> new TrackerLauncher(client, session))
			.collect(Collectors.toList());
			this.trackers.forEach(launcher -> {
				SystemThreadContext.submit(launcher);
			});
		} catch (NetException e) {
			throw new DownloadException(e);
		}
	}

}
