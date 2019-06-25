package com.acgist.snail.net.torrent.utp.bootstrap;

import java.nio.ByteBuffer;

import com.acgist.snail.utils.DateUtils;

/**
 * UTP窗口数据
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpWindowData {

	private final short seqnr; // seqnr
	private int timestamp; // 时间戳（微秒），发送时重新修改。
	private byte pushTimes; // 发送次数
	private final byte[] data; // 数据
	private final int length; // 数据长度
	
	private UtpWindowData(short seqnr, int timestamp, byte[] data) {
		this.pushTimes = 0;
		this.seqnr = seqnr;
		this.data = data;
		if(data == null) {
			this.length = 0;
		} else {
			this.length = data.length;
		}
	}
	
	public static final UtpWindowData newInstance(short seqnr, int timestamp, byte[] data) {
		return new UtpWindowData(seqnr, timestamp, data);
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

	public ByteBuffer buffer() {
		return ByteBuffer.wrap(this.data);
	}

	public byte pushTimes() {
		return this.pushTimes;
	}
	
	/**
	 * 发送时更新数据：时间戳、发送次数
	 * 
	 * @return 时间戳
	 */
	public int pushUpdateGetTimestamp() {
		this.pushTimes++;
		this.timestamp = DateUtils.timestampUs();
		return this.timestamp;
	}

}
