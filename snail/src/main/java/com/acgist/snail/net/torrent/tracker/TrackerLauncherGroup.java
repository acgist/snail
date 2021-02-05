package com.acgist.snail.net.torrent.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.PeerConfig.Action;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.context.TrackerContext;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.session.TrackerSession;

/**
 * <p>Tracker执行器组</p>
 * <p>Tracker执行器加载和管理</p>
 * 
 * @author acgist
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
	
	/**
	 * @param torrentSession BT任务信息
	 */
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
	 */
	public void loadTracker() {
		List<TrackerSession> sessions = null;
		final var action = this.torrentSession.action();
		if(action == Action.TORRENT) {
			final var torrent = this.torrentSession.torrent();
			sessions = TrackerContext.getInstance().sessions(torrent.getAnnounce(), torrent.getAnnounceList(), this.torrentSession.privateTorrent());
		} else if(action == Action.MAGNET) {
			final var magnet = this.torrentSession.magnet();
			sessions = TrackerContext.getInstance().sessions(magnet.getTr());
		} else {
			sessions = TrackerContext.getInstance().sessions();
		}
		final var list = sessions.stream()
			.map(client -> TrackerContext.getInstance().buildTrackerLauncher(client, this.torrentSession))
			.collect(Collectors.toList());
		synchronized (this.trackerLaunchers) {
			this.trackerLaunchers.addAll(list);
		}
	}

	/**
	 * <p>查找Peer</p>
	 * 
	 * @see TrackerLauncher#findPeer()
	 */
	public void findPeer() {
		final List<TrackerLauncher> list;
		// 新建集合进行查找：防止释放资源等待
		synchronized (this.trackerLaunchers) {
			list = new ArrayList<>(this.trackerLaunchers);
		}
		list.forEach(TrackerLauncher::findPeer);
	}

	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		LOGGER.debug("释放TrackerLauncherGroup");
		synchronized (this.trackerLaunchers) {
			this.trackerLaunchers.forEach(launcher -> SystemThreadContext.submit(launcher::release));
			this.trackerLaunchers.clear();
		}
	}
	
}
