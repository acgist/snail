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
	 * 缓存的大小
	 */
	private int cacheSize;
	/**
	 * 最后一个接收的seqnr
	 */
	private short lastSeqnr;
	/**
	 * 最后一个接收的timestamp
	 */
	private int lastTimestamp;
	
	private Map<Short, ByteBuffer> map = new ConcurrentHashMap<>(MAX_SIZE);
	
	/**
	 * 获取剩余缓存大小
	 */
	public int remaining() {
		return UtpConfig.WND_SIZE - this.cacheSize;
	}

	/**
	 * 设置buffer，如果是下一个滑块直接返回，否者缓存，等待下一个返回null。
	 */
	public synchronized ByteBuffer put(final int timestamp, final short seqnr, final ByteBuffer buffer) throws NetException {
		storage(seqnr, buffer);
		byte[] tmpBytes;
		ByteBuffer tmpBuffer;
		short nextSeqnr; // 下一个seqnr
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		while(true) {
			nextSeqnr = (short) (this.lastSeqnr + 1);
			tmpBuffer = take(nextSeqnr);
			if(tmpBuffer == null) {
				break;
			} else {
				this.lastSeqnr = nextSeqnr;
				this.lastTimestamp = timestamp; // TODO：优化时间
				tmpBytes = new byte[tmpBuffer.remaining()];
				tmpBuffer.get(tmpBytes);
				try {
					output.write(tmpBytes);
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
	
	private ByteBuffer take(final short seqnr) {
		return this.map.remove(seqnr);
	}
	
	private void storage(final short seqnr, final ByteBuffer buffer) throws NetException {
		if(this.map.size() > MAX_SIZE) {
			throw new NetException("UTP消息长度超过缓存最大长度");
		}
		this.map.put(seqnr, buffer);
	}

	public int timestamp() {
		return this.lastTimestamp;
	}

	public short seqnr() {
		return this.lastSeqnr;
	}
	
}
