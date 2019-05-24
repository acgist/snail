package com.acgist.snail.net.utp.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.acgist.snail.system.config.UtpConfig;
import com.acgist.snail.system.exception.NetException;

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
	public synchronized ByteBuffer put(final short seqnr, final ByteBuffer buffer) throws NetException {
		storage(seqnr, buffer);
		byte[] tmpBytes;
		ByteBuffer tmpBuffer;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		while(true) {
			tmpBuffer = take(this.nextSeqnr);
			if(tmpBuffer == null) {
				break;
			} else {
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
	
	private void storage(final short seqnr, final ByteBuffer buffer) {
		this.map.put(seqnr, buffer);
	}
	
}
