package com.acgist.snail.pojo.bean;

import com.acgist.snail.utils.NumberUtils;

/**
 * <p>Piece信息</p>
 * <p>保存时必须是一个完成的Piece：end - begin == length == data.length && pos == begin</p>
 * 
 * @author acgist
 * @since 1.0.0
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
	private final int length; // 数据的长度：等于end-begin
	private final byte[] data; // 数据：长度等于length

	private int size; // 已下载大小
	private int position; // 请求内偏移
	
	public TorrentPiece(long pieceLength, int index, int begin, int end) {
		this.pieceLength = pieceLength;
		this.index = index;
		this.begin = begin;
		this.end = end;
		this.length = end - begin;
		this.data = new byte[length];
		this.size = 0;
		this.position = 0;
	}

	/**
	 * 开始偏移
	 */
	public long beginPos() {
		return this.pieceLength * this.getIndex() + this.begin;
	}
	
	/**
	 * 结束偏移
	 */
	public long endPos() {
		return beginPos() + length;
	}
	
	/**
	 * 判断文件是否包含当前Piece
	 * 
	 * @param fileBeginPos 文件开始偏移
	 * @param fileEndPos 文件结束偏移
	 */
	public boolean contain(long fileBeginPos, long fileEndPos) {
		long beginPos = beginPos();
		long endPos = endPos();
		if(endPos <= fileBeginPos) {
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

	public int getLength() {
		return length;
	}

	public byte[] getData() {
		return data;
	}
	
	public boolean complete() {
		return this.size >= this.length;
	}
	
	public boolean more() {
		return this.position < this.length;
	}

	public int limit() {
		return this.length - this.position;
	}
	
	public int limitSlice() {
		final int limit = limit();
		return NumberUtils.divideUp(limit, SLICE_SIZE);
	}
	
	/**
	 * 获取当前整个Piece的偏移
	 */
	public int position() {
		return this.begin + this.position;
	}
	
	/**
	 * 获取本次获取数据大小，返回0时表示已经发送所有请求。
	 */
	public int length() {
		if(this.position == this.length) {
			return 0;
		}
		if(this.position + SLICE_SIZE > this.length) {
			final int size = this.length - this.position;
			this.position = this.length;
			return size;
		}
		this.position += SLICE_SIZE;
		return SLICE_SIZE;
	}
	
	/**
	 * 放入数据
	 * 
	 * @param begin 数据开始位移
	 * @param bytes 数据
	 * @return true-完成；false-未完成
	 */
	public boolean put(final int begin, final byte[] bytes) {
		synchronized (this) {
			System.arraycopy(bytes, 0, this.data, begin - this.begin, bytes.length);
			this.size += bytes.length;
			return complete();
		}
	}

}
