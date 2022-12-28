package com.acgist.snail.net.torrent;

import java.util.Arrays;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.DigestUtils;

/**
 * Piece下载信息
 * Piece一般大小设置为512KB、256KB、1MB，目前已知最大16MB。
 * BT任务基于文件下载，当某个Piece处于两个文件交接处时会被分为两次下载。
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
	 * <p>数据长度</p>
	 */
	private final int length;
	/**
	 * <p>数据</p>
	 * 
	 * TODO：使用直接内存文件读写使用NIO优化（没太大必要毕竟下载最重要的问题是网络IO）
	 */
	private final byte[] data;
	/**
	 * <p>校验数据</p>
	 */
	private final byte[] hash;
	/**
	 * <p>是否校验</p>
	 */
	private final boolean verify;
	/**
	 * <p>已经下载数据大小</p>
	 */
	private int size;
	/**
	 * <p>Piece数据内偏移</p>
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
	 * <p>新建Piece下载信息</p>
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
	 * <p>获取Piece在BT任务中的开始偏移</p>
	 * 
	 * @return 开始偏移
	 */
	public long beginPos() {
		return this.pieceLength * this.index + this.begin;
	}
	
	/**
	 * <p>获取Piece在BT任务中的结束偏移</p>
	 * 
	 * @return 结束偏移
	 */
	public long endPos() {
		return this.beginPos() + this.length;
	}
	
	/**
	 * <p>判断文件是否包含当前Piece</p>
	 * <p>包含开始和不包含结束（两边判断条件一样）：判断时都使用等于</p>
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
	 * @return 是否还有更多的数据请求
	 */
	public boolean hasMoreSlice() {
		return this.position < this.length;
	}
	
	/**
	 * <p>判断是否下载完成</p>
	 * 
	 * @return 是否下载完成
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
	 * <p>注意：会重新计算内偏移</p>
	 * 
	 * @return 本次请求数据大小
	 */
	public int length() {
		if(this.position >= this.length) {
			return 0;
		}
		final int remaining = this.length - this.position;
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
	 * 
	 * @param begin Piece内开始偏移
	 * @param bytes Slice数据
	 * 
	 * @return 是否下载完成
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
	 * @param begin Piece内开始偏移
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
			return Arrays.equals(DigestUtils.sha1(this.data), this.hash);
		}
		return true;
	}
	
	/**
	 * <p>判断是否下载完成并且校验成功</p>
	 * 
	 * @return 是否下载完成并且校验成功
	 * 
	 * @see #completed()
	 * @see #verify()
	 */
	public boolean completedAndVerify() {
		return this.completed() && this.verify();
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
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.index, this.begin, this.end);
	}
	
}
