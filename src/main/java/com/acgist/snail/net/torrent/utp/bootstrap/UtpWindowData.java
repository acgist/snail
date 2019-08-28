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
	private final byte[] data; // 负载数据：握手时没有负载数据
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

	public byte getPushTimes() {
		return pushTimes;
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
	
	/**
	 * 验证数据是否正确，正确的数据才能发送。
	 * 握手失败，导致超时定时任务执行时负载数据为空，导致发送时空指针。
	 */
	public boolean verify() {
		return this.data != null;
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
