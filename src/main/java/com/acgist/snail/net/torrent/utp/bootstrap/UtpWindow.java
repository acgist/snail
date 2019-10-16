package com.acgist.snail.net.torrent.utp.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.UtpConfig;
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
	 * 默认最大超时时间（微秒）
	 */
	private static final int MAX_TIMEOUT = 500 * 1000;
	/**
	 * 默认慢开始wnd数量
	 */
	private static final int DEFAULT_SLOW_WND = 16;
//	/**
//	 * 默认拥堵算法wnd数量
//	 */
//	private static final int DEFAULT_LIMIT_WND = 64;
	
	//================流量控制、阻塞控制================//
//	private volatile int nowWnd = 0;
//	private volatile int slowWnd = DEFAULT_SLOW_WND;
//	private volatile int limitWnd = DEFAULT_LIMIT_WND;
	
	//================超时================//
	private volatile int rtt;
	private volatile int rttVar;
	private volatile int timeout;
	
	/**
	 * 是否关闭
	 */
	private volatile boolean close = false;
	/**
	 * <dl>
	 * 	<dt>窗口大小</dt>
	 * 	<dd>接收端：发送端剩余大小</dd>
	 * 	<dd>发送端：发送端缓存大小</dd>
	 * </dl>
	 */
	private volatile int wndSize;
	/**
	 * <dl>
	 * 	<dt>最大窗口大小</dt>
	 * 	<dd>接收端：发送端最大窗口大小</dd>
	 * 	<dd>发送端：接收端最大窗口大小</dd>
	 * </dl>
	 */
	private volatile int maxWndSize;
	/**
	 * <dl>
	 * 	<dt>seqnr</dt>
	 * 	<dd>接收端：最后处理的seqnr</dd>
	 * 	<dd>发送端：最后发送的seqnr</dd>
	 * </dl>
	 */
	private volatile short seqnr;
	/**
	 * <dl>
	 * 	<dt>timestamp</dt>
	 * 	<dd>接收端：最后收到响应的时间</dd>
	 * 	<dd>发送端：最后发送数据的时间</dd>
	 * </dl>
	 */
	private volatile int timestamp;
	/**
	 * <dl>
	 * 	<dt>窗口数据</dt>
	 * 	<dd>接收端：未处理的数据（不连贯的数据）</dd>
	 * 	<dd>发送端：未收到响应的数据</dd>
	 * </dl>
	 */
	private final Map<Short, UtpWindowData> wndMap;
	/**
	 * 发送窗口控制信号量
	 */
	private final Semaphore semaphore;
	
	private UtpWindow() {
		this.rtt = 0;
		this.rttVar = 0;
		this.timeout = MAX_TIMEOUT;
		this.wndSize = 0;
		this.seqnr = 1;
		this.timestamp = 0;
		this.wndMap = new LinkedHashMap<>();
		this.semaphore = new Semaphore(DEFAULT_SLOW_WND);
	}
	
	public static final UtpWindow newInstance() {
		return new UtpWindow();
	}
	
	/**
	 * <p>设置连接信息</p>
	 * <p>接收端的seqnr可以设置为随机值，默认设置和发送端一样。</p>
	 */
	public void connect(int timestamp, short seqnr) {
		this.seqnr = seqnr;
		this.timestamp = timestamp;
	}

	/**
	 * 接收窗口获取剩余窗口缓存大小
	 */
	public int remainWndSize() {
		synchronized (this) {
			return UtpConfig.WND_SIZE - this.wndSize;
		}
	}
	
	/**
	 * 发送数据：没有负载
	 */
	public UtpWindowData build() {
		return build(null);
	}
	
	/**
	 * 发送数据：递增seqnr
	 */
	public UtpWindowData build(byte[] data) {
		this.acquire(); // 不能加锁
		synchronized (this) {
			this.timestamp = DateUtils.timestampUs();
			final UtpWindowData windowData = storage(this.timestamp, this.seqnr, data);
			this.seqnr++;
			return windowData;
		}
	}

	/**
	 * 发送窗口获取超时的数据包（丢包）
	 */
	public List<UtpWindowData> timeoutWindowData() {
		synchronized (this) {
			final int timestamp = DateUtils.timestampUs();
			final int timeout = this.timeout;
			return this.wndMap.values().stream()
				.filter(windowData -> {
					return timestamp - windowData.getTimestamp() > timeout;
				})
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * 响应：移除发送数据并更新超时时间
	 */
	public void ack(final short acknr, int wndSize) {
		synchronized (this) {
			this.wndSize = wndSize;
			this.maxWndSize = Math.max(this.maxWndSize, wndSize);
			final int timestamp = DateUtils.timestampUs();
			this.wndMap.entrySet().stream()
				.filter(entry -> {
					// 移除序号小于等于当前响应序号的数据
					final short diff = (short) (acknr - entry.getKey());
					return diff >= 0;
				})
				.peek(entry -> {
					timeout(timestamp - entry.getValue().getTimestamp()); // 计算超时
				})
				.map(Entry::getKey)
				.collect(Collectors.toList())
				.forEach(seqnr -> {
					this.release(); // 释放信号量
					this.take(seqnr); // 删除数据
				});
		}
	}
	
	/**
	 * 丢弃超时数据
	 */
	public void discard(short seqnr) {
		synchronized (this) {
			this.take(seqnr);
		}
	}
	
	/**
	 * <dl>
	 * 	<dt>接收数据</dt>
	 * 	<dd>如果seqnr数据已被处理，返回null。</dd>
	 * 	<dd>如果seqnr != 下一个序号时，放入缓存，返回null。</dd>
	 * 	<dd>如果seqnr == 下一个序号时，读取数据，更新seqnr，然后继续获取seqnr直到seqnr != 下一个序号为止，最后合并返回。</dd>
	 * </dl>
	 */
	public UtpWindowData receive(int timestamp, short seqnr, ByteBuffer buffer) throws IOException {
		synchronized (this) {
			final short diff = (short) (this.seqnr - seqnr);
			if(diff >= 0) { // seqnr已被处理
				// TODO：已被处理再次响应
				return null;
			}
			storage(timestamp, seqnr, buffer); // 先保存数据
			UtpWindowData nextWindowData;
			short nextSeqnr = this.seqnr;
			final var output = new ByteArrayOutputStream();
			while(true) {
				nextSeqnr = (short) (nextSeqnr + 1); // 下一个seqnr
				nextWindowData = take(nextSeqnr);
				if(nextWindowData == null) {
					break;
				} else {
					this.seqnr = nextWindowData.getSeqnr();
					this.timestamp = nextWindowData.getTimestamp();
					output.write(nextWindowData.getData());
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
	 * 取出窗口数据：更新窗口被占用大小
	 */
	private UtpWindowData take(short seqnr) {
		final UtpWindowData windowData = this.wndMap.remove(seqnr);
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
	 * 存入窗口数据
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
	
//	/**
//	 * <dl>
//	 * 	<dt>流量控制和阻塞控制</dt>
//	 * 	<dd>慢开始：wnd * 2</dd>
//	 * 	<dd>拥堵算法：wnd + 1</dd>
//	 * 	<dd>出现超时（丢包）：wnd / 2</dd>
//	 * </dl>
//	 * 
//	 * TODO：实时计算窗口大小（信号量）
//	 */
//	private void sendWindowLimit() {
//		if(wndSizeLimit()) { // 客户端缓存即将耗尽
//			// TODO：
//		}
//		this.nowWnd++;
//		final boolean loss = timeoutRetry(); // 出现丢包
//		if(loss) {
//			this.nowWnd = 0;
//			this.slowWnd = this.slowWnd / 2;
//			if(this.slowWnd < DEFAULT_SLOW_WND) {
//				this.slowWnd = DEFAULT_SLOW_WND;
//			}
//			if(this.limitWnd < this.slowWnd) {
//				this.limitWnd = this.slowWnd;
//			}
//			LOGGER.debug("UTP阻塞控制：{}-{}-{}", this.nowWnd, this.slowWnd, this.limitWnd);
//		} else if (this.nowWnd > this.slowWnd) {
//			this.nowWnd = 0;
//			if(this.slowWnd >= this.limitWnd) {
//				this.slowWnd++;
//				LOGGER.debug("UTP拥堵算法：{}-{}-{}", this.nowWnd, this.slowWnd, this.limitWnd);
//			} else {
//				this.slowWnd = this.slowWnd * 2;
//				LOGGER.debug("UTP慢开始：{}-{}-{}", this.nowWnd, this.slowWnd, this.limitWnd);
//			}
//		}
//	}
//	
//	/**
//	 * 发送窗口获取客户端窗口是否限制：客户端窗口大小剩余最大时1/4
//	 */
//	private boolean wndSizeLimit() {
//		return this.wndSize < (this.maxWndSize / 4);
//	}
	
	/**
	 * <p>获取信号量</p>
	 * <p>如果已经关闭了，不需要获取信号量。</p>
	 */
	private void acquire() {
		if(this.close) {
			return;
		}
		try {
			LOGGER.debug("信号量（获取）：{}", this.semaphore.availablePermits());
			this.semaphore.acquire();
		} catch (InterruptedException e) {
			LOGGER.debug("信号量获取异常", e);
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * 释放信号量
	 */
	public void release() {
		LOGGER.debug("信号量（释放）：{}", this.semaphore.availablePermits());
		this.semaphore.release();
	}
	
	/**
	 * <p>关闭窗口</p>
	 * <p>标记关闭，同时释放一个信号量。</p>
	 */
	public void close() {
		this.close = true;
		this.release();
	}
	
	public short seqnr() {
		return this.seqnr;
	}
	
	public int timestamp() {
		return this.timestamp;
	}
	
}
