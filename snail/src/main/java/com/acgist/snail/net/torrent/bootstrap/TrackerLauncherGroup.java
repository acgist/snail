package com.acgist.snail.net.torrent.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.PeerConfig.Action;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.exception.DownloadException;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * <p>Tracker执行器组</p>
 * <p>Tracker执行器加载和管理</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TrackerLauncherGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncherGroup.class);
	
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	/**
	 * <p>Tracker执行器集合</p>
	 */
	private final List<TrackerLauncher> trackerLaunchers;
	
	private TrackerLauncherGroup(TorrentSession torrentSession) {
		this.torrentSession = torrentSession;
		this.trackerLaunchers = new ArrayList<>();
	}
	
	/**
	 * <p>创建Tracker执行器组</p>
	 * 
	 * @param torrentSession BT任务信息
	 * 
	 * @return Tracker执行器组
	 */
	public static final TrackerLauncherGroup newInstance(TorrentSession torrentSession) {
		return new TrackerLauncherGroup(torrentSession);
	}

	/**
	 * <p>获取当前使用的所有Tracker服务器声明地址</p>
	 * 
	 * @return 当前使用的所有Tracker服务器声明地址
	 */
	public List<String> trackers() {
		synchronized (this.trackerLaunchers) {
			return this.trackerLaunchers.stream()
				.map(TrackerLauncher::announceUrl)
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * <p>加载TrackerLauncher</p>
	 * <p>优先使用种子的Tracker，如果数量不够可以从系统Tracker列表中添加。</p>
	 * <p>私有种子不从系统Tracker列表中添加</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	public void loadTracker() throws DownloadException {
		List<TrackerClient> clients = null;
		final var action = this.torrentSession.action(); // 下载动作
		if(action == Action.TORRENT) { // BT任务
			final var torrent = this.torrentSession.torrent();
			clients = TrackerManager.getInstance().clients(torrent.getAnnounce(), torrent.getAnnounceList(), this.torrentSession.isPrivateTorrent());
		} else if(action == Action.MAGNET) { // 磁力链接任务
			final var magnet = this.torrentSession.magnet();
			clients = TrackerManager.getInstance().clients(null, magnet.getTr());
		} else {
			LOGGER.warn("加载TrackerLauncher失败（未知动作）：{}", action);
			return;
		}
		clients.stream()
			.map(client -> TrackerManager.getInstance().buildTrackerLauncher(client, this.torrentSession))
			.forEach(launcher -> this.trackerLaunchers.add(launcher));
	}

	/**
	 * <p>查找Peer</p>
	 * 
	 * @see TrackerLauncher#findPeer()
	 */
	public void findPeer() {
		this.trackerLaunchers.forEach(launcher -> launcher.findPeer());
	}

	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		LOGGER.debug("释放TrackerLauncherGroup");
		this.trackerLaunchers.forEach(launcher -> SystemThreadContext.submit(
			() -> launcher.release()
		));
		this.trackerLaunchers.clear();
	}
	
}
