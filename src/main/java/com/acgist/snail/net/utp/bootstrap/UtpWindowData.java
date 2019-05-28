package com.acgist.snail.net.utp.bootstrap;

import java.nio.ByteBuffer;

/**
 * UTP窗口数据
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpWindowData {

	private final short seqnr; // seqnr
	private int timestamp; // 时间戳（毫秒），重新发送时修改时间戳。
	private final byte[] data; // 数据
	private final int length; // 数据长度
	
	private UtpWindowData(short seqnr, int timestamp, byte[] data) {
		this.seqnr = seqnr;
		this.timestamp = timestamp;
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

	public void updateTimestamp() {
		this.timestamp = UtpWindowHandler.timestamp();
	}

}
