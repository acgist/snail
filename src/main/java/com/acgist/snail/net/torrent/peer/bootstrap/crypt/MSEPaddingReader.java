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
public class MSEPaddingReader {

	/**
	 * 读取数据数量
	 */
	private int count;
	/**
	 * 数据
	 */
	private byte[] bytes;
	/**
	 * 剩余数据长度
	 */
	private short length = -1;
	/**
	 * 数据集合
	 */
	private final List<byte[]> list;
	
	private MSEPaddingReader(int count) {
		this.count = count;
		this.list = new ArrayList<>(count);
	}
	
	public static final MSEPaddingReader newInstance(int count) {
		return new MSEPaddingReader(count);
	}
	
	/**
	 * 数据读取
	 * 
	 * @param buffer 数据
	 * 
	 * @return 是否读取完成：true-完成；false-未完成；
	 */
	public boolean read(ByteBuffer buffer) {
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
			return read(buffer);
		} else if(remain >= this.length) {
			buffer.get(this.bytes, this.bytes.length - this.length, this.length);
			this.count--;
			this.length = -1;
			this.list.add(this.bytes);
			buffer.compact().flip();
			return read(buffer);
		} else {
			buffer.get(this.bytes, this.bytes.length - this.length, remain);
			this.length -= remain;
			buffer.compact();
			return false;
		}
	}
	
	/**
	 * 获取所有数据集合
	 */
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
