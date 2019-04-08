package com.acgist.snail.system.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.tracker.TrackerClient;
import com.acgist.snail.net.tracker.TrackerLauncher;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * tracker session管理<br>
 */
public class TrackerLauncherManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncherManager.class);
	
	private static final TrackerLauncherManager INSTANCE = new TrackerLauncherManager();
	
	private TrackerLauncherManager() {
		TRACKER_TORRENT_MAP = new ConcurrentHashMap<>();
	}

	public static final TrackerLauncherManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * key：tracker session id
	 */
	private Map<Integer, TrackerLauncher> TRACKER_TORRENT_MAP;
	
	/**
	 * 新建
	 */
	public TrackerLauncher build(TrackerClient client, TorrentSession torrentSession) {
		final TrackerLauncher launcher = TrackerLauncher.newInstance(client, torrentSession);
		register(launcher);
		return launcher;
	}
	
	/**
	 * 注册
	 */
	private void register(TrackerLauncher launcher) {
		LOGGER.debug("注册TrackerLauncher，ID：{}", launcher.id());
		TRACKER_TORRENT_MAP.put(launcher.id(), launcher);
	}

	/**
	 * 处理announce返回
	 */
	public void announce(AnnounceMessage message) {
		if(message == null) {
			return;
		}
		final Integer id = message.getId();
		final TrackerLauncher trackerLauncher = TRACKER_TORRENT_MAP.get(id);
		if(trackerLauncher != null) {
			trackerLauncher.announce(message);
		} else {
			LOGGER.warn("不存在的TorrentSession，AnnounceMessage：{}", message);
		}
	}

}
