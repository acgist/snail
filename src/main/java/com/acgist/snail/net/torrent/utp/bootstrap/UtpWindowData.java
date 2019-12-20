package com.acgist.snail.net.torrent.utp.bootstrap;

import java.nio.ByteBuffer;

import com.acgist.snail.utils.DateUtils;

/**
 * <p>UTP窗口数据</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class UtpWindowData {

	/**
	 * <p>seqnr</p>
	 */
	private final short seqnr;
	/**
	 * <p>时间戳（微秒）</p>
	 */
	private volatile int timestamp;
	/**
	 * <p>发送次数</p>
	 */
	private volatile byte pushTimes;
	/**
	 * <p>负载数据</p>
	 * <p>握手消息没有负载数据</p>
	 */
	private final byte[] data;
	/**
	 * <p>数据长度</p>
	 */
	private final int length;
	
	private UtpWindowData(short seqnr, int timestamp, byte[] data) {
		this.pushTimes = 0;
		this.seqnr = seqnr;
		if(data == null) {
			data = new byte[0];
		}
		this.data = data;
		this.length = data.length;
	}
	
	public static final UtpWindowData newInstance(short seqnr, int timestamp, byte[] data) {
		return new UtpWindowData(seqnr, timestamp, data);
	}

	public short getSeqnr() {
		return this.seqnr;
	}

	public int getTimestamp() {
		return this.timestamp;
	}

	public byte getPushTimes() {
		return this.pushTimes;
	}

	public byte[] getData() {
		return this.data;
	}

	public int getLength() {
		return this.length;
	}

	/**
	 * <p>将数据转为buffer数据</p>
	 * 
	 * @return buffer
	 */
	public ByteBuffer buffer() {
		return ByteBuffer.wrap(this.data).compact();
	}
	
	/**
	 * <p>更新数据并返回时间戳</p>
	 * <p>更新数据：时间戳、发送次数</p>
	 * 
	 * @return 时间戳
	 */
	public int pushUpdateGetTimestamp() {
		this.pushTimes++;
		this.timestamp = DateUtils.timestampUs();
		return this.timestamp;
	}

}
