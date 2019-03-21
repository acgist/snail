package com.acgist.snail.net.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.acgist.snail.net.peer.PeerClientGroup;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.utils.CollectionUtils;

/**
 * tracker分组<br>
 * 定时循环
 */
public class TrackerGroup {

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

}
