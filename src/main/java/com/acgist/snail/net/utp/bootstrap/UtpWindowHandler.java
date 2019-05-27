package com.acgist.snail.net.utp.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
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
	
	/**
	 * 最大缓存数量
	 */
	private static final int MAX_SIZE = 200;
	/**
	 * 重试序号差量
	 */
	private static final int RETRY_SIZE = 3;

	/**
	 * map缓存大小
	 */
	private int mapCacheSize;
	/**
	 * 最后一个接收/发送的seqnr
	 */
	private short lastSeqnr;
	/**
	 * 最后一个接收/发送的timestamp
	 */
	private int lastTimestamp;
	/**
	 * 数据
	 */
	private final Map<Short, UtpWindowData> map;
	
	private UtpWindowHandler() {
		this.mapCacheSize = 0;
		this.lastSeqnr = 0;
		this.lastTimestamp = 0;
		this.map = new ConcurrentHashMap<>(MAX_SIZE);
	}
	
	public static final UtpWindowHandler newInstance() {
		return new UtpWindowHandler();
	}
	
	public void connect(int timestamp, short lastSeqnr) {
		this.lastSeqnr = lastSeqnr;
		this.lastTimestamp = timestamp;
	}

	/**
	 * 获取剩余缓存大小
	 */
	public int remaining() {
		return UtpConfig.WND_SIZE - this.mapCacheSize;
	}
	
	/**
	 * 发送数据，递增seqnr。
	 * TODO：是否记录
	 */
	public synchronized List<UtpWindowData> send(byte[] data) {
		this.lastSeqnr++;
		this.lastTimestamp = timestamp();
		final UtpWindowData windowData = storage(this.lastTimestamp, this.lastSeqnr, data);
		final List<UtpWindowData> list = new ArrayList<>();
		this.map.keySet().forEach(key -> {
			if((this.lastSeqnr - key) > RETRY_SIZE) {
				list.add(this.map.get(key));
			}
		});
		list.add(windowData);
		System.out.println("send-------------------" + this.map.size());
		return list;
	}
	
	/**
	 * 响应，小于这个序号的都移除。
	 */
	public synchronized void ack(short acknr) {
		this.map.keySet().forEach(key -> {
			if(key <= acknr) {
				this.map.remove(key);
			}
		});
		System.out.println("ack-------------------" + this.map.size());
	}
	
	/**
	 * 接收buffer，如果是下一个滑块直接返回，否者缓存，等待下一个返回null。
	 */
	public synchronized UtpWindowData receive(int timestamp, short seqnr, ByteBuffer buffer) throws NetException {
		storage(timestamp, seqnr, buffer);
		short nextSeqnr; // 下一个seqnr
		UtpWindowData nextWindowData;
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
		return UtpWindowData.newInstance(this.lastSeqnr, this.lastTimestamp, bytes);
	}
	
	/**
	 * 取出
	 */
	private UtpWindowData take(final short seqnr) {
		final UtpWindowData windowData = this.map.remove(seqnr);
		if(windowData == null) {
			return windowData;
		}
		this.mapCacheSize = this.mapCacheSize - windowData.getLength();
		return windowData;
	}

	/**
	 * 存入
	 */
	private UtpWindowData storage(final int timestamp, final short seqnr, final ByteBuffer buffer) throws NetException {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return storage(timestamp, seqnr, bytes);
	}
	
	/**
	 * 存入
	 */
	private UtpWindowData storage(final int timestamp, final short seqnr, byte[] bytes) {
		final UtpWindowData windowData = UtpWindowData.newInstance(seqnr, timestamp, bytes);
		this.map.put(seqnr, windowData);
		this.mapCacheSize = this.mapCacheSize + windowData.getLength();
		return windowData;
	}
	
	/**
	 * 时间戳
	 */
	public static final int timestamp() {
		return (int) System.nanoTime();
	}

	public int lastTimestamp() {
		return this.lastTimestamp;
	}

	public short lastSeqnr() {
		return this.lastSeqnr;
	}
	
}
