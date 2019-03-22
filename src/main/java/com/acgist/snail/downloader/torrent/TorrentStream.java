package com.acgist.snail.downloader.torrent;

import java.io.RandomAccessFile;

/**
 * torrent文件流
 */
public class TorrentStream {

	private int pieceLength; // 每个块的大小
	
	private long buffer; // 缓冲大小
	
	private String file; // 文件路径
	private RandomAccessFile stream; // 文件流

	public TorrentStream() {
	}

}
