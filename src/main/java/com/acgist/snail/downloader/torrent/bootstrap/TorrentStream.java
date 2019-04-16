package com.acgist.snail.downloader.torrent.bootstrap;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.TorrentPiece;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;

/**
 * torrent文件流<br>
 * 写文件：每次写入必须是一个完整的块<br>
 * 块是否已经下载，判断前面十个字节<br>
 * TODO：文件校验
 */
public class TorrentStream {

	/**
	 * 校验文件的字节数
	 */
	private static final int VERIFY_SIZE = 10;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStream.class);
	
	// 固定值
	private final long pieceLength; // 每个块的大小
	private final TorrentStreamGroup torrentStreamGroup; // 下载文件组
	
	// 初始值：变化值
	private AtomicLong fileBuffer; // 缓冲大小：插入queue时修改
	private AtomicLong fileDownloadSize; // 已下载大小：写入文件时修改
	private BlockingQueue<TorrentPiece> filePieces; // Piece队列
	
	// 初始值：不变值
	private String file; // 文件路径
	private long fileSize; // 文件大小
	private long fileBeginPos; // 文件开始偏移：包含该值
	private long fileEndPos; // 文件结束偏移：不包含该值
	private RandomAccessFile fileStream; // 文件流
	
	// 初始值：计算
	private int filePieceSize; // 文件Piece数量
	private int fileBeginPieceIndex; // 文件Piece开始索引
	private int fileEndPieceIndex; // 文件Piece结束索引
	private BitSet bitSet; // 当前文件位图
	private BitSet downloadingBitSet; // 下载中的位图
	
	/**
	 * @param pieceLength 块大小
	 */
	public TorrentStream(long pieceLength, TorrentStreamGroup torrentStreamGroup) {
		this.pieceLength = pieceLength;
		this.torrentStreamGroup = torrentStreamGroup;
	}
	
	/**
	 * 创建新的文件
	 * @param file 文件路径
	 * @param size 文件大小
	 * @param pos 文件开始位置
	 */
	public void buildFile(String file, long size, long pos) throws IOException {
		if(filePieces != null && filePieces.size() > 0) {
			throw new IOException("Torrent文件未被释放");
		}
		this.file = file;
		this.fileSize = size;
		this.fileBeginPos = pos;
		this.fileEndPos = pos + size;
		fileBuffer = new AtomicLong(0);
		fileDownloadSize = new AtomicLong(0);
		filePieces = new LinkedBlockingQueue<>();
		FileUtils.buildFolder(this.file, true); // 创建文件父目录，否者会抛出FileNotFoundException
		fileStream = new RandomAccessFile(this.file, "rw");
		initFilePiece();
		initFileBitSet();
		if(LOGGER.isDebugEnabled()) {
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
	 * 添加Piece
	 * 每次下载一个完成的Piece
	 */
	public void piece(TorrentPiece piece) {
		if(!piece.contain(this.fileBeginPos, this.fileEndPos)) { // 不符合当前文件位置
			return;
		}
		List<TorrentPiece> list = null;
		synchronized (this) {
			if(filePieces.offer(piece)) {
				download(piece.getIndex());
				torrentStreamGroup.piece(piece.getIndex()); // 下载完成
				fileBuffer.addAndGet(piece.getLength());
				// 刷新缓存
				long bufferSize = fileBuffer.get();
				long downloadSize = this.fileDownloadSize.addAndGet(bufferSize); // 设置已下载大小
				if(
					bufferSize >= DownloadConfig.getPeerMemoryBufferByte() || // 大于缓存
					downloadSize == fileSize // 下载完成
				) {
					fileBuffer.set(0L); // 清空
					list = flush();
				}
			} else {
				// TODO：重新返回Piece位图
				LOGGER.error("添加Piece失败");
			}
		}
		write(list);
	}
	
	/**
	 * 选择未下载的Piece索引，设置为下载状态
	 * @param peerBitSet Peer含有的位图
	 */
	public TorrentPiece pick(final BitSet peerBitSet) {
		if(peerBitSet.cardinality() == 0) {
			return null;
		}
		synchronized (this) {
			final BitSet pickBitSet = new BitSet();
			pickBitSet.or(peerBitSet);
			pickBitSet.andNot(this.bitSet);
			pickBitSet.andNot(this.downloadingBitSet);
			if(pickBitSet.cardinality() == 0) {
				return null;
			}
			int index = pickBitSet.nextSetBit(this.fileBeginPieceIndex);
			if(index > this.fileEndPieceIndex) {
				return null;
			}
			int begin = 0;
			if(index == this.fileBeginPieceIndex) {
				begin = firstPiecePos();
			}
			int end = (int) this.pieceLength;
			if(index == this.fileEndPieceIndex) {
				end = lastPieceSize();
			}
			return new TorrentPiece(this.pieceLength, index, begin, end);
		}
	}

	/**
	 * 读取块数据<br>
	 * 第一块数据下标：0<br>
	 * 如果文件开始和结束都不是一个完整的piece大小，修改为实际大小
	 */
	public byte[] read(int index) {
		return read(index, (int) this.pieceLength);
	}
	
	/**
	 * 读取块数据
	 */
	public byte[] read(int index, int size) {
		return read(index, size, 0);
	}
	
	public byte[] read(int index, int size, int pos) {
		return read(index, size, pos, false);
	}
	
	/**
	 * 读取块数据<br>
	 * 第一块数据下标：0
	 * @param ignoreBitSet 忽略已下载位图
	 */
	private byte[] read(int index, int size, int pos, boolean ignoreBitSet) {
		if(!hasIndex(index)) {
			return null;
		}
		if(!ignoreBitSet && !hasPiece(index)) {
			return null;
		}
		long seek = 0L;
		final long beginPos = pieceLength * index + pos;
		final long endPos = beginPos + size;
		if(beginPos >= this.fileEndPos) {
			return null;
		}
		if(endPos < this.fileBeginPos) {
			return null;
		}
		if(beginPos < this.fileBeginPos) {
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
			fileStream.seek(seek);
			fileStream.read(bytes);
		} catch (IOException e) {
			LOGGER.error("读取块信息异常", e);
		}
		return bytes;
	}
	
	/**
	 * 获取大小
	 */
	public long size() {
		long size = 0L;
		int downloadPieceSize = this.bitSet.cardinality();
		if(hasPiece(this.fileBeginPieceIndex)) {
			size += firstPieceSize();
			downloadPieceSize--;
		}
		if(hasPiece(this.fileEndPieceIndex)) {
			size += lastPieceSize();
			downloadPieceSize--;
		}
		return size + downloadPieceSize * this.pieceLength;
	}
	
	/**
	 * 是否下载完成
	 */
	public boolean over() {
		return bitSet.cardinality() >= this.filePieceSize;
	}
	
	/**
	 * 取消下载
	 */
	public void undone(int index) {
		synchronized (this) {
			this.downloadingBitSet.clear(index);
		}
	}
	
	/**
	 * 关闭资源
	 */
	public void release() {
		List<TorrentPiece> list = null;
		synchronized (this) {
			list = flush();
		}
		write(list);
		try {
			fileStream.close();
		} catch (IOException e) {
			LOGGER.error("关闭流异常", e);
		}
	}

	/**
	 * 刷出缓存
	 */
	private List<TorrentPiece> flush() {
		int size = filePieces.size();
		List<TorrentPiece> list = new ArrayList<>(size);
		filePieces.drainTo(list, size);
		return list;
	}
	
	/**
	 * 写入硬盘
	 */
	private void write(List<TorrentPiece> list) {
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		list.stream().forEach(piece -> {
			if(
				piece.getIndex() < this.fileBeginPieceIndex ||
				piece.getIndex() > this.fileEndPieceIndex
			) {
				LOGGER.warn("写入文件索引错误");
				return;
			}
			int offset = 0;
			long seek = 0L;
			int length = piece.getLength();
			final long beginPos = piece.beginPos();
			final long endPos = piece.endPos();
			if(beginPos < this.fileBeginPos) {
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
				fileStream.seek(seek);
				fileStream.write(piece.getData(), offset, length);
			} catch (IOException e) {
				LOGGER.error("文件流写入失败", e);
			}
		});
	}
	
	/**
	 * 初始化：开始块大小，结束块大小
	 */
	private void initFilePiece() {
		this.fileBeginPieceIndex = (int) (this.fileBeginPos / this.pieceLength);
		this.fileEndPieceIndex = (int) (this.fileEndPos / this.pieceLength);
		this.filePieceSize = this.fileEndPieceIndex - this.fileBeginPieceIndex;
		int endPieceSize = (int) (this.fileEndPos % this.pieceLength);
		if(endPieceSize > 0) {
			this.filePieceSize++;
		}
		bitSet = new BitSet();
		downloadingBitSet = new BitSet();
	}
	
	/**
	 * 初始化：已下载块
	 */
	private void initFileBitSet() {
		int pos = 0;
		byte[] bytes = null;
		for (int index = this.fileBeginPieceIndex; index <= this.fileEndPieceIndex; index++) {
			if(index == this.fileBeginPieceIndex) {
				pos = firstPiecePos();
			} else {
				pos = 0;
			}
			bytes = read(index, VERIFY_SIZE, pos, true); // 第一块需要偏移
			if(hasData(bytes)) {
				download(index);
				torrentStreamGroup.piece(index);
			}
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("当前文件已下载Piece数量：{}，剩余下载Piece数量：{}",
				bitSet.cardinality(),
				this.filePieceSize - bitSet.cardinality()
			);
		}
	}
	
	/**
	 * 是否有数据
	 */
	private boolean hasData(byte[] bytes) {
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
	 * 下载完成
	 */
	private void download(int index) {
		bitSet.set(index, true); // 下载成功
		downloadingBitSet.clear(index); // 去掉下载状态
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
	 * 判断是否包含块索引
	 * @param index 文件索引
	 */
	private boolean hasIndex(int index) {
		if(index < this.fileBeginPieceIndex || index > this.fileEndPieceIndex) { // 不符合当前文件位置
			return false;
		}
		return true;
	}
	
	/**
	 * 是否含有Piece数据
	 */
	private boolean hasPiece(int index) {
		synchronized (this) {
			return bitSet.get(index);
		}
	}

}
