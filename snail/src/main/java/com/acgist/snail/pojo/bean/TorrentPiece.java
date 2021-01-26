package com.acgist.snail.pojo.bean;

import java.util.Arrays;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Piece下载信息</p>
 * <p>下载基于文件下载，所以当某个Piece处于两个文件交接处时，该Piece会被分为两次下载。</p>
 * 
 * @author acgist
 */
public final class TorrentPiece {

	/**
	 * <p>默认下载长度：{@value}</p>
	 */
	public static final int SLICE_LENGTH = 16 * SystemConfig.ONE_KB;

	/**
	 * <p>Piece大小</p>
	 */
	private final long pieceLength;
	/**
	 * <p>Piece索引</p>
	 */
	private final int index;
	/**
	 * <p>Piece开始偏移</p>
	 */
	private final int begin;
	/**
	 * <p>Piece结束偏移</p>
	 */
	private final int end;
	/**
	 * <p>数据长度：end - begin</p>
	 */
	private final int length;
	/**
	 * <p>数据</p>
	 */
	private final byte[] data;
	/**
	 * <p>校验数据</p>
	 */
	private final byte[] hash;
	/**
	 * <p>是否校验</p>
	 * <p>文件第一块和最后一块不验证：多文件可能不同时下载</p>
	 */
	private final boolean verify;
	/**
	 * <p>已下载大小</p>
	 * <p>每次获取到Slice数据后修改</p>
	 */
	private int size;
	/**
	 * <p>请求内偏移：当前选择下载Piece数据的内偏移</p>
	 * <p>每次获取到Slice数据后修改</p>
	 */
	private int position;
	
	/**
	 * @param pieceLength Piece大小
	 * @param index Piece索引
	 * @param begin Piece开始偏移
	 * @param end Piece结束偏移
	 * @param hash 校验数据
	 * @param verify 是否校验
	 */
	private TorrentPiece(long pieceLength, int index, int begin, int end, byte[] hash, boolean verify) {
		this.pieceLength = pieceLength;
		this.index = index;
		this.begin = begin;
		this.end = end;
		this.hash = hash;
		this.verify = verify;
		this.length = end - begin;
		this.data = new byte[this.length];
		this.size = 0;
		this.position = 0;
	}

	/**
	 * <p>创建Piece下载信息</p>
	 * 
	 * @param pieceLength Piece大小
	 * @param index Piece索引
	 * @param begin Piece开始偏移
	 * @param end Piece结束偏移
	 * @param hash 校验数据
	 * @param verify 是否校验
	 * 
	 * @return Piece下载信息
	 */
	public static final TorrentPiece newInstance(long pieceLength, int index, int begin, int end, byte[] hash, boolean verify) {
		return new TorrentPiece(pieceLength, index, begin, end, hash, verify);
	}
	
	/**
	 * <p>开始偏移</p>
	 * <p>Piece开始位置在整个任务中的绝对偏移</p>
	 * 
	 * @return 开始偏移
	 */
	public long beginPos() {
		return this.pieceLength * this.index + this.begin;
	}
	
	/**
	 * <p>结束偏移</p>
	 * <p>Piece结束位置在整个任务中的绝对偏移</p>
	 * 
	 * @return 结束偏移
	 */
	public long endPos() {
		return this.beginPos() + this.length;
	}
	
	/**
	 * <p>判断文件是否包含当前Piece</p>
	 * <p>包含开始不包含结束（即两边判断条件一样）：判断时都使用等于</p>
	 * 
	 * @param fileBeginPos 文件开始偏移
	 * @param fileEndPos 文件结束偏移
	 * 
	 * @return 是否包含
	 */
	public boolean contain(long fileBeginPos, long fileEndPos) {
		final long endPos = this.endPos();
		if(endPos <= fileBeginPos) {
			return false;
		}
		final long beginPos = this.beginPos();
		if(beginPos >= fileEndPos) {
			return false;
		}
		return true;
	}
	
	/**
	 * <p>判断是否还有更多的数据请求</p>
	 * 
	 * @return 是否还有更多
	 */
	public boolean hasMoreSlice() {
		return this.position < this.length;
	}
	
	/**
	 * <p>判断是否下载完成</p>
	 * 
	 * @return 是否完成
	 */
	public boolean completed() {
		return this.size >= this.length;
	}
	
	/**
	 * <p>获取整个Piece内偏移</p>
	 * 
	 * @return 整个Piece内偏移
	 */
	public int position() {
		return this.begin + this.position;
	}
	
	/**
	 * <p>获取本次请求数据大小</p>
	 * <p>已经发送所有请求返回：{@code 0}</p>
	 * <p>获取数据后修改{@link #position}</p>
	 * 
	 * @return 本地请求数据大小
	 */
	public int length() {
		if(this.position == this.length) {
			return 0;
		}
		final int remaining = this.length - this.position;
		// 剩余大小不满足一个Slice
		if(SLICE_LENGTH > remaining) {
			this.position = this.length;
			return remaining;
		} else {
			this.position += SLICE_LENGTH;
			return SLICE_LENGTH;
		}
	}
	
	/**
	 * <p>写入Slice数据</p>
	 * <p>写入后修改{@link #size}</p>
	 * 
	 * @param begin 数据开始位移：整个Piece内偏移
	 * @param bytes 数据
	 * 
	 * @return true-完成；false-没有完成；
	 */
	public boolean write(final int begin, final byte[] bytes) {
		synchronized (this) {
			System.arraycopy(bytes, 0, this.data, begin - this.begin, bytes.length);
			this.size += bytes.length;
			return this.completed();
		}
	}
	
	/**
	 * <p>读取Slice数据</p>
	 * 
	 * @param begin 数据开始位移：整个Piece内偏移
	 * @param size 长度
	 * 
	 * @return Slice数据
	 */
	public byte[] read(final int begin, final int size) {
		if(begin >= this.end) {
			return null;
		}
		final int end = begin + size;
		if(end <= this.begin) {
			return null;
		}
		// 当前数据开始偏移
		int beginPos = 0;
		if(begin > this.begin) {
			beginPos = begin - this.begin;
		}
		// 当前数据结束偏移
		int endPos = end - this.begin;
		if (endPos > this.data.length) {
			endPos = this.data.length;
		}
		// 读取数据真实长度
		final int length = endPos - beginPos;
		if(length <= 0) {
			return null;
		}
		final byte[] bytes = new byte[length];
		System.arraycopy(this.data, beginPos, bytes, 0, length);
		return bytes;
	}
	
	/**
	 * <p>校验数据</p>
	 * 
	 * @return 是否校验成功
	 */
	public boolean verify() {
		if(this.verify) {
			final var hash = StringUtils.sha1(this.data);
			return Arrays.equals(hash, this.hash);
		}
		return true;
	}

	/**
	 * <p>获取Piece大小</p>
	 * 
	 * @return Piece大小
	 */
	public long getPieceLength() {
		return this.pieceLength;
	}

	/**
	 * <p>获取Piece索引</p>
	 * 
	 * @return Piece索引
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * <p>获取Piece开始偏移</p>
	 * 
	 * @return Piece开始偏移
	 */
	public int getBegin() {
		return this.begin;
	}

	/**
	 * <p>获取Piece结束偏移</p>
	 * 
	 * @return Piece结束偏移
	 */
	public int getEnd() {
		return this.end;
	}

	/**
	 * <p>获取数据长度</p>
	 * 
	 * @return 数据长度
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * <p>获取数据</p>
	 * 
	 * @return 数据
	 */
	public byte[] getData() {
		return this.data;
	}

}
