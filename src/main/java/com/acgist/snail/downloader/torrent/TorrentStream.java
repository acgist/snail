package com.acgist.snail.downloader.torrent;

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

import com.acgist.snail.downloader.torrent.bean.TorrentPiece;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.utils.CollectionUtils;

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
	private final int pieceSize; // 块数量
	private final long pieceLength; // 每个块的大小
	
	// 初始值：变化值
	private AtomicLong fileBuffer; // 缓冲大小：插入queue时修改
	private AtomicLong fileDownloadSize; // 已下载大小：写入文件时修改
	private BlockingQueue<TorrentPiece> filePieces; // Piece队列
	
	// 初始值：不变值
	private String file; // 文件路径
	private long fileSize; // 文件大小
	private long fileBegin; // 文件开始
	private RandomAccessFile fileStream; // 文件流
	
	// 初始值：计算
	private int fileBeginSize; // 开始块大小
	private int fileEndSize; // 结束块大小
	private int filePieceSize; // 文件Piece数量
	private int filePieceIndex; // 文件Piece开始序号
	private BitSet fileBitSet; // 文件块位图
	
	/**
	 * @param pieceSize 块数量
	 * @param pieceLength 块大小
	 */
	public TorrentStream(int pieceSize, long pieceLength) {
		this.pieceSize = pieceSize;
		this.pieceLength = pieceLength;
	}
	
	/**
	 * 创建新的文件
	 * @param file 文件路径
	 * @param size 文件大小
	 * @param begin 文件开始位置
	 */
	public void newFile(String file, long size, long begin) throws IOException {
		if(filePieces != null && filePieces.size() > 0) {
			throw new IOException("Torrent文件未被释放");
		}
		this.file = file;
		this.fileSize = size;
		this.fileBegin = begin;
		fileBuffer = new AtomicLong(0);
		fileDownloadSize = new AtomicLong(0);
		filePieces = new LinkedBlockingQueue<>();
		fileStream = new RandomAccessFile(this.file, "rw");
		initFilePiece();
		initFileBitSet();
	}
	
	/**
	 * 添加Piece
	 * 每次下载一个完成的Piece
	 */
	public void pieces(TorrentPiece piece) {
		List<TorrentPiece> list = null;
		synchronized (filePieces) {
			if(filePieces.offer(piece)) {
				fileBuffer.addAndGet(piece.getLength());
				// 刷新缓存
				long bufferSize = fileBuffer.get();
				long downloadSize = this.fileDownloadSize.addAndGet(bufferSize); // 设置已下载大小
				if(
					bufferSize >= DownloadConfig.getMemoryBufferByte() || // 大于缓存
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
	 * 挑选一个下载
	 * 设置为已经下载
	 */
	public int pick() {
		synchronized (fileBitSet) {
			return fileBitSet.nextClearBit(0);
		}
	}
	
	/**
	 * 清空下载索引
	 */
	public void unset(int index) {
		synchronized (fileBitSet) {
			fileBitSet.clear(index);
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
		int size = (int) this.pieceLength;
		if(index == 0 && this.fileBeginSize != 0) {
			size = this.fileBeginSize;
		} else if(this.fileEndSize != 0 && index == (this.filePieceSize - 1)) {
			size = this.fileEndSize;
		}
		return read(index, size);
	}
	
	/**
	 * 读取块数据<br>
	 * 第一块数据下标：0
	 */
	private byte[] read(int index, final int size) {
		if(index >= this.filePieceSize) {
			throw new IndexOutOfBoundsException("读取文件块超限");
		}
		byte[] bytes = new byte[size];
		try {
			final long pos = pieceLength * index; // 跳过字节数
			fileStream.seek(pos);
			fileStream.read(bytes);
		} catch (IOException e) {
			LOGGER.error("读取块信息异常");
		}
		return bytes;
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
			long begin = pieceLength * piece.getIndex() + piece.getBegin(); 
			try {
				fileStream.seek(begin);
				fileStream.write(piece.getData(), 0, piece.getLength());
			} catch (IOException e) {
				LOGGER.error("文件流写入失败", e);
			}
		});
	}
	
	/**
	 * 初始化：开始块大小，结束块大小
	 */
	private void initFilePiece() {
		int pieceSize = 0; // 总块数
		this.filePieceIndex = (int) (this.fileBegin / this.pieceLength);
		int beginSize = (int) (this.fileBegin % this.pieceSize);
		if(this.fileSize <= this.pieceLength) {
			this.fileBeginSize = (int) this.fileSize;
			this.fileEndSize = 0;
			this.filePieceSize = 1;
		} else {
			if(beginSize == 0) {
				this.fileBeginSize = 0;
			} else {
				this.fileBeginSize = this.pieceSize - beginSize;
				pieceSize++;
			}
			int endSize = (int) ((this.fileSize + this.fileBegin) % this.pieceSize);
			if(endSize == 0) {
				this.fileEndSize = 0;
			} else {
				this.fileEndSize = endSize;
				pieceSize++;
			}
			this.filePieceSize = (int) (pieceSize + ((this.fileSize - this.fileBeginSize - this.fileEndSize) / this.pieceSize));
		}
	}
	
	/**
	 * 初始化：已下载块
	 */
	private void initFileBitSet() {
		byte[] bytes = null;
		BitSet bitSet = new BitSet();
		for (int index = 0; index < this.filePieceSize; index++) {
			bytes = read(index, VERIFY_SIZE);
			bitSet.set(index, hasData(bytes));
		}
		this.fileBitSet = bitSet;
		System.out.println(bitSet);
	}
	
	/**
	 * 是否有数据
	 */
	private boolean hasData(byte[] bytes) {
		for (byte value : bytes) {
			if(value != 0) {
				return true;
			}
		}
		return false;
	}
	
}
