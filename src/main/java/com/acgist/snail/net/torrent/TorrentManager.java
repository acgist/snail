package com.acgist.snail.net.torrent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.bean.Torrent;
import com.acgist.snail.pojo.session.TorrentSession;
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
public final class TorrentManager {

//	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentManager.class);
	
	private static final TorrentManager INSTANCE = new TorrentManager();
	
	/**
	 * <p>BT任务MAP</p>
	 * <p>InfoHashHex=BT任务</p>
	 */
	private Map<String, TorrentSession> torrentSessions;
	
	private TorrentManager() {
		this.torrentSessions = new ConcurrentHashMap<>();
	}
	
	public static final TorrentManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * @return 所有的InfoHash拷贝
	 */
	public List<InfoHash> allInfoHash() {
		synchronized (this.torrentSessions) {
			return this.torrentSessions.values().stream()
				.map(session -> session.infoHash())
				.collect(Collectors.toList());
		}
	}

	/**
	 * @return 所有的TorrentSession拷贝
	 */
	public List<TorrentSession> allTorrentSession() {
		synchronized (this.torrentSessions) {
			return this.torrentSessions.values().stream()
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * <p>获取BT任务信息</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return BT任务信息
	 */
	public TorrentSession torrentSession(String infoHashHex) {
		return this.torrentSessions.get(infoHashHex);
	}
	
	/**
	 * <p>删除TorrentSession</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 */
	public void remove(String infoHashHex) {
		synchronized (this.torrentSessions) {
			this.torrentSessions.remove(infoHashHex);
		}
	}
	
	/**
	 * <p>判断是否存在下载任务</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return 任务是否存在
	 */
	public boolean exist(String infoHashHex) {
		return this.torrentSessions.containsKey(infoHashHex);
	}
	
	/**
	 * <p>新建TorrentSession</p>
	 * <p>如果已存在InfoHashHex：直接返回</p>
	 * <p>如果不存在InfoHashHex：path为空时使用InfoHashHex加载、path不为空使用path加载</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * @param path 种子文件路径
	 * 
	 * @return BT任务信息
	 * 
	 * @throws DownloadException 下载异常
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
	 * 
	 * @return BT任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	public TorrentSession newTorrentSession(String path) throws DownloadException {
		final Torrent torrent = loadTorrent(path);
		return newTorrentSession(torrent.infoHash(), torrent);
	}
	
	/**
	 * <p>新建TorrentSession</p>
	 * 
	 * @param infoHash InfoHash
	 * @param torrent 种子信息
	 * 
	 * @return BT任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	private TorrentSession newTorrentSession(InfoHash infoHash, Torrent torrent) throws DownloadException {
		if(infoHash == null) {
			throw new DownloadException("创建TorrentSession失败（InfoHash为空）");
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
	 * <p>种子文件加载</p>
	 * 
	 * @param path 种子文件地址
	 * 
	 * @return 种子信息
	 * 
	 * @throws DownloadException 下载异常
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
			// 直接转储原始信息：防止顺序不对导致种子Hash计算错误
			final var info = decoder.getMap("info");
			final var infoHash = InfoHash.newInstance(BEncodeEncoder.encodeMap(info));
			torrent.infoHash(infoHash);
			return torrent;
		} catch (DownloadException e) {
			throw e;
		} catch (NetException | IOException e) {
			throw new DownloadException("种子文件加载失败", e);
		}
	}

}
