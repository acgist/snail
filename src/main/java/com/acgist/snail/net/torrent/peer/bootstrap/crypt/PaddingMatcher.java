package com.acgist.snail.net.torrent.peer.bootstrap.crypt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Padding数据同步</p>
 * <p>(len(padding) + padding)+</p>
 * <p>长度数据类型short</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class PaddingMatcher {

	/**
	 * 同步次数
	 */
	private int count;
	/**
	 * 数据
	 */
	private byte[] bytes;
	/**
	 * 数据长度（每次读取减少）
	 */
	private short length = -1;
	/**
	 * 数据
	 */
	private final List<byte[]> list;
	
	private PaddingMatcher(int count) {
		this.count = count;
		this.list = new ArrayList<>(count);
	}
	
	public static final PaddingMatcher newInstance(int count) {
		return new PaddingMatcher(count);
	}
	
	/**
	 * 数据同步
	 * 
	 * @param buffer 数据
	 * 
	 * @return 是否同步完成：true-完成；false-未完成；
	 */
	public boolean match(ByteBuffer buffer) {
		if(this.count == 0) {
			return true;
		}
		if(!buffer.hasRemaining()) {
			return false;
		}
		if(this.length == -1) {
			this.length = buffer.getShort();
			this.bytes = new byte[this.length];
		}
		final int remain = buffer.remaining();
		if(this.length == 0) {
			this.count--;
			this.length = -1;
			this.list.add(this.bytes);
			buffer.compact().flip();
			return match(buffer);
		} else if(remain >= this.length) {
			buffer.get(this.bytes, this.bytes.length - this.length, this.length);
			this.count--;
			this.length = -1;
			this.list.add(this.bytes);
			buffer.compact().flip();
			return match(buffer);
		} else {
			buffer.get(this.bytes, this.bytes.length - this.length, remain);
			this.length -= remain;
			buffer.compact();
			return false;
		}
	}
	
	public List<byte[]> allPadding() {
		return this.list;
	}
	
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer
			.append("count[").append(this.count).append("],")
			.append("length[").append(this.length).append("],")
			.append("size[").append(this.list.size()).append("]");
		return buffer.toString();
	}
	
}
