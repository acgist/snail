package com.acgist.snail.system.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.TorrentProtocol;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;

/**
 * TorrentSession工厂
 */
public class TorrentManager {

//	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentManager.class);
	
	private static final TorrentManager INSTANCE = new TorrentManager();
	
	/**
	 * infoHashHex作为key
	 */
	private Map<String, TorrentSession> TORRENT_SESSION_MAP;
	
	private TorrentManager() {
		TORRENT_SESSION_MAP = new ConcurrentHashMap<>();
	}
	
	public static final TorrentManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 通过infoHash获取TorrentSession
	 */
	public TorrentSession torrentSession(String infoHashHex) {
		return TORRENT_SESSION_MAP.get(infoHashHex);
	}
	
	/**
	 * 新建session，如果已经存在infoHashHex，直接返回反之使用path加载
	 */
	public TorrentSession newTorrentSession(String infoHashHex, String path) throws DownloadException {
		var session = torrentSession(infoHashHex);
		if(session != null) {
			return session;
		}
		return newTorrentSession(path);
	}

	/**
	 * 新建session
	 * @param path torrent文件
	 */
	public TorrentSession newTorrentSession(String path) throws DownloadException {
		final var bytes = loadTorrent(path);
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		final Map<String, Object> map = decoder.mustMap();
		final Torrent torrent = Torrent.valueOf(map);
		final Map<?, ?> info = (Map<?, ?>) map.get("info"); // 只需要数据不符
		final InfoHash infoHash = InfoHash.newInstance(BCodeEncoder.mapToBytes(info));
		return newTorrentSession(torrent, infoHash);
	}
	
	/**
	 * 新建session，如果已经存在返回已存在session
	 */
	private TorrentSession newTorrentSession(Torrent torrent, InfoHash infoHash) throws DownloadException {
		if(infoHash == null) {
			throw new DownloadException("infoHash不合法");
		}
		String key = infoHash.infoHashHex();
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
	private byte[] loadTorrent(String path) throws DownloadException {
		File file = new File(path);
		if(!file.exists()) {
			throw new DownloadException("种子文件不存在");
		}
		try {
			return Files.readAllBytes(Paths.get(file.getPath()));
		} catch (IOException e) {
			throw new DownloadException("种子文件读取失败", e);
		}
	}

}
