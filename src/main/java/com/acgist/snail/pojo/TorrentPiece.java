package com.acgist.snail.pojo;

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
	private byte[] data; // 数据

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
