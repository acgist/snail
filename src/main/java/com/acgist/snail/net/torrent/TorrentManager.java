package com.acgist.snail.net.torrent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
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
	 * key=InfoHashHex
	 */
	private Map<String, TorrentSession> torrentSessions;
	
	private TorrentManager() {
		this.torrentSessions = new ConcurrentHashMap<>();
	}
	
	public static final TorrentManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 所有的InfoHash
	 */
	public List<InfoHash> allInfoHash() {
		synchronized (this.torrentSessions) {
			return this.torrentSessions.values().stream()
				.map(session -> session.infoHash())
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * 通过InfoHashHex获取TorrentSession
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
	 * <p>新建TorrentSession</p>
	 * <p>如果已经存在InfoHashHex，直接返回。</p>
	 * <p>如果不存在，path为空时使用InfoHashHex加载，反之使用path加载。</p>
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
	 * <p>新建TorrentSession</p>
	 * 
	 * @param path 种子文件路径
	 */
	public TorrentSession newTorrentSession(String path) throws DownloadException {
		final Torrent torrent = loadTorrent(path);
		return newTorrentSession(torrent.getInfoHash(), torrent);
	}
	
	/**
	 * 新建TorrentSession
	 */
	private TorrentSession newTorrentSession(InfoHash infoHash, Torrent torrent) throws DownloadException {
		if(infoHash == null) {
			throw new DownloadException("创建TorrentSession失败（InfoHash）");
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
	 * 种子文件加载
	 * 
	 * @param path 种子文件地址
	 */
	public static final Torrent loadTorrent(String path) throws DownloadException {
		final File file = new File(path);
		if(!file.exists()) {
			throw new DownloadException("种子文件不存在");
		}
		try {
			final var bytes = Files.readAllBytes(Paths.get(file.getPath()));
			final var decoder = BEncodeDecoder.newInstance(bytes);
			decoder.nextMap();
			if(decoder.isEmpty()) {
				throw new DownloadException("种子文件格式错误");
			}
			final var torrent = Torrent.valueOf(decoder);
			final var info = decoder.getMap("info");
			final var infoHash = InfoHash.newInstance(BEncodeEncoder.encodeMap(info));
			torrent.setInfoHash(infoHash);
			return torrent;
		} catch (DownloadException e) {
			throw e;
		} catch (NetException | IOException e) {
			throw new DownloadException("种子文件加载失败", e);
		}
	}

}
