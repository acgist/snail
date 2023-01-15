package com.acgist.snail.net.torrent;

import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.acgist.snail.config.PeerConfig.Action;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.IStatisticsSession;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.ScheduledException;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.IMultifileCompletedChecker;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.net.torrent.dht.DhtLauncher;
import com.acgist.snail.net.torrent.peer.PeerContext;
import com.acgist.snail.net.torrent.peer.PeerDownloaderGroup;
import com.acgist.snail.net.torrent.peer.PeerSession;
import com.acgist.snail.net.torrent.peer.PeerSubMessageHandler;
import com.acgist.snail.net.torrent.peer.PeerUploader;
import com.acgist.snail.net.torrent.peer.PeerUploaderGroup;
import com.acgist.snail.net.torrent.tracker.TrackerLauncherGroup;
import com.acgist.snail.protocol.magnet.MagnetBuilder;
import com.acgist.snail.protocol.magnet.TorrentBuilder;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.MapUtils;

/**
 * <p>BT任务信息</p>
 * <p>负责下载任务整体调度：查询Peer、文件管理等等</p>
 * <p>BT任务需要先上传才能进行下载</p>
 * <p>磁力链接不存在上传和下载状态</p>
 * 
 * @author acgist
 */
public final class TorrentSession implements IMultifileCompletedChecker {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentSession.class);
	
	/**
	 * <p>任务动作</p>
	 */
	private Action action;
	/**
	 * <p>是否可用</p>
	 */
	private volatile boolean useable = false;
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
	 * <p>Tracker执行器组</p>
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
	private ScheduledExecutorService executorScheduled;
	/**
	 * <p>PEX定时器</p>
	 */
	private ScheduledFuture<?> pexScheduled;
	/**
	 * <p>HAVE定时器</p>
	 */
	private ScheduledFuture<?> haveScheduled;
	/**
	 * <p>DHT定时器</p>
	 */
	private ScheduledFuture<?> dhtLauncherScheduled;
	/**
	 * <p>PeerUploaderGroup定时器</p>
	 */
	private ScheduledFuture<?> peerUploaderGroupScheduled;
	/**
	 * <p>PeerDownloaderGroup定时器</p>
	 */
	private ScheduledFuture<?> peerDownloaderGroupScheduled;
	/**
	 * <p>TrackerLauncherGroup定时器</p>
	 */
	private ScheduledFuture<?> trackerLauncherGroupScheduled;
	
	/**
	 * @param infoHash InfoHash
	 * @param torrent 种子文件
	 * 
	 * @throws DownloadException 下载异常
	 */
	private TorrentSession(InfoHash infoHash, Torrent torrent) throws DownloadException {
		if(infoHash == null) {
			throw new DownloadException("新建TorrentSession失败（InfoHash）");
		}
		this.torrent = torrent;
		this.infoHash = infoHash;
	}
	
	/**
	 * <p>新建BT任务信息</p>
	 * <p>磁力链接下载种子文件可以为空</p>
	 * 
	 * @param infoHash InfoHash
	 * @param torrent 种子信息
	 * 
	 * @return BT任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	public static final TorrentSession newInstance(InfoHash infoHash, Torrent torrent) throws DownloadException {
		return new TorrentSession(infoHash, torrent);
	}
	
	/**
	 * <p>磁力链接转换</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return 是否转换完成
	 * 
	 * @throws DownloadException 下载异常
	 */
	public boolean magnet(ITaskSession taskSession) throws DownloadException {
		if(this.useable) {
			LOGGER.debug("任务已经开始转换");
			return false;
		}
		this.action = Action.MAGNET;
		this.taskSession = taskSession;
		if(this.checkCompleted()) {
			return true;
		}
		this.loadMagnet();
		this.loadExecutor();
		this.loadExecutorScheduled();
		this.loadTrackerLauncherGroup();
		this.loadTrackerLauncherGroupScheduled();
		this.loadDhtLauncher();
		this.loadDhtLauncherScheduled();
		this.loadPeerUploaderGroup();
		this.loadPeerUploaderGroupScheduled();
		this.loadPeerDownloaderGroup();
		this.loadPeerDownloaderGroupScheduled();
		this.useable = true;
		this.uploadable = false;
		this.downloadable = false;
		return false;
	}
	
	/**
	 * <p>开始上传</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link TorrentSession}
	 * 
	 * @throws DownloadException 下载异常
	 */
	public TorrentSession upload(ITaskSession taskSession) {
		if(this.uploadable) {
			// 防止任务重复开启上传
			LOGGER.debug("任务已经开始上传");
			return this;
		}
		this.taskSession = taskSession;
		this.loadExecutorScheduled();
		this.loadTorrentStreamGroup();
		this.loadPeerUploaderGroup();
		this.loadPeerUploaderGroupScheduled();
		this.useable = true;
		this.uploadable = true;
		return this;
	}

	/**
	 * <p>开始下载</p>
	 * 
	 * @return 是否下载完成
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
	 * 
	 * @param findPeer 是否查找Peer
	 * 
	 * @return 是否下载完成
	 * 
	 * @throws DownloadException 下载异常
	 * 
	 * @see #upload(ITaskSession)
	 */
	public boolean download(boolean findPeer) throws DownloadException {
		if(this.downloadable) {
			// 防止重复开始下载
			LOGGER.debug("任务已经开始下载");
			return false;
		}
		if(!this.uploadable) {
			throw new DownloadException("请先开启任务上传");
		}
		this.action = Action.TORRENT;
		if(this.checkCompleted()) {
			return true;
		}
		this.loadExecutor();
		final boolean privateTorrent = this.privateTorrent();
		if(findPeer) {
			this.loadTrackerLauncherGroup();
			this.loadTrackerLauncherGroupScheduled();
			if(privateTorrent) {
				LOGGER.debug("私有种子：不加载DHT定时任务");
			} else {
				this.loadDhtLauncher();
				this.loadDhtLauncherScheduled();
			}
		}
		this.loadPeerDownloaderGroup();
		this.loadPeerDownloaderGroupScheduled();
		this.loadPeerUploaderDownload();
		if(privateTorrent) {
			LOGGER.debug("私有种子：不加载PEX定时任务");
		} else {
			this.loadPexScheduled();
		}
		this.loadHaveScheduled();
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
		this.executor = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_BT);
	}

	/**
	 * <p>加载定时线程池</p>
	 */
	private void loadExecutorScheduled() {
		final int poolSize = SystemThreadContext.threadSize(2, 4);
		this.executorScheduled = SystemThreadContext.newScheduledExecutor(poolSize, SystemThreadContext.SNAIL_THREAD_BT_SCHEDULED);
	}
	
	/**
	 * <p>加载文件流组</p>
	 */
	private void loadTorrentStreamGroup() {
		this.torrentStreamGroup = TorrentStreamGroup.newInstance(
			this.taskSession.downloadFolder().getAbsolutePath(),
			this.buildSelectedFiles(),
			this
		);
	}

	/**
	 * <p>加载PeerDownloaderGroup</p>
	 */
	private void loadPeerDownloaderGroup() {
		this.peerDownloaderGroup = PeerDownloaderGroup.newInstance(this);
	}
	
	/**
	 * <p>加载PeerDownloader定时任务</p>
	 */
	private void loadPeerDownloaderGroupScheduled() {
		// 任务加载完成立即执行
		final int peerOptimizeInterval = SystemConfig.getPeerOptimizeInterval();
		this.peerDownloaderGroupScheduled = this.scheduledAtFixedDelay(
			0L,
			peerOptimizeInterval,
			TimeUnit.SECONDS,
			this.peerDownloaderGroup::optimize
		);
	}

	/**
	 * <p>加载PeerUploaderGroup</p>
	 */
	private void loadPeerUploaderGroup() {
		this.peerUploaderGroup = PeerUploaderGroup.newInstance(this);
	}
	
	/**
	 * <p>加载PeerUploader定时任务</p>
	 */
	private void loadPeerUploaderGroupScheduled() {
		final int peerOptimizeInterval = SystemConfig.getPeerOptimizeInterval();
		this.peerUploaderGroupScheduled = this.scheduledAtFixedDelay(
			peerOptimizeInterval,
			peerOptimizeInterval,
			TimeUnit.SECONDS,
			this.peerUploaderGroup::optimize
		);
	}
	
	/**
	 * <p>加载PeerUploader下载</p>
	 */
	private void loadPeerUploaderDownload() {
		this.submit(this.peerUploaderGroup::download);
	}
	
	/**
	 * <p>加载Tracker</p>
	 */
	private void loadTrackerLauncherGroup() {
		this.trackerLauncherGroup = TrackerLauncherGroup.newInstance(this);
		this.trackerLauncherGroup.loadTracker();
	}

	/**
	 * <p>加载Tracker定时任务</p>
	 */
	private void loadTrackerLauncherGroupScheduled() {
		// 任务加载完成立即执行
		final int trackerInterval = SystemConfig.getTrackerInterval();
		this.trackerLauncherGroupScheduled = this.scheduledAtFixedDelay(
			0L,
			trackerInterval,
			TimeUnit.SECONDS,
			this.trackerLauncherGroup::findPeer
		);
	}
	
	/**
	 * <p>加载DHT定时任务</p>
	 */
	private void loadDhtLauncher() {
		this.dhtLauncher = DhtLauncher.newInstance(this);
		if(this.action == Action.TORRENT) {
			final var nodes = this.torrent.getNodes();
			// 添加种子自带DHT节点
			if(MapUtils.isNotEmpty(nodes)) {
				nodes.forEach(this.dhtLauncher::put);
			}
		}
	}

	/**
	 * <p>加载DHT定时任务</p>
	 */
	private void loadDhtLauncherScheduled() {
		final int dhtInterval = SystemConfig.getDhtInterval();
		this.dhtLauncherScheduled = this.scheduledAtFixedDelay(
			dhtInterval,
			dhtInterval,
			TimeUnit.SECONDS,
			this.dhtLauncher
		);
	}
	
	/**
	 * <p>加载PEX定时任务</p>
	 */
	private void loadPexScheduled() {
		final int pexInterval = SystemConfig.getPexInterval();
		this.pexScheduled = this.scheduledAtFixedDelay(
			pexInterval,
			pexInterval,
			TimeUnit.SECONDS,
			() -> PeerContext.getInstance().pex(this.infoHashHex())
		);
	}
	
	/**
	 * <p>加载HAVE定时任务</p>
	 * 
	 * TODO：调小定时时间或者固定长度通知
	 */
	private void loadHaveScheduled() {
		final int haveInterval = SystemConfig.getHaveInterval();
		this.haveScheduled = this.scheduledAtFixedDelay(
			haveInterval,
			haveInterval,
			TimeUnit.SECONDS,
			() -> PeerContext.getInstance().have(this.infoHashHex())
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
	 * <p>定时任务（重复执行）</p>
	 * <p>固定周期（周期受到执行时间影响）</p>
	 * 
	 * @param delay 延迟时间
	 * @param period 周期时间
	 * @param unit 时间单位
	 * @param runnable 任务
	 * 
	 * @return 定时任务
	 */
	public ScheduledFuture<?> scheduledAtFixedDelay(long delay, long period, TimeUnit unit, Runnable runnable) {
		ScheduledException.verify(delay);
		ScheduledException.verify(period);
		return this.executorScheduled.scheduleWithFixedDelay(runnable, delay, period, unit);
	}
	
	/**
	 * <p>获取选择下载文件列表</p>
	 * 
	 * @return 选择下载文件列表
	 */
	private List<TorrentFile> buildSelectedFiles() {
		// 切记不可排除填充文件
		final List<TorrentFile> torrentFiles = this.torrent.getInfo().files();
		final List<String> selectedFiles = this.taskSession.multifileSelected();
		for (TorrentFile torrentFile : torrentFiles) {
			torrentFile.selected(selectedFiles.contains(torrentFile.path()));
		}
		return torrentFiles;
	}

	@Override
	public boolean checkCompleted() {
		if(this.completed()) {
			// 判断任务是否完成
			return true;
		}
		if(this.action == Action.TORRENT) {
			return this.torrentStreamGroup.completed();
		} else {
			return this.torrent != null;
		}
	}
	
	@Override
	public void checkCompletedAndUnlock() {
		if(this.checkCompleted()) {
			this.taskSession.unlockDownload();
		}
	}
	
	/**
	 * <p>释放磁力链接资源</p>
	 * 
	 * @see #releaseDownload()
	 * @see #releaseUpload()
	 */
	public void releaseMagnet() {
		LOGGER.debug("Torrent释放资源（磁力链接）");
		this.releaseDownload();
		this.releaseUpload();
	}
	
	/**
	 * <p>释放下载资源</p>
	 */
	public void releaseDownload() {
		this.downloadable = false;
		LOGGER.debug("Torrent释放资源（下载）");
		if(this.completed()) {
			PeerContext.getInstance().uploadOnly(this.infoHashHex());
		}
		SystemThreadContext.shutdownNow(this.haveScheduled);
		SystemThreadContext.shutdownNow(this.pexScheduled);
		SystemThreadContext.shutdownNow(this.peerDownloaderGroupScheduled);
		if(this.peerDownloaderGroup != null) {
			this.peerDownloaderGroup.release();
		}
		SystemThreadContext.shutdownNow(this.dhtLauncherScheduled);
		SystemThreadContext.shutdownNow(this.trackerLauncherGroupScheduled);
		if(this.trackerLauncherGroup != null) {
			this.trackerLauncherGroup.release();
		}
		SystemThreadContext.shutdownNow(this.executor);
		if(this.torrentStreamGroup != null) {
			this.torrentStreamGroup.flush();
		}
	}
	
	/**
	 * <p>释放上传资源</p>
	 */
	public void releaseUpload() {
		this.useable = false;
		this.uploadable = false;
		LOGGER.debug("Torrent释放资源（上传）");
		SystemThreadContext.shutdownNow(this.peerUploaderGroupScheduled);
		if(this.peerUploaderGroup != null) {
			this.peerUploaderGroup.release();
		}
		if(this.torrentStreamGroup != null) {
			this.torrentStreamGroup.release();
		}
		SystemThreadContext.shutdownNow(this.executorScheduled);
	}

	/**
	 * <p>删除任务信息</p>
	 */
	public void delete() {
		final String infoHashHex = this.infoHashHex();
		PeerContext.getInstance().remove(infoHashHex);
		TorrentContext.getInstance().remove(infoHashHex);
	}

	/**
	 * <p>保存种子文件</p>
	 * <p>重新并加载种子文件和InfoHash</p>
	 */
	public void saveTorrent() {
		String torrentFilePath = null;
		final TorrentBuilder builder = TorrentBuilder.newInstance(this.infoHash, this.trackerLauncherGroup.trackers());
		try {
			torrentFilePath = builder.buildFile(this.taskSession.downloadFolder().getAbsolutePath());
			this.torrent = TorrentContext.loadTorrent(torrentFilePath);
			this.infoHash = this.torrent.infoHash();
		} catch (DownloadException | PacketSizeException e) {
			LOGGER.error("加载种子异常：{}", torrentFilePath, e);
		}
		if(torrentFilePath == null) {
			return;
		}
		final long torrentFileSize = FileUtils.fileSize(torrentFilePath);
		this.taskSession.setTorrent(torrentFilePath);
		this.taskSession.setSize(torrentFileSize);
		this.taskSession.downloadSize(torrentFileSize);
		this.taskSession.update();
		this.checkCompletedAndUnlock();
	}
	
	/**
	 * <p>发送have消息</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @see PeerContext#have(String, int)
	 */
	public void have(int index) {
		PeerContext.getInstance().have(this.infoHashHex(), index);
	}
	
	/**
	 * <p>获取任务名称</p>
	 * 
	 * @return 任务名称
	 */
	public String name() {
		// 任务信息可能为空：任务还没有准备完成
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
	 * <p>获取文件大小（B）</p>
	 * 
	 * @return 文件大小（B）
	 * 
	 * @see ITaskSession#getSize()
	 */
	public long size() {
		return this.taskSession.getSize();
	}
	
	/**
	 * <p>新建已经下载Piece位图</p>
	 * 
	 * @return 已经下载Piece位图
	 */
	public BitSet buildPieces() {
		final byte[] payload = this.taskSession.getPayload();
		if(payload == null) {
			return new BitSet(this.torrent.getInfo().pieceSize());
		} else {
			return BitSet.valueOf(payload);
		}
	}
	
	/**
	 * <p>更新已经下载Piece位图</p>
	 * 
	 * @param persistent 是否保存
	 */
	public void updatePieces(boolean persistent) {
		final byte[] payload = this.pieces().toByteArray();
		this.taskSession.setPayload(payload);
		if(persistent) {
			this.taskSession.update();
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
	 * <p>获取种子info数据Hash（HEX）</p>
	 * 
	 * @return 种子info数据Hash（HEX）
	 * 
	 * @see InfoHash#infoHashHex()
	 */
	public String infoHashHex() {
		return this.infoHash.infoHashHex();
	}
	
	/**
	 * <p>判断是否是私有种子</p>
	 * 
	 * @return 是否是私有种子
	 */
	public boolean privateTorrent() {
		if(this.torrent == null) {
			return false;
		}
		return this.torrent.getInfo().privateTorrent();
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
	 * <p>获取文件流组</p>
	 * 
	 * @return 文件流组
	 */
	public TorrentStreamGroup torrentStreamGroup() {
		return this.torrentStreamGroup;
	}
	
	/**
	 * <p>判断是否可用</p>
	 * 
	 * @return 是否可用
	 */
	public boolean useable() {
		return this.useable;
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
	 * <p>设置已经下载大小</p>
	 * 
	 * @param size 已经下载大小
	 * 
	 * @see ITaskSession#downloadSize(long)
	 */
	public void downloadSize(long size) {
		this.taskSession.downloadSize(size);
	}
	
	/**
	 * <p>判断任务是否处于完成状态</p>
	 * 
	 * @return 是否处于完成状态
	 * 
	 * @see ITaskSession#statusCompleted()
	 */
	public boolean completed() {
		return this.taskSession.statusCompleted();
	}
	
	/**
	 * <p>获取统计信息</p>
	 * 
	 * @return 统计信息
	 * 
	 * @see ITaskSession#statistics()
	 */
	public IStatisticsSession statistics() {
		return this.taskSession.statistics();
	}
	
	/**
	 * <p>重新加载下载文件</p>
	 * 
	 * @return 新增下载文件数量
	 * 
	 * @see TorrentStreamGroup#reload(String, List)
	 */
	public int reload() {
		return this.torrentStreamGroup.reload(
			this.taskSession.downloadFolder().getAbsolutePath(),
			this.buildSelectedFiles()
		);
	}
	
	/**
	 * <p>指定下载Piece索引</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @see TorrentStreamGroup#piecePos(int)
	 */
	public void piecePos(int index) {
		this.torrentStreamGroup.piecePos(index);
	}
	
	/**
	 * <p>挑选下载Piece</p>
	 * 
	 * @param peerPieces Peer已经下载Piece位图
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
	 * <p>读取Piece数据</p>
	 * 
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
	 * <p>保存Piece</p>
	 * 
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
	 * <p>判断Piece是否已经下载</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return 是否已经下载
	 * 
	 * @see TorrentStreamGroup#hasPiece(int)
	 */
	public boolean hasPiece(int index) {
		return this.torrentStreamGroup.hasPiece(index);
	}

	/**
	 * <p>设置下载失败Piece</p>
	 * 
	 * @param piece Piece
	 * 
	 * @see TorrentStreamGroup#undone(TorrentPiece)
	 */
	public void undone(TorrentPiece piece) {
		this.torrentStreamGroup.undone(piece);
	}
	
	/**
	 * <p>设置完整Piece位图</p>
	 * 
	 * @param pieces Piece位图
	 * 
	 * @see TorrentStreamGroup#fullPieces(BitSet)
	 */
	public void fullPieces(BitSet pieces) {
		this.torrentStreamGroup.fullPieces(pieces);
	}
	
	/**
	 * <p>设置完整Piece位图</p>
	 * 
	 * @see TorrentStreamGroup#fullPieces()
	 */
	public void fullPieces() {
		this.torrentStreamGroup.fullPieces();
	}
	
	/**
	 * <p>获取健康度</p>
	 * 
	 * @return 健康度
	 * 
	 * @see TorrentStreamGroup#health()
	 */
	public int health() {
		return this.torrentStreamGroup.health();
	}

	/**
	 * <p>校验文件</p>
	 * 
	 * @return 是否校验成功
	 * 
	 * @see TorrentStreamGroup#verify()
	 */
	public boolean verify() {
		try {
			return this.torrentStreamGroup.verify();
		} catch (IOException e) {
			LOGGER.error("文件校验异常", e);
		}
		return false;
	}
	
	/**
	 * <p>获取已经下载Piece位图</p>
	 * 
	 * @return 已经下载Piece位图
	 * 
	 * @see TorrentStreamGroup#pieces()
	 */
	public BitSet pieces() {
		return this.torrentStreamGroup.pieces();
	}
	
	/**
	 * <p>获取选择下载Piece位图</p>
	 * 
	 * @return 选择下载Piece位图
	 * 
	 * @see TorrentStreamGroup#selectPieces()
	 */
	public BitSet selectPieces() {
		return this.torrentStreamGroup.selectPieces();
	}
	
	/**
	 * <p>所有Piece位图</p>
	 * 
	 * @return 所有Piece位图
	 * 
	 * @see TorrentStreamGroup#allPieces()
	 */
	public BitSet allPieces() {
		return this.torrentStreamGroup.allPieces();
	}
	
	/**
	 * <p>添加Peer客户端节点</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @see DhtLauncher#put(String, Integer)
	 */
	public void newNode(String host, int port) {
		if(this.dhtLauncher != null) {
			this.dhtLauncher.put(host, port);
		}
	}
	
	/**
	 * <p>新建Peer接入连接</p>
	 * 
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
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.name());
	}
	
}
