package com.acgist.snail.net.torrent.utp.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.UtpConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.DateUtils;

/**
 * UTP滑块窗口
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpWindow {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UtpWindow.class);
	
	/**
	 * 最大超时时间（微秒）
	 */
	private static final int MAX_TIMEOUT = 500 * 1000;
	
	private int rtt;
	private int rttVar;
	private int timeout;
	/**
	 * 客户端窗口大小
	 */
	private int wndSize;
	/**
	 * 最大窗口大小（客户端最大缓存）
	 */
	private int maxWndSize;
	/**
	 * 最后一个接收/发送的seqnr或者下一个seqnr
	 */
	private short seqnr;
	/**
	 * 最后一个接收/发送的timestamp
	 */
	private int timestamp;
	/**
	 * 窗口数据
	 */
	private final Map<Short, UtpWindowData> wndMap;
	
	private UtpWindow() {
		this.rtt = 0;
		this.rttVar = 0;
		this.timeout = MAX_TIMEOUT;
		this.wndSize = 0;
		this.seqnr = 0;
		this.timestamp = 0;
		this.wndMap = new LinkedHashMap<>();
	}
	
	public static final UtpWindow newInstance() {
		return new UtpWindow();
	}
	
	/**
	 * 设置连接信息
	 */
	public void connect(int timestamp, short seqnr) {
		this.seqnr = seqnr;
		this.timestamp = timestamp;
	}

	/**
	 * 发送窗口获取剩余窗口缓存大小
	 */
	public int remainWndSize() {
		synchronized (this) {
			return UtpConfig.WND_SIZE - this.wndSize;
		}
	}
	
	/**
	 * 发送窗口获取客户端窗口是否限制：
	 * 客户端窗口大小剩余最大时1/4。
	 */
	public boolean wndSizeControl() {
		synchronized (this) {
			return this.wndSize < (this.maxWndSize / 4);
		}
	}
	
	/**
	 * 发送数据：没有负载
	 */
	public UtpWindowData build() {
		synchronized (this) {
			return build(null);
		}
	}
	
	/**
	 * 发送数据：递增seqnr。
	 */
	public UtpWindowData build(byte[] data) {
		synchronized (this) {
			this.timestamp = DateUtils.timestampUs();
			final UtpWindowData windowData = storage(this.timestamp, this.seqnr, data);
			this.seqnr++;
			return windowData;
		}
	}

	/**
	 * 发送窗口获取超时的数据包（丢包）。
	 */
	public List<UtpWindowData> timeoutWindowData() {
		synchronized (this) {
			final int timestamp = DateUtils.timestampUs();
			final int timeout = this.timeout;
			return this.wndMap.entrySet().stream()
				.map(entry -> entry.getValue())
				.filter(windowData -> {
					return timestamp - windowData.getTimestamp() > timeout;
				})
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * 响应，移除发送数据并更新超时时间。
	 */
	public void ack(short acknr, int wndSize) {
		synchronized (this) {
			this.wndSize = wndSize;
			this.maxWndSize = Math.max(this.maxWndSize, wndSize);
			short diff;
			Map.Entry<Short, UtpWindowData> entry;
			final int timestamp = DateUtils.timestampUs();
			final var iterator = this.wndMap.entrySet().iterator();
			while(iterator.hasNext()) {
				entry = iterator.next();
				diff = (short) (acknr - entry.getKey()); // 移除序号小于等于当前响应序号的数据
				if(diff >= 0) {
					iterator.remove(); // 删除数据
					take(entry.getValue()); // 计算wndSize
					timeout(timestamp - entry.getValue().getTimestamp()); // 计算超时
				}
			}
		}
	}
	
	/**
	 * 丢弃超时数据。
	 */
	public void discard(short seqnr) {
		synchronized (this) {
			this.take(seqnr);
		}
	}
	
	/**
	 * 接收数据
	 * 如果数据已经被处理返回null。
	 * 如果seqnr不是下一个数据时，放入缓存。
	 * 如果seqnr是下一个数据时，继续获取直到找不到下一个seqnr为止，然后合并返回。更新当前接收的seqnr。
	 */
	public UtpWindowData receive(int timestamp, short seqnr, ByteBuffer buffer) throws NetException {
		synchronized (this) {
			final short diff = (short) (this.seqnr - seqnr);
			if(diff >= 0) { // 该seqnr已被处理
				return null;
			}
			storage(timestamp, seqnr, buffer);
			UtpWindowData nextWindowData;
			short nextSeqnr = this.seqnr;
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			while(true) {
				nextSeqnr = (short) (nextSeqnr + 1); // 下一个seqnr
				nextWindowData = take(nextSeqnr);
				if(nextWindowData == null) {
					break;
				} else {
					this.seqnr = nextWindowData.getSeqnr();
					this.timestamp = nextWindowData.getTimestamp();
					try {
						output.write(nextWindowData.getData());
					} catch (IOException e) {
						throw new NetException("UTP消息处理失败", e);
					}
				}
			}
			final byte[] bytes = output.toByteArray();
			if(bytes.length == 0) {
				return null;
			}
			return UtpWindowData.newInstance(this.seqnr, this.timestamp, bytes);
		}
	}
	
	/**
	 * 取出窗口数据，更新窗口被占用窗口大小。
	 */
	private UtpWindowData take(short seqnr) {
		final UtpWindowData windowData = this.wndMap.remove(seqnr);
		return take(windowData);
	}
	
	/**
	 * 取出窗口数据，更新窗口被占用窗口大小。
	 */
	private UtpWindowData take(UtpWindowData windowData) {
		if(windowData == null) {
			return windowData;
		}
		this.wndSize = this.wndSize - windowData.getLength();
		return windowData;
	}
	
	/**
	 * 存入窗口数据
	 */
	private UtpWindowData storage(final int timestamp, final short seqnr, final ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return storage(timestamp, seqnr, bytes);
	}
	
	/**
	 * 传入窗口数据
	 */
	private UtpWindowData storage(final int timestamp, final short seqnr, byte[] bytes) {
		final UtpWindowData windowData = UtpWindowData.newInstance(seqnr, timestamp, bytes);
		this.wndMap.put(seqnr, windowData);
		this.wndSize = this.wndSize + windowData.getLength();
		return windowData;
	}
	
	/**
	 * 计算超时时间
	 */
	private void timeout(int packetRtt) {
		int delta = this.rtt - packetRtt;
		this.rttVar += (Math.abs(delta) - this.rttVar) / 4;
		this.rtt += (packetRtt - this.rtt) / 8;
		this.timeout = Math.max(this.rtt + this.rttVar * 4, MAX_TIMEOUT);
		LOGGER.debug("UTP超时时间：{}", this.timeout);
	}

	public short seqnr() {
		return this.seqnr;
	}
	
	public int timestamp() {
		return this.timestamp;
	}
	
}
