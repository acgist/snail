package com.acgist.snail.net.torrent.utp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.config.UtpConfig;
import com.acgist.snail.net.codec.IMessageDecoder;
import com.acgist.snail.utils.ByteUtils;
import com.acgist.snail.utils.DateUtils;

/**
 * <p>UTP窗口</p>
 * 
 * @author acgist
 */
public final class UtpWindow {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UtpWindow.class);

	/**
	 * <p>最小窗口大小：{@value}</p>
	 */
	private static final int MIN_WND_SIZE = 16;
	/**
	 * <p>最大窗口大小：{@value}</p>
	 */
	private static final int MAX_WND_SIZE = 64;
	/**
	 * <p>默认最大超时时间（微秒）：{@value}</p>
	 */
	private static final int MAX_TIMEOUT = 500 * SystemConfig.DATE_SCALE;
	/**
	 * <p>获取信号量超时时间（秒）：{@value}</p>
	 * <p>防止长时间阻塞</p>
	 */
	private static final int SEMAPHORE_TIMEOUT = 2;
	
	/**
	 * <p>当前窗口大小</p>
	 */
	private volatile int wnd;
	/**
	 * <p>往返时间</p>
	 */
	private volatile int rtt;
	/**
	 * <p>RTT平均偏差样本</p>
	 */
	private volatile int rttVar;
	/**
	 * <p>超时时间</p>
	 */
	private volatile int timeout;
	/**
	 * <p>是否关闭</p>
	 */
	private volatile boolean close;
	/**
	 * <dl>
	 * 	<dt>seqnr</dt>
	 * 	<dd>接收端：最后处理的seqnr</dd>
	 * 	<dd>发送端：最后发送的seqnr</dd>
	 * </dl>
	 * <p>固定值：1</p>
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
	 * 	<dt>窗口大小</dt>
	 * 	<dd>接收端：发送端剩余大小</dd>
	 * 	<dd>发送端：发送端缓存大小</dd>
	 * </dl>
	 */
	private volatile int wndSize;
	/**
	 * <dl>
	 * 	<dt>窗口数据</dt>
	 * 	<dd>接收端：未处理的数据</dd>
	 * 	<dd>发送端：未响应的数据</dd>
	 * </dl>
	 * <p>数据可能是乱序的</p>
	 */
	private final Map<Short, UtpWindowData> wndMap;
	/**
	 * <p>窗口信号量</p>
	 */
	private final Semaphore semaphore;
	/**
	 * <p>窗口请求队列</p>
	 */
	private final BlockingQueue<UtpRequest> requests;
	/**
	 * <p>窗口消息处理器</p>
	 */
	private final IMessageDecoder<ByteBuffer> messageDecoder;
	
	/**
	 * @see #UtpWindow(IMessageDecoder)
	 */
	private UtpWindow() {
		this(null);
	}
	
	/**
	 * @param messageDecoder 消息处理器
	 */
	private UtpWindow(IMessageDecoder<ByteBuffer> messageDecoder) {
		this.wnd = MIN_WND_SIZE;
		this.rtt = 0;
		this.rttVar = 0;
		this.timeout = MAX_TIMEOUT;
		this.close = false;
		this.seqnr = 1;
		this.timestamp = 0;
		this.wndSize = 0;
		this.wndMap = new LinkedHashMap<>();
		if(messageDecoder == null) {
			// 发送窗口对象
			this.requests = null;
			this.messageDecoder = null;
			this.semaphore = new Semaphore(MIN_WND_SIZE);
		} else {
			// 接收窗口对象
			// 相同窗口必须将消息发送到相同请求队列：防止消息出现乱序
			this.requests = UtpRequestQueue.getInstance().queue();
			this.messageDecoder = messageDecoder;
			this.semaphore = null;
		}
	}
	
	/**
	 * <p>新建发送窗口</p>
	 * 
	 * @return {@link UtpWindow}
	 */
	public static final UtpWindow newSendInstance() {
		return new UtpWindow();
	}
	
	/**
	 * <p>新建接收窗口</p>
	 * 
	 * @param messageDecoder 窗口消息处理器
	 * 
	 * @return {@link UtpWindow}
	 */
	public static final UtpWindow newRecvInstance(IMessageDecoder<ByteBuffer> messageDecoder) {
		return new UtpWindow(messageDecoder);
	}
	
	/**
	 * <p>设置连接信息</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 */
	public void connect(final int timestamp, final short seqnr) {
		this.seqnr = seqnr;
		this.timestamp = timestamp;
	}

	/**
	 * <p>获取剩余窗口缓存大小</p>
	 * 
	 * @return 剩余窗口缓存大小
	 */
	public int wndSize() {
		synchronized (this) {
			return UtpConfig.WND_SIZE - this.wndSize;
		}
	}
	
	/**
	 * <p>发送数据</p>
	 * 
	 * @return {@link UtpWindowData}
	 * 
	 * @see #build(byte[])
	 */
	public UtpWindowData build() {
		return this.build(null);
	}
	
	/**
	 * <p>发送数据</p>
	 * 
	 * @param data 数据
	 * 
	 * @return {@link UtpWindowData}
	 */
	public UtpWindowData build(byte[] data) {
		// 不能加锁
		this.acquire();
		synchronized (this) {
			// 最后发送时间
			this.timestamp = DateUtils.timestampUs();
			final UtpWindowData windowData = this.storage(this.timestamp, this.seqnr, data);
			// 新建完成递增
			this.seqnr++;
			return windowData;
		}
	}

	/**
	 * <p>获取超时的数据包</p>
	 * 
	 * @return 超时的数据包
	 */
	public List<UtpWindowData> timeoutWindowData() {
		synchronized (this) {
			final int timeout = this.timeout;
			final int timestamp = DateUtils.timestampUs();
			return this.wndMap.values().stream()
				.filter(windowData -> timestamp - windowData.getTimestamp() > timeout)
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * <p>处理响应</p>
	 * <p>删除已经响应数据、更新超时时间</p>
	 * 
	 * @param acknr 响应编号：最后处理编号
	 * @param wndSize 剩余窗口大小
	 * 
	 * @return 是否丢包
	 */
	public boolean ack(final short acknr, final int wndSize) {
		synchronized (this) {
			this.wndSize = wndSize;
			// 响应编号已经处理说明可能丢包
			boolean loss = true;
			Entry<Short, UtpWindowData> entry;
			final int timestamp = DateUtils.timestampUs();
			final var iterator = this.wndMap.entrySet().iterator();
			while(iterator.hasNext()) {
				entry = iterator.next();
				// 编号是否已经处理
				final short diff = (short) (acknr - entry.getKey());
				if(diff >= 0) {
					// 响应编号没有处理说明没有丢包
					loss = false;
					this.timeout(timestamp - entry.getValue().getTimestamp());
					this.release();
					// 删除已经响应数据
					iterator.remove();
				}
			}
			if(!loss) {
				// 没有丢包计算窗口
				this.wndControl();
			}
			return loss;
		}
	}
	
	/**
	 * <p>接收数据</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param buffer 请求数据
	 * 
	 * @throws IOException IO异常
	 */
	public void receive(final int timestamp, final short seqnr, final ByteBuffer buffer) throws IOException {
		synchronized (this) {
			final short diff = (short) (this.seqnr - seqnr);
			if(diff >= 0) {
				// seqnr已被处理
				return;
			}
			// 优先保存数据
			this.storage(timestamp, seqnr, buffer);
			UtpWindowData nextWindowData;
			short nextSeqnr = this.seqnr;
			final var output = new ByteArrayOutputStream();
			while(true) {
				// 下一个请求编号
				nextSeqnr = (short) (nextSeqnr + 1);
				nextWindowData = this.take(nextSeqnr);
				if(nextWindowData == null) {
					break;
				} else {
					this.seqnr = nextWindowData.getSeqnr();
					// 最后接收时间
					this.timestamp = nextWindowData.getTimestamp();
					output.write(nextWindowData.getData());
				}
			}
			final byte[] bytes = output.toByteArray();
			if(bytes.length == 0) {
				return;
			}
			// 添加请求队列：异步处理请求
			if(this.requests.offer(UtpRequest.newInstance(ByteBuffer.wrap(bytes), this.messageDecoder))) {
				LOGGER.debug("处理UTP数据消息：{}-{}", seqnr, this.seqnr);
			} else {
				LOGGER.warn("处理UTP数据消息失败：{}-{}", seqnr, this.seqnr);
			}
		}
	}
	
	/**
	 * <p>获取最后一个未确认数据包</p>
	 * 
	 * @return 最后一个未确认数据包
	 */
	public UtpWindowData lastUnack() {
		synchronized (this) {
			return this.wndMap.get((short) (this.seqnr + 1));
		}
	}
	
	/**
	 * <p>丢弃超时数据</p>
	 * 
	 * @param seqnr 请求编号
	 */
	public void discard(short seqnr) {
		synchronized (this) {
			this.take(seqnr);
		}
	}
	
	/**
	 * <p>取出窗口数据</p>
	 * 
	 * @param seqnr 请求编号
	 * 
	 * @return {@link UtpWindowData}
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
	 * <p>保存窗口数据</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param buffer 请求数据
	 * 
	 * @return {@link UtpWindowData}
	 */
	private UtpWindowData storage(final int timestamp, final short seqnr, final ByteBuffer buffer) {
		final byte[] bytes = ByteUtils.remainingToBytes(buffer);
		return this.storage(timestamp, seqnr, bytes);
	}
	
	/**
	 * <p>保存窗口数据</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param bytes 请求数据
	 * 
	 * @return {@link UtpWindowData}
	 */
	private UtpWindowData storage(final int timestamp, final short seqnr, byte[] bytes) {
		final UtpWindowData windowData = UtpWindowData.newInstance(seqnr, timestamp, bytes);
		this.wndMap.put(seqnr, windowData);
		this.wndSize = this.wndSize + windowData.getLength();
		return windowData;
	}
	
	/**
	 * <p>计算超时时间</p>
	 * 
	 * @param packetRtt 数据往返时间
	 */
	private void timeout(final int packetRtt) {
		int rtt = this.rtt;
		int rttVar = this.rttVar;
		final int delta = rtt - packetRtt;
		rtt += (packetRtt - rtt) / 8;
		rttVar += (Math.abs(delta) - rttVar) / 4;
		this.rtt = rtt;
		this.rttVar = rttVar;
		this.timeout = Math.max(rtt + rttVar * 4, MAX_TIMEOUT);
		LOGGER.debug("UTP超时时间：{}", this.timeout);
	}
	
	/**
	 * <p>流量控制和阻塞控制</p>
	 * <p>超时时间等于默认超时时间：窗口大小 + 1</p>
	 * <p>超时时间大于默认超时时间：窗口大小 / 2</p>
	 */
	private void wndControl() {
		int wnd = this.wnd;
		if(this.timeout <= MAX_TIMEOUT) {
			if(wnd < MAX_WND_SIZE) {
				wnd++;
				this.release();
			}
		} else {
			wnd = wnd / 2;
			if(wnd < MIN_WND_SIZE) {
				wnd = MIN_WND_SIZE;
			}
		}
		this.wnd = wnd;
		LOGGER.debug("UTP窗口大小：{}", this.wnd);
	}
	
	/**
	 * <p>获取信号量</p>
	 */
	private void acquire() {
		if(this.close) {
			// 如果窗口已经关闭：不需要获取信号量
			return;
		}
		try {
			if(!this.semaphore.tryAcquire(SEMAPHORE_TIMEOUT, TimeUnit.SECONDS)) {
				LOGGER.debug("获取信号量失败：{}-{}", this.wnd, this.wndSize);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.debug("获取信号量异常", e);
		}
	}
	
	/**
	 * <p>释放信号量</p>
	 */
	private void release() {
		if(this.semaphore == null) {
			return;
		}
		final int available = this.semaphore.availablePermits();
		if(available < this.wnd) {
			LOGGER.debug("信号量释放：{}", available);
			this.semaphore.release();
		}
	}
	
	/**
	 * <p>关闭窗口</p>
	 * <p>标记关闭、释放信号量</p>
	 */
	public void close() {
		this.close = true;
		this.release();
	}
	
	/**
	 * <p>获取seqnr</p>
	 * 
	 * @return seqnr
	 */
	public short seqnr() {
		return this.seqnr;
	}
	
	/**
	 * <p>获取timestamp</p>
	 * 
	 * @return timestamp
	 */
	public int timestamp() {
		return this.timestamp;
	}
	
}
