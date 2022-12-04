package com.acgist.snail.net.torrent.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Padding数据同步工具</p>
 * <p>同步数据格式：(len(padding) + padding)+</p>
 * <p>长度数据类型：short</p>
 * 
 * @author acgist
 */
public final class MSEPaddingSync {

	/**
	 * <p>Padding数据数量</p>
	 */
	private int count;
	/**
	 * <p>当前Padding数据</p>
	 */
	private byte[] bytes;
	/**
	 * <p>剩余Padding数据长度</p>
	 */
	private short length;
	/**
	 * <p>Padding数据集合</p>
	 */
	private final List<byte[]> list;
	
	/**
	 * @param count Padding数据数量
	 */
	private MSEPaddingSync(int count) {
		this.length = -1;
		this.count = count;
		this.list = new ArrayList<>(count);
	}
	
	/**
	 * <p>新建同步工具</p>
	 * 
	 * @param count Padding数据数量
	 * 
	 * @return 同步工具
	 */
	public static final MSEPaddingSync newInstance(int count) {
		return new MSEPaddingSync(count);
	}
	
	/**
	 * <p>Padding数据同步</p>
	 * 
	 * @param buffer Padding数据
	 * 
	 * @return 是否同步完成
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	public boolean sync(ByteBuffer buffer) throws PacketSizeException {
		if(this.count == 0) {
			return true;
		}
		// 没有剩余数据
		if(!buffer.hasRemaining()) {
			return false;
		}
		// 开始新的同步数据
		if(this.length == -1) {
			if(buffer.remaining() < 2) {
				// 数据长度不够
				return false;
			}
			// 获取数据长度
			this.length = buffer.getShort();
			PacketSizeException.verify(this.length);
			this.bytes = new byte[this.length];
		}
		final int remaining = buffer.remaining();
		if(this.length == 0) {
			// 数据完整：没有数据
			this.count--;
			this.length = -1;
			this.list.add(this.bytes);
			buffer.compact().flip();
			return this.sync(buffer);
		} else if(remaining >= this.length) {
			// 数据完整：含有数据
			buffer.get(this.bytes, this.bytes.length - this.length, this.length);
			this.count--;
			this.length = -1;
			this.list.add(this.bytes);
			buffer.compact().flip();
			return this.sync(buffer);
		} else {
			// 数据缺失
			buffer.get(this.bytes, this.bytes.length - this.length, remaining);
			this.length -= remaining;
			buffer.compact();
			return false;
		}
	}
	
	@Override
	public String toString() {
		final var padding = this.list.stream()
			.map(StringUtils::hex)
			.collect(Collectors.toList());
		return BeanUtils.toString(this, this.count, this.length, padding);
	}
	
}
