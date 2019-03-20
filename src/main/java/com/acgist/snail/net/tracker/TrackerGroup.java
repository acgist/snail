package com.acgist.snail.net.tracker;

import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.net.peer.PeerClientGroup;

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

	
	
}
