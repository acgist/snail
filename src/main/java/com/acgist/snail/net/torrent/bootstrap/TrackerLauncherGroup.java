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
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>TrackerLauncher组</p>
 * <p>加载TrackerClient管理，获取Peer。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerLauncherGroup {

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
	 * 获取当前使用的Tracker列表
	 */
	public List<String> trackers() {
		synchronized (this.trackerLaunchers) {
			return this.trackerLaunchers.stream()
				.map(launcher -> launcher.announceUrl())
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * <p>加载TrackerClient</p>
	 * <p>
	 * 加载TrackerClient，优先使用种子的Tracker，如果不够可以继续从系统Tracker列表添加。
	 * 获取到Tracker列表加入定时线程池执行。
	 * </p>
	 */
	public void loadTracker() throws DownloadException {
		List<TrackerClient> clients = null;
		var action = this.torrentSession.action();
		if(action == Action.torrent) {
			var torrent = this.torrentSession.torrent();
			clients = TrackerManager.getInstance().clients(torrent.getAnnounce(), torrent.getAnnounceList());
		} else if(action == Action.magnet) {
			var magnet = this.torrentSession.magnet();
			clients = TrackerManager.getInstance().clients(null, magnet.getTr());
		} else {
			LOGGER.warn("加载TrackerClient时出现未知动作：{}", action);
		}
		if(CollectionUtils.isEmpty(clients)) {
			return;
		}
		clients.stream()
		.map(client -> {
			LOGGER.debug("加载TrackerClient，ID：{}，announceUrl：{}", client.id(), client.announceUrl());
			return TrackerManager.getInstance().newTrackerLauncher(client, this.torrentSession);
		}).forEach(launcher -> {
			try {
				this.trackerLaunchers.add(launcher);
			} catch (Exception e) {
				LOGGER.error("Tracker执行异常", e);
			}
		});
	}

	/**
	 * 查询Peer
	 */
	public void findPeer() {
		this.trackerLaunchers.forEach(launcher -> {
			launcher.findPeer();
		});
	}

	/**
	 * 释放资源
	 */
	public void release() {
		LOGGER.debug("释放TrackerLauncherGroup");
		this.trackerLaunchers.forEach(launcher -> {
			launcher.release();
		});
		this.trackerLaunchers.clear();
	}
	
}
