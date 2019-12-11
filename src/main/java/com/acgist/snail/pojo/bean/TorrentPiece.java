package com.acgist.snail.pojo.bean;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Piece下载信息</p>
 * <p>保存时必须是下载完成的Piece：end - begin == length == data.length && pos == begin</p>
 * <p>下载基于文件下载，所以当某个Piece处于两个文件交接处时，该Piece会被分为两次下载。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentPiece {

	/**
	 * <p>默认下载长度：16KB</p>
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
	 * <p>开始偏移</p>
	 * <p>Piece开始位置在整个任务中的绝对偏移</p>
	 */
	public long beginPos() {
		return this.pieceLength * this.getIndex() + this.begin;
	}
	
	/**
	 * <p>结束偏移</p>
	 * <p>Piece结束位置在整个任务中的绝对偏移</p>
	 */
	public long endPos() {
		return beginPos() + length;
	}
	
	/**
	 * <p>判断文件是否包含当前Piece</p>
	 * <p>包含开始不包含结束（即两边判断条件一样）：判断时都使用等于</p>
	 * 
	 * @param fileBeginPos 文件开始偏移
	 * @param fileEndPos 文件结束偏移
	 */
	public boolean contain(long fileBeginPos, long fileEndPos) {
		final long beginPos = beginPos();
		final long endPos = endPos();
		if(endPos <= fileBeginPos) {
			return false;
		}
		if(beginPos >= fileEndPos) {
			return false;
		}
		return true;
	}
	
	/**
	 * <p>是否还有更多的数据请求</p>
	 */
	public boolean haveMoreSlice() {
		return this.position < this.length;
	}
	
	/**
	 * <p>是否下载完成</p>
	 */
	public boolean complete() {
		return this.size >= this.length;
	}
	
	/**
	 * <p>获取整个Piece内偏移</p>
	 */
	public int position() {
		return this.begin + this.position;
	}
	
	/**
	 * <p>获取本次请求数据大小</>
	 * <p>{@code 0}：已经发送所有请求</p>
	 * <p>修改{@link #position}</p>
	 */
	public int length() {
		if(this.position == this.length) {
			return 0;
		}
		final int size = this.length - this.position;
		// 剩余大小不满足一个Slice
		if(SLICE_LENGTH > size) {
			this.position = this.length;
			return size;
		} else {
			this.position += SLICE_LENGTH;
			return SLICE_LENGTH;
		}
	}
	
	/**
	 * <p>放入Slice数据</p>
	 * <p>修改{@link #size}</p>
	 * 
	 * @param begin 数据开始位移：整个Piece内偏移
	 * @param bytes 数据
	 * 
	 * @return true-完成；false-未完成；
	 */
	public boolean write(final int begin, final byte[] bytes) {
		synchronized (this) {
			System.arraycopy(bytes, 0, this.data, begin - this.begin, bytes.length);
			this.size += bytes.length;
			return complete();
		}
	}
	
	/**
	 * <p>读取Piece数据</p>
	 * 
	 * @param begin 数据开始位移：整个Piece内偏移
	 * @param size 长度
	 * 
	 * @return Piece数据
	 */
	public byte[] read(final int begin, final int size) {
		if(begin >= this.end) {
			return null;
		}
		final int end = begin + size;
		if(end <= this.begin) {
			return null;
		}
		int beginPos = begin - this.begin; // 当前数据开始偏移
		final int endPos = end - this.begin; // 当前数据结束偏移
		if(beginPos < 0) {
			beginPos = 0;
		}
		int length = endPos - beginPos; // 读取数据真实长度
		if (length + beginPos > this.data.length) {
			length = this.data.length - beginPos;
		}
		if(length <= 0) {
			return null;
		}
		final byte[] bytes = new byte[length];
		System.arraycopy(this.data, beginPos, bytes, 0, length);
		return bytes;
	}
	
	/**
	 * <p>校验数据</p>
	 */
	public boolean verify() {
		if(this.verify) {
			final var hash = StringUtils.sha1(this.data);
			return ArrayUtils.equals(hash, this.hash);
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

}
