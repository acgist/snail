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
 * <p>负责下载任务整体调度：查询Peer、文件管理等</p>
 * <p>BT任务需要先上传才能进行下载</p>
 * <p>磁力链接不存在下载和上次状态</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentSession {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentSession.class);
	
	/**
	 * PEX优化任务执行周期
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
	 * Peer优化任务执行周期
	 */
	private static final Duration PEER_OPTIMIZE_INTERVAL = Duration.ofSeconds(SystemConfig.getPeerOptimizeInterval());

	/**
	 * 动作：磁力链接转换、种子任务下载
	 */
	private Action action;
	/**
	 * <p>准备状态</p>
	 * <p>任务已经准备所有基础数据</p>
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
	 * 种子信息
	 */
	private Torrent torrent;
	/**
	 * 种子InfoHash
	 */
	private InfoHash infoHash;
	/**
	 * 任务信息
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
	 * <p>使用缓存线程池：防止过多下载时出现卡死现象</p>
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
			throw new DownloadException("创建TorrentSession失败（InfoHash为空）");
		}
		this.torrent = torrent;
		this.infoHash = infoHash;
	}
	
	public static final TorrentSession newInstance(InfoHash infoHash, Torrent torrent) throws DownloadException {
		return new TorrentSession(infoHash, torrent);
	}
	
	/**
	 * <p>磁力链接转换</p>
	 * 
	 * @return true-下载完成；false-等待下载；
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
	 * <p>准备：定时线程池、文件流、Peer上传</p>
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
	 * <p>开始下载</p>
	 * 
	 * @see {@link #download(boolean)}
	 */
	public boolean download() throws DownloadException {
		return this.download(true);
	}
	
	/**
	 * <p>开始下载</p>
	 * <p>准备：线程池、Tracker、DHT、Peer下载</p>
	 * <p>如果任务已经完成或文件已经下载完成不会再加载准备数据</p>
	 * <p>需要先调用{@link #upload(TaskSession)}对任务进行上传</p>
	 * 
	 * @param findPeer 是否查找Peer（加载Tracker、DHT）：true-查找；false-不查找；
	 * 
	 * @return true-下载完成；false-等待下载；
	 */
	public boolean download(boolean findPeer) throws DownloadException {
		this.action = Action.TORRENT;
		if(this.taskSession == null) {
			throw new DownloadException("下载任务没有上传");
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
	 * <p>加载磁力链接</p>
	 */
	private void loadMagnet() throws DownloadException {
		this.magnet = MagnetBuilder.newInstance(this.taskSession.getUrl()).build();
	}
	
	/**
	 * <p>加载线程池</p>
	 */
	private void loadExecutor() {
		this.executor = SystemThreadContext.newCacheExecutor(SystemThreadContext.SNAIL_THREAD_BT);
	}

	/**
	 * <p>加载定时线程池</p>
	 */
	private void loadExecutorTimer() {
		this.executorTimer = SystemThreadContext.newTimerExecutor(2, SystemThreadContext.SNAIL_THREAD_BT_TIMER);
	}
	
	/**
	 * <p>加载文件流</p>
	 */
	private void loadTorrentStreamGroup() {
		this.torrentStreamGroup = TorrentStreamGroup.newInstance(
			this.taskSession.downloadFolder().getPath(),
			buildSelectedFiles(),
			this
		);
	}

	/**
	 * <p>加载PeerDownloader</p>
	 */
	private void loadPeerDownloaderGroup() {
		this.peerDownloaderGroup = PeerDownloaderGroup.newInstance(this);
	}
	
	/**
	 * <p>加载PeerDownloader定时任务</p>
	 * <p>任务延迟4秒执行，等待加载Peer的任务执行4秒，防止没有Peer数据导致的长时间等待。</p>
	 */
	private void loadPeerDownloaderGroupTimer() {
		this.peerDownloaderGroupTimer = this.timerFixedDelay(4L, PEER_OPTIMIZE_INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
			this.peerDownloaderGroup.optimize(); // 优化Peer下载
		});
	}

	/**
	 * <p>加载PeerUploader</p>
	 */
	private void loadPeerUploaderGroup() {
		this.peerUploaderGroup = PeerUploaderGroup.newInstance(this);
	}
	
	/**
	 * <p>加载PeerUploader定时任务</p>
	 */
	private void loadPeerUploaderGroupTimer() {
		this.peerUploaderGroupTimer = this.timerFixedDelay(PEER_OPTIMIZE_INTERVAL.toSeconds(), PEER_OPTIMIZE_INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
			this.peerUploaderGroup.optimize(); // 优化Peer上传
		});
	}
	
	/**
	 * <p>加载PeerUploader下载</p>
	 * <p>如果连接的Peer已经解除阻塞，开始发送下载请求。</p>
	 */
	private void loadPeerUploaderDownload() {
		this.submit(() -> {
			this.peerUploaderGroup.download();
		});
	}
	
	/**
	 * <p>加载Tracker</p>
	 */
	private void loadTrackerLauncherGroup() throws DownloadException {
		this.trackerLauncherGroup = TrackerLauncherGroup.newInstance(this);
		this.trackerLauncherGroup.loadTracker();
	}

	/**
	 * <p>加载Tracker定时任务</p>
	 */
	private void loadTrackerLauncherGroupTimer() {
		this.trackerLauncherGroupTimer = this.timerFixedDelay(0L, TRACKER_INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
			this.trackerLauncherGroup.findPeer();
		});
	}
	
	/**
	 * <p>加载DHT</p>
	 * <p>如果种子文件自带DHT节点，将这些节点也加入到DHT网络中。</p>
	 */
	private void loadDhtLauncher() {
		this.dhtLauncher = DhtLauncher.newInstance(this);
		if(this.action == Action.TORRENT) { // 种子下载任务
			final var nodes = this.torrent.getNodes();
			if(CollectionUtils.isNotEmpty(nodes)) { // 添加种子自带DHT节点
				nodes.forEach((host, port) -> {
					this.dhtLauncher.put(host, port);
				});
			}
		}
	}

	/**
	 * <p>加载DHT定时任务</p>
	 */
	private void loadDhtLauncherTimer() {
		this.dhtLauncherTimer = this.timerFixedDelay(DHT_INTERVAL.getSeconds(), DHT_INTERVAL.getSeconds(), TimeUnit.SECONDS, this.dhtLauncher);
	}
	
	/**
	 * <p>加载PEX定时任务</p>
	 */
	private void loadPexTimer() {
		this.pexTimer = this.timerFixedDelay(PEX_INTERVAL.toSeconds(), PEX_INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> {
			PeerManager.getInstance().pex(this.infoHashHex(), this.peerDownloaderGroup.optimizePeerSession());
		});
	}
	
	/**
	 * <p>异步执行</p>
	 */
	public void submit(Runnable runnable) {
		this.executor.submit(runnable);
	}
	
	/**
	 * <p>定时任务（不重复执行）</p>
	 */
	public ScheduledFuture<?> timer(long delay, TimeUnit unit, Runnable runnable) {
		if(delay < 0) {
			throw new TimerArgumentException(delay);
		} else {
			return this.executorTimer.schedule(runnable, delay, unit);
		}
	}
	
	/**
	 * <p>定时任务（重复执行）</p>
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
	 * <p>定时任务（重复执行）</p>
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
	 * <p>设置种子文件中被选择下载文件并返回文件列表</p>
	 */
	private List<TorrentFile> buildSelectedFiles() {
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
	 * 	<dd>如果已经完成直接返回完成</dd>
	 * 	<dd>种子任务：文件下载完成</dd>
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
	 * <p>检测任务是否下载完成</p>
	 * <p>如果任务已经完成：刷出缓存、解除下载锁、UploadOnly</p>
	 */
	public void checkCompletedAndDone() {
		if(this.torrentStreamGroup.complete()) {
			LOGGER.debug("任务下载完成：{}", name());
			this.torrentStreamGroup.flush();
			final var downloader = this.taskSession.downloader();
			if(downloader != null) {
				downloader.unlockDownload(); // 解除下载锁
			}
			PeerManager.getInstance().uploadOnly(this.infoHashHex());
		}
	}
	
	/**
	 * <p>释放资源（磁力链接）</p>
	 */
	public void releaseMagnet() {
		LOGGER.debug("Torrent释放资源（磁力链接下载）");
		this.releaseDownload();
		this.releaseUpload();
	}
	
	/**
	 * <p>释放资源（释放下载资源）</p>
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
	 * <p>释放资源（释放上传资源）</p>
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
	 * @param index Piece索引
	 * 
	 * TODO：优化have消息，使用异步线程发送，防止部分Peer通知过慢，导致所有线程卡死。
	 */
	public void have(int index) {
		PeerManager.getInstance().have(this.infoHashHex(), index);
	}

	/**
	 * @see {@link TorrentStreamGroup#pick(BitSet, BitSet)}
	 */
	public TorrentPiece pick(final BitSet peerPieces, final BitSet suggestPieces) {
		return torrentStreamGroup.pick(peerPieces, suggestPieces);
	}
	
	/**
	 * @see {@link TorrentStreamGroup#read(int, int, int)}
	 */
	public byte[] read(int index, int begin, int length) throws NetException {
		return this.torrentStreamGroup.read(index, begin, length);
	}

	/**
	 * @see {@link TorrentStreamGroup#write(TorrentPiece)}
	 */
	public boolean write(TorrentPiece piece) {
		return this.torrentStreamGroup.write(piece);
	}
	
	/**
	 * @see {@link TorrentStreamGroup#havePiece(int)}
	 */
	public boolean havePiece(int index) {
		return this.torrentStreamGroup.havePiece(index);
	}

	/**
	 * @see {@link TorrentStreamGroup#undone(TorrentPiece)}
	 */
	public void undone(TorrentPiece piece) {
		this.torrentStreamGroup.undone(piece);
	}
	
	/**
	 * <p>保存种子文件</p>
	 * <p>重新加载种子文件和InfoHash</p>
	 */
	public void saveTorrent() {
		final TorrentBuilder builder = TorrentBuilder.newInstance(this.infoHash, this.trackerLauncherGroup.trackers());
		final String torrentFilePath = builder.buildFile(this.taskSession.downloadFolder().getPath());
		this.taskSession.setTorrent(torrentFilePath);
		this.taskSession.update();
		try {
			this.torrent = TorrentManager.loadTorrent(torrentFilePath);
			this.infoHash = this.torrent.infoHash();
		} catch (DownloadException e) {
			LOGGER.error("解析种子异常", e);
		}
		final var downloader = this.taskSession.downloader();
		if(downloader != null) {
			downloader.unlockDownload();
		}
	}
	
	/**
	 * @see {@link PeerUploaderGroup#newPeerUploader(PeerSession, PeerSubMessageHandler)}
	 */
	public PeerUploader newPeerUploader(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		return this.peerUploaderGroup.newPeerUploader(peerSession, peerSubMessageHandler);
	}
	
	/**
	 * @see {@link DhtLauncher#put(String, Integer)}
	 */
	public void newDhtNode(String host, int port) {
		if(this.dhtLauncher != null) {
			this.dhtLauncher.put(host, port);
		}
	}
	
	/**
	 * <p>重新设置下载大小</p>
	 */
	public void resize(long size) {
		this.taskSession.downloadSize(size);
	}

	/**
	 * <p>是否可以上传</p>
	 */
	public boolean uploadable() {
		return this.uploadable;
	}
	
	/**
	 * <p>是否可以下载</p>
	 */
	public boolean downloadable() {
		return this.downloadable;
	}
	
	/**
	 * <p>任务是否下载中</p>
	 */
	public boolean downloading() {
		return this.taskSession.download();
	}
	
	/**
	 * <p>任务是否完成</p>
	 */
	public boolean completed() {
		return this.taskSession.complete();
	}
	
	/**
	 * @return 任务是否准备完成
	 */
	public boolean ready() {
		return this.ready;
	}
	
	/**
	 * <p>任务名称</p>
	 * <p>需要验证{@linkplain #taskSession 任务信息}是否为空：任务加载后没有开始下载时会导致空指针</p>
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
	
	/**
	 * @see {@link TaskSession#getSize()}
	 */
	public long size() {
		return this.taskSession.getSize();
	}
	
	/**
	 * @see {@link TorrentStreamGroup#fullPieces(BitSet)}
	 */
	public void fullPieces(BitSet pieces) {
		this.torrentStreamGroup.fullPieces(pieces);
	}
	
	/**
	 * @see {@link TorrentStreamGroup#fullPieces()}
	 */
	public void fullPieces() {
		this.torrentStreamGroup.fullPieces();
	}
	
	/**
	 * @see {@link TorrentStreamGroup#health()}
	 */
	public int health() {
		if(this.torrentStreamGroup != null) {
			return this.torrentStreamGroup.health();
		}
		return 0;
	}
	
	/**
	 * @see {@link TorrentStreamGroup#pieces()}
	 */
	public BitSet pieces() {
		return this.torrentStreamGroup.pieces();
	}
	
	/**
	 * @see {@link TorrentStreamGroup#allPieces()}
	 */
	public BitSet allPieces() {
		return this.torrentStreamGroup.allPieces();
	}
	
	/**
	 * @return 任务动作
	 */
	public Action action() {
		return this.action;
	}
	
	/**
	 * @return 磁力链接
	 */
	public Magnet magnet() {
		return this.magnet;
	}
	
	/**
	 * @return 种子信息
	 */
	public Torrent torrent() {
		return this.torrent;
	}
	
	/**
	 * @return InfoHash
	 */
	public InfoHash infoHash() {
		return this.infoHash;
	}
	
	/**
	 * @see {@link InfoHash#infoHashHex()}
	 */
	public String infoHashHex() {
		return this.infoHash.infoHashHex();
	}
	
	/**
	 * @return 任务信息
	 */
	public ITaskSession taskSession() {
		return this.taskSession;
	}
	
	/**
	 * @return 是否是私有种子
	 */
	public boolean isPrivateTorrent() {
		if(this.torrent == null) {
			return false;
		}
		return this.torrent.getInfo().isPrivateTorrent();
	}
	
	/**
	 * <p>获取任务统计信息</p>
	 */
	public IStatisticsSession statistics() {
		return this.taskSession.statistics();
	}
	
}
