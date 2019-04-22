package com.acgist.snail.downloader.torrent.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.tracker.TrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.manager.TrackerClientManager;
import com.acgist.snail.system.manager.TrackerLauncherManager;

/**
 * tracker组<br>
 * 定时循环
 */
public class TrackerLauncherGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncherGroup.class);
	
//	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	/**
	 * tracker
	 */
	private final List<TrackerLauncher> trackerLaunchers;
	
	public TrackerLauncherGroup(TorrentSession torrentSession) {
//		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
		this.trackerLaunchers = new ArrayList<>();
	}

	/**
	 * 开始加载tracker
	 */
	public void loadTracker() throws DownloadException {
		var torrent = torrentSession.torrent();
		if(torrent == null) {
			throw new DownloadException("无效种子文件");
		}
		List<TrackerClient> clients = null;
		try {
			clients = TrackerClientManager.getInstance().clients(torrent.getAnnounce(), torrent.getAnnounceList());
		} catch (NetException e) {
			throw new DownloadException(e);
		}
		clients.stream()
		.map(client -> {
			LOGGER.debug("添加TrackerClient，ID：{}，announceUrl：{}", client.id(), client.announceUrl());
			return TrackerLauncherManager.getInstance().build(client, torrentSession);
		}).forEach(launcher -> {
			try {
				this.trackerLaunchers.add(launcher);
				this.torrentSession.timer(0, TimeUnit.SECONDS, launcher);
			} catch (Exception e) {
				LOGGER.error("Tracker执行异常", e);
			}
		});
	}

	/**
	 * 释放资源
	 */
	public void release() {
		LOGGER.debug("释放TrackerLauncherGroup");
		trackerLaunchers.forEach(launcher -> {
			launcher.release();
		});
	}
	
}
