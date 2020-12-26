package com.acgist.snail.net.torrent.utp;

import java.nio.ByteBuffer;

import com.acgist.snail.utils.DateUtils;

/**
 * <p>UTP窗口数据</p>
 * 
 * @author acgist
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
	
	/**
	 * @param seqnr seqnr
	 * @param timestamp 时间戳
	 * @param data 负载数据
	 */
	private UtpWindowData(short seqnr, int timestamp, byte[] data) {
		this.pushTimes = 0;
		this.seqnr = seqnr;
		if(data == null) {
			data = new byte[0];
		}
		this.data = data;
		this.length = data.length;
	}
	
	/**
	 * <p>创建窗口数据</p>
	 * 
	 * @param seqnr seqnr
	 * @param timestamp 时间戳
	 * @param data 负载数据
	 * 
	 * @return 窗口数据
	 */
	public static final UtpWindowData newInstance(short seqnr, int timestamp, byte[] data) {
		return new UtpWindowData(seqnr, timestamp, data);
	}

	/**
	 * <p>获取seqnr</p>
	 * 
	 * @return seqnr
	 */
	public short getSeqnr() {
		return this.seqnr;
	}

	/**
	 * <p>获取时间戳</p>
	 * 
	 * @return 时间戳
	 */
	public int getTimestamp() {
		return this.timestamp;
	}

	/**
	 * <p>获取发送次数</p>
	 * 
	 * @return 发送次数
	 */
	public byte getPushTimes() {
		return this.pushTimes;
	}

	/**
	 * <p>获取负载数据</p>
	 * 
	 * @return 负载数据
	 */
	public byte[] getData() {
		return this.data;
	}

	/**
	 * <p>获取数据长度</p>
	 * 
	 * @return 数据长度
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * <p>将数据转为buffer数据</p>
	 * 
	 * @return buffer数据
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
