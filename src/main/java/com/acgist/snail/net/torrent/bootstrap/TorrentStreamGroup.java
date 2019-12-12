package com.acgist.snail.net.torrent.bootstrap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.bean.Torrent;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.bean.TorrentInfo;
import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.exception.PacketSizeException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>文件流组</p>
 * <p>管理BT任务下载文件</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentStreamGroup {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStreamGroup.class);

	/**
	 * <p>计算文件大小等待时间（秒）：{@code}</p>
	 */
	private static final int DOWNLOAD_SIZE_TIMEOUT = 120;
	
	/**
	 * <p>已下载Piece位图</p>
	 */
	private final BitSet pieces;
	/**
	 * <p>被选中Piece位图</p>
	 */
	private final BitSet selectPieces;
	/**
	 * <p>是否含有完整的被选中Piece位图数据</p>
	 * <p>含有完整数据健康度等于{@code 100}</p>
	 */
	private boolean full;
	/**
	 * <p>完整Piece位图</p>
	 * <p>已下载Piece位图和Peer含有的Piece位图数据，用来计算健康度。</p>
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
	private final AtomicLong fileBuffer;
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

	private TorrentStreamGroup(BitSet pieces, BitSet selectPieces, List<TorrentStream> streams, TorrentSession torrentSession) {
		this.pieces = pieces;
		this.selectPieces = selectPieces;
		this.full = false;
		this.fullPieces = new BitSet();
		this.fullPieces.or(this.pieces);
		this.streams = streams;
		this.torrent = torrentSession.torrent();
		this.torrentSession = torrentSession;
		this.fileBuffer = new AtomicLong(0);
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
		final Torrent torrent = torrentSession.torrent();
		final TorrentInfo torrentInfo = torrent.getInfo();
		final long pieceLength = torrentInfo.getPieceLength();
		final boolean complete = torrentSession.completed();
		final BitSet pieces = new BitSet(torrentInfo.pieceSize());
		final BitSet selectPieces = new BitSet(torrentInfo.pieceSize());
		final List<TorrentStream> streams = new ArrayList<>(files.size());
		final TorrentStreamGroup torrentStreamGroup = new TorrentStreamGroup(pieces, selectPieces, streams, torrentSession);
		// 下载文件数量
		final int fileCount = (int) files.stream()
			.filter(file -> file.selected())
			.count();
		torrentStreamGroup.load(fileCount, pieceLength, complete, folder, files);
		return torrentStreamGroup;
	}
	
	/**
	 * <p>重新加载</p>
	 * 
	 * @param folder 任务下载目录
	 * @param files 任务文件
	 * @param torrentSession BT任务信息
	 * 
	 * @return 新增下载文件数量
	 */
	public int reload(String folder, List<TorrentFile> files, TorrentSession torrentSession) {
		if(CollectionUtils.isNotEmpty(files)) {
			final Torrent torrent = torrentSession.torrent();
			final TorrentInfo torrentInfo = torrent.getInfo();
			final long pieceLength = torrentInfo.getPieceLength();
			// 需要加载文件数量
			final int fileCount = (int) files.stream()
				.filter(file -> file.selected())
				.filter(file -> {
					final String path = FileUtils.file(folder, file.path()); // 文佳路径
					return this.haveStream(path) == null; // 文件未被加载
				}).count();
			this.load(fileCount, pieceLength, false, folder, files);
			return fileCount;
		}
		return 0;
	}
	
	/**
	 * <p>加载任务</p>
	 * <p>需要下载文件：没有加载-加载；已经加载-重载；</p>
	 * <p>不用下载文件：没有加载-忽略；已经加载-卸载；</p>
	 * 
	 * @param fileCount 加载文件数量
	 * @param pieceLength Piece长度
	 * @param complete 任务是否完成
	 * @param folder 任务下载目录
	 * @param files 任务文件列表
	 */
	private void load(int fileCount, long pieceLength, boolean complete, String folder, List<TorrentFile> files) {
		final var startTime = System.currentTimeMillis(); // 开始时间
		this.full = false; // 健康度重新检查
		this.selectPieces.clear(); // 清除所有已选择Piece
		// 异步文件加载计数器
		final CountDownLatch sizeCount = new CountDownLatch(fileCount);
		final List<TorrentStream> sortList = new ArrayList<>(); // 排序
		// 开始加载下载文件
		if(CollectionUtils.isNotEmpty(files)) {
			long pos = 0; // 数据偏移
			for (TorrentFile file : files) {
				final String path = FileUtils.file(folder, file.path()); // 文佳路径
				final TorrentStream oldStream = this.haveStream(path);
				try {
					if(file.selected()) { // 加载选择下载的文件
						if(oldStream == null) {
							LOGGER.debug("文件选中下载（加载）：{}", path);
							final TorrentStream stream = TorrentStream.newInstance(
								pieceLength, path, file.getLength(), pos,
								this.fileBuffer, this,
								this.selectPieces, complete, sizeCount
							);
							this.streams.add(stream);
							sortList.add(stream);
						} else {
							LOGGER.debug("文件选中下载（重载）：{}", path);
							oldStream.buildSelectPieces(this.selectPieces);
							oldStream.install();
							sortList.add(oldStream);
						}
					} else {
						if(oldStream == null) {
							LOGGER.debug("文件没有没有选中下载（忽略）：{}", path);
						} else {
							LOGGER.debug("文件没有没有选中下载（卸载）：{}", path);
							oldStream.uninstall();
							sortList.add(oldStream);
						}
					}
				} catch (Exception e) {
					LOGGER.error("TorrentStream创建异常：{}", file.path(), e);
				}
				pos += file.getLength();
			}
		}
		// 文件排序
		this.streams.sort((a, b) -> {
			final int aIndex = sortList.indexOf(a);
			final int bIndex = sortList.indexOf(b);
			return Integer.compare(aIndex, bIndex);
		});
		// 异步等待加载完成
		SystemThreadContext.submit(() -> {
			try {
				final var ok = sizeCount.await(DOWNLOAD_SIZE_TIMEOUT, TimeUnit.SECONDS);
				if(ok) {
					final var finishTime = System.currentTimeMillis(); // 结束时间
					LOGGER.debug("{}-任务准备完成，消耗时间：{}", this.torrent.name(), (finishTime - startTime));
					this.torrentSession.resize(this.size());
					this.fullPieces(this.pieces());
				} else {
					LOGGER.warn("{}-任务准备超时", this.torrent.name());
				}
			} catch (InterruptedException e) {
				LOGGER.debug("统计下载文件大小等待异常", e);
				Thread.currentThread().interrupt();
			}
		});
	}
	
	/**
	 * <p>获取对应文件路径的文件流</p>
	 * 
	 * @param path 文件路径
	 * 
	 * @return 文件流：{@code null}-没有加载
	 */
	private TorrentStream haveStream(String path) {
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
	 * @see {@link TorrentSession#have(int)}
	 */
	public void have(int index) {
		this.torrentSession.have(index);
	}
	
	/**
	 * <p>挑选下载Piece</p>
	 * <p>如果文件流没有被选中下载不挑选Piece</p>
	 * 
	 * @param peerPieces Peer已下载Piece位图
	 * @param suggestPieces Peer推荐Piece位图
	 * 
	 * @return 下载Piece
	 * 
	 * @see {@link TorrentStream#pick(BitSet, BitSet)}
	 */
	public TorrentPiece pick(final BitSet peerPieces, final BitSet suggestPieces) {
		TorrentPiece pickPiece = null;
		for (TorrentStream torrentStream : this.streams) {
			if(torrentStream.selected()) { // 下载选中文件
				pickPiece = torrentStream.pick(peerPieces, suggestPieces);
				if(pickPiece != null) {
					break;
				}
			}
		}
		return pickPiece;
	}
	
	/**
	 * <p>读取Piece数据</p>
	 * <p>如果跨越多个文件则合并返回</p>
	 * 
	 * @param index Piece索引
	 * @param begin Piece偏移
	 * @param length 数据长度
	 * 
	 * @return Piece数据
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see {@link TorrentStream#read(int, int, int)}
	 */
	public byte[] read(final int index, final int begin, final int length) throws NetException {
		PacketSizeException.verify(length);
		final ByteBuffer buffer = ByteBuffer.allocate(length);
		for (TorrentStream torrentStream : this.streams) {
			final byte[] bytes = torrentStream.read(index, length, begin);
			if(bytes != null) {
				buffer.put(bytes);
				if(buffer.position() >= length) {
					break;
				}
			}
		}
		if(buffer.position() < length) {
			LOGGER.warn("读取Piece数据错误，读取长度：{}，要求长度：{}", buffer.position(), length);
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
	 * @see {@link TorrentStream#write(TorrentPiece)}
	 */
	public boolean write(TorrentPiece piece) {
		boolean ok = false;
		for (TorrentStream torrentStream : this.streams) {
			// 不能跳出：可能存在一个Piece多个文件的情况
			if(torrentStream.write(piece)) {
				ok = true;
			}
		}
		// 判断是否刷出缓存
		final long oldValue = this.fileBuffer.get();
		if(oldValue > DownloadConfig.getMemoryBufferByte()) {
			if(this.fileBuffer.compareAndSet(oldValue, 0)) {
				LOGGER.debug("缓冲区被占满");
				this.flush();
			}
		}
		// 保存成功发送have消息
		if(ok) {
			this.have(piece.getIndex());
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("当前任务已下载Piece数量：{}，剩余下载Piece数量：{}",
				this.pieces.cardinality(),
				this.remainingPieceSize()
			);
		}
		return ok;
	}
	
	/**
	 * <p>判断是否已下载Piece</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return {@code true}-已下载；{@code false}-未下载；
	 */
	public boolean havePiece(int index) {
		if(index < 0) {
			return false;
		}
		return this.pieces.get(index);
	}
	
	/**
	 * <p>设置已下载的Piece</p>
	 * 
	 * @param index Piece索引
	 */
	public void done(int index) {
		synchronized (this.pieces) {
			this.pieces.set(index);
		}
	}
	
	/**
	 * <p>设置Piece下载失败</p>
	 * 
	 * @param piece Piece
	 * 
	 * @see {@link TorrentStream#undone(TorrentPiece)}
	 */
	public void undone(TorrentPiece piece) {
		for (TorrentStream torrentStream : this.streams) {
			torrentStream.undone(piece);
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
	 * <p>获取被选中Piece位图</p>
	 * 
	 * @return 被选中Piece位图
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
	 * <p>获取剩余选中未下载的Piece数量</p>
	 * 
	 * @return 剩余选中未下载的Piece数量
	 * 
	 * @since 1.0.2
	 */
	public int remainingPieceSize() {
		return this.selectPieces.cardinality() - this.pieces.cardinality();
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
		// 排除没有选中的Piece：防止部分下载时健康度超过100
		this.fullPieces.and(this.selectPieces);
		// 计算选中下载的Piece是否全部健康
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
	 * <p>健康度：完整Piece数量除以选中下载Piece数量</p>
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
	 * <p>获取Piece的Hash数据</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return Piece的Hash数据
	 */
	public byte[] pieceHash(int index) {
		final byte[] pieces = this.torrent.getInfo().getPieces();
		final byte[] value = new byte[SystemConfig.SHA1_HASH_LENGTH];
		System.arraycopy(pieces, index * SystemConfig.SHA1_HASH_LENGTH, value, 0, SystemConfig.SHA1_HASH_LENGTH);
		return value;
	}

	/**
	 * <p>刷出缓存</p>
	 */
	public void flush() {
		LOGGER.debug("刷出缓存");
		for (TorrentStream torrentStream : this.streams) {
			torrentStream.flush();
		}
	}
	
	/**
	 * <p>获取任务已下载大小</p>
	 * 
	 * @return 任务已下载大小
	 * 
	 * @see {@link TorrentStream#size()}
	 */
	public long size() {
		long size = 0L;
		for (TorrentStream torrentStream : this.streams) {
			size += torrentStream.size();
		}
		return size;
	}
	
	/**
	 * <p>检测任务是否下载完成</p>
	 * <p>完成：所有文件流下载完成</p>
	 * 
	 * @return {@code true}-完成；{@code false}-没有完成；
	 */
	public boolean complete() {
		for (TorrentStream torrentStream : this.streams) {
			if(!torrentStream.complete()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>资源释放</p>
	 */
	public void release() {
		LOGGER.debug("释放TorrentStreamGroup");
		for (TorrentStream torrentStream : this.streams) {
			torrentStream.release();
		}
	}

}
