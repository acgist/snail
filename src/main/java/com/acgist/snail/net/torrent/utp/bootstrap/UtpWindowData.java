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
	 * seqnr
	 */
	private final short seqnr;
	/**
	 * 时间戳（微秒）：发送时重新修改
	 */
	private volatile int timestamp;
	/**
	 * 发送次数
	 */
	private volatile byte pushTimes;
	/**
	 * 负载数据：握手时没有负载数据
	 */
	private final byte[] data;
	/**
	 * 数据长度
	 */
	private final int length;
	
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

	public ByteBuffer buffer() {
		return ByteBuffer.wrap(this.data).compact();
	}
	
	/**
	 * <p>验证是否含有数据</p>
	 * <p>握手失败：超时定时任务执行时，握手消息负载数据为空，导致发送时空指针。</p>
	 */
	public boolean haveData() {
		return this.data != null;
	}
	
	/**
	 * <p>发送时更新数据：时间戳、发送次数</p>
	 * 
	 * @return 时间戳
	 */
	public int pushUpdateGetTimestamp() {
		this.pushTimes++;
		this.timestamp = DateUtils.timestampUs();
		return this.timestamp;
	}

}
