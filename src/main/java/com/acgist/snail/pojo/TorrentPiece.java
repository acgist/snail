package com.acgist.snail.pojo;

/**
 * Piece信息
 * 保存时必须是一个完成的Piece：end - begin == length == data.length && pos == begin
 */
public class TorrentPiece {

	/**
	 * 默认每次下载长度：16KB
	 */
	public static final int SLICE_SIZE = 16 * 1024;

	private final long pieceLength; // Piece块大小
	private final int index; // Piece索引
	private final int begin; // Piece开始偏移：选择下载时设置
	private final int end; // Piece结束偏移：选择下载时设置
	
	private int pos; // Piece内的偏移：等于begin
	private int length; // 数据的长度：等于end-begin
	private byte[] data; // 数据：长度等于length

	public TorrentPiece(long pieceLength, int index, int begin, int end) {
		this.pieceLength = pieceLength;
		this.index = index;
		this.begin = begin;
		this.end = end;
	}

	/**
	 * 开始偏倚
	 */
	public long beginPos() {
		return this.pieceLength * this.getIndex() + pos;
	}
	
	/**
	 * 结束偏倚
	 */
	public long endPos() {
		return beginPos() + length;
	}
	
	/**
	 * 数据包含
	 * @param fileBeginPos 文件开始偏移
	 * @param fileEndPos 文件结束偏移
	 */
	public boolean contain(long fileBeginPos, long fileEndPos) {
		long beginPos = beginPos();
		long endPos = endPos();
		if(endPos < fileBeginPos) {
			return false;
		}
		if(beginPos > fileEndPos) {
			return false;
		}
		return true;
	}
	
	public int getIndex() {
		return index;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
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
