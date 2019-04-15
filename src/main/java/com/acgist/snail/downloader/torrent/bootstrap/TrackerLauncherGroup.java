package com.acgist.snail.downloader.torrent.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.tracker.TrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.context.SystemThreadContext;
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
	 * 线程池
	 */
	private final ScheduledExecutorService executor;
	/**
	 * tracker
	 */
	private final List<TrackerLauncher> trackerLaunchers;
	
	public TrackerLauncherGroup(TorrentSession torrentSession) {
//		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
		this.trackerLaunchers = new ArrayList<>();
		final String name = SystemThreadContext.SNAIL_THREAD_TRACKER + "-" + torrentSession.infoHashHex();
		this.executor = SystemThreadContext.newScheduledExecutor(10, name);
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
		AtomicInteger index = new AtomicInteger(0);
		clients.stream()
		.map(client -> {
			LOGGER.debug("添加Tracker Client，ID：{}，announce：{}", client.id(), client.announceUrl());
			return TrackerLauncherManager.getInstance().build(client, torrentSession);
		}).forEach(launcher -> {
			try {
				this.trackerLaunchers.add(launcher);	
				this.timer(index.get() / 10 * TrackerClient.TIMEOUT, TimeUnit.SECONDS, launcher);
				index.incrementAndGet();
			} catch (Exception e) {
				LOGGER.error("Tracker执行异常", e);
			}
		});
	}

	public void timer(long delay, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			executor.schedule(runnable, delay, unit);
		}
	}
	
	/**
	 * 释放资源
	 */
	public void release() {
		trackerLaunchers.forEach(launcher -> {
			SystemThreadContext.submit(() -> {
				launcher.release();
			});
		});
		executor.shutdownNow();
	}
	
}
