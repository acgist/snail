package com.acgist.snail.pojo.session;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bootstrap.DhtLauncher;
import com.acgist.snail.downloader.torrent.bootstrap.PeerConnectGroup;
import com.acgist.snail.downloader.torrent.bootstrap.PeerLauncherGroup;
import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.downloader.torrent.bootstrap.TrackerLauncherGroup;
import com.acgist.snail.protocol.magnet.bootstrap.TorrentBuilder;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentFile;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.ArgumentException;
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
	 * PEX优化定时
	 */
	private static final Duration PEX_INTERVAL = Duration.ofSeconds(SystemConfig.getPexInterval());
	/**
	 * DHT任务执行周期
	 */
	private static final Duration DHT_INTERVAL = Duration.ofSeconds(SystemConfig.getDhtInterval());
	/**
	 * Tracker任务执行周期
	 */
	private static final Duration TRACKER_INTERVAL = Duration.ofSeconds(SystemConfig.getTrackerInterval());
	/**
	 * Peer优化定时
	 */
	private static final Duration PEER_OPTIMIZE_INTERVAL = Duration.ofSeconds(SystemConfig.getPeerOptimizeInterval());

	/**
	 * 可上传
	 */
	private boolean uploadable = false;
	/**
	 * 可下载
	 */
	private boolean downloadable = false;
	
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
	 * PeerConnect组
	 */
	private PeerConnectGroup peerConnectGroup;
	/**
	 * PeerLauncher组
	 */
	private PeerLauncherGroup peerLauncherGroup;
	/**
	 * 文件流组
	 */
	private TorrentStreamGroup torrentStreamGroup;
	/**
	 * Tracker组
	 */
	private TrackerLauncherGroup trackerLauncherGroup;
	/**
	 * 线程池
	 */
	private ExecutorService executor;
	/**
	 * 定时线程池
	 */
	private ScheduledExecutorService executorTimer;
	/**
	 * PEX定时器
	 */
	private ScheduledFuture<?> pexTimer;
	/**
	 * DHT定时器
	 */
	private ScheduledFuture<?> dhtLauncherTimer;
	/**
	 * PeerConnectGroup定时器
	 */
	private ScheduledFuture<?> peerConnectGroupTimer;
	/**
	 * PeerLauncherGroup定时器
	 */
	private ScheduledFuture<?> peerLauncherGroupTimer;
	/**
	 * TrackerLauncherGroup定时器
	 */
	private ScheduledFuture<?> trackerLauncherGroupTimer;

	private TorrentSession(InfoHash infoHash, Torrent torrent) throws DownloadException {
		if(torrent == null || infoHash == null) {
			throw new DownloadException("解析种子文件异常");
		}
		this.torrent = torrent;
		this.infoHash = infoHash;
	}
	
	public static final TorrentSession newInstance(InfoHash infoHash, Torrent torrent) throws DownloadException {
		return new TorrentSession(infoHash, torrent);
	}
	
	/**
	 * 开始上传
	 */
	public TorrentSession upload(TaskSession taskSession) throws DownloadException {
		this.taskSession = taskSession;
		this.loadExecutorTimer();
		this.loadTorrentStreamGroup();
		this.loadPeerConnectGroup();
		this.loadPeerConnectGroupTimer();
		this.uploadable = true;
		return this;
	}
	
	/**
	 * 开始下载
	 * 需要先调用{@link #upload(TaskSession)}对任务进行上传。
	 */
	public boolean download() throws DownloadException {
		return this.download(true);
	}
	
	/**
	 * 开始下载：加载线程池、Peer、Tracker、DHT。
	 * 如果文件已经下载完成或者任务已经完成不会再加载线程池、Peer、Tracker、DHT。
	 * 需要先调用{@link #upload(TaskSession)}对任务进行上传。
	 * 
	 * @param findPeer 是否查找Peer：true-使用Tracker、DHT查找Peer，false-不查找
	 * 
	 * @return true-下载完成；false-未完成
	 */
	public boolean download(boolean findPeer) throws DownloadException {
		if(this.taskSession.complete() || this.torrentStreamGroup.complete()) {
			return true;
		}
		this.loadExecutor();
		if(findPeer) {
			this.loadTrackerLauncherGroup();
			this.loadTrackerLauncherGroupTimer();
			this.loadDhtLauncher();
			this.loadDhtLauncherTimer();
		}
		this.loadPeerLauncherGroup();
		this.loadPeerLauncherGroupTimer();
		this.loadPexTimer();
		this.downloadable = true;
		return false;
	}

	/**
	 * 加载线程池
	 */
	private void loadExecutor() {
		this.executor = SystemThreadContext.newExecutor(5, 10, 100, 60L, SystemThreadContext.SNAIL_THREAD_BT);
	}

	/**
	 * 加载定时线程池
	 */
	private void loadExecutorTimer() {
		this.executorTimer = SystemThreadContext.newScheduledExecutor(2, SystemThreadContext.SNAIL_THREAD_BT_TIMER);
	}
	
	/**
	 * 加载文件流
	 */
	private void loadTorrentStreamGroup() throws DownloadException {
		if(this.taskSession == null) {
			throw new DownloadException("BT任务不存在");
		}
		this.torrentStreamGroup = TorrentStreamGroup.newInstance(
			this.taskSession.downloadFolder().getPath(),
			selectFiles(),
			this);
	}

	/**
	 * 加载PeerLauncher
	 */
	private void loadPeerLauncherGroup() {
		this.peerLauncherGroup = PeerLauncherGroup.newInstance(this);
	}
	
	/**
	 * 加载PeerLauncher定时优化任务
	 */
	private void loadPeerLauncherGroupTimer() {
		this.peerLauncherGroupTimer = this.timerFixedDelay(0L, PEER_OPTIMIZE_INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
			this.peerLauncherGroup.optimize(); // 优化下载Peer下载
		});
	}

	/**
	 * 加载PeerConnect
	 */
	private void loadPeerConnectGroup() {
		this.peerConnectGroup = PeerConnectGroup.newInstance(this);
	}
	
	/**
	 * 加载PeerConnect定时优化任务
	 */
	private void loadPeerConnectGroupTimer() {
		this.peerConnectGroupTimer = this.timerFixedDelay(PEER_OPTIMIZE_INTERVAL.toSeconds(), PEER_OPTIMIZE_INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
			this.peerConnectGroup.optimize(); // 优化连接Peer连接
		});
	}

	/**
	 * 加载Tracker
	 */
	private void loadTrackerLauncherGroup() throws DownloadException {
		this.trackerLauncherGroup = TrackerLauncherGroup.newInstance(this);
		this.trackerLauncherGroup.loadTracker();
	}

	/**
	 * 加载Tracker定时查询任务
	 */
	private void loadTrackerLauncherGroupTimer() {
		this.trackerLauncherGroupTimer = this.timerFixedDelay(0L, TRACKER_INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
			this.trackerLauncherGroup.findPeer();
		});
	}
	
	/**
	 * 加载DHT，将种子文件中的节点加入到DHT网络中。
	 */
	private void loadDhtLauncher() {
		this.dhtLauncher = DhtLauncher.newInstance(this);
		final var nodes = this.torrent.getNodes();
		if(CollectionUtils.isNotEmpty(nodes)) { // 添加DHT节点
			nodes.forEach((host, port) -> {
				this.dhtLauncher.put(host, port.intValue());
			});
		}
	}

	/**
	 * 加载DHT定时任务
	 */
	private void loadDhtLauncherTimer() {
		this.dhtLauncherTimer = this.timerFixedDelay(DHT_INTERVAL.getSeconds(), DHT_INTERVAL.getSeconds(), TimeUnit.SECONDS, this.dhtLauncher);
	}
	
	/**
	 * 加载PEX定时任务
	 */
	private void loadPexTimer() {
		this.pexTimer = this.timerFixedDelay(PEX_INTERVAL.toSeconds(), PEX_INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
			PeerManager.getInstance().exchange(this.infoHashHex(), this.peerLauncherGroup.optimizePeerSession()); // PEX消息
		});
	}
	
	/**
	 * 异步执行
	 */
	public void submit(Runnable runnable) {
		this.executor.submit(runnable);
	}
	
	/**
	 * 定时任务（不重复）
	 */
	public ScheduledFuture<?> timer(long delay, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			return this.executorTimer.schedule(runnable, delay, unit);
		} else {
			throw new ArgumentException("定时任务时间错误：" + delay);
		}
	}
	
	/**
	 * <p>定时任务（重复）</p>
	 * <p>固定时间（周期不受执行时间影响）</p>
	 * 
	 * @param delay 延迟时间
	 * @param period 周期时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 */
	public ScheduledFuture<?> timer(long delay, long period, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			return this.executorTimer.scheduleAtFixedRate(runnable, delay, period, unit);
		} else {
			throw new ArgumentException("定时任务时间错误：" + delay);
		}
	}
	
	/**
	 * <p>定时任务（重复）</p>
	 * <p>固定周期（周期受到执行时间影响）</p>
	 * 
	 * @param delay 延迟时间
	 * @param period 周期时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 */
	public ScheduledFuture<?> timerFixedDelay(long delay, long period, TimeUnit unit, Runnable runnable) {
		if(delay >= 0) {
			return this.executorTimer.scheduleWithFixedDelay(runnable, delay, period, unit);
		} else {
			throw new ArgumentException("定时任务时间错误：" + delay);
		}
	}
	
	/**
	 * 获取选择的下载文件
	 */
	public List<TorrentFile> selectFiles() {
		final TorrentInfo info = this.torrent.getInfo();
		final List<TorrentFile> files = info.files();
		final List<String> selectedFiles = this.taskSession.downloadTorrentFiles();
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
	public void completeCheck() {
		if(this.torrentStreamGroup.complete()) {
			LOGGER.debug("任务下载完成：{}", name());
			this.torrentStreamGroup.flush();
			this.taskSession.downloader().unlockDownload();
		}
	}
	
	/**
	 * 释放资源（释放下载使用的资源），完成时不释放文件资源。
	 */
	public void releaseDownload() {
		LOGGER.debug("Torrent释放资源（下载）");
		this.pexTimer.cancel(false);
		this.peerLauncherGroupTimer.cancel(false);
		this.peerLauncherGroup.release();
		if(this.dhtLauncherTimer != null) {
			this.dhtLauncherTimer.cancel(false);
		}
		if(this.trackerLauncherGroup != null) {
			this.trackerLauncherGroupTimer.cancel(false);
			this.trackerLauncherGroup.release();
		}
		SystemThreadContext.shutdownNow(this.executor);
		this.downloadable = false;
	}
	
	/**
	 * 释放资源（释放分享使用的资源）
	 */
	public void releaseUpload() {
		LOGGER.debug("Torrent释放资源（上传）");
		this.peerConnectGroupTimer.cancel(false);
		this.peerConnectGroup.release();
		this.torrentStreamGroup.release();
		SystemThreadContext.shutdownNow(this.executorTimer);
		this.uploadable = false;
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
			manager.newPeerSession(this.infoHashHex(), this.taskSession.statistics(), host, port, PeerConfig.SOURCE_TRACKER);
		});
	}

	/**
	 * <p>发送have消息，通知所有已连接的Peer已下载对应的Piece</p>
	 * 
	 * @param index Piece序号
	 */
	public void have(int index) {
		PeerManager.getInstance().have(this.infoHash.infoHashHex(), index);
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

	/**
	 * 重新获取下载大小
	 */
	public void resize() {
		this.taskSession.downloadSize(this.torrentStreamGroup.size());
	}

	/**
	 * 下载名称
	 */
	public String name() {
		TorrentInfo torrentInfo = this.torrent.getInfo();
		String name = torrentInfo.getNameUtf8();
		if(StringUtils.isEmpty(name)) {
			name = StringUtils.charset(torrentInfo.getName(), torrent.getEncoding());
		}
		return name;
	}

	/**
	 * 是否可以下载
	 */
	public boolean uploadable() {
		return this.uploadable;
	}
	
	/**
	 * 是否可以下载
	 */
	public boolean downloadable() {
		return this.downloadable;
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
	
	/**
	 * 如果没有下载，只是加载时为null。
	 */
	public DhtLauncher dhtLauncher() {
		return this.dhtLauncher;
	}
	
	public PeerConnectGroup peerConnectGroup() {
		return this.peerConnectGroup;
	}
	
	public TorrentStreamGroup torrentStreamGroup() {
		return this.torrentStreamGroup;
	}
	
}
