package com.acgist.snail.protocol.torrent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.BCodeUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * TorrentSession工厂
 */
public class TorrentSessionManager {

	private static final TorrentSessionManager INSTANCE = new TorrentSessionManager();
	
	/**
	 * hashHex作为key
	 */
	private Map<String, TorrentSession> TORRENT_SESSION_MAP;
	
	private TorrentSessionManager() {
		TORRENT_SESSION_MAP = new HashMap<>();
	}
	
	public static final TorrentSessionManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 新建session
	 * @param path torrent文件
	 */
	public TorrentSession buildSession(String path) throws DownloadException {
		ByteArrayInputStream input = loadTorrent(path);
		if(BCodeUtils.isMap(input)) {
			final Map<String, Object> map = BCodeUtils.d(input);
			final Torrent torrent = Torrent.valueOf(map);
			final Map<?, ?> info = (Map<?, ?>) map.get("info"); // 只需要数据不符
			InfoHash infoHash = InfoHashBuilder.newInstance().build(info).buildInfoHash();
			return buildSession(torrent, infoHash);
		} else {
			throw new DownloadException("不支持的种子");
		}
	}
	
	/**
	 * 新建session，如果已经存在返回已存在session
	 */
	private TorrentSession buildSession(Torrent torrent, InfoHash infoHash) throws DownloadException {
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
	
	/**
	 * 验证BT种子
	 */
	public static final boolean verify(String url) {
		return StringUtils.endsWith(url.toLowerCase(), TorrentProtocol.TORRENT_SUFFIX);
	}
	
	/**
	 * 加载种子文件
	 * @param path 文件地址
	 */
	private ByteArrayInputStream loadTorrent(String path) throws DownloadException {
		File file = new File(path);
		if(!file.exists()) {
			throw new DownloadException("种子文件不存在");
		}
		try {
			return new ByteArrayInputStream(Files.readAllBytes(Paths.get(file.getPath())));
		} catch (IOException e) {
			throw new DownloadException("种子文件读取失败", e);
		}
	}

}
