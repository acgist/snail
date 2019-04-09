package com.acgist.snail.pojo;

/**
 * Piece信息
 */
public class TorrentPiece {

	/**
	 * 默认每次下载长度：16KB
	 */
	public static final int SLICE_SIZE = 16 * 1024;

	private final int index; // Piece的索引
	private final int begin; // Piece开始偏移
	private final int end; // Piece结束偏移
	
	private int pos; // Piece内的偏移
	private int length; // 数据的长度
	private byte[] data; // 数据

	public TorrentPiece(int index, int begin, int end) {
		super();
		this.index = index;
		this.begin = begin;
		this.end = end;
	}

	/**
	 * 开始偏倚
	 */
	public long beginPos(long pieceLength) {
		return pieceLength * this.getIndex() + pos;
	}
	
	/**
	 * 结束偏倚
	 */
	public long endPos(long pieceLength) {
		return beginPos(pieceLength) + length;
	}
	
	/**
	 * 数据包含
	 * @param fileBeginPos 文件开始偏移
	 * @param fileEndPos 文件结束偏移
	 * @param pieceLength 块大小
	 */
	public boolean contain(long fileBeginPos, long fileEndPos, long pieceLength) {
		long beginPos = beginPos(pieceLength);
		long endPos = endPos(pieceLength);
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
