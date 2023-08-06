package com.acgist.snail.net.torrent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.acgist.snail.context.IContext;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Torrent上下文</p>
 * 
 * @author acgist
 */
public final class TorrentContext implements IContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentContext.class);
	
	private static final TorrentContext INSTANCE = new TorrentContext();
	
	public static final TorrentContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>BT任务信息MAP</p>
	 * <p>InfoHashHex=BT任务信息</p>
	 */
	private final Map<String, TorrentSession> torrentSessions;
	
	private TorrentContext() {
		this.torrentSessions = new ConcurrentHashMap<>();
	}
	
	/**
	 * <p>获取所有InfoHash拷贝</p>
	 * 
	 * @return 所有InfoHash拷贝
	 */
	public List<InfoHash> allInfoHash() {
		return this.torrentSessions.values().stream()
			.map(TorrentSession::infoHash)
			.collect(Collectors.toList());
	}

	/**
	 * <p>获取所有TorrentSession拷贝</p>
	 * 
	 * @return 所有TorrentSession拷贝
	 */
	public List<TorrentSession> allTorrentSession() {
		return this.torrentSessions.values().stream()
			.collect(Collectors.toList());
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
	 * <p>删除BT任务信息</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return BT任务信息
	 */
	public TorrentSession remove(String infoHashHex) {
		LOGGER.debug("删除BT任务信息：{}", infoHashHex);
		return this.torrentSessions.remove(infoHashHex);
	}
	
	/**
	 * <p>判断BT任务信息是否存在</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * 
	 * @return 是否存在
	 */
	public boolean exist(String infoHashHex) {
		return this.torrentSessions.containsKey(infoHashHex);
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
		return this.newTorrentSession(torrent.infoHash(), torrent);
	}
	
	/**
	 * <p>新建TorrentSession</p>
	 * <p>种子文件路径可以为空：磁力链接只要InfoHashHex</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * @param path 种子文件路径
	 * 
	 * @return BT任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	public TorrentSession newTorrentSession(String infoHashHex, String path) throws DownloadException {
		final var session = this.torrentSession(infoHashHex);
		if(session != null) {
			return session;
		}
		if(StringUtils.isEmpty(path)) {
			return this.newTorrentSession(InfoHash.newInstance(infoHashHex), null);
		} else {
			return this.newTorrentSession(path);
		}
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
			throw new DownloadException("新建TorrentSession失败（InfoHash为空）");
		}
		final String infoHashHex = infoHash.getInfoHashHex();
		var torrentSession = this.torrentSession(infoHashHex);
		if(torrentSession == null) {
			torrentSession = TorrentSession.newInstance(infoHash, torrent);
			this.torrentSessions.put(infoHashHex, torrentSession);
		}
		return torrentSession;
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
			throw new DownloadException("不存在的种子文件");
		}
		try {
			final var bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
			final var decoder = BEncodeDecoder.newInstance(bytes).next();
			if(decoder.isEmpty()) {
				throw new DownloadException("种子文件格式错误");
			}
			final var torrent = Torrent.valueOf(decoder);
			// 直接转储原始信息：防止顺序不对导致种子Hash计算错误
			final var info = decoder.getMap(Torrent.ATTR_INFO);
			final var infoHash = InfoHash.newInstance(BEncodeEncoder.encodeMap(info));
			torrent.infoHash(infoHash);
			return torrent;
		} catch (NetException | IOException e) {
			throw new DownloadException("种子文件加载失败", e);
		}
	}

}
