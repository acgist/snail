package com.acgist.snail.net.torrent.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig.Action;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>TrackerLauncher组</p>
 * <p>TrackerLauncher加载和管理</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TrackerLauncherGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncherGroup.class);
	
	private final TorrentSession torrentSession;
	private final List<TrackerLauncher> trackerLaunchers;
	
	private TrackerLauncherGroup(TorrentSession torrentSession) {
		this.torrentSession = torrentSession;
		this.trackerLaunchers = new ArrayList<>();
	}
	
	public static final TrackerLauncherGroup newInstance(TorrentSession torrentSession) {
		return new TrackerLauncherGroup(torrentSession);
	}

	/**
	 * @return 所有当前使用的Tracker服务器地址列表
	 */
	public List<String> trackers() {
		synchronized (this.trackerLaunchers) {
			return this.trackerLaunchers.stream()
				.map(launcher -> launcher.announceUrl())
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * <p>加载TrackerLauncher</p>
	 * <p>优先使用种子的Tracker，如果不够可以继续从系统Tracker列表添加（私有种子不添加）</p>
	 */
	public void loadTracker() throws DownloadException {
		List<TrackerClient> clients = null;
		final var action = this.torrentSession.action();
		if(action == Action.TORRENT) { // BT任务
			var torrent = this.torrentSession.torrent();
			clients = TrackerManager.getInstance().clients(torrent.getAnnounce(), torrent.getAnnounceList(), this.torrentSession.isPrivateTorrent());
		} else if(action == Action.MAGNET) { // 磁力链接任务
			var magnet = this.torrentSession.magnet();
			clients = TrackerManager.getInstance().clients(null, magnet.getTr());
		} else {
			LOGGER.warn("加载TrackerLauncher失败（未知动作）：{}", action);
			return;
		}
		clients.stream()
			.map(client -> {
				LOGGER.debug("加载TrackerLauncher：ID：{}，announceUrl：{}", client.id(), client.announceUrl());
				return TrackerManager.getInstance().newTrackerLauncher(client, this.torrentSession);
			})
			.filter(launcer -> launcer != null)
			.forEach(launcher -> {
				this.trackerLaunchers.add(launcher);
			});
	}

	/**
	 * <p>查询Peer</p>
	 */
	public void findPeer() {
		this.trackerLaunchers.forEach(launcher -> {
			launcher.findPeer();
		});
	}

	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		LOGGER.debug("释放TrackerLauncherGroup");
		this.trackerLaunchers.forEach(launcher -> {
			SystemThreadContext.submit(() -> {
				launcher.release();
			});
		});
		this.trackerLaunchers.clear();
	}
	
}
