package com.acgist.snail.net.bt;

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
 * <p>Torrent管理器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentManager {

//	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentManager.class);
	
	private static final TorrentManager INSTANCE = new TorrentManager();
	
	/**
	 * infoHashHex作为key
	 */
	private Map<String, TorrentSession> torrentSessions;
	
	private TorrentManager() {
		this.torrentSessions = new ConcurrentHashMap<>();
	}
	
	public static final TorrentManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 通过infoHashHex获取TorrentSession
	 */
	public TorrentSession torrentSession(String infoHashHex) {
		return this.torrentSessions.get(infoHashHex);
	}
	
	/**
	 * 删除种子信息
	 */
	public void remove(String infoHashHex) {
		synchronized (this.torrentSessions) {
			this.torrentSessions.remove(infoHashHex);
		}
	}
	
	/**
	 * 判断是否存在下载任务
	 */
	public boolean exist(String infoHashHex) {
		return this.torrentSessions.containsKey(infoHashHex);
	}
	
	/**
	 * <p>新建TorrentSession。</p>
	 * <p>如果已经存在infoHashHex，直接返回，反之使用path加载。</p>
	 */
	public TorrentSession newTorrentSession(String infoHashHex, String path) throws DownloadException {
		final var session = torrentSession(infoHashHex);
		if(session != null) {
			return session;
		}
		if(StringUtils.isEmpty(path)) {
			return newTorrentSession(InfoHash.newInstance(infoHashHex), null);
		} else {
			return newTorrentSession(path);
		}
	}

	/**
	 * 新建TorrentSession，如果已经存在返回已存在TorrentSession。
	 * 
	 * @param path torrent文件路径
	 */
	public TorrentSession newTorrentSession(String path) throws DownloadException {
		final Torrent torrent = loadTorrent(path);
		return newTorrentSession(torrent.getInfoHash(), torrent);
	}
	
	/**
	 * 新建TorrentSession，如果已经存在返回已存在TorrentSession。
	 */
	private TorrentSession newTorrentSession(InfoHash infoHash, Torrent torrent) throws DownloadException {
		if(infoHash == null) {
			throw new DownloadException("创建TorrentSession失败");
		}
		synchronized (this.torrentSessions) {
			final String infoHashHex = infoHash.infoHashHex();
			var torrentSession = this.torrentSessions.get(infoHashHex);
			if(torrentSession == null) {
				torrentSession = TorrentSession.newInstance(infoHash, torrent);
				this.torrentSessions.put(infoHashHex, torrentSession);
			}
			return torrentSession;
		}
	}
	
	/**
	 * 加载种子文件。
	 * 
	 * @param path torrent文件地址
	 */
	public static final Torrent loadTorrent(String path) throws DownloadException {
		final File file = new File(path);
		if(!file.exists()) {
			throw new DownloadException("种子文件不存在");
		}
		try {
			final var bytes = Files.readAllBytes(Paths.get(file.getPath()));
			final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
			final Map<String, Object> map = decoder.nextMap();
			if(map == null) {
				throw new DownloadException("种子文件格式错误");
			}
			
			final Torrent torrent = Torrent.valueOf(map);
			final Map<String, Object> info = BCodeDecoder.getMap(map, "info");
			final InfoHash infoHash = InfoHash.newInstance(BCodeEncoder.encodeMap(info));
			torrent.setInfoHash(infoHash);
			return torrent;
		} catch (IOException e) {
			throw new DownloadException("种子文件读取失败", e);
		}
	}

	/**
	 * 验证BT种子
	 */
	public static final boolean verify(String url) {
		return StringUtils.endsWith(url.toLowerCase(), TorrentProtocol.TORRENT_SUFFIX);
	}

}
