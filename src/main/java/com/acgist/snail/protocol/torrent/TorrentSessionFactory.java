package com.acgist.snail.protocol.torrent;

import java.util.HashMap;
import java.util.Map;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.system.exception.DownloadException;

/**
 * TorrentSession工厂
 */
public class TorrentSessionFactory {

	private static final TorrentSessionFactory INSTANCE = new TorrentSessionFactory();
	
	/**
	 * hashHex作为key
	 */
	private Map<String, TorrentSession> TORRENT_SESSION_MAP;
	
	private TorrentSessionFactory() {
		TORRENT_SESSION_MAP = new HashMap<>();
	}
	
	public static final TorrentSessionFactory getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 新建session，如果已经存在返回已存在session
	 */
	public TorrentSession newSession(Torrent torrent, InfoHash infoHash) throws DownloadException {
		if(infoHash == null) {
			throw new DownloadException("infoHash不合法");
		}
		String key = infoHash.hashHex();
		var session = TORRENT_SESSION_MAP.get(key);
		if(session == null) {
			session = TorrentSession.newInstance(torrent, infoHash);
			TORRENT_SESSION_MAP.put(key, session);
		}
		return session;
	}
	
	/**
	 * 下载完成去掉下载信息
	 */
	public void complate() {
	}
	
}
