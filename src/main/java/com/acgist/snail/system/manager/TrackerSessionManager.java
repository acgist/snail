package com.acgist.snail.system.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.tracker.TrackerLauncher;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.CollectionUtils;

/**
 * tracker session管理
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
	public void build(TorrentSession torrentSession) {
	}

	/**
	 * 处理announce返回
	 */
	public void announce(AnnounceMessage message) {
		if(message == null) {
			return;
		}
		final Integer id = message.getId();
		TrackerLauncher session = TRACKER_TORRENT_MAP.get(id);
		if(session != null) {
			session.announce(message);
		} else {
			LOGGER.warn("不存在的TorrentSession，ID：{}", id);
		}
	}
	
}
