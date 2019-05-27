package com.acgist.snail.net.utp.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.acgist.snail.system.config.UtpConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * UTP滑块窗口
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpWindowHandler {
	
	private static final int MAX_SIZE = 200;

	/**
	 * map缓存大小
	 */
	private int mapCacheSize;
	/**
	 * 最后一个接收的seqnr
	 */
	private short lastSeqnr;
	/**
	 * 最后一个接收的timestamp
	 */
	private int lastTimestamp;
	/**
	 * 数据
	 */
	private final Map<Short, WindowData> map;
	
	private UtpWindowHandler(int timestamp, short lastSeqnr) {
		this.lastSeqnr = lastSeqnr;
		this.lastTimestamp = timestamp;
		this.map = new ConcurrentHashMap<>(MAX_SIZE);
	}
	
	public static final UtpWindowHandler newInstance(int timestamp, short lastSeqnr) {
		return new UtpWindowHandler(timestamp, lastSeqnr);
	}

	/**
	 * 获取剩余缓存大小
	 */
	public int remaining() {
		return UtpConfig.WND_SIZE - this.mapCacheSize;
	}

	/**
	 * 接收buffer，如果是下一个滑块直接返回，否者缓存，等待下一个返回null。
	 */
	public synchronized ByteBuffer receive(int timestamp, short seqnr, ByteBuffer buffer) throws NetException {
		storage(timestamp, seqnr, buffer);
		short nextSeqnr; // 下一个seqnr
		WindowData nextWindowData;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		while(true) {
			nextSeqnr = (short) (this.lastSeqnr + 1);
			nextWindowData = take(nextSeqnr);
			if(nextWindowData == null) {
				break;
			} else {
				this.lastSeqnr = nextWindowData.getSeqnr();
				this.lastTimestamp = nextWindowData.getTimestamp();
				try {
					output.write(nextWindowData.getData());
				} catch (IOException e) {
					throw new NetException("UTP消息处理异常", e);
				}
			}
		}
		final byte[] bytes = output.toByteArray();
		if(bytes.length == 0) {
			return null;
		}
		return ByteBuffer.wrap(bytes);
	}
	
	/**
	 * 取出
	 */
	private WindowData take(final short seqnr) {
		final WindowData windowData = this.map.remove(seqnr);
		if(windowData == null) {
			return windowData;
		}
		this.mapCacheSize = this.mapCacheSize - windowData.getLength();
		return windowData;
	}

	/**
	 * 存入
	 */
	private void storage(final int timestamp, final short seqnr, final ByteBuffer buffer) throws NetException {
		if(this.map.size() > MAX_SIZE) {
			throw new NetException("UTP消息长度超过缓存最大长度");
		}
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.put(bytes);
		final WindowData windowData = WindowData.newInstance(seqnr, timestamp, bytes);
		this.map.put(seqnr, windowData);
		this.mapCacheSize = this.mapCacheSize + windowData.getLength();
	}

	public int timestamp() {
		return this.lastTimestamp;
	}

	public short seqnr() {
		return this.lastSeqnr;
	}
	
}

/**
 * UTP窗口数据
 * 
 * @author acgist
 * @since 1.1.0
 */
class WindowData {

	private final short seqnr;
	private final int timestamp;
	private final byte[] data;
	private final int length;

	private WindowData(short seqnr, int timestamp, byte[] data) {
		this.seqnr = seqnr;
		this.timestamp = timestamp;
		this.data = data;
		this.length = data.length;
	}
	
	public static final WindowData newInstance(short seqnr, int timestamp, byte[] data) {
		return new WindowData(seqnr, timestamp, data);
	}

	public short getSeqnr() {
		return seqnr;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public byte[] getData() {
		return data;
	}

	public int getLength() {
		return length;
	}

}