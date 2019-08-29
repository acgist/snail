package com.acgist.snail.net.torrent.bootstrap;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Torrent下载文件流</p>
 * <p>
 * 下载：每次下载必须是一个完整的Piece。
 * （除了文件开头和结尾可能不是一个完整的Piece）
 * </p>
 * <p>
 * 块是否下载：判断每个块前面10字节数据。数据校验在下载时校验。
 * </p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentStream {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStream.class);

	private final long pieceLength; // 每个块的大小
	private final AtomicLong fileBuffer; // 缓冲大小：写入文件时修改，TorrentStreamGroup中引用
	private final TorrentStreamGroup torrentStreamGroup; // 下载文件组
	private final AtomicLong fileDownloadSize; // 已下载大小：写入文件时修改
	private final BlockingQueue<TorrentPiece> filePieces; // Piece队列
	
	private String file; // 文件路径
	private long fileSize; // 文件大小
	private long fileBeginPos; // 文件开始偏移：包含该值
	private long fileEndPos; // 文件结束偏移：不包含该值
	private RandomAccessFile fileStream; // 文件流
	
	private int filePieceSize; // 文件Piece数量
	private int fileBeginPieceIndex; // 文件Piece开始索引
	private int fileEndPieceIndex; // 文件Piece结束索引
	
	private BitSet pieces; // 当前文件位图
	private BitSet pausePieces; // 暂停位图：上次下载失败，下一次请求时不选取这个Piece，然后清除。主要用来处理两个文件都包含某一个Piece时，上一个文件不存在Piece，而下一个文件存在的情况。
	private BitSet downloadPieces; // 下载中位图
	
	private TorrentStream(long pieceLength, AtomicLong fileBuffer, TorrentStreamGroup torrentStreamGroup) {
		this.pieceLength = pieceLength;
		this.fileBuffer = fileBuffer;
		this.torrentStreamGroup = torrentStreamGroup;
		this.fileDownloadSize = new AtomicLong(0);
		this.filePieces = new LinkedBlockingQueue<>();
	}
	
	public static final TorrentStream newInstance(long pieceLength, AtomicLong fileBuffer, TorrentStreamGroup torrentStreamGroup) {
		return new TorrentStream(pieceLength, fileBuffer, torrentStreamGroup);
	}
	
	/**
	 * <p>设置文件信息</p>
	 * <p>初始化已下载Piece，已下载文件大小，创建本地文件流等</p>
	 * 
	 * @param file 文件路径
	 * @param size 文件大小
	 * @param pos 文件开始偏移
	 * @param selectPieces 被选中的Piece
	 * @param complete 完成
	 * @param allReady 准备完成
	 */
	public void buildFile(final String file, final long size, final long pos, final BitSet selectPieces, final boolean complete, final CountDownLatch allReady) throws IOException {
		if(this.fileStream != null) {
			throw new IOException("Torrent文件未被释放");
		}
		synchronized (this) {
			this.file = file;
			this.fileSize = size;
			this.fileBeginPos = pos;
			this.fileEndPos = pos + size;
			FileUtils.buildFolder(this.file, true); // 创建文件父目录，否者会抛出FileNotFoundException
			this.fileStream = new RandomAccessFile(this.file, "rw");
			buildFilePiece();
			buildFileAsyn(complete, allReady);
			selectPieces.set(this.fileBeginPieceIndex, this.fileEndPieceIndex + 1, true);
			LOGGER.debug(
				"TorrentStream信息，块大小：{}，文件路径：{}，文件大小：{}，文件开始偏移：{}，文件Piece数量：{}，文件Piece开始索引：{}，文件Piece结束索引：{}",
				this.pieceLength,
				this.file,
				this.fileSize,
				this.fileBeginPos,
				this.filePieceSize,
				this.fileBeginPieceIndex,
				this.fileEndPieceIndex
			);
		}
	}
	
	/**
	 * <P>添加Piece</p>
	 * <P>每次添加的必须是一个完成的Piece，如果不在该文件范围内则不操作。</p>
	 * <P>如果缓存达到缓存大小或者文件下载完成则写入文件。</p>
	 * 
	 * @return 是否保存成功
	 */
	public boolean piece(TorrentPiece piece) {
		if(!piece.contain(this.fileBeginPos, this.fileEndPos)) { // 不符合当前文件位置
			return false;
		}
		synchronized (this) {
			boolean ok = false;
			if(havePiece(piece.getIndex())) {
				LOGGER.debug("已经下载完成Piece，忽略：{}", piece.getIndex());
				return false;
			}
			if(this.filePieces.offer(piece)) {
				ok = true;
				LOGGER.debug("保存Piece：{}", piece.getIndex());
				this.done(piece.getIndex());
				this.fileBuffer.addAndGet(piece.getLength()); // 更新缓存大小
				this.buildFileDownloadSize();
				if(this.complete()) { // 下载完成数据刷出
					this.flush();
				}
			} else {
				LOGGER.warn("保存Piece失败：{}", piece.getIndex());
			}
			return ok;
		}
	}
	
	/**
	 * <p>选择未下载的Piece序号</p>
	 * <p>
	 * 选择Peer有同时没有下载并且不处于失败和下载中的Piece，选择后清除失败的Piece数据。
	 * 如果挑选不到符合条件的Piece且任务处于接近完成状态时（最后还剩下{@linkplain SystemConfig#getPieceRepeatSize() 多少个}Piece时），那么选择下载中的Piece进行下载。
	 * </p>
	 * 
	 * @param peerPieces Peer位图
	 */
	public TorrentPiece pick(final BitSet peerPieces) {
		if(peerPieces.cardinality() <= 0) {
			return null;
		}
		synchronized (this) {
			final BitSet pickPieces = new BitSet();
			pickPieces.or(peerPieces);
			pickPieces.andNot(this.pieces);
			pickPieces.andNot(this.pausePieces);
			pickPieces.andNot(this.downloadPieces);
			this.pausePieces.clear();
			if(pickPieces.cardinality() <= 0) {
				if(this.torrentStreamGroup.remainingPieceSize() <= SystemConfig.getPieceRepeatSize()) {
					LOGGER.debug("选择Piece时已经处于任务即将完成状态找不到更多块，开始选择下载中的块");
					pickPieces.or(peerPieces);
					pickPieces.andNot(this.pieces);
					if(pickPieces.cardinality() <= 0) {
						LOGGER.debug("选择Piece时Piece全部下载完成");
						return null;
					}
				} else {
					return null;
				}
			}
			final int index = pickPieces.nextSetBit(this.fileBeginPieceIndex);
			if(index == -1 || index > this.fileEndPieceIndex) {
				return null;
			}
			this.downloadPieces.set(index);
			int begin = 0;
			boolean verify = true;
			if(index == this.fileBeginPieceIndex) { // 第一块获取开始偏移
				verify = false;
				begin = firstPiecePos();
			}
			int end = (int) this.pieceLength;
			if(index == this.fileEndPieceIndex) { // 最后一块获取结束偏移
				verify = false;
				end = lastPieceSize();
			}
			return TorrentPiece.newInstance(torrentStreamGroup.pieceHash(index), this.pieceLength, index, begin, end, verify);
		}
	}

	/**
	 * <p>读取块数据</p>
	 * <p>数据大小：{@link #pieceLength}，默认偏移：0</p>
	 */
	public byte[] read(int index) {
		return read(index, (int) this.pieceLength);
	}
	
	/**
	 * <p>读取块数据</p>
	 * <p>默认偏移：0</p>
	 * 
	 * @param size 数据大小
	 */
	public byte[] read(int index, int size) {
		return read(index, size, 0);
	}
	
	/**
	 * <p>读取块数据</p>
	 * 
	 * @param size 数据大小
	 * @param pos 数据偏移
	 */
	public byte[] read(int index, int size, int pos) {
		synchronized (this) {
			return read(index, size, pos, false);
		}
	}
	
	/**
	 * <p>读取块数据</p>
	 * <p>如果选择的Piece不在文件范围内返回null。</p>
	 * <p>如果读取数据只有部分符合文件的范围，会自动修正范围，读取符合部分数据返回。</p>
	 * 
	 * @param size 数据大小
	 * @param pos 数据偏移
	 * @param ignorePieces 忽略已下载位图，读取文件验证
	 */
	private byte[] read(int index, int size, int pos, boolean ignorePieces) {
		if(!haveIndex(index)) {
			return null;
		}
		if(!ignorePieces && !havePiece(index)) {
			return null;
		}
		long seek = 0L;
		final long beginPos = this.pieceLength * index + pos;
		final long endPos = beginPos + size;
		if(beginPos >= this.fileEndPos) {
			return null;
		}
		if(endPos <= this.fileBeginPos) {
			return null;
		}
		if(beginPos <= this.fileBeginPos) {
			size = (int) (size - (this.fileBeginPos - beginPos));
		} else {
			seek = beginPos - this.fileBeginPos;
		}
		if(endPos >= this.fileEndPos) {
			size = (int) (size - (endPos - this.fileEndPos));
		}
		if(size <= 0) {
			return null;
		}
		final byte[] bytes = new byte[size];
		try {
			this.fileStream.seek(seek);
			this.fileStream.read(bytes);
		} catch (Exception e) {
			LOGGER.error("Piece读取异常：{}-{}-{}-{}", index, size, pos, ignorePieces, e);
		}
		return bytes;
	}
	
	/**
	 * 已下载大小
	 */
	public long size() {
		return this.fileDownloadSize.get();
	}
	
	/**
	 * Piece下载完成
	 */
	private void done(int index) {
		this.pieces.set(index, true); // 下载成功
		this.downloadPieces.clear(index); // 去掉下载状态
		this.torrentStreamGroup.piece(index); // 设置下载完成
	}

	/**
	 * Piece下载失败
	 * 
	 * @param piece 下载失败的块
	 */
	public void undone(TorrentPiece piece) {
		if(!piece.contain(this.fileBeginPos, this.fileEndPos)) { // 不符合当前文件位置
			return;
		}
		synchronized (this) {
			this.pausePieces.set(piece.getIndex());
			this.downloadPieces.clear(piece.getIndex());
		}
	}
	
	/**
	 * 是否下载完成
	 * 
	 * @return true-完成；false-未完成
	 */
	public boolean complete() {
		return this.pieces.cardinality() >= this.filePieceSize;
	}
	
	/**
	 * 刷新所有缓存，保存到硬盘
	 */
	public void flush() {
		synchronized (this) {
			final var list = flushPieces();
			this.write(list);
		}
	}

	/**
	 * <p>释放资源</p>
	 * <p>将已下载的块写入文件，然后关闭文件流。</p>
	 */
	public void release() {
		this.flush();
		try {
			this.fileStream.close();
		} catch (IOException e) {
			LOGGER.error("TorrentStream关闭异常", e);
		}
	}

	/**
	 * <p>刷新缓存</p>
	 * <p>将缓存队列所有的Piece块刷出。</p>
	 */
	private List<TorrentPiece> flushPieces() {
		final List<TorrentPiece> list = new ArrayList<>();
		this.filePieces.drainTo(list);
		return list;
	}
	
	/**
	 * <p>写入硬盘，注意线程安全。</p>
	 * <p>注：多线程写入时seek（跳过）可能导致写入数据错乱。</p>
	 */
	private void write(List<TorrentPiece> list) {
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		list.stream().forEach(piece -> {
			if(!haveIndex(piece.getIndex())) {
				LOGGER.warn("写入文件索引错误：{}", piece.getIndex());
				return;
			}
			LOGGER.debug("写入硬盘Piece：{}", piece.getIndex());
			int offset = 0;
			long seek = 0L;
			int length = piece.getLength();
			final long beginPos = piece.beginPos();
			final long endPos = piece.endPos();
			if(beginPos <= this.fileBeginPos) {
				offset = (int) (this.fileBeginPos - beginPos);
				length = length - offset;
			} else {
				seek = beginPos - this.fileBeginPos;
			}
			if(endPos >= this.fileEndPos) {
				length = (int) (length - (endPos - this.fileEndPos));
			}
			if(length <= 0) {
				return;
			}
			try {
				this.fileStream.seek(seek);
				this.fileStream.write(piece.getData(), offset, length);
			} catch (Exception e) {
				LOGGER.error("TorrentStream写入异常", e);
			}
		});
	}
	
	/**
	 * 初始化：开始块序号，结束块序号等
	 */
	private void buildFilePiece() {
		this.fileBeginPieceIndex = (int) (this.fileBeginPos / this.pieceLength);
		this.fileEndPieceIndex = (int) (this.fileEndPos / this.pieceLength);
		this.filePieceSize = this.fileEndPieceIndex - this.fileBeginPieceIndex;
		int endPieceSize = (int) (this.fileEndPos % this.pieceLength);
		if(endPieceSize > 0) {
			this.filePieceSize++;
		}
		this.pieces = new BitSet();
		this.pausePieces = new BitSet();
		this.downloadPieces = new BitSet();
	}
	
	/**
	 * 异步加载，如果已经完成的任务使用同步加载，其他使用异步加载。
	 */
	private void buildFileAsyn(boolean complete, CountDownLatch allReady) throws IOException {
		if(complete) {
			buildFilePieces(complete);
			buildFileDownloadSize();
			allReady.countDown();
		} else {
			SystemThreadContext.submit(() -> {
				try {
					buildFilePieces(complete);
					buildFileDownloadSize();
				} catch (IOException e) {
					LOGGER.error("TorrentStream异步加载异常", e);
				} finally {
					allReady.countDown();
				}
			});
		}
	}
	
	/**
	 * 初始化：已下载块，校验HASH（第一块和最后一块不校验）。
	 */
	private void buildFilePieces(boolean complete) throws IOException {
		int pos = 0;
		int length = 0;
		byte[] hash = null;
		byte[] bytes = null;
		boolean verify = true; // 第一块和最后一块不要校验HASH
		if(this.fileStream.length() == 0) {
			return;
		}
		for (int index = this.fileBeginPieceIndex; index <= this.fileEndPieceIndex; index++) {
			if(complete) {
				this.done(index);
				continue;
			}
			if(index == this.fileBeginPieceIndex) { // 第一块需要偏移
				verify = false;
				pos = firstPiecePos();
				length = this.firstPieceSize();
			} else if(index == this.fileEndPieceIndex) {
				verify = false;
				pos = 0;
				length = this.lastPieceSize();
			} else {
				verify = true;
				pos = 0;
				length = (int) this.pieceLength;
			}
			bytes = read(index, length, pos, true);
			if(verify) {
				hash = StringUtils.sha1(bytes);
				if(ArrayUtils.equals(hash, this.torrentStreamGroup.pieceHash(index))) {
					this.done(index);
				}
			} else {
				if(haveData(bytes)) {
					this.done(index);
				}
			}
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("当前文件已下载Piece数量：{}，剩余下载Piece数量：{}",
				this.pieces.cardinality(),
				this.filePieceSize - this.pieces.cardinality()
			);
		}
	}
	
	/**
	 * 初始化：已下载文件大小
	 */
	private void buildFileDownloadSize() {
		long size = 0L;
		int downloadPieceSize = this.pieces.cardinality();
		if(havePiece(this.fileBeginPieceIndex)) {
			size += firstPieceSize();
			downloadPieceSize--;
		}
		if(havePiece(this.fileEndPieceIndex)) {
			size += lastPieceSize();
			downloadPieceSize--;
		}
		this.fileDownloadSize.set(size + downloadPieceSize * this.pieceLength);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("当前任务已下载Piece数量：{}，剩余下载Piece数量：{}",
				this.torrentStreamGroup.pieces().cardinality(),
				this.torrentStreamGroup.selectPieces().cardinality() - this.torrentStreamGroup.pieces().cardinality()
			);
		}
	}
	
	/**
	 * 第一块的偏移
	 */
	private int firstPiecePos() {
		return (int) (this.fileBeginPos - (this.fileBeginPieceIndex * this.pieceLength));
	}
	
	/**
	 * 第一块的大小
	 */
	private int firstPieceSize() {
		return (int) (this.pieceLength - firstPiecePos());
	}
	
	/**
	 * 结束块的大小=偏移
	 */
	private int lastPieceSize() {
		return (int) (this.fileEndPos - (this.pieceLength * this.fileEndPieceIndex));
	}

	/**
	 * 是否有数据
	 */
	private boolean haveData(byte[] bytes) {
		if(bytes == null) {
			return false;
		}
		for (byte value : bytes) {
			if(value != 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 文件是否包含块
	 * 
	 * @param index Piece序号
	 */
	private boolean haveIndex(int index) {
		if(index < this.fileBeginPieceIndex || index > this.fileEndPieceIndex) { // 不符合当前文件位置
			return false;
		}
		return true;
	}
	
	/**
	 * 是否含有Piece数据
	 */
	private boolean havePiece(int index) {
		return this.pieces.get(index);
	}

}
