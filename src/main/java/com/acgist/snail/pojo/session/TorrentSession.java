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
import com.acgist.snail.net.torrent.bootstrap.PeerConnect;
import com.acgist.snail.net.torrent.bootstrap.PeerConnectGroup;
import com.acgist.snail.net.torrent.bootstrap.PeerLauncherGroup;
import com.acgist.snail.net.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.net.torrent.bootstrap.TrackerLauncherGroup;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.pojo.bean.Magnet;
import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.protocol.magnet.bootstrap.MagnetBuilder;
import com.acgist.snail.protocol.magnet.bootstrap.TorrentBuilder;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentFile;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.config.PeerConfig.Action;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>Torrent Session</p>
 * <p>负责下载任务整体调度，查询Peer、文件管理等。</p>
 * <p>任务一定需要先设置上传，然后才能进行下载。</p>
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
	 * 动作：磁力链接转换、文件下载
	 */
	private Action action;
	/**
	 * 可上传
	 */
	private boolean uploadable = false;
	/**
	 * 可下载
	 */
	private boolean downloadable = false;
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
	public boolean magnet(TaskSession taskSession) throws DownloadException {
		this.action = Action.magnet;
		this.taskSession = taskSession;
		this.loadMagnet();
		this.loadExecutor();
		this.loadExecutorTimer();
		this.loadTrackerLauncherGroup();
		this.loadTrackerLauncherGroupTimer();
		this.loadDhtLauncher();
		this.loadDhtLauncherTimer();
		this.loadPeerLauncherGroup();
		this.loadPeerLauncherGroupTimer();
		return this.torrent != null;
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
	 * <p>开始下载</p>
	 * <p>需要先调用{@link #upload(TaskSession)}对任务进行上传。</p>
	 */
	public boolean download() throws DownloadException {
		return this.download(true);
	}
	
	/**
	 * <p>开始下载：加载线程池、Peer、Tracker、DHT。</p>
	 * <p>如果文件已经下载完成或者任务已经完成不会再加载线程池、Peer、Tracker、DHT。</p>
	 * <p>需要先调用{@link #upload(TaskSession)}对任务进行上传。</p>
	 * 
	 * @param findPeer 是否查找Peer：true-使用Tracker、DHT查找Peer；false-不查找；
	 * 
	 * @return true-下载完成；false-未完成；
	 */
	public boolean download(boolean findPeer) throws DownloadException {
		this.action = Action.torrent;
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
	 * 加载磁力链接
	 */
	private void loadMagnet() throws DownloadException {
		this.magnet = MagnetBuilder.newInstance(this.taskSession.entity().getUrl()).build();
	}
	
	/**
	 * 加载线程池
	 */
	private void loadExecutor() {
		this.executor = SystemThreadContext.newExecutor(4, 10, 100, 60L, SystemThreadContext.SNAIL_THREAD_BT);
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
			throw new DownloadException("下载任务不存在");
		}
		this.torrentStreamGroup = TorrentStreamGroup.newInstance(
			this.taskSession.downloadFolder().getPath(),
			setSelectFiles(),
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
		if(this.action == Action.torrent) {
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
			PeerManager.getInstance().pex(this.infoHashHex(), this.peerLauncherGroup.optimizePeerSession()); // pex消息
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
	 * 设置选择的下载文件并返回文件列表。
	 */
	private List<TorrentFile> setSelectFiles() {
		final TorrentInfo torrentInfo = this.torrent.getInfo();
		final List<TorrentFile> torrentFiles = torrentInfo.files();
		final List<String> selectedFiles = this.taskSession.downloadTorrentFiles();
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
	 * 	<dd>磁力链接：种子文件不为空</dd>
	 * </dl>
	 */
	public boolean checkCompleted() {
		if(completed()) {
			return true;
		}
		if(this.action == Action.torrent) {
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
	 * 释放资源（释放下载使用的资源），完成时不释放文件资源。
	 */
	public void releaseDownload() {
		LOGGER.debug("Torrent释放资源（下载）");
		SystemThreadContext.shutdown(this.pexTimer);
		SystemThreadContext.shutdown(this.peerLauncherGroupTimer);
		if(this.peerLauncherGroup != null) {
			this.peerLauncherGroup.release();
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
		SystemThreadContext.shutdown(this.peerConnectGroupTimer);
		if(this.peerConnectGroup != null) {
			this.peerConnectGroup.release();
		}
		if(this.torrentStreamGroup != null) {
			this.torrentStreamGroup.release();
		}
		SystemThreadContext.shutdownNow(this.executorTimer);
		this.uploadable = false;
	}

	/**
	 * <p>发送have消息</p>
	 * 
	 * TODO：优化have消息，使用异步线程发送，防止部分Peer通知过慢，导致所有线程卡死。
	 * 
	 * @param index Piece序号
	 */
	public void have(int index) {
		PeerManager.getInstance().have(this.infoHash.infoHashHex(), index);
	}

	/**
	 * <p>挑选一个Piece下载</p>
	 */
	public TorrentPiece pick(final BitSet peerPieces) {
		return torrentStreamGroup.pick(peerPieces);
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
	public boolean piece(TorrentPiece piece) {
		return this.torrentStreamGroup.piece(piece);
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
	 * 保存种子文件，并重新加载种子和InfoHash。
	 */
	public void saveTorrentFile() {
		final var entity = this.taskSession.entity();
		final TorrentBuilder builder = TorrentBuilder.newInstance(this.infoHash, this.trackerLauncherGroup.trackers());
		final String torrentFile = builder.buildFile(this.taskSession.downloadFolder().getPath());
		entity.setTorrent(torrentFile);
		final TaskRepository repository = new TaskRepository();
		repository.update(entity);
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
	public PeerConnect newPeerConnect(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		return this.peerConnectGroup.newPeerConnect(peerSession, peerSubMessageHandler);
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
	 * 是否可以下载，能够下载一定能够上传。
	 */
	public boolean downloadable() {
		return this.downloadable;
	}
	
	/**
	 * 任务处于下载中
	 */
	public boolean running() {
		return this.taskSession != null && this.taskSession.download();
	}
	
	/**
	 * 任务是否完成
	 */
	public boolean completed() {
		return this.taskSession != null && this.taskSession.complete();
	}
	
	public String name() {
		return this.torrent.name();
	}
	
	public BitSet pieces() {
		return this.torrentStreamGroup.pieces();
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
	
	public TaskSession taskSession() {
		return this.taskSession;
	}
	
	public StatisticsSession statistics() {
		return this.taskSession == null ? null : this.taskSession.statistics();
	}
	
}
