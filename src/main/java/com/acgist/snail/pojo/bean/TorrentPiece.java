package com.acgist.snail.pojo.bean;

import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

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

	/**
	 * Piece块大小
	 */
	private final long pieceLength;
	/**
	 * Piece索引
	 */
	private final int index;
	/**
	 * Piece开始偏移
	 */
	private final int begin;
	/**
	 * Piece结束偏移
	 */
	private final int end;
	/**
	 * 数据的长度：等于end-begin
	 */
	private final int length;
	/**
	 * 数据：长度等于length
	 */
	private final byte[] data;
	/**
	 * 校验数据
	 */
	private final byte[] hash;
	/**
	 * 是否校验
	 */
	private final boolean verify;
	/**
	 * 已下载大小
	 */
	private int size;
	/**
	 * 请求内偏移
	 */
	private int position;
	
	private TorrentPiece(byte[] hash, long pieceLength, int index, int begin, int end, boolean verify) {
		this.pieceLength = pieceLength;
		this.index = index;
		this.begin = begin;
		this.end = end;
		this.hash = hash;
		this.verify = verify;
		this.length = end - begin;
		this.data = new byte[length];
		this.size = 0;
		this.position = 0;
	}

	public static final TorrentPiece newInstance(byte[] hash, long pieceLength, int index, int begin, int end, boolean verify) {
		return new TorrentPiece(hash, pieceLength, index, begin, end, verify);
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
	 * 判断文件是否包含当前Piece。包含开始，不包含结束，所以判断时都需要使用等于。
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
		if(beginPos >= fileEndPos) {
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
	
	/**
	 * 是否还有更多的数据请求
	 */
	public boolean more() {
		return this.position < this.length;
	}
	
	/**
	 * 是否下载完成
	 */
	public boolean complete() {
		return this.size >= this.length;
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
	
	/**
	 * 校验数据
	 */
	public boolean verify() {
		if(this.verify) {
			final var hash = StringUtils.sha1(this.data);
			return ArrayUtils.equals(hash, this.hash);
		}
		return true;
	}

}
