package com.acgist.snail.system.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.tracker.AbstractTrackerClient;
import com.acgist.snail.net.tracker.TrackerLauncher;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * tracker session管理<br>
 */
public class TrackerSessionManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentSessionManager.class);
	
	private static final TrackerSessionManager INSTANCE = new TrackerSessionManager();
	
	private TrackerSessionManager() {
		TRACKER_TORRENT_MAP = new ConcurrentHashMap<>();
	}

	public static final TrackerSessionManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * key：tracker session id
	 */
	private Map<Integer, TrackerLauncher> TRACKER_TORRENT_MAP;
	
	/**
	 * 新建
	 */
	public TrackerLauncher build(AbstractTrackerClient client, TorrentSession torrentSession) {
		final TrackerLauncher launcher = new TrackerLauncher(client, torrentSession);
		TRACKER_TORRENT_MAP.put(launcher.id(), launcher);
		return launcher;
	}

	/**
	 * 处理announce返回
	 */
	public void announce(AnnounceMessage message) {
		if(message == null) {
			return;
		}
		final Integer id = message.getId();
		TrackerLauncher trackerLauncher = TRACKER_TORRENT_MAP.get(id);
		if(trackerLauncher != null) {
			trackerLauncher.announce(message);
		} else {
			LOGGER.warn("不存在的TorrentSession，AnnounceMessage：{}", message);
		}
	}
	
}
