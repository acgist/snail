package com.acgist.snail.net.utp.bootstrap;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.acgist.snail.system.config.UtpConfig;

/**
 * 滑块窗口
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpWindowHandler {
	
	private static final int MAX_SIZE = 20;

	/**
	 * 缓存的大小
	 */
	private int cacheSize;
	/**
	 * 下一个seqnr
	 */
	private short nextSeqnr;
	
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
	public synchronized ByteBuffer put(final short seqnr, final ByteBuffer buffer) {
		put(seqnr, buffer);
		ByteBuffer tmp = take(this.nextSeqnr);
		if(tmp == null) {
			return null;
		} else {
			
		}
	}
	
	private ByteBuffer take(final short seqnr) {
		return this.map.remove(seqnr);
	}
	
	private void storage(final short seqnr, final ByteBuffer buffer) {
		this.map.put(seqnr, buffer);
	}
	
}
