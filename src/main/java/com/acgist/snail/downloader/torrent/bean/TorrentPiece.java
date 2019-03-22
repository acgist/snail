package com.acgist.snail.downloader.torrent.bean;

/**
 * Piece信息
 */
public class TorrentPiece {

	/**
	 * 默认每次下载长度：16KB
	 */
	public static final int SLICE_SIZE = 16 * 1024;
	
	private int index; // piece的索引
	private int begin; // piece内的偏移
	private int length; // 数据的长度
	private byte[] bytes; // 数据
	
}
