package com.acgist.snail.downloader.torrent.bean;

/**
 * Piece信息
 */
public class TorrentPiece {

	/**
	 * 默认每次下载长度：16KB
	 */
	public static final int SLICE_SIZE = 16 * 1024;
	
	private int index;
	private int begin;
	private int length;

}
