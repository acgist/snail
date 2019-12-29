package com.acgist.snail.net.torrent.utp.bootstrap;

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

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.system.config.UtpConfig;
import com.acgist.snail.utils.DateUtils;

/**
 * <p>UTP滑块窗口</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class UtpWindow {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UtpWindow.class);

	/**
	 * <p>默认最大超时时间（微秒）：{@value}</p>
	 */
	private static final int MAX_TIMEOUT = 500 * 1000;
	/**
	 * <p>最小窗口大小：{@value}</p>
	 */
	private static final int MIN_WND_SIZE = 16;
	/**
	 * <p>最大窗口大小：{@value}</p>
	 */
	private static final int MAX_WND_SIZE = 64;
	/**
	 * <p>获取信号量超时时间（秒）：{@value}</p>
	 * <p>防止长时间获取不到信号量导致线程阻塞</p>
	 */
	private static final int SEMAPHORE_TIMEOUT = 2;
	
	//================流量控制、阻塞控制================//
	
	/**
	 * <p>当前窗口大小</p>
	 */
	private volatile int wnd = MIN_WND_SIZE;
	
	//================超时================//
	
	private volatile int rtt;
	private volatile int rttVar;
	private volatile int timeout;
	
	/**
	 * <p>是否关闭</p>
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
	 * 	<dd>接收端：未处理的数据</dd>
	 * 	<dd>发送端：未响应的数据</dd>
	 * </dl>
	 * <p>数据可能是不连贯的</p>
	 */
	private final Map<Short, UtpWindowData> wndMap;
	/**
	 * <p>发送窗口控制信号量</p>
	 */
	private final Semaphore semaphore;
	/**
	 * <p>UTP窗口请求队列</p>
	 */
	private final BlockingQueue<UtpRequest> requests;
	/**
	 * <p>消息处理器</p>
	 */
	private final IMessageCodec<ByteBuffer> messageCodec;
	
	/**
	 * @see {@link #UtpWindow(IMessageCodec)}
	 */
	private UtpWindow() {
		this(null);
	}
	
	/**
	 * <p>创建窗口对象</p>
	 * <p>如果消息处理器等于{@code null}时不创建请求队列</p>
	 * 
	 * @param messageCodec 消息处理器
	 */
	private UtpWindow(IMessageCodec<ByteBuffer> messageCodec) {
		this.rtt = 0;
		this.rttVar = 0;
		this.timeout = MAX_TIMEOUT;
		this.wndSize = 0;
		this.seqnr = 1;
		this.timestamp = 0;
		this.wndMap = new LinkedHashMap<>();
		this.semaphore = new Semaphore(MIN_WND_SIZE);
		if(messageCodec == null) {
			this.requests = null;
			this.messageCodec = null;
		} else {
			// 同一个窗口必须将消息发送到同一个请求队列防止消息出现乱序
			this.requests = UtpRequestQueue.getInstance().queue();
			this.messageCodec = messageCodec;
		}
	}
	
	/**
	 * <p>创建发送窗口对象</p>
	 * <p>发送窗口不接收和处理请求，不创建请求队列。</p>
	 * 
	 * @return 窗口对象
	 */
	public static final UtpWindow newSendInstance() {
		return new UtpWindow();
	}
	
	/**
	 * <p>创建接收窗口对象</p>
	 * <p>接收窗口接收和处理请求，创建请求队列。</p>
	 * 
	 * @return 窗口对象
	 */
	public static final UtpWindow newRecvInstance(IMessageCodec<ByteBuffer> messageCodec) {
		return new UtpWindow(messageCodec);
	}
	
	/**
	 * <p>设置连接信息</p>
	 * <p>接收端的seqnr可以设置为随机值：默认设置和发送端一样</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 */
	public void connect(int timestamp, short seqnr) {
		this.seqnr = seqnr;
		this.timestamp = timestamp;
	}

	/**
	 * <p>获取剩余窗口缓存大小</p>
	 * 
	 * @return 剩余窗口缓存大小
	 */
	public int remainWndSize() {
		synchronized (this) {
			return UtpConfig.WND_SIZE - this.wndSize;
		}
	}
	
	/**
	 * <p>发送数据</p>
	 * <p>没有负载</p>
	 * 
	 * @see {@link #build(byte[])}
	 */
	public UtpWindowData build() {
		return build(null);
	}
	
	/**
	 * <p>发送数据</p>
	 * <p>递增seqnr</p>
	 * 
	 * @param data 数据
	 * 
	 * @return 窗口数据
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
	 * <p>获取超时的数据包</p>
	 * 
	 * @return 超时的数据包
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
	 * <p>处理响应</p>
	 * <p>移除已经响应数据并更新超时时间</p>
	 * <p>如果响应编号没有处理说明没有丢包，如果响应编号已经处理说明可能发生丢包。</p>
	 * 
	 * @param acknr 响应编号：最后处理编号
	 * @param wndSize 剩余窗口大小
	 * 
	 * @return 是否丢包：{@code true}-丢包；{@code false}-没有丢包；
	 */
	public boolean ack(final short acknr, int wndSize) {
		synchronized (this) {
			this.wndSize = wndSize;
			final int timestamp = DateUtils.timestampUs();
			final var ackList = this.wndMap.entrySet().stream()
				.filter(entry -> {
					// 移除编号小于等于当前响应编号的数据
					final short diff = (short) (acknr - entry.getKey());
					return diff >= 0;
				})
				.peek(entry -> {
					timeout(timestamp - entry.getValue().getTimestamp()); // 计算超时时间
				})
				.map(Entry::getKey)
				.collect(Collectors.toList());
			if(ackList.isEmpty()) {
				return true;
			} else {
				ackList.forEach(seqnr -> {
					this.release(); // 释放信号量
					this.take(seqnr); // 删除数据
				});
				this.wnd();
				return false;
			}
		}
	}
	
	/**
	 * <dl>
	 * 	<dt>接收数据</dt>
	 * 	<dd>如果seqnr != 下一个编号：放入缓存</dd>
	 * 	<dd>如果seqnr == 下一个编号：放入缓存、读取数据、更新seqnr，然后继续获取seqnr直到seqnr != 下一个编号为止，最后合并消息并处理。</dd>
	 * </dl>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param buffer 请求数据
	 */
	public void receive(int timestamp, short seqnr, ByteBuffer buffer) throws IOException {
		synchronized (this) {
			final short diff = (short) (this.seqnr - seqnr);
			if(diff >= 0) { // seqnr已被处理
				return;
			}
			storage(timestamp, seqnr, buffer); // 先保存数据
			UtpWindowData nextWindowData;
			short nextSeqnr = this.seqnr;
			final var output = new ByteArrayOutputStream();
			while(true) {
				nextSeqnr = (short) (nextSeqnr + 1); // 下一个请求编号
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
				return;
			}
			LOGGER.debug("处理数据消息：{}", this.seqnr);
			// 同步处理
//			this.messageCodec.decode(windowData.buffer());
			// 异步处理
			if(!this.requests.offer(UtpRequest.newInstance(ByteBuffer.wrap(bytes).compact(), this.messageCodec))) {
				LOGGER.warn("UTP请求插入请求队列失败：{}", this.seqnr);
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
	 * <p>取出窗口数据并更新窗口大小</p>
	 * 
	 * @param seqnr 请求编号
	 * 
	 * @return 窗口数据
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
	 * <p>存入窗口数据</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param buffer 请求数据
	 * 
	 * @return 窗口数据
	 */
	private UtpWindowData storage(final int timestamp, final short seqnr, final ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return storage(timestamp, seqnr, bytes);
	}
	
	/**
	 * <p>存入窗口数据</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param bytes 请求数据
	 * 
	 * @return 窗口数据
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
	 * @param packetRtt 时间差
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
	 * <p>超时时间等于默认超时时间：窗口{@code +1}</p>
	 * <p>超时时间大于默认超时时间：窗口{@code /2}</p>
	 */
	private void wnd() {
		int wnd = this.wnd;
		if(this.timeout <= MAX_TIMEOUT) {
			if(wnd < MAX_WND_SIZE) {
				wnd++;
				this.release();
			}
		} else {
			wnd = wnd / 2;
			if(wnd < MIN_WND_SIZE) {
				this.wnd = MIN_WND_SIZE;
			}
		}
		this.wnd = wnd;
		LOGGER.debug("UTP窗口大小：{}", this.wnd);
	}
	
	/**
	 * <p>获取信号量</p>
	 * <p>如果窗口已经关闭：不需要获取信号量</p>
	 */
	private void acquire() {
		if(this.close) {
			return;
		}
		try {
			LOGGER.debug("信号量获取：{}", this.semaphore.availablePermits());
//			this.semaphore.acquire();
			final boolean ok = this.semaphore.tryAcquire(SEMAPHORE_TIMEOUT, TimeUnit.SECONDS);
			if(!ok) {
				LOGGER.debug("信号量获取失败");
			}
		} catch (InterruptedException e) {
			LOGGER.debug("信号量获取异常", e);
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * <p>释放信号量</p>
	 */
	public void release() {
		final int available = this.semaphore.availablePermits();
		LOGGER.debug("信号量释放：{}", available);
		if(available < this.wnd) {
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
	
	public short seqnr() {
		return this.seqnr;
	}
	
	public int timestamp() {
		return this.timestamp;
	}
	
}
