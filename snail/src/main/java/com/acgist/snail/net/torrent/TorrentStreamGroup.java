package com.acgist.snail.net.torrent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>文件流组</p>
 * 
 * @author acgist
 */
public final class TorrentStreamGroup {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStreamGroup.class);

	/**
	 * <p>指定下载Piece索引</p>
	 * <p>选择下载Piece开始位置索引</p>
	 * 
	 * @see #pick(BitSet, BitSet)
	 */
	private volatile int piecePos = 0;
	/**
	 * <p>已经下载Piece位图</p>
	 */
	private final BitSet pieces;
	/**
	 * <p>选择下载Piece位图</p>
	 */
	private final BitSet selectPieces;
	/**
	 * <p>是否含有完整选择下载Piece位图</p>
	 * 
	 * @see #health()
	 */
	private boolean full;
	/**
	 * <p>完整Piece位图</p>
	 * <p>已经下载Piece位图和Peer已经下载Piece位图</p>
	 * <dl>
	 * 	<dt>更新条件</dt>
	 * 	<dd>加载本地文件</dd>
	 * 	<dd>收到haveAll消息</dd>
	 * 	<dd>收到交换Piece位图消息</dd>
	 * </dl>
	 * 
	 * @see #health()
	 */
	private final BitSet fullPieces;
	/**
	 * <p>文件流中Piece缓存大小</p>
	 */
	private final AtomicLong fileBufferSize;
	/**
	 * <p>种子信息</p>
	 */
	private final Torrent torrent;
	/**
	 * <p>文件流的集合</p>
	 * <p>注意顺序：跨越文件数据读取</p>
	 */
	private final List<TorrentStream> streams;
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	/**
	 * <p>读写锁</p>
	 */
	private final ReadWriteLock readWriteLock;
	/**
	 * <p>写锁</p>
	 * 
	 * @see #readWriteLock
	 */
	private final Lock readLock;
	/**
	 * <p>读锁</p>
	 * 
	 * @see #readWriteLock
	 */
	private final Lock writeLock;

	/**
	 * @param torrentSession BT任务信息
	 */
	private TorrentStreamGroup(TorrentSession torrentSession) {
		final Torrent torrent = torrentSession.torrent();
		this.pieces = torrentSession.buildPieces();
		this.selectPieces = new BitSet(torrent.getInfo().pieceSize());
		this.full = false;
		this.fullPieces = new BitSet();
		this.fullPieces(this.pieces);
		this.fileBufferSize = new AtomicLong(0);
		this.torrent = torrent;
		this.streams = new ArrayList<>();
		this.torrentSession = torrentSession;
		this.readWriteLock = new ReentrantReadWriteLock();
		this.readLock = this.readWriteLock.readLock();
		this.writeLock = this.readWriteLock.writeLock();
	}
	
	/**
	 * <p>新建文件流组</p>
	 * 
	 * @param folder 任务下载目录
	 * @param files 任务文件列表
	 * @param torrentSession BT任务信息
	 * 
	 * @return {@link TorrentStreamGroup}
	 */
	public static final TorrentStreamGroup newInstance(String folder, List<TorrentFile> files, TorrentSession torrentSession) {
		final TorrentStreamGroup torrentStreamGroup = new TorrentStreamGroup(torrentSession);
		torrentStreamGroup.load(torrentSession.completed(), folder, files);
		return torrentStreamGroup;
	}
	
	/**
	 * <p>重新加载下载文件</p>
	 * 
	 * @param folder 任务下载目录
	 * @param files 任务文件列表
	 * 
	 * @return 新增下载文件数量
	 * 
	 * @see #load(boolean, String, List)
	 */
	public int reload(String folder, List<TorrentFile> files) {
		return this.load(false, folder, files);
	}
	
	/**
	 * <p>加载任务</p>
	 * <p>需要下载文件：没有加载-加载；已经加载-重载；</p>
	 * <p>不用下载文件：没有加载-忽略；已经加载-卸载；</p>
	 * 
	 * @param completed 任务是否完成
	 * @param folder 任务下载目录
	 * @param files 任务文件列表
	 * 
	 * @return 新增下载文件数量
	 */
	private int load(boolean completed, String folder, List<TorrentFile> files) {
		// 新增下载文件数量：原来没有下载
		int loadFileCount = 0;
		if(CollectionUtils.isEmpty(files)) {
			LOGGER.error("任务文件列表为空：{}", files);
			return loadFileCount;
		}
		this.full = false;
		this.selectPieces.clear();
		this.writeLock.lock();
		try {
			final long startTime = System.currentTimeMillis();
			// 开始加载下载文件
			long pos = 0;
			final long pieceLength = this.torrent.getInfo().getPieceLength();
			// 文件排序列表
			final List<TorrentStream> sortList = new ArrayList<>();
			for (TorrentFile file : files) {
				final long fileSize = file.getLength();
				final String filePath = FileUtils.file(folder, file.path());
				final TorrentStream oldStream = this.oldStream(filePath);
				try {
					if(file.selected()) {
						// 加载选择下载文件
						if(oldStream == null) {
							LOGGER.debug("文件选择下载（加载）：{}", filePath);
							loadFileCount++;
							final TorrentStream newStream = TorrentStream.newInstance(pieceLength, filePath, fileSize, pos, completed, this);
							this.streams.add(newStream);
							newStream.buildSelectPieces(this.selectPieces);
							newStream.install();
							sortList.add(newStream);
						} else {
							LOGGER.debug("文件选择下载（重载）：{}", filePath);
							if(!oldStream.selected()) {
								loadFileCount++;
							}
							oldStream.buildSelectPieces(this.selectPieces);
							oldStream.install();
							sortList.add(oldStream);
						}
					} else {
						// 加载没有选择下载文件
						if(oldStream == null) {
							LOGGER.debug("文件没有选择下载（忽略）：{}", filePath);
						} else {
							// 文件流不删除：标记为不下载
							LOGGER.debug("文件没有选择下载（卸载）：{}", filePath);
							oldStream.uninstall();
							sortList.add(oldStream);
						}
					}
				} catch (Exception e) {
					LOGGER.error("新建TorrentStream异常：{}", filePath, e);
				}
				pos += fileSize;
			}
			// 文件排序
			this.streams.sort((source, target) -> {
				final int sourceIndex = sortList.indexOf(source);
				final int targetIndex = sortList.indexOf(target);
				return Integer.compare(sourceIndex, targetIndex);
			});
			final long finishTime = System.currentTimeMillis();
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("任务加载耗时：{}-{}", this.torrentSession, (finishTime - startTime));
			}
		} finally {
			this.writeLock.unlock();
		}
		this.torrentSession.downloadSize(this.downloadSize());
		this.fullPieces(this.pieces);
		return loadFileCount;
	}
	
	/**
	 * <p>获取对应文件路径的文件流</p>
	 * <p>没有加载返回：null</p>
	 * 
	 * @param path 文件路径
	 * 
	 * @return {@link TorrentStream}
	 */
	private TorrentStream oldStream(String path) {
		for (TorrentStream torrentStream : this.streams) {
			if(torrentStream.equalsPath(path)) {
				return torrentStream;
			}
		}
		return null;
	}
	
	/**
	 * <p>发送have消息</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @see TorrentSession#have(int)
	 */
	public void have(int index) {
		this.torrentSession.have(index);
	}
	
	/**
	 * <p>指定下载Piece索引</p>
	 * 
	 * @param index Piece索引
	 */
	public void piecePos(int index) {
		if(
			PeerConfig.checkPiece(index) &&
			// 没有下载
			!this.pieces.get(index) &&
			// 选择下载
			this.selectPieces.get(index)
		) {
			LOGGER.debug("指定下载Piece索引：{}", index);
			this.piecePos = index;
		}
	}
	
	/**
	 * <p>挑选下载Piece</p>
	 * 
	 * @param peerPieces Peer已经下载Piece位图
	 * @param suggestPieces Peer推荐Piece位图
	 * 
	 * @return {@link TorrentPiece}
	 * 
	 * @see TorrentStream#pick(int, BitSet, BitSet)
	 */
	public TorrentPiece pick(final BitSet peerPieces, final BitSet suggestPieces) {
		TorrentPiece pickPiece = null;
		this.readLock.lock();
		try {
			for (TorrentStream torrentStream : this.streams) {
				// 挑选选择下载文件
				if(torrentStream.selected()) {
					pickPiece = torrentStream.pick(this.piecePos, peerPieces, suggestPieces);
					if(pickPiece != null) {
						break;
					}
				}
			}
		} finally {
			this.readLock.unlock();
		}
		// 清空指定下载Piece索引
		if(pickPiece == null && this.piecePos != 0) {
			this.piecePos = 0;
			pickPiece = this.pick(peerPieces, suggestPieces);
		}
		return pickPiece;
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
	 * @see TorrentStream#read(int, int, int)
	 */
	public byte[] read(final int index, final int begin, final int length) throws NetException {
		PacketSizeException.verify(length);
		final ByteBuffer buffer = ByteBuffer.allocate(length);
		this.readLock.lock();
		try {
			// 如果跨越多个文件合并返回
			for (TorrentStream torrentStream : this.streams) {
				final byte[] bytes = torrentStream.read(index, length, begin);
				if(bytes != null) {
					buffer.put(bytes);
					if(buffer.position() >= length) {
						break;
					}
				}
			}
		} finally {
			this.readLock.unlock();
		}
		return buffer.array();
	}

	/**
	 * <p>保存Piece</p>
	 * 
	 * @param piece Piece数据
	 * 
	 * @return 是否保存成功
	 * 
	 * @see TorrentStream#write(TorrentPiece)
	 */
	public boolean write(TorrentPiece piece) {
		boolean success = false;
		this.readLock.lock();
		try {
			for (TorrentStream torrentStream : this.streams) {
				// 不能跳出：可能存在一个Piece处于多个文件
				if(torrentStream.write(piece)) {
					success = true;
				}
			}
		} finally {
			this.readLock.unlock();
		}
		if(success) {
			// 发送have消息
			this.have(piece.getIndex());
			// 修改缓存大小
			final long oldValue = this.fileBufferSize.addAndGet(piece.getLength());
			// 判断是否刷出缓存
			if(
				oldValue > DownloadConfig.getMemoryBufferByte() &&
				this.fileBufferSize.compareAndSet(oldValue, 0)
			) {
				LOGGER.debug("缓冲区被占满：{}", this.torrentSession);
				this.flush();
			}
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("""
				已经下载Piece数量：{}
				剩余下载Piece数量：{}""",
				this.pieces.cardinality(),
				this.remainingPieceSize()
			);
		}
		return success;
	}
	
	/**
	 * <p>判断Piece是否已经下载</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return 是否已经下载
	 */
	public boolean hasPiece(int index) {
		if(index < 0) {
			return false;
		}
		return this.pieces.get(index);
	}
	
	/**
	 * <p>设置已经下载Piece</p>
	 * 
	 * @param index Piece索引
	 */
	void done(int index) {
		synchronized (this.pieces) {
			this.pieces.set(index);
		}
	}
	
	/**
	 * <p>设置下载失败Piece</p>
	 * 
	 * @param index Piece索引
	 */
	void undone(int index) {
		synchronized (this.pieces) {
			this.pieces.clear(index);
		}
	}
	
	/**
	 * <p>设置下载失败Piece</p>
	 * 
	 * @param piece Piece
	 * 
	 * @see TorrentStream#undone(TorrentPiece)
	 */
	public void undone(TorrentPiece piece) {
		this.readLock.lock();
		try {
			for (TorrentStream torrentStream : this.streams) {
				torrentStream.undone(piece);
			}
		} finally {
			this.readLock.unlock();
		}
	}
	
	/**
	 * <p>获取已经下载Piece位图</p>
	 * 
	 * @return 已经下载Piece位图
	 */
	public BitSet pieces() {
		return this.pieces;
	}
	
	/**
	 * <p>获取选择下载Piece位图</p>
	 * 
	 * @return 选择下载Piece位图
	 */
	public BitSet selectPieces() {
		return this.selectPieces;
	}

	/**
	 * <p>获取所有Piece位图</p>
	 * 
	 * @return 所有Piece位图
	 */
	public BitSet allPieces() {
		final int pieceSize = this.torrent.getInfo().pieceSize();
		final var allPieces = new BitSet(pieceSize);
		allPieces.set(0, pieceSize);
		return allPieces;
	}
	
	/**
	 * <p>获取剩余未下载的Piece数量</p>
	 * 
	 * @return 剩余未下载的Piece数量
	 */
	public int remainingPieceSize() {
		final BitSet condition = new BitSet();
		// 已经选择下载
		condition.or(this.selectPieces);
		// 排除已经下载
		condition.andNot(this.pieces);
		return condition.cardinality();
	}
	
	/**
	 * <p>设置完整Piece位图</p>
	 */
	public void fullPieces() {
		if(this.full) {
			return;
		}
		this.full = true;
		this.fullPieces.clear();
	}
	
	/**
	 * <p>设置完整Piece位图</p>
	 * 
	 * @param pieces Piece位图
	 */
	public void fullPieces(BitSet pieces) {
		if(this.full) {
			return;
		}
		this.fullPieces.or(pieces);
		// 排除没有选择下载Piece：防止部分下载时健康度超过100
		this.fullPieces.and(this.selectPieces);
		// 计算选择下载Piece是否全部健康
		final BitSet condition = new BitSet();
		condition.or(this.selectPieces);
		condition.andNot(this.fullPieces);
		if(condition.isEmpty()) {
			this.full = true;
			this.fullPieces.clear();
		}
	}
	
	/**
	 * <p>获取健康度</p>
	 * 
	 * @return 健康度
	 */
	public int health() {
		final int health = 100;
		if(this.full) {
			return health;
		}
		return this.fullPieces.cardinality() * health / this.selectPieces.cardinality();
	}

	/**
	 * <p>校验文件</p>
	 * 
	 * @throws IOException IO异常
	 */
	public boolean verify() throws IOException {
		int verifyFailCount = 0;
		this.readLock.lock();
		try {
			for (TorrentStream torrentStream : this.streams) {
				if(torrentStream.selected() && !torrentStream.verify()) {
					verifyFailCount++;
				}
			}
			this.torrentSession.downloadSize(this.downloadSize());
		} finally {
			this.readLock.unlock();
		}
		return verifyFailCount == 0;
	}
	
	/**
	 * <p>获取Piece校验数据（Hash）</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return 校验数据（Hash）
	 */
	public byte[] pieceHash(int index) {
		final byte[] pieceHashs = this.torrent.getInfo().getPieces();
		final byte[] pieceHash = new byte[SystemConfig.SHA1_HASH_LENGTH];
		System.arraycopy(pieceHashs, index * SystemConfig.SHA1_HASH_LENGTH, pieceHash, 0, SystemConfig.SHA1_HASH_LENGTH);
		return pieceHash;
	}

	/**
	 * <p>刷出缓存</p>
	 */
	public void flush() {
		LOGGER.debug("刷出缓存：{}", this.torrentSession);
		this.readLock.lock();
		try {
			for (TorrentStream torrentStream : this.streams) {
				torrentStream.flush();
			}
		} finally {
			this.readLock.unlock();
		}
		this.torrentSession.updatePieces(false);
	}
	
	/**
	 * <p>获取任务已经下载大小</p>
	 * 
	 * @return 任务已经下载大小
	 * 
	 * @see TorrentStream#downloadSize()
	 */
	public long downloadSize() {
		long downloadSize = 0L;
		this.readLock.lock();
		try {
			for (TorrentStream torrentStream : this.streams) {
				if(torrentStream.selected()) {
					downloadSize += torrentStream.downloadSize();
				}
			}
		} finally {
			this.readLock.unlock();
		}
		return downloadSize;
	}
	
	/**
	 * <p>检测任务是否下载完成</p>
	 * 
	 * @return 是否下载完成
	 * 
	 * @see TorrentStream#completed()
	 */
	public boolean completed() {
		this.readLock.lock();
		try {
			for (TorrentStream torrentStream : this.streams) {
				if(torrentStream.selected() && !torrentStream.completed()) {
					// 所有选择任务下载完成
					return false;
				}
			}
			return true;
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * <p>资源释放</p>
	 * 
	 * @see TorrentStream#release()
	 */
	public void release() {
		LOGGER.debug("释放TorrentStreamGroup：{}", this.torrentSession);
		this.readLock.lock();
		try {
			for (TorrentStream torrentStream : this.streams) {
				torrentStream.release();
			}
		} finally {
			this.readLock.unlock();
		}
	}

}
