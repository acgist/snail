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
	private long fileBeginPos; // 文件开始偏移
	private long fileEndPos; // 文件结束偏移
	private RandomAccessFile fileStream; // 文件流
	
	// 初始值：计算
	private int filePieceSize; // 文件Piece数量
	private int fileBeginPieceSize; // 开始块大小
	private int fileEndPieceSize; // 结束块大小
	private int fileBeginPieceIndex; // 文件Piece开始索引
	private int fileEndPieceIndex; // 文件Piece结束索引
	private BitSet bitSet; // 当前文件位图：下标从零开始，需要转换
	private BitSet downloadingBitSet; // 下载中的位图：下标从零开始，需要转换
	
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
				"TorrentStream信息，块大小：{}，文件路径：{}，文件大小：{}，文件开始偏移：{}，文件Piece数量：{}，开始块大小：{}，结束块大小：{}，文件Piece开始索引：{}，文件Piece结束索引：{}",
				this.pieceLength,
				this.file,
				this.fileSize,
				this.fileBeginPos,
				this.filePieceSize,
				this.fileBeginPieceSize,
				this.fileEndPieceSize,
				this.fileBeginPieceIndex,
				this.fileEndPieceIndex
			);
		}
	}
	
	/**
	 * 添加Piece
	 * 每次下载一个完成的Piece
	 */
	public void pieces(TorrentPiece piece) {
		if(!piece.contain(this.fileBeginPos, this.fileEndPos, this.pieceLength)) { // 不符合当前文件位置
			return;
		}
		List<TorrentPiece> list = null;
		synchronized (filePieces) {
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
	 * @param index 整个文件中的索引
	 */
	private boolean hasPiece(int index) {
		synchronized (this) {
			return bitSet.get(indexIn(index));
		}
	}
	
	/**
	 * 选择未下载的Piece索引，设置为下载状态
	 * @param index 整个文件中的索引
	 */
	public TorrentPiece pick(int index) {
		synchronized (this) {
			int pickIndex = indexIn(index);
			if(pickIndex >= 0) {
				while(true) {
					pickIndex = bitSet.nextClearBit(pickIndex);
					if(pickIndex < this.filePieceSize) {
						if(downloadingBitSet.get(pickIndex)) {
							pickIndex++;
							continue;
						}
						downloadingBitSet.set(pickIndex);
						pickIndex = indexOut(pickIndex);
						break;
					} else {
						pickIndex = -1;
						break;
					}
				}
			} else {
				pickIndex = -1;
			}
			if(pickIndex == -1) {
				return null;
			}
			int begin = 0; // TODO：优化判断
			if(pickIndex == this.fileBeginPieceIndex) {
				begin = (int) (this.pieceLength - this.fileBeginPieceSize);
			}
			int end = (int) this.pieceLength; // TODO：优化判断
			if(pickIndex == this.fileEndPieceIndex) {
				end = this.fileEndPieceSize;
			}
			return new TorrentPiece(pickIndex, begin, end);
		}
	}

	/**
	 * 关闭资源
	 */
	public void release() {
		List<TorrentPiece> list = null;
		synchronized (filePieces) {
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
	
	/**
	 * 读取块数据<br>
	 * 第一块数据下标：0
	 */
	public byte[] read(int index, int size, int pos) {
		if(!hasIndex(index)) {
			return null;
		}
		if(!hasPiece(index)) {
			return null;
		}
		long beginPos = pieceLength * index + pos;
		long seek = beginPos - this.fileBeginPos;
		if(beginPos < this.fileBeginPos) {
			seek = 0;
			beginPos = this.fileBeginPos;
		}
		long endPos = beginPos + size;
		if(endPos > this.fileEndPos) {
			endPos = this.fileEndPos;
		}
		if(endPos - beginPos < size) {
			size = (int) (endPos - beginPos);
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
		if(this.fileBeginPieceSize != 0) {
			size += this.fileBeginPieceSize;
			downloadPieceSize--;
		}
		if(this.fileEndPieceSize != 0) {
			size += this.fileEndPieceSize;
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
			int length = piece.getLength();
			long beginPos = piece.beginPos(pieceLength);
			long seek = beginPos - this.fileBeginPos;
			if(beginPos < this.fileBeginPos) {
				offset = (int) Math.abs(seek);
				seek = 0;
				length = length - offset;
			}
			long endPos = piece.endPos(pieceLength);
			if(endPos > this.fileEndPos) {
				length = (int) (length - (endPos - this.fileEndPos));
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
		int fileBeginPiecePos = (int) (this.fileBeginPos % this.pieceLength);
		if(fileBeginPiecePos == 0) {
			this.fileBeginPieceSize = 0;
		} else {
			this.fileBeginPieceSize = (int) (this.pieceLength - fileBeginPiecePos);
		}
		int endPieceSize = (int) (this.fileEndPos % this.pieceLength);
		this.fileEndPieceIndex = (int) (this.fileEndPos / this.pieceLength);
		if(endPieceSize == 0) {
			this.fileEndPieceSize = 0;
		} else {
			this.fileEndPieceSize = endPieceSize;
		}
		this.filePieceSize = this.fileEndPieceIndex - this.fileBeginPieceIndex;
		if(endPieceSize > 0) {
			this.filePieceSize++;
		}
		bitSet = new BitSet(this.filePieceSize);
		downloadingBitSet = new BitSet(this.filePieceSize);
	}
	
	/**
	 * 初始化：已下载块
	 */
	private void initFileBitSet() {
		byte[] bytes = null;
		int pieceIndex = 0;
		for (int index = 0; index < this.filePieceSize; index++) {
			pieceIndex = index + this.fileBeginPieceIndex;
			bytes = read(pieceIndex, VERIFY_SIZE);
			if(hasData(bytes)) {
				download(index);
				torrentStreamGroup.piece(pieceIndex);
			}
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
	 * @param index 整个文件中的索引
	 */
	private void download(int index) {
		bitSet.set(indexIn(index), true); // 下载成功
		downloadingBitSet.clear(indexIn(index)); // 去掉下载状态
	}
	
	/**
	 * 索引转换
	 * @param index 整个任务的索引
	 * @return 当前文件的索引
	 */
	private int indexIn(int index) {
		return index - this.fileBeginPieceIndex;
	}
	
	/**
	 * 索引转换
	 * @param index 当前文件的索引
	 * @return 整个任务的索引
	 */
	private int indexOut(int index) {
		return index + this.fileBeginPieceIndex;
	}
	
}
