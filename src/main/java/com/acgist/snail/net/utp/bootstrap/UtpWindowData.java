package com.acgist.snail.net.utp.bootstrap;

import java.nio.ByteBuffer;

/**
 * UTP窗口数据
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpWindowData {

	private final short seqnr;
	private final int timestamp;
	private final byte[] data;
	private final int length;

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

}
