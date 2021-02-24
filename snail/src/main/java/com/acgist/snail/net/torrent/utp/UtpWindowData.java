package com.acgist.snail.net.torrent.utp;

import com.acgist.snail.config.UtpConfig;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.DateUtils;

/**
 * <p>UTP窗口数据</p>
 * 
 * @author acgist
 */
public final class UtpWindowData {

	/**
	 * <p>请求编号</p>
	 */
	private final short seqnr;
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
	 * <p>时间戳（微秒）</p>
	 */
	private volatile int timestamp;
	/**
	 * <p>发送次数</p>
	 */
	private volatile byte pushTimes;
	
	/**
	 * @param seqnr 请求编号
	 * @param timestamp 时间戳（微秒）
	 * @param data 负载数据
	 */
	private UtpWindowData(final short seqnr, final int timestamp, final byte[] data) {
		this.seqnr = seqnr;
		if(data == null) {
			this.data = new byte[0];
		} else {
			this.data = data;
		}
		this.length = this.data.length;
		this.timestamp = timestamp;
		this.pushTimes = 0;
	}
	
	/**
	 * <p>创建窗口数据</p>
	 * 
	 * @param seqnr 请求编号
	 * @param timestamp 时间戳（微秒）
	 * @param data 负载数据
	 * 
	 * @return {@link UtpWindowData}
	 */
	public static final UtpWindowData newInstance(final short seqnr, final int timestamp, final byte[] data) {
		return new UtpWindowData(seqnr, timestamp, data);
	}

	/**
	 * <p>获取请求编号</p>
	 * 
	 * @return 请求编号
	 */
	public short getSeqnr() {
		return this.seqnr;
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
	 * <p>获取时间戳（微秒）</p>
	 * 
	 * @return 时间戳（微秒）
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
	 * <p>判断是否废弃</p>
	 * 
	 * @return 是否废弃
	 */
	public boolean discard() {
		return this.pushTimes > UtpConfig.MAX_PUSH_TIMES;
	}
	
	/**
	 * <p>更新数据并返回时间戳</p>
	 * 
	 * @return 时间戳（微秒）
	 */
	public int updateGetTimestamp() {
		this.pushTimes++;
		this.timestamp = DateUtils.timestampUs();
		return this.timestamp;
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.seqnr, this.pushTimes);
	}

}
