package com.acgist.snail.downloader.torrent.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TrackerManager;

/**
 * <p>TrackerLauncher组</p>
 * <p>加载TrackerClient管理，获取Peer。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerLauncherGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncherGroup.class);
	
//	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
	private final List<TrackerLauncher> trackerLaunchers;
	
	private TrackerLauncherGroup(TorrentSession torrentSession) {
//		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
		this.trackerLaunchers = new ArrayList<>();
	}
	
	public static final TrackerLauncherGroup newInstance(TorrentSession torrentSession) {
		return new TrackerLauncherGroup(torrentSession);
	}

	/**
	 * <p>加载TrackerClient</p>
	 * <p>
	 * 加载TrackerClient，优先使用种子的Tracker，如果不够可以继续从系统Tracker列表添加。
	 * 获取到Tracker列表加入定时线程池执行。
	 * </p>
	 */
	public void loadTracker() throws DownloadException {
		var torrent = torrentSession.torrent();
		if(torrent == null) {
			throw new DownloadException("无效种子文件");
		}
		TrackerManager.getInstance().clients(torrent.getAnnounce(), torrent.getAnnounceList()).stream()
		.map(client -> {
			LOGGER.debug("加载TrackerClient，ID：{}，announceUrl：{}", client.id(), client.announceUrl());
			return TrackerManager.getInstance().newTrackerLauncher(client, torrentSession);
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
		trackerLaunchers.clear();
	}
	
}
