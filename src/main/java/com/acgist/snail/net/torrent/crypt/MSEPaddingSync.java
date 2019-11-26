package com.acgist.snail.net.torrent.crypt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.system.exception.PacketSizeException;
import com.acgist.snail.utils.ObjectUtils;

/**
 * <p>Padding数据同步</p>
 * <p>同步数据格式：(len(padding) + padding)+</p>
 * <p>长度数据类型：short</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class MSEPaddingSync {

	/**
	 * Padding数据数量
	 */
	private int count;
	/**
	 * 当前Padding数据
	 */
	private byte[] bytes;
	/**
	 * 剩余Padding数据长度
	 */
	private short length = -1;
	/**
	 * Padding数据集合
	 */
	private final List<byte[]> list;
	
	private MSEPaddingSync(int count) {
		this.count = count;
		this.list = new ArrayList<>(count);
	}
	
	public static final MSEPaddingSync newInstance(int count) {
		return new MSEPaddingSync(count);
	}
	
	/**
	 * Padding数据同步
	 * 
	 * @param buffer Padding数据
	 * 
	 * @return 是否同步完成：true-完成；false-未完成；
	 */
	public boolean sync(ByteBuffer buffer) throws PacketSizeException {
		if(this.count == 0) {
			return true;
		}
		if(!buffer.hasRemaining()) {
			return false;
		}
		if(this.length == -1) {
			if(buffer.remaining() < 2) {
				return false;
			}
			this.length = buffer.getShort();
			// 不可能大于：SystemConfig.MAX_NET_BUFFER_LENGTH
			if(this.length < 0) {
				throw new PacketSizeException(this.length);
			}
			this.bytes = new byte[this.length];
		}
		final int remaining = buffer.remaining();
		if(this.length == 0) {
			this.count--;
			this.length = -1;
			this.list.add(this.bytes);
			buffer.compact().flip();
			return sync(buffer);
		} else if(remaining >= this.length) {
			buffer.get(this.bytes, this.bytes.length - this.length, this.length);
			this.count--;
			this.length = -1;
			this.list.add(this.bytes);
			buffer.compact().flip();
			return sync(buffer);
		} else {
			buffer.get(this.bytes, this.bytes.length - this.length, remaining);
			this.length -= remaining;
			buffer.compact();
			return false;
		}
	}
	
	/**
	 * @return 所有的Padding数据
	 */
	public List<byte[]> allPadding() {
		return this.list;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.count, this.length, this.list.size());
	}
	
}
