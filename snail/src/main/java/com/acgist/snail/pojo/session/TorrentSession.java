package com.acgist.snail.pojo.session;

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.PeerConfig.Action;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.exception.DownloadException;
import com.acgist.snail.exception.NetException;
import com.acgist.snail.exception.TimerException;
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
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>BT任务信息</p>
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
	 * <p>动作：磁力链接下载、BT任务下载</p>
	 */
	private Action action;
	/**
	 * <p>是否准备完成</p>
	 */
	private volatile boolean done = false;
	/**
	 * <p>上传状态</p>
	 */
	private volatile boolean uploadable = false;
	/**
	 * <p>下载状态</p>
	 */
	private volatile boolean downloadable = false;
	/**
	 * <p>磁力链接</p>
	 */
	private Magnet magnet;
	/**
	 * <p>种子信息</p>
	 */
	private Torrent torrent;
	/**
	 * <p>种子InfoHash</p>
	 */
	private InfoHash infoHash;
	/**
	 * <p>任务信息</p>
	 */
	private ITaskSession taskSession;
	/**
	 * <p>DHT定时任务</p>
	 */
	private DhtLauncher dhtLauncher;
	/**
	 * <p>PeerUploader组</p>
	 */
	private PeerUploaderGroup peerUploaderGroup;
	/**
	 * <p>PeerDownloader组</p>
	 */
	private PeerDownloaderGroup peerDownloaderGroup;
	/**
	 * <p>文件流组</p>
	 */
	private TorrentStreamGroup torrentStreamGroup;
	/**
	 * <p>Tracker组</p>
	 */
	private TrackerLauncherGroup trackerLauncherGroup;
	/**
	 * <p>线程池</p>
	 * <p>使用缓存线程池：防止过多下载时出现卡死现象</p>
	 */
	private ExecutorService executor;
	/**
	 * <p>定时线程池</p>
	 */
	private ScheduledExecutorService executorTimer;
	/**
	 * <p>PEX定时器</p>
	 */
	private ScheduledFuture<?> pexTimer;
	/**
	 * <p>DHT定时器</p>
	 */
	private ScheduledFuture<?> dhtLauncherTimer;
	/**
	 * <p>PeerUploaderGroup定时器</p>
	 */
	private ScheduledFuture<?> peerUploaderGroupTimer;
	/**
	 * <p>PeerDownloaderGroup定时器</p>
	 */
	private ScheduledFuture<?> peerDownloaderGroupTimer;
	/**
	 * <p>TrackerLauncherGroup定时器</p>
	 */
	private ScheduledFuture<?> trackerLauncherGroupTimer;
	
	/**
	 * <p>BT任务信息</p>
	 * <p>磁力链接下载种子文件可以为空</p>
	 * 
	 * @param infoHash InfoHash
	 * @param torrent 种子文件
	 * 
	 * @throws DownloadException 下载异常
	 */
	private TorrentSession(InfoHash infoHash, Torrent torrent) throws DownloadException {
		if(infoHash == null) {
			throw new DownloadException("创建TorrentSession失败（InfoHash为空）");
		}
		this.torrent = torrent;
		this.infoHash = infoHash;
	}
	
	/**
	 * <p>新建BT任务信息</p>
	 * 
	 * @param infoHash InfoHash
	 * @param torrent 种子信息
	 * 
	 * @return BT任务信息
	 * 
	 * @throws DownloadException 下载异常
	 * 
	 * @see #TorrentSession(InfoHash, Torrent)
	 */
	public static final TorrentSession newInstance(InfoHash infoHash, Torrent torrent) throws DownloadException {
		return new TorrentSession(infoHash, torrent);
	}
	
	/**
	 * <p>磁力链接转换</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return true-下载完成；false-等待下载；
	 * 
	 * @throws DownloadException 下载异常
	 */
	public boolean magnet(ITaskSession taskSession) throws DownloadException {
		this.action = Action.MAGNET;
		this.taskSession = taskSession;
		final boolean complete = this.torrent != null;
		if(complete) {
			LOGGER.debug("磁力链接任务已经完成不加载资源");
		} else {
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
		}
		this.done = true;
		this.uploadable = false;
		this.downloadable = false;
		return complete;
	}
	
	/**
	 * <p>开始上传</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return BT任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	public TorrentSession upload(ITaskSession taskSession) {
		this.taskSession = taskSession;
		this.loadExecutorTimer();
		this.loadTorrentStreamGroup();
		this.loadPeerUploaderGroup();
		this.loadPeerUploaderGroupTimer();
		this.done = true;
		this.uploadable = true;
		return this;
	}

	/**
	 * <p>开始下载</p>
	 * 
	 * @return true-下载完成；false-等待下载；
	 * 
	 * @throws DownloadException 下载异常
	 * 
	 * @see #download(boolean)
	 */
	public boolean download() throws DownloadException {
		return this.download(true);
	}
	
	/**
	 * <p>开始下载</p>
	 * <p>如果任务已经完成或文件已经下载完成直接返回下载完成</p>
	 * <p>需要调用{@link #upload(ITaskSession)}方法开启任务上传</p>
	 * 
	 * @param findPeer 是否查找Peer（加载Tracker、DHT）：true-查找；false-不查找；
	 * 
	 * @return true-下载完成；false-等待下载；
	 * 
	 * @throws DownloadException 下载异常
	 */
	public boolean download(boolean findPeer) throws DownloadException {
		if(!this.uploadable) {
			throw new DownloadException("请先开启任务上传");
		}
		if(this.taskSession == null) {
			throw new DownloadException("下载任务准备失败");
		}
		this.action = Action.TORRENT;
		if(this.taskSession.complete() || this.torrentStreamGroup.complete()) {
			return true;
		}
		this.loadExecutor();
		if(findPeer) {
			this.loadTrackerLauncherGroup();
			this.loadTrackerLauncherGroupTimer();
			if(this.isPrivateTorrent()) {
				LOGGER.debug("私有种子：不加载DHT定时任务");
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
	 * 
	 * @throws DownloadException 下载异常
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
			this.taskSession.downloadFolder().getAbsolutePath(),
			this.buildSelectedFiles(),
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
	 * <p>PeerDownloader定时任务加载完成立即执行</p>
	 */
	private void loadPeerDownloaderGroupTimer() {
		final int peerOptimizeInterval = SystemConfig.getPeerOptimizeInterval();
		this.peerDownloaderGroupTimer = this.timerFixedDelay(
			0L,
			peerOptimizeInterval,
			TimeUnit.SECONDS,
			() -> this.peerDownloaderGroup.optimize()
		);
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
		final int peerOptimizeInterval = SystemConfig.getPeerOptimizeInterval();
		this.peerUploaderGroupTimer = this.timerFixedDelay(
			peerOptimizeInterval,
			peerOptimizeInterval,
			TimeUnit.SECONDS,
			() -> this.peerUploaderGroup.optimize()
		);
	}
	
	/**
	 * <p>加载PeerUploader下载</p>
	 * <p>如果连接的Peer可以下载，开始发送下载请求。</p>
	 */
	private void loadPeerUploaderDownload() {
		this.submit(() -> this.peerUploaderGroup.download());
	}
	
	/**
	 * <p>加载Tracker</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void loadTrackerLauncherGroup() throws DownloadException {
		this.trackerLauncherGroup = TrackerLauncherGroup.newInstance(this);
		this.trackerLauncherGroup.loadTracker();
	}

	/**
	 * <p>加载Tracker定时任务</p>
	 * <p>Tracker定时任务加载完成立即执行</p>
	 */
	private void loadTrackerLauncherGroupTimer() {
		final int trackerInterval = SystemConfig.getTrackerInterval();
		this.trackerLauncherGroupTimer = this.timerFixedDelay(
			0L,
			trackerInterval,
			TimeUnit.SECONDS,
			() -> this.trackerLauncherGroup.findPeer()
		);
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
				nodes.forEach((host, port) -> this.dhtLauncher.put(host, port));
			}
		}
	}

	/**
	 * <p>加载DHT定时任务</p>
	 */
	private void loadDhtLauncherTimer() {
		final int dhtInterval = SystemConfig.getDhtInterval();
		this.dhtLauncherTimer = this.timerFixedDelay(
			dhtInterval,
			dhtInterval,
			TimeUnit.SECONDS,
			this.dhtLauncher
		);
	}
	
	/**
	 * <p>加载PEX定时任务</p>
	 */
	private void loadPexTimer() {
		final int pexInterval = SystemConfig.getPexInterval();
		this.pexTimer = this.timerFixedDelay(
			pexInterval,
			pexInterval,
			TimeUnit.SECONDS,
			() -> PeerManager.getInstance().pex(this.infoHashHex())
		);
	}
	
	/**
	 * <p>异步执行</p>
	 * 
	 * @param runnable 任务
	 */
	public void submit(Runnable runnable) {
		this.executor.submit(runnable);
	}
	
	/**
	 * <p>定时任务（不重复执行）</p>
	 * 
	 * @param delay 延迟时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 * 
	 * @return 定时任务
	 */
	public ScheduledFuture<?> timer(long delay, TimeUnit unit, Runnable runnable) {
		TimerException.verify(delay);
		return this.executorTimer.schedule(runnable, delay, unit);
	}
	
	/**
	 * <p>定时任务（重复执行）</p>
	 * 
	 * @param delay 延迟时间
	 * @param period 执行周期
	 * @param unit 时间单位
	 * @param runnable 任务
	 * 
	 * @return 定时任务
	 */
	public ScheduledFuture<?> timer(long delay, long period, TimeUnit unit, Runnable runnable) {
		TimerException.verify(delay);
		TimerException.verify(period);
		return this.executorTimer.scheduleAtFixedRate(runnable, delay, period, unit);
	}
	
	/**
	 * <p>定时任务（重复执行）</p>
	 * 
	 * @param delay 延迟时间
	 * @param period 执行周期
	 * @param unit 时间单位
	 * @param runnable 任务
	 * 
	 * @return 定时任务
	 */
	public ScheduledFuture<?> timerFixedDelay(long delay, long period, TimeUnit unit, Runnable runnable) {
		TimerException.verify(delay);
		TimerException.verify(period);
		return this.executorTimer.scheduleWithFixedDelay(runnable, delay, period, unit);
	}
	
	/**
	 * <p>设置种子文件中选择下载的文件并返回文件列表</p>
	 * 
	 * @return 选择下载文件列表
	 */
	private List<TorrentFile> buildSelectedFiles() {
		final TorrentInfo torrentInfo = this.torrent.getInfo();
		final List<TorrentFile> torrentFiles = torrentInfo.files();
		final List<String> selectedFiles = this.taskSession.multifileSelected();
		for (TorrentFile torrentFile : torrentFiles) {
			torrentFile.selected(selectedFiles.contains(torrentFile.path()));
		}
		return torrentFiles;
	}

	/**
	 * <p>检测任务是否下载完成</p>
	 * <p>BT任务：文件下载完成</p>
	 * <p>磁力链接：种子文件下载完成</p>
	 * 
	 * @return 是否下载完成
	 */
	public boolean checkCompleted() {
		if(this.completed()) {
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
		if(this.checkCompleted()) {
			LOGGER.debug("任务下载完成：{}", this.name());
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
		SystemThreadContext.shutdownNow(this.pexTimer);
		SystemThreadContext.shutdownNow(this.peerDownloaderGroupTimer);
		if(this.peerDownloaderGroup != null) {
			this.peerDownloaderGroup.release();
		}
		SystemThreadContext.shutdownNow(this.dhtLauncherTimer);
		SystemThreadContext.shutdownNow(this.trackerLauncherGroupTimer);
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
		SystemThreadContext.shutdownNow(this.peerUploaderGroupTimer);
		if(this.peerUploaderGroup != null) {
			this.peerUploaderGroup.release();
		}
		if(this.torrentStreamGroup != null) {
			this.torrentStreamGroup.release();
		}
		SystemThreadContext.shutdownNow(this.executorTimer);
		this.done = false;
		this.uploadable = false;
	}

	/**
	 * <p>保存种子文件</p>
	 * <p>重新加载种子文件和InfoHash</p>
	 */
	public void saveTorrent() {
		final TorrentBuilder builder = TorrentBuilder.newInstance(this.infoHash, this.trackerLauncherGroup.trackers());
		final String torrentFilePath = builder.buildFile(this.taskSession.downloadFolder().getAbsolutePath());
		this.taskSession.setTorrent(torrentFilePath); // 保存种子文件路径
		this.taskSession.update();
		try {
			this.torrent = TorrentManager.loadTorrent(torrentFilePath);
			this.infoHash = this.torrent.infoHash();
		} catch (DownloadException e) {
			LOGGER.error("解析种子异常", e);
		}
		final var downloader = this.taskSession.downloader();
		final long size = FileUtils.fileSize(torrentFilePath);
		this.taskSession.setSize(size); // 设置任务大小
		this.taskSession.downloadSize(size); // 设置已下载大小
		if(downloader != null) {
			downloader.unlockDownload();
		}
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
	 * <p>获取任务名称</p>
	 * 
	 * @return 任务名称
	 */
	public String name() {
		// 任务加载后没有开始下载时任务信息可能为空
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
	 * <p>获取任务动作</p>
	 * 
	 * @return 任务动作
	 */
	public Action action() {
		return this.action;
	}
	
	/**
	 * <p>获取磁力链接</p>
	 * 
	 * @return 磁力链接
	 */
	public Magnet magnet() {
		return this.magnet;
	}
	
	/**
	 * <p>获取种子信息</p>
	 * 
	 * @return 种子信息
	 */
	public Torrent torrent() {
		return this.torrent;
	}
	
	/**
	 * <p>获取InfoHash</p>
	 * 
	 * @return InfoHash
	 */
	public InfoHash infoHash() {
		return this.infoHash;
	}
	
	/**
	 * @return 16进制种子info数据Hash
	 * 
	 * @see InfoHash#infoHashHex()
	 */
	public String infoHashHex() {
		return this.infoHash.infoHashHex();
	}
	
	/**
	 * <p>获取任务信息</p>
	 * 
	 * @return 任务信息
	 */
	public ITaskSession taskSession() {
		return this.taskSession;
	}
	
	/**
	 * <p>判断是否是私有种子</p>
	 * 
	 * @return 是否是私有种子
	 */
	public boolean isPrivateTorrent() {
		if(this.torrent == null) {
			return false;
		}
		return this.torrent.getInfo().isPrivateTorrent();
	}
	
	/**
	 * <p>判断是否准备完成</p>
	 * 
	 * @return 是否准备完成
	 */
	public boolean done() {
		return this.done;
	}

	/**
	 * <p>判断是否可以上传</p>
	 * 
	 * @return 是否可以上传
	 */
	public boolean uploadable() {
		return this.uploadable;
	}
	
	/**
	 * <p>判断是否可以下载</p>
	 * 
	 * @return 是否可以下载
	 */
	public boolean downloadable() {
		return this.downloadable;
	}
	
	/**
	 * @return 文件大小（B）
	 * 
	 * @see ITaskSession#getSize()
	 */
	public long size() {
		return this.taskSession.getSize();
	}
	
	/**
	 * @param size 已下载大小
	 * 
	 * @see ITaskSession#downloadSize(long)
	 */
	public void resize(long size) {
		this.taskSession.downloadSize(size);
	}
	
	/**
	 * @return 是否处于完成状态
	 * 
	 * @see ITaskSession#complete()
	 */
	public boolean completed() {
		return this.taskSession.complete();
	}
	
	/**
	 * @return 统计信息
	 * 
	 * @see ITaskSession#statistics()
	 */
	public IStatisticsSession statistics() {
		return this.taskSession.statistics();
	}
	
	/**
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @see DhtLauncher#put(String, Integer)
	 */
	public void newDhtNode(String host, int port) {
		if(this.dhtLauncher != null) {
			this.dhtLauncher.put(host, port);
		}
	}
	
	/**
	 * @return 新增下载文件数量
	 * 
	 * @see TorrentStreamGroup#reload(String, List, TorrentSession)
	 */
	public int reload() {
		return this.torrentStreamGroup.reload(
			this.taskSession.downloadFolder().getAbsolutePath(),
			this.buildSelectedFiles(),
			this
		);
	}
	
	/**
	 * @param peerPieces Peer已下载Piece位图
	 * @param suggestPieces Peer推荐Piece位图
	 * 
	 * @return 下载Piece
	 * 
	 * @see TorrentStreamGroup#pick(BitSet, BitSet)
	 */
	public TorrentPiece pick(BitSet peerPieces, BitSet suggestPieces) {
		return torrentStreamGroup.pick(peerPieces, suggestPieces);
	}
	
	/**
	 * @param index Piece索引
	 * @param begin Piece偏移
	 * @param length 数据长度
	 * 
	 * @return Piece数据
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see TorrentStreamGroup#read(int, int, int)
	 */
	public byte[] read(int index, int begin, int length) throws NetException {
		return this.torrentStreamGroup.read(index, begin, length);
	}

	/**
	 * @param piece Piece数据
	 * 
	 * @return 是否保存成功
	 * 
	 * @see TorrentStreamGroup#write(TorrentPiece)
	 */
	public boolean write(TorrentPiece piece) {
		return this.torrentStreamGroup.write(piece);
	}
	
	/**
	 * @param index Piece索引
	 * 
	 * @return {@code true}-已下载；{@code false}-未下载；
	 * 
	 * @see TorrentStreamGroup#havePiece(int)
	 */
	public boolean havePiece(int index) {
		return this.torrentStreamGroup.havePiece(index);
	}

	/**
	 * @param piece Piece
	 * 
	 * @see TorrentStreamGroup#undone(TorrentPiece)
	 */
	public void undone(TorrentPiece piece) {
		this.torrentStreamGroup.undone(piece);
	}
	
	/**
	 * @param pieces Piece位图
	 * 
	 * @see TorrentStreamGroup#fullPieces(BitSet)
	 */
	public void fullPieces(BitSet pieces) {
		this.torrentStreamGroup.fullPieces(pieces);
	}
	
	/**
	 * @see TorrentStreamGroup#fullPieces()
	 */
	public void fullPieces() {
		this.torrentStreamGroup.fullPieces();
	}
	
	/**
	 * @return 健康度
	 * 
	 * @see TorrentStreamGroup#health()
	 */
	public int health() {
		return this.torrentStreamGroup.health();
	}
	
	/**
	 * @return 已下载Piece位图
	 * 
	 * @see TorrentStreamGroup#pieces()
	 */
	public BitSet pieces() {
		return this.torrentStreamGroup.pieces();
	}
	
	/**
	 * @return 被选中Piece位图
	 * 
	 * @see TorrentStreamGroup#selectPieces()
	 */
	public BitSet selectPieces() {
		return this.torrentStreamGroup.selectPieces();
	}
	
	/**
	 * @return 所有Piece位图
	 * 
	 * @see TorrentStreamGroup#allPieces()
	 */
	public BitSet allPieces() {
		return this.torrentStreamGroup.allPieces();
	}
	
	/**
	 * @param peerSession Peer信息
	 * @param peerSubMessageHandler Peer消息代理
	 * 
	 * @return Peer接入
	 * 
	 * @see PeerUploaderGroup#newPeerUploader(PeerSession, PeerSubMessageHandler)
	 */
	public PeerUploader newPeerUploader(PeerSession peerSession, PeerSubMessageHandler peerSubMessageHandler) {
		return this.peerUploaderGroup.newPeerUploader(peerSession, peerSubMessageHandler);
	}
	
}
