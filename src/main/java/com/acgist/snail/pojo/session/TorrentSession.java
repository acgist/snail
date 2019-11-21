package com.acgist.snail.pojo.session;

import java.time.Duration;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.bootstrap.DhtLauncher;
import com.acgist.snail.net.torrent.bootstrap.PeerDownloaderGroup;
import com.acgist.snail.net.torrent.bootstrap.PeerUploader;
import com.acgist.snail.net.torrent.bootstrap.PeerUploaderGroup;
import com.acgist.snail.net.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.net.torrent.bootstrap.TrackerLauncherGroup;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.bean.Magnet;
import com.acgist.snail.pojo.bean.Torrent;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.bean.TorrentInfo;
import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.protocol.magnet.bootstrap.MagnetBuilder;
import com.acgist.snail.protocol.magnet.bootstrap.TorrentBuilder;
import com.acgist.snail.system.config.PeerConfig.Action;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.exception.TimerArgumentException;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>Torrent Session</p>
 * <p>负责下载任务整体调度，查询Peer、文件管理等。</p>
 * <p>任务一定需要先设置上传，然后才能进行下载。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentSession {
	
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
	 * 动作：磁力链接转换、文件下载
	 */
	private Action action;
	/**
	 * 准备状态
	 */
	private volatile boolean ready = false;
	/**
	 * 上传状态
	 */
	private volatile boolean uploadable = false;
	/**
	 * 下载状态
	 */
	private volatile boolean downloadable = false;
	/**
	 * 磁力链接
	 */
	private Magnet magnet;
	/**
	 * 种子
	 */
	private Torrent torrent;
	/**
	 * 种子信息
	 */
	private InfoHash infoHash;
	/**
	 * 任务
	 */
	private ITaskSession taskSession;
	/**
	 * DHT任务
	 */
	private DhtLauncher dhtLauncher;
	/**
	 * PeerUploader组
	 */
	private PeerUploaderGroup peerUploaderGroup;
	/**
	 * PeerDownloader组
	 */
	private PeerDownloaderGroup peerDownloaderGroup;
	/**
	 * 文件流组
	 */
	private TorrentStreamGroup torrentStreamGroup;
	/**
	 * Tracker组
	 */
	private TrackerLauncherGroup trackerLauncherGroup;
	/**
	 * <p>线程池</p>
	 * <p>使用缓存线程池，防止过多下载时出现卡死现象。</p>
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
	 * PeerUploaderGroup定时器
	 */
	private ScheduledFuture<?> peerUploaderGroupTimer;
	/**
	 * PeerDownloaderGroup定时器
	 */
	private ScheduledFuture<?> peerDownloaderGroupTimer;
	/**
	 * TrackerLauncherGroup定时器
	 */
	private ScheduledFuture<?> trackerLauncherGroupTimer;
	
	private TorrentSession(InfoHash infoHash, Torrent torrent) throws DownloadException {
		if(infoHash == null) {
			throw new DownloadException("创建TorrentSession失败（InfoHash）");
		}
		this.torrent = torrent;
		this.infoHash = infoHash;
	}
	
	public static final TorrentSession newInstance(InfoHash infoHash, Torrent torrent) throws DownloadException {
		return new TorrentSession(infoHash, torrent);
	}
	
	/**
	 * 磁力链接转换
	 * 
	 * @return true-下载完成；false-未完成；
	 */
	public boolean magnet(ITaskSession taskSession) throws DownloadException {
		this.action = Action.MAGNET;
		this.taskSession = taskSession;
		this.loadMagnet();
		this.loadExecutor();
		this.loadExecutorTimer();
		this.loadTrackerLauncherGroup();
		this.loadTrackerLauncherGroupTimer();
		this.loadDhtLauncher();
		this.loadDhtLauncherTimer();
		this.loadPeerUploaderGroup();
		this.loadPeerUploaderGroupTimer();
		this.loadPeerDownloaderGroup();
		this.loadPeerDownloaderGroupTimer();
		this.ready = true;
		return this.torrent != null;
	}
	
	/**
	 * <p>开始上传</p>
	 * <p>分享已下载数据</p>
	 */
	public TorrentSession upload(ITaskSession taskSession) throws DownloadException {
		this.taskSession = taskSession;
		this.loadExecutorTimer();
		this.loadTorrentStreamGroup();
		this.loadPeerUploaderGroup();
		this.loadPeerUploaderGroupTimer();
		this.uploadable = true;
		this.ready = true;
		return this;
	}

	/**
	 * 默认加载加载Peer
	 * 
	 * @see {@link #download(boolean)}
	 */
	public boolean download() throws DownloadException {
		return this.download(true);
	}
	
	/**
	 * <p>开始下载：加载线程池、Peer、Tracker、DHT</p>
	 * <p>如果任务已经完成或文件已经下载完成不会再加载线程池、Peer、Tracker、DHT</p>
	 * <p>需要先调用{@link #upload(TaskSession)}对任务进行分享</p>
	 * 
	 * @param findPeer 是否查找Peer：true-使用Tracker、DHT查找Peer；false-不查找；
	 * 
	 * @return true-下载完成；false-未完成；
	 */
	public boolean download(boolean findPeer) throws DownloadException {
		this.action = Action.TORRENT;
		if(this.taskSession == null) {
			throw new DownloadException("下载任务不存在");
		}
		if(this.taskSession.complete() || this.torrentStreamGroup.complete()) {
			return true;
		}
		this.loadExecutor();
		if(findPeer) {
			this.loadTrackerLauncherGroup();
			this.loadTrackerLauncherGroupTimer();
			if(this.isPrivateTorrent()) {
				LOGGER.debug("私有种子：不加载DHT任务");
			} else {
				this.loadDhtLauncher();
				this.loadDhtLauncherTimer();
			}
		}
		this.loadPeerDownloaderGroup();
		this.loadPeerDownloaderGroupTimer();
		this.loadPeerUploaderDownload();
		if(this.isPrivateTorrent()) {
			LOGGER.debug("私有种子：不加载PEX任务");
		} else {
			this.loadPexTimer();
		}
		this.downloadable = true;
		return false;
	}

	/**
	 * 加载磁力链接
	 */
	private void loadMagnet() throws DownloadException {
		this.magnet = MagnetBuilder.newInstance(this.taskSession.getUrl()).build();
	}
	
	/**
	 * 加载线程池
	 */
	private void loadExecutor() {
		this.executor = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_BT);
	}

	/**
	 * 加载定时线程池
	 */
	private void loadExecutorTimer() {
		this.executorTimer = SystemThreadContext.newTimerExecutor(2, SystemThreadContext.SNAIL_THREAD_BT_TIMER);
	}
	
	/**
	 * 加载文件流
	 */
	private void loadTorrentStreamGroup() {
		this.torrentStreamGroup = TorrentStreamGroup.newInstance(
			this.taskSession.downloadFolder().getPath(),
			setSelectedFiles(),
			this
		);
	}

	/**
	 * 加载PeerDownloader
	 */
	private void loadPeerDownloaderGroup() {
		this.peerDownloaderGroup = PeerDownloaderGroup.newInstance(this);
	}
	
	/**
	 * <p>加载PeerDownloader定时优化任务</p>
	 * <p>第一次任务执行延迟4秒，让加载Peer的任务执行4秒，防止一开始没有Peer数据而导致的长时间等待。</p>
	 */
	private void loadPeerDownloaderGroupTimer() {
		this.peerDownloaderGroupTimer = this.timerFixedDelay(4L, PEER_OPTIMIZE_INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
			this.peerDownloaderGroup.optimize(); // 优化下载Peer下载
		});
	}

	/**
	 * 加载PeerUploader下载
	 */
	private void loadPeerUploaderDownload() {
		this.submit(() -> {
			this.peerUploaderGroup.download();
		});
	}
	
	/**
	 * 加载PeerUploader
	 */
	private void loadPeerUploaderGroup() {
		this.peerUploaderGroup = PeerUploaderGroup.newInstance(this);
	}
	
	/**
	 * 加载PeerUploader定时优化任务
	 */
	private void loadPeerUploaderGroupTimer() {
		this.peerUploaderGroupTimer = this.timerFixedDelay(PEER_OPTIMIZE_INTERVAL.toSeconds(), PEER_OPTIMIZE_INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
			this.peerUploaderGroup.optimize(); // 优化连接Peer连接
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
	 * 加载DHT：将种子文件中的节点加入到DHT网络中
	 */
	private void loadDhtLauncher() {
		this.dhtLauncher = DhtLauncher.newInstance(this);
		if(this.action == Action.TORRENT) {
			final var nodes = this.torrent.getNodes();
			if(CollectionUtils.isNotEmpty(nodes)) { // 添加DHT节点
				nodes.forEach((host, port) -> {
					this.dhtLauncher.put(host, port);
				});
			}
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
			PeerManager.getInstance().pex(this.infoHashHex(), this.peerDownloaderGroup.optimizePeerSession());
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
		if(delay < 0) {
			throw new TimerArgumentException(delay);
		} else {
			return this.executorTimer.schedule(runnable, delay, unit);
		}
	}
	
	/**
	 * 定时任务（重复）
	 */
	public ScheduledFuture<?> timer(long delay, long period, TimeUnit unit, Runnable runnable) {
		if(delay < 0) {
			throw new TimerArgumentException(delay);
		} else if(period < 0) {
			throw new TimerArgumentException(period);
		} else {
			return this.executorTimer.scheduleAtFixedRate(runnable, delay, period, unit);
		}
	}
	
	/**
	 * 定时任务（重复）
	 */
	public ScheduledFuture<?> timerFixedDelay(long delay, long period, TimeUnit unit, Runnable runnable) {
		if(delay < 0) {
			throw new TimerArgumentException(delay);
		} else if(period < 0) {
			throw new TimerArgumentException(period);
		} else {
			return this.executorTimer.scheduleWithFixedDelay(runnable, delay, period, unit);
		}
	}
	
	/**
	 * 设置选择的下载文件并返回文件列表
	 */
	private List<TorrentFile> setSelectedFiles() {
		final TorrentInfo torrentInfo = this.torrent.getInfo();
		final List<TorrentFile> torrentFiles = torrentInfo.files();
		final List<String> selectedFiles = this.taskSession.selectTorrentFiles();
		for (TorrentFile torrentFile : torrentFiles) {
			if(selectedFiles.contains(torrentFile.path())) {
				torrentFile.selected(true);
			} else {
				torrentFile.selected(false);
			}
		}
		return torrentFiles;
	}

	/**
	 * <dl>
	 * 	<dt>检测任务是否下载完成</dt>
	 * 	<dd>如果已经完成直接返回</dd>
	 * 	<dd>BT任务：文件下载完成</dd>
	 * 	<dd>磁力链接：种子文件下载完成</dd>
	 * </dl>
	 */
	public boolean checkCompleted() {
		if(completed()) {
			return true;
		}
		if(this.action == Action.TORRENT) {
			return this.torrentStreamGroup.complete();
		} else {
			return this.torrent != null;
		}
	}
	
	/**
	 * 检测是否完成下载，完成后刷出缓存并且解除下载锁。
	 */
	public void checkCompletedAndDone() {
		if(this.torrentStreamGroup.complete()) {
			LOGGER.debug("任务下载完成：{}", name());
			this.torrentStreamGroup.flush();
			final var downloader = this.taskSession.downloader();
			if(downloader != null) {
				downloader.unlockDownload();
			}
			PeerManager.getInstance().uploadOnly(this.infoHashHex());
		}
	}
	
	/**
	 * 释放资源（磁力链接）
	 */
	public void releaseMagnet() {
		LOGGER.debug("Torrent释放资源（磁力链接下载）");
		this.releaseDownload();
		this.releaseUpload();
	}
	
	/**
	 * <p>释放资源（释放下载使用的资源）</p>
	 * <p>注：不释放文件资源</p>
	 */
	public void releaseDownload() {
		LOGGER.debug("Torrent释放资源（下载）");
		SystemThreadContext.shutdown(this.pexTimer);
		SystemThreadContext.shutdown(this.peerDownloaderGroupTimer);
		if(this.peerDownloaderGroup != null) {
			this.peerDownloaderGroup.release();
		}
		SystemThreadContext.shutdown(this.dhtLauncherTimer);
		SystemThreadContext.shutdown(this.trackerLauncherGroupTimer);
		if(this.trackerLauncherGroup != null) {
			this.trackerLauncherGroup.release();
		}
		if(this.torrentStreamGroup != null) {
			this.torrentStreamGroup.flush();
		}
		SystemThreadContext.shutdownNow(this.executor);
		this.downloadable = false;
	}
	
	/**
	 * 释放资源（释放分享使用的资源）
	 */
	public void releaseUpload() {
		LOGGER.debug("Torrent释放资源（上传）");
		SystemThreadContext.shutdown(this.peerUploaderGroupTimer);
		if(this.peerUploaderGroup != null) {
			this.peerUploaderGroup.release();
		}
		if(this.torrentStreamGroup != null) {
			this.torrentStreamGroup.release();
		}
		SystemThreadContext.shutdownNow(this.executorTimer);
		this.ready = false;
		this.uploadable = false;
	}

	/**
	 * <p>发送have消息</p>
	 * 
	 * @param index Piece序号
	 * 
	 * TODO：优化have消息，使用异步线程发送，防止部分Peer通知过慢，导致所有线程卡死。
	 */
	public void have(int index) {
		PeerManager.getInstance().have(this.infoHashHex(), index);
	}

	/**
	 * <p>挑选一个Piece下载</p>
	 */
	public TorrentPiece pick(final BitSet peerPieces, final BitSet suggestPieces) {
		return torrentStreamGroup.pick(peerPieces, suggestPieces);
	}
	
	/**
	 * <p>读取Piece数据</p>
	 */
	public byte[] read(int index, int begin, int length) throws NetException {
		return this.torrentStreamGroup.read(index, begin, length);
	}

	/**
	 * <p>保存Piece数据</p>
	 */
	public boolean write(TorrentPiece piece) {
		return this.torrentStreamGroup.write(piece);
	}
	
	/**
	 * 是否已下载Piece
	 */
	public boolean havePiece(int index) {
		return this.torrentStreamGroup.havePiece(index);
	}

	/**
	 * <p>Piece下载失败</p>
	 */
	public void undone(TorrentPiece piece) {
		this.torrentStreamGroup.undone(piece);
	}
	
	/**
	 * 保存种子文件，重新加载种子和InfoHash。
	 */
	public void saveTorrentFile() {
		final TorrentBuilder builder = TorrentBuilder.newInstance(this.infoHash, this.trackerLauncherGroup.trackers());
		final String torrentFile = builder.buildFile(this.taskSession.downloadFolder().getPath());
		this.taskSession.setTorrent(torrentFile);
		this.taskSession.update();
		try {
			this.torrent = TorrentManager.loadTorrent(torrentFile);
			this.infoHash = this.torrent.getInfoHash();
		} catch (DownloadException e) {
			LOGGER.error("解析种子异常", e);
		}
		final var downloader = this.taskSession.downloader();
		if(downloader != null) {
			downloader.unlockDownload();
		}
	}
	
	/**
	 * 创建接入连接
	 */
	public PeerUploader newPeerUploader(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		return this.peerUploaderGroup.newPeerUploader(peerSession, peerSubMessageHandler);
	}
	
	/**
	 * 添加DHT节点
	 */
	public void newDhtNode(String host, int port) {
		if(this.dhtLauncher != null) {
			LOGGER.debug("DHT扩展添加DHT节点：{}-{}", host, port);
			this.dhtLauncher.put(host, port);
		}
	}
	
	/**
	 * 重新获取下载大小
	 */
	public void resize(long size) {
		this.taskSession.downloadSize(size);
	}

	/**
	 * 是否可以上传
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
	
	/**
	 * 任务是否下载中
	 */
	public boolean running() {
		return this.taskSession.download();
	}
	
	/**
	 * 任务是否完成
	 */
	public boolean completed() {
		return this.taskSession.complete();
	}
	
	/**
	 * 需要验证{@link #taskSession}是否为空，任务只加载并没有开始下载时会导致空指针。
	 */
	public String name() {
		if(this.taskSession == null) {
			if(this.torrent == null) {
				return this.infoHash.infoHashHex();
			} else {
				return this.torrent.name();
			}
		} else {
			return this.taskSession.getName();
		}
	}
	
	public void fullPieces(BitSet pieces) {
		this.torrentStreamGroup.fullPieces(pieces);
	}
	
	public void fullPieces() {
		this.torrentStreamGroup.fullPieces();
	}
	
	public int health() {
		if(this.torrentStreamGroup != null) {
			return this.torrentStreamGroup.health();
		}
		return 0;
	}
	
	public long size() {
		return this.taskSession.getSize();
	}
	
	public BitSet pieces() {
		return this.torrentStreamGroup.pieces();
	}
	
	public BitSet allPieces() {
		return this.torrentStreamGroup.allPieces();
	}
	
	public boolean ready() {
		return this.ready;
	}
	
	public Action action() {
		return this.action;
	}
	
	public Magnet magnet() {
		return this.magnet;
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
	
	public ITaskSession taskSession() {
		return this.taskSession;
	}
	
	public boolean isPrivateTorrent() {
		if(this.torrent == null) {
			return false;
		}
		return this.torrent.getInfo().isPrivateTorrent();
	}
	
	/**
	 * 需要判断{@link #taskSession}是否为空，任务加载时Peer接入会导致空指针。
	 */
	public IStatisticsSession statistics() {
		return this.taskSession.statistics();
	}
	
}
