package com.acgist.snail.downloader.torrent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bean.TorrentPiece;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.CollectionUtils;

/**
 * torrent文件流
 */
public class TorrentStream {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStream.class);
	
	// 固定值
	private final long pieceLength; // 每个块的大小
	
	// 初始值：变化值
	private AtomicLong buffer; // 缓冲大小：插入queue时修改
	private AtomicLong downloadSize; // 已下载大小：写入文件时修改
	private BlockingQueue<TorrentPiece> pieces; // Piece队列
	
	// 初始值：不变值
	private long size; // 文件大小
	private String file; // 文件路径
	private RandomAccessFile stream; // 文件流

	public TorrentStream(long pieceLength) {
		this.pieceLength = pieceLength;
	}
	
	/**
	 * 创建新的文件
	 */
	public void newFile(String file, long size) throws DownloadException {
		this.file = file;
		this.size = size;
		buffer = new AtomicLong(0);
		downloadSize = new AtomicLong(0);
		pieces = new LinkedBlockingQueue<>();
		try {
			stream = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e) {
			throw new DownloadException("创建文件流失败", e);
		}
	}

	/**
	 * 添加Piece
	 */
	public void pieces(TorrentPiece piece) {
		List<TorrentPiece> list = null;
		synchronized (pieces) {
			if(pieces.offer(piece)) {
				buffer.addAndGet(piece.getLength());
				// 刷新缓存
				long bufferSize = buffer.get();
				long downloadSize = this.downloadSize.addAndGet(bufferSize); // 设置已下载大小
				if(
					bufferSize >= DownloadConfig.getMemoryBufferByte() || // 大于缓存
					downloadSize == size // 下载完成
				) {
					buffer.set(0L); // 清空
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
	 * 关闭资源
	 */
	public void release() {
		List<TorrentPiece> list = null;
		synchronized (pieces) {
			list = flush();
		}
		write(list);
		try {
			stream.close();
		} catch (IOException e) {
			LOGGER.error("关闭流异常", e);
		}
	}
	
	/**
	 * 刷出缓存
	 */
	private List<TorrentPiece> flush() {
		int size = pieces.size();
		List<TorrentPiece> list = new ArrayList<>(size);
		pieces.drainTo(list, size);
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
				stream.seek(begin);
				stream.write(piece.getData(), 0, piece.getLength());
			} catch (IOException e) {
				LOGGER.error("文件流写入失败", e);
			}
		});
	}
	
}
