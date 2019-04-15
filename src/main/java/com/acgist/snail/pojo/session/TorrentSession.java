package com.acgist.snail.pojo.session;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bootstrap.PeerClientGroup;
import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.downloader.torrent.bootstrap.TrackerLauncherGroup;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentFile;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.PeerSessionManager;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子session
 */
public class TorrentSession {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentSession.class);
	
	/**
	 * 种子
	 */
	private final Torrent torrent;
	/**
	 * 种子信息
	 */
	private final InfoHash infoHash;
	/**
	 * 任务
	 */
	private TaskSession taskSession;
	/**
	 * Peer组
	 */
	private PeerClientGroup peerClientGroup;
	/**
	 * 文件组
	 */
	private TorrentStreamGroup torrentStreamGroup;
	/**
	 * Tracker组
	 */
	private TrackerLauncherGroup trackerLauncherGroup;
	
	public static final TorrentSession newInstance(Torrent torrent, InfoHash infoHash) throws DownloadException {
		return new TorrentSession(torrent, infoHash);
	}

	private TorrentSession(Torrent torrent, InfoHash infoHash) throws DownloadException {
		if(torrent == null || infoHash == null) {
			throw new DownloadException("解析种子文件异常");
		}
		this.torrent = torrent;
		this.infoHash = infoHash;
	}
	
	/**
	 * 开始下载任务：获取tracker、peer
	 */
	public void build(TaskSession taskSession) {
		this.taskSession = taskSession;
		this.peerClientGroup = new PeerClientGroup(this);
		this.trackerLauncherGroup = new TrackerLauncherGroup(this);
		this.torrentStreamGroup = TorrentStreamGroup.newInstance(taskSession.downloadFolder().getPath(), torrent, selectFiles());
	}
	
	/**
	 * 加载tracker
	 */
	public void loadTracker() throws DownloadException {
		this.trackerLauncherGroup.loadTracker();
	}

	/**
	 * 下载名称
	 */
	public String name() {
		TorrentInfo torrentInfo = torrent.getInfo();
		String name = torrentInfo.getNameUtf8();
		if(StringUtils.isEmpty(name)) {
			name = StringUtils.charset(torrentInfo.getName(), torrent.getEncoding());
		}
		return name;
	}
	
	public Torrent torrent() {
		return this.torrent;
	}
	
	public InfoHash infoHash() {
		return this.infoHash;
	}
	
	public String infoHashHex() {
		return this.infoHash.infoHashHex();
	}
	
	public TaskSession taskSession() {
		return this.taskSession;
	}
	
	public PeerClientGroup peerClientGroup() {
		return this.peerClientGroup;
	}
	
	public TorrentStreamGroup torrentStreamGroup() {
		return this.torrentStreamGroup;
	}
	
	public TrackerLauncherGroup trackerLauncherGroup() {
		return this.trackerLauncherGroup;
	}
	
	/**
	 * 获取选择的下载文件
	 */
	public List<TorrentFile> selectFiles() {
		final TorrentInfo info = torrent.getInfo();
		final List<TorrentFile> files = info.files();
		final List<String> selectedFiles = taskSession.downloadTorrentFiles();
		for (TorrentFile file : files) {
			if(selectedFiles.contains(file.path())) {
				file.select(true);
			} else {
				file.select(false);
			}
		}
		return files;
	}
	
	/**
	 * 释放资源
	 */
	public void release() {
		trackerLauncherGroup.release();
		peerClientGroup.release();
		torrentStreamGroup.release();
	}

	/**
	 * 设置Peer
	 */
	public void peer(Map<String, Integer> peers) {
		if(CollectionUtils.isEmpty(peers)) {
			return;
		}
		final PeerSessionManager manager = PeerSessionManager.getInstance();
		peers.forEach((host, port) -> {
			LOGGER.debug("添加Peer，HOST：{}，PORT：{}", host, port);
			manager.newPeer(this.infoHashHex(), taskSession.statistics(), host, port);
		});
		peerClientGroup.launchers();
	}

	/**
	 * 获取已下载大小
	 */
	public long size() {
		return torrentStreamGroup.size();
	}

}
