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
import com.acgist.snail.pojo.session.TorrentSession;
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
	private TorrentSession torrentSession; // 下载任务
	
	// 初始值：变化值
	private AtomicLong fileBuffer; // 缓冲大小：插入queue时修改
	private AtomicLong fileDownloadSize; // 已下载大小：写入文件时修改
	private BlockingQueue<TorrentPiece> filePieces; // Piece队列
	
	// 初始值：不变值
	private String file; // 文件路径
	private long fileSize; // 文件大小
	private long filePos; // 文件开始偏移
	private RandomAccessFile fileStream; // 文件流
	
	// 初始值：计算
	private int filePieceSize; // 文件Piece数量
	private int fileBeginPieceSize; // 开始块大小
	private int fileEndPieceSize; // 结束块大小
	private int fileBeginPieceIndex; // 文件Piece开始序号
	private int fileEndPieceIndex; // 文件Piece结束序号
	private BitSet downloadingBitSet; // 下载中的位图
	
	/**
	 * @param pieceSize 块数量
	 * @param pieceLength 块大小
	 */
	public TorrentStream(int pieceSize, long pieceLength, TorrentSession torrentSession) {
		this.pieceSize = pieceSize;
		this.pieceLength = pieceLength;
		this.torrentSession = torrentSession;
	}
	
	/**
	 * 创建新的文件
	 * @param file 文件路径
	 * @param size 文件大小
	 * @param pos 文件开始位置
	 */
	public void newFile(String file, long size, long pos) throws IOException {
		if(filePieces != null && filePieces.size() > 0) {
			throw new IOException("Torrent文件未被释放");
		}
		this.file = file;
		this.fileSize = size;
		this.filePos = pos;
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
				torrentSession.piece(piece.getIndex()); // 下载完成
				unset(piece.getIndex());
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
	public int pick(int index) {
		synchronized (downloadingBitSet) {
			int pickIndex = 0;
			while(true) {
				pickIndex = torrentSession.nextPiece(index);
				if(!downloadingBitSet.get(pickIndex)) {
					break;
				}
			}
			return pickIndex;
		}
	}
	
	/**
	 * 清空下载索引
	 */
	public void unset(int index) {
		synchronized (downloadingBitSet) {
			downloadingBitSet.clear(index);
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
		if(index == this.fileBeginPieceIndex && this.fileBeginPieceSize != 0) {
			size = this.fileBeginPieceSize;
		} else if(this.fileEndPieceSize != 0 && index == (this.fileEndPieceIndex - 1)) {
			size = this.fileEndPieceSize;
		}
		return read(index, size);
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
		if(index >= this.filePieceSize) {
			throw new IndexOutOfBoundsException("读取文件块超限");
		}
		if(index == this.fileBeginPieceIndex && this.fileBeginPieceSize != 0) {
			if(size > this.fileBeginPieceSize) {
				size = this.fileBeginPieceSize;
			}
		} else if(this.fileEndPieceSize != 0 && index == (this.fileEndPieceIndex - 1)) {
			if(size > this.fileEndPieceSize) {
				size = this.fileEndPieceSize;
			}
		}
		final byte[] bytes = new byte[size];
		try {
			final long seek = (pieceLength * index) - this.filePos + pos; // 跳过字节数
			fileStream.seek(seek);
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
			if(
				piece.getIndex() < this.fileBeginPieceIndex ||
				piece.getIndex() > this.fileEndPieceIndex
			) {
				throw new IllegalArgumentException("写入块索引超出文件索引");
			}
			long begin = pieceLength * (piece.getIndex() - this.fileBeginPieceIndex) + piece.getBegin(); 
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
		this.fileBeginPieceIndex = (int) (this.filePos / this.pieceLength);
		int beginSize = (int) (this.filePos % this.pieceSize);
		if(this.fileSize <= this.pieceLength) {
			this.fileBeginPieceSize = (int) this.fileSize;
			this.fileEndPieceSize = 0;
			this.filePieceSize = 1;
		} else {
			if(beginSize == 0) {
				this.fileBeginPieceSize = 0;
			} else {
				this.fileBeginPieceSize = this.pieceSize - beginSize;
				pieceSize++;
			}
			int endSize = (int) ((this.fileSize + this.filePos) % this.pieceSize);
			if(endSize == 0) {
				this.fileEndPieceSize = 0;
			} else {
				this.fileEndPieceSize = endSize;
				pieceSize++;
			}
			this.filePieceSize = (int) (pieceSize + ((this.fileSize - this.fileBeginPieceSize - this.fileEndPieceSize) / this.pieceSize));
		}
		this.fileEndPieceIndex = this.fileBeginPieceIndex + this.filePieceSize;
	}
	
	/**
	 * 初始化：已下载块
	 */
	private void initFileBitSet() {
		byte[] bytes = null;
		BitSet bitSet = new BitSet();
		for (int index = this.fileBeginPieceIndex; index < this.fileEndPieceIndex; index++) {
			bytes = read(index, VERIFY_SIZE);
			bitSet.set(index, hasData(bytes));
		}
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
