package com.acgist.snail.pojo.session;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bootstrap.DhtLauncher;
import com.acgist.snail.downloader.torrent.bootstrap.PeerClientGroup;
import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.downloader.torrent.bootstrap.TrackerLauncherGroup;
import com.acgist.snail.protocol.torrent.TorrentBuilder;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentFile;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.PeerManager;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * Torrent Session
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentSession {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentSession.class);
	
	/**
	 * DHT任务执行周期
	 */
	private static final int INTERVAL = 60;
	
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
	 * DHT任务
	 */
	private DhtLauncher dhtLauncher;
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
	/**
	 * 线程池：PeerClient和新建PeerClient时使用
	 */
	private ExecutorService executor;
	/**
	 * 定时线程池：TrackerClient定时刷新
	 */
	private ScheduledExecutorService executorTimer;
	
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
	 * 加载Task，同时加载文件流
	 */
	public TorrentSession loadTask(TaskSession taskSession) throws DownloadException {
		this.taskSession = taskSession;
		this.loadTorrentStream();
		return this;
	}
	
	/**
	 * 开始下载
	 */
	public boolean download() throws DownloadException {
		return download(true);
	}
	
	/**
	 * 开始下载：加载线程池、Peer、Tracker、DHT
	 * 如果文件已经下载完成或者任务已经完成不会再加载线程池、Peer、Tracker、DHT
	 * @param findPeer 是否查找Peer：true-使用Tracker、DHT查找Peer，false-不查找
	 * @return true-下载完成；false-未完成
	 */
	public boolean download(boolean findPeer) throws DownloadException {
		if(taskSession.complete() || this.torrentStreamGroup.complete()) {
			return true;
		}
		this.loadExecutor();
		this.loadPeer();
		if(findPeer) {
			this.loadTrackerLauncher();
			this.loadDhtLauncher();
		}
		return false;
	}
	
	/**
	 * 加载文件流
	 */
	private void loadTorrentStream() throws DownloadException {
		if(this.taskSession == null) {
			throw new DownloadException("BT任务不存在");
		}
		this.torrentStreamGroup = TorrentStreamGroup.newInstance(
			this.taskSession.downloadFolder().getPath(),
			this.torrent,
			selectFiles(),
			this);
	}
	
	/**
	 * 加载线程池
	 */
	private void loadExecutor() {
		this.executor = SystemThreadContext.newExecutor(10, 10, 100, 60L, SystemThreadContext.SNAIL_THREAD_PEER);
		final String executorTimerName = SystemThreadContext.SNAIL_THREAD_TRACKER + "-" + this.infoHashHex();
		this.executorTimer = SystemThreadContext.newScheduledExecutor(4, executorTimerName);
	}
	
	/**
	 * 加载Peer
	 */
	private void loadPeer() {
		this.peerClientGroup = PeerClientGroup.newInstance(this);
	}
	
	/**
	 * 加载Tracker
	 */
	private void loadTrackerLauncher() throws DownloadException {
		this.trackerLauncherGroup = TrackerLauncherGroup.newInstance(this);
		this.trackerLauncherGroup.loadTracker();
	}
	
	/**
	 * 加载DHT定时任务
	 */
	private void loadDhtLauncher() {
		this.dhtLauncher = DhtLauncher.newInstance(this);
		this.timer(INTERVAL, INTERVAL, TimeUnit.SECONDS, this.dhtLauncher);
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
	
	public DhtLauncher dhtLauncher() {
		return this.dhtLauncher;
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
	
	public void submit(Runnable runnable) {
		executor.submit(runnable);
	}
	
	/**
	 * 定时任务（不重复）
	 */
	public void timer(long delay, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			executorTimer.schedule(runnable, delay, unit);
		}
	}
	
	/**
	 * 定时任务（重复），固定时间（周期不受执行时间影响）
	 * @param delay 延迟时间
	 * @param period 周期时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 */
	public void timer(long delay, long period, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			executorTimer.scheduleAtFixedRate(runnable, delay, period, unit);
		}
	}
	
	/**
	 * 定时任务（重复），固定周期（周期受到执行时间影响）
	 * @param delay 延迟时间
	 * @param period 周期时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 */
	public void timerFixedDelay(long delay, long period, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			executorTimer.scheduleWithFixedDelay(runnable, delay, period, unit);
		}
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
	 * 检测是否完成下载，释放资源
	 */
	public void complete() {
		if(torrentStreamGroup.complete()) {
			LOGGER.debug("任务下载完成：{}", name());
			taskSession.downloader().unlockDownload();
		}
	}
	
	/**
	 * 释放资源
	 */
	public void release() {
		LOGGER.debug("Torrent释放资源");
		trackerLauncherGroup.release();
		peerClientGroup.release();
		torrentStreamGroup.release();
		SystemThreadContext.shutdownNow(executor);
		SystemThreadContext.shutdownNow(executorTimer);
	}

	/**
	 * 设置Peer
	 */
	public void peer(Map<String, Integer> peers) {
		if(CollectionUtils.isEmpty(peers)) {
			return;
		}
		final PeerManager manager = PeerManager.getInstance();
		peers.forEach((host, port) -> {
			manager.newPeerSession(this.infoHashHex(), taskSession.statistics(), host, port, PeerConfig.SOURCE_TRACKER);
		});
	}

	/**
	 * 获取已下载大小
	 */
	public long size() {
		return torrentStreamGroup.size();
	}

	/**
	 * <p>发送have消息，通知所有已连接的Peer已下载对应的Piece</p>
	 * 
	 * @param index Piece序号
	 */
	public void have(int index) {
		PeerManager.getInstance().have(infoHash.infoHashHex(), index);
	}

	/**
	 * 保存种子文件
	 */
	public void saveTorrentFile() {
		if(this.taskSession == null) {
			return;
		}
		final var entity = this.taskSession.entity();
		if(entity == null) {
			return;
		}
		final TorrentBuilder builder = TorrentBuilder.newInstance(this.infoHash);
		builder.buildFile(entity.getFile());
	}

}
