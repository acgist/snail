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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.pojo.bean.Torrent;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>文件流组</p>
 * <p>管理BT任务下载文件</p>
 * 
 * @author acgist
 */
public final class TorrentStreamGroup {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStreamGroup.class);

	/**
	 * <p>指定下载Piece索引</p>
	 * <p>默认按照顺序从零开始下载</p>
	 * <p>选择Piece时优先选择该索引后面的Piece</p>
	 * 
	 * @see #pick(BitSet, BitSet)
	 */
	private volatile int piecePos = 0;
	/**
	 * <p>已下载Piece位图</p>
	 */
	private final BitSet pieces;
	/**
	 * <p>选择下载Piece位图</p>
	 */
	private final BitSet selectPieces;
	/**
	 * <p>是否含有完整选择下载Piece位图</p>
	 * <p>含有完整数据健康度等于：100</p>
	 */
	private boolean full;
	/**
	 * <p>完整Piece位图</p>
	 * <p>已下载Piece位图和Peer的Piece位图</p>
	 * <dl>
	 * 	<dt>更新条件</dt>
	 * 	<dd>加载本地文件</dd>
	 * 	<dd>收到haveAll消息</dd>
	 * 	<dd>收到交换Piece位图消息</dd>
	 * </dl>
	 */
	private final BitSet fullPieces;
	/**
	 * <p>Piece缓存大小</p>
	 * <p>所有文件流中Piece缓存队列数据大小</p>
	 */
	private final AtomicLong fileBufferSize;
	/**
	 * <p>种子信息</p>
	 */
	private final Torrent torrent;
	/**
	 * <p>文件流集合</p>
	 * <p>注意顺序（跨越文件数据读取）</p>
	 * <p>如果文件开始选择下载然后不选择下载时，文件流不删除，标记为不下载即可。</p>
	 */
	private final List<TorrentStream> streams;
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	/**
	 * <p>读写锁</p>
	 * <p>保存文件流集合线程安全</p>
	 * 
	 * @see #load(boolean, String, List)
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
	 * <p>创建文件流组</p>
	 * 
	 * @param folder 任务下载目录
	 * @param files 任务文件
	 * @param torrentSession BT任务信息
	 * 
	 * @return 文件流组
	 */
	public static final TorrentStreamGroup newInstance(String folder, List<TorrentFile> files, TorrentSession torrentSession) {
		final TorrentStreamGroup torrentStreamGroup = new TorrentStreamGroup(torrentSession);
		torrentStreamGroup.load(torrentSession.completed(), folder, files);
		return torrentStreamGroup;
	}
	
	/**
	 * <p>重新加载</p>
	 * 
	 * @param folder 任务下载目录
	 * @param files 任务文件
	 * 
	 * @return 新增下载文件数量
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
	 * @return 新增下载文件数量：原来没有下载
	 */
	private int load(boolean completed, String folder, List<TorrentFile> files) {
		// 新增下载文件数量：原来没有下载
		int loadFileCount = 0;
		if(CollectionUtils.isEmpty(files)) {
			LOGGER.error("任务文件列表为空：{}", files);
			return loadFileCount;
		}
		this.full = false; // 健康度重新检查
		this.selectPieces.clear(); // 清除所有已选择Piece
		this.writeLock.lock();
		try {
			final long startTime = System.currentTimeMillis();
			// 开始加载下载文件
			long pos = 0; // 数据偏移
			final long pieceLength = this.torrent.getInfo().getPieceLength();
			final List<TorrentStream> sortList = new ArrayList<>(); // 排序
			for (TorrentFile file : files) {
				final long fileSize = file.getLength();
				final String filePath = FileUtils.file(folder, file.path()); // 文件路径
				final TorrentStream oldStream = this.oldStream(filePath);
				try {
					if(file.selected()) {
						// 加载选择下载文件
						if(oldStream == null) {
							LOGGER.debug("文件选择下载（加载）：{}", filePath);
							loadFileCount++;
							final TorrentStream newStream = TorrentStream.newInstance(
								pieceLength, filePath, fileSize, pos, completed,
								this.fileBufferSize, this
							);
							this.streams.add(newStream);
							newStream.buildSelectPieces(this.selectPieces);
							newStream.install();
							sortList.add(newStream);
						} else {
							LOGGER.debug("文件选择下载（重载）：{}", filePath);
							// 文件没有选择下载
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
							LOGGER.debug("文件没有选择下载（卸载）：{}", filePath);
							oldStream.uninstall();
							sortList.add(oldStream);
						}
					}
				} catch (Exception e) {
					LOGGER.error("TorrentStream创建异常：{}", filePath, e);
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
			LOGGER.debug("任务-{}-加载完成耗时：{}", this.torrentSession.name(), (finishTime - startTime));
		} finally {
			this.writeLock.unlock();
		}
		this.torrentSession.downloadSize(this.downloadSize());
		this.fullPieces(this.pieces);
		return loadFileCount;
	}
	
	/**
	 * <p>获取对应文件路径的文件流</p>
	 * 
	 * @param path 文件路径
	 * 
	 * @return 文件流（null-没有加载）
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
	 * @param piecePos 指定下载Piece索引
	 */
	public void piecePos(int piecePos) {
		this.piecePos = piecePos;
	}
	
	/**
	 * <p>挑选下载Piece</p>
	 * 
	 * @param peerPieces Peer已下载Piece位图
	 * @param suggestPieces Peer推荐Piece位图
	 * 
	 * @return 下载Piece
	 * 
	 * @see TorrentStream#pick(int, BitSet, BitSet)
	 */
	public TorrentPiece pick(final BitSet peerPieces, final BitSet suggestPieces) {
		TorrentPiece pickPiece = null;
		this.readLock.lock();
		try {
			for (TorrentStream torrentStream : this.streams) {
				if(torrentStream.selected()) {
					// 挑选选择下载文件
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
			// 如果跨越多个文件则合并返回
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
		if(buffer.position() < length) {
			LOGGER.warn("读取Piece数据错误：{}-{}-{}", index, length, buffer);
			return null;
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
				// 不能跳出：可能存在一个Piece多个文件的情况
				if(torrentStream.write(piece)) {
					success = true;
				}
			}
		} finally {
			this.readLock.unlock();
		}
		// 判断是否刷出缓存
		final long oldValue = this.fileBufferSize.get();
		if(
			oldValue > DownloadConfig.getMemoryBufferByte() &&
			this.fileBufferSize.compareAndSet(oldValue, 0)
		) {
			LOGGER.debug("缓冲区被占满");
			this.flush();
		}
		// 保存成功发送have消息
		if(success) {
			this.have(piece.getIndex());
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("当前任务已下载Piece数量：{}，剩余未下载Piece数量：{}",
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
			// 此处需要验证：Peer请求Piece索引可能出错
			return false;
		}
		return this.pieces.get(index);
	}
	
	/**
	 * <p>设置已下载的Piece</p>
	 * <p>文件流Piece下载完成使用</p>
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
	 * <p>文件流Piece校验失败使用</p>
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
	 * <p>获取已下载Piece位图</p>
	 * 
	 * @return 已下载Piece位图
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
		condition.or(this.selectPieces); // 已选择下载
		condition.andNot(this.pieces); // 排除已下载
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
	 * <p>健康度范围：{@code 0} ~ {@code 100}</p>
	 * <p>健康度：完整Piece数量除以选择下载Piece数量</p>
	 * 
	 * @return 健康度
	 */
	public int health() {
		final int health = 100;
		if(this.full) {
			return health;
		}
		LOGGER.debug("健康度：{}-{}", this.fullPieces.cardinality(), this.selectPieces.cardinality());
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
	 * <p>获取Piece的Hash数据</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return Piece的Hash数据
	 */
	public byte[] pieceHash(int index) {
		final byte[] pieceHashs = this.torrent.getInfo().getPieces();
		final byte[] hash = new byte[SystemConfig.SHA1_HASH_LENGTH];
		System.arraycopy(pieceHashs, index * SystemConfig.SHA1_HASH_LENGTH, hash, 0, SystemConfig.SHA1_HASH_LENGTH);
		return hash;
	}

	/**
	 * <p>刷出缓存</p>
	 */
	public void flush() {
		LOGGER.debug("刷出缓存");
		this.readLock.lock();
		try {
			for (TorrentStream torrentStream : this.streams) {
				torrentStream.flush();
			}
		} finally {
			this.readLock.unlock();
		}
		// 更新Piece信息
		this.torrentSession.updatePieces(false);
	}
	
	/**
	 * <p>获取任务已下载大小</p>
	 * 
	 * @return 任务已下载大小
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
	 * <p>完成：所有选择下载文件下载完成</p>
	 * 
	 * @return 是否下载完成
	 */
	public boolean completed() {
		this.readLock.lock();
		try {
			for (TorrentStream torrentStream : this.streams) {
				if(torrentStream.selected() && !torrentStream.completed()) {
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
	 */
	public void release() {
		LOGGER.debug("释放TorrentStreamGroup");
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
