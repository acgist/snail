package com.acgist.snail.net.torrent.utp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.PeerUnpackMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpService;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpWindow;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpWindowData;
import com.acgist.snail.system.config.UtpConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>uTorrent transport protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0029.html</p>
 * 流量控制：
 * 阻塞控制：
 * TODO：TCP UDP读取部分合并
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpMessageHandler extends UdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtpMessageHandler.class);
	
	/**
	 * 连接超时时间（秒）
	 */
	private static final int CONNECT_TIMEOUT = 2;
	/**
	 * 客户端阻塞控制等待（秒）
	 */
	private static final int WND_SIZE_CONTROL_TIMEOUT = 10;
	
	/**
	 * 是否连接
	 */
	private boolean connect;
	/**
	 * 接收连接ID
	 */
	private final short recvId;
	/**
	 * 发送连接ID
	 */
	private final short sendId;
	/**
	 * 连接锁
	 */
	private final AtomicBoolean connectLock = new AtomicBoolean(false);

	/**
	 * 默认慢开始wnd数量
	 */
	private static final int DEFAULT_SLOW_WND = 2;
	/**
	 * 默认拥堵算法wnd数量
	 */
	private static final int DEFAULT_LIMIT_WND = 64;
	/**
	 * 休眠时间
	 */
	private static final int DEFAULT_SLEEP_TIME = 10;
	
	private volatile int slowWnd = DEFAULT_SLOW_WND;
	private volatile int limitWnd = DEFAULT_LIMIT_WND;
	private volatile int nowWnd = 0;
	
	/**
	 * 发送窗口
	 */
	private final UtpWindow sendWindow;
	/**
	 * 接收窗口
	 */
	private final UtpWindow receiveWindow;
	
	private final UtpService utpService = UtpService.getInstance();
	
	private final PeerSubMessageHandler peerSubMessageHandler;
	private final PeerUnpackMessageHandler peerUnpackMessageHandler;
	
	/**
	 * 服务端
	 */
	public UtpMessageHandler(final short connectionId, InetSocketAddress socketAddress) {
		this.peerSubMessageHandler = PeerSubMessageHandler.newInstance();
		this.peerUnpackMessageHandler = PeerUnpackMessageHandler.newInstance(this.peerSubMessageHandler);
		this.peerSubMessageHandler.messageHandler(this);
		this.sendWindow = UtpWindow.newInstance();
		this.receiveWindow = UtpWindow.newInstance();
		this.socketAddress = socketAddress;
		this.sendId = connectionId;
		this.recvId = (short) (this.sendId + 1);
		this.utpService.put(this);
	}

	/**
	 * 客户端
	 */
	public UtpMessageHandler(PeerSubMessageHandler peerSubMessageHandler, InetSocketAddress socketAddress) {
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.peerUnpackMessageHandler = PeerUnpackMessageHandler.newInstance(this.peerSubMessageHandler);
		this.peerSubMessageHandler.messageHandler(this);
		this.sendWindow = UtpWindow.newInstance();
		this.receiveWindow = UtpWindow.newInstance();
		this.socketAddress = socketAddress;
		this.recvId = this.utpService.connectionId();
		this.sendId = (short) (this.recvId + 1);
		this.utpService.put(this);
	}
	
	/**
	 * key = socketAddress + connectionId
	 */
	public String key() {
		return this.utpService.buildKey(this.recvId, this.socketAddress);
	}
	
	@Override
	public void onMessage(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		if(this.socketAddress == null) {
			this.socketAddress = socketAddress;
		}
		buffer.flip();
		if(buffer.remaining() < 20) {
			throw new NetException("UTP信息格式错误");
		}
		final byte typeVersion = buffer.get();
		final byte type = (byte) (typeVersion >> 4);
		final byte extension = buffer.get();
		final short connectionId = buffer.getShort();
		final int timestamp = buffer.getInt();
		final int timestampDifference = buffer.getInt();
		final int wndSize = buffer.getInt();
		final short seqnr = buffer.getShort();
		final short acknr = buffer.getShort();
		if(extension != 0) { // 扩展数据
			final short extLength = buffer.getShort();
			if(extLength <= 0) {
				throw new NetException("UTP信息格式错误（扩展消息长度）：" + extLength);
			}
			final byte[] extData = new byte[extLength];
			buffer.get(extData);
		}
		LOGGER.debug(
			"UTP收到消息，类型：{}，扩展：{}，连接ID：{}，时间戳：{}，时间戳对比：{}，窗口大小：{}，请求号：{}，应答号：{}",
			type, extension, connectionId, timestamp, timestampDifference, wndSize, seqnr, acknr);
		switch (type) {
		case UtpConfig.ST_DATA:
			data(timestamp, seqnr, acknr, buffer);
			break;
		case UtpConfig.ST_FIN:
			fin(timestamp, seqnr, acknr);
			break;
		case UtpConfig.ST_STATE:
			state(timestamp, seqnr, acknr, wndSize);
			break;
		case UtpConfig.ST_RESET:
			reset(timestamp, seqnr, acknr);
			break;
		case UtpConfig.ST_SYN:
			syn(timestamp, seqnr, acknr);
			break;
		default:
			LOGGER.warn(
				"UTP不支持的消息类型，类型：{}，扩展：{}，连接ID：{}，时间戳：{}，时间戳对比：{}，窗口大小：{}，请求号：{}，应答号：{}",
				type, extension, connectionId, timestamp, timestampDifference, wndSize, seqnr, acknr);
			break;
		}
	}

	@Override
	public void send(ByteBuffer buffer) throws NetException {
		if(buffer.position() != 0) { //  重置标记
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("发送消息为空");
			return;
		}
		byte[] bytes;
		int remaining;
		while((remaining = buffer.remaining()) > 0) { // UDP拆包
			if(remaining > UtpConfig.MAX_PACKET_SIZE) {
				bytes = new byte[UtpConfig.MAX_PACKET_SIZE];
			} else {
				bytes = new byte[remaining];
			}
			buffer.get(bytes);
			final UtpWindowData windowData = this.sendWindow.send(bytes);
			this.data(windowData);
			wndControl();
		}
		wndSizeControl();
	}
	
	/**
	 * 流量控制和阻塞控制。
	 * 慢开始：发送数据包（wnd）2的指数增长。
	 * 拥堵算法：每次+1。
	 * 出现超时（丢包）时发送数据包/2。
	 */
	public void wndControl() {
		if(!(this.connect && available())) { // 如果没有连接成功或者连接不可用时不发送。
			return;
		}
		this.nowWnd++;
		final boolean loss = timeoutRetry(); // 出现丢包
		if(loss) {
			this.nowWnd = 0;
			this.slowWnd = this.slowWnd / 2;
			if(this.slowWnd < DEFAULT_SLOW_WND) {
				this.slowWnd = DEFAULT_SLOW_WND;
			}
			if(this.limitWnd < this.slowWnd) {
				this.limitWnd = this.slowWnd;
			}
			ThreadUtils.sleep(DEFAULT_SLEEP_TIME);
		} else if (++this.nowWnd > this.slowWnd) {
			this.nowWnd = 0;
			if(this.slowWnd >= this.limitWnd) {
				this.slowWnd++;
			} else {
				this.slowWnd = this.slowWnd * 2;
			}
			ThreadUtils.sleep(DEFAULT_SLEEP_TIME);
		}
	}
	
	/**
	 * 客户端缓存耗尽
	 */
	private void wndSizeControl() {
		while(this.sendWindow.wndSizeControl()) {
			timeoutRetry();
			synchronized (this.sendWindow) {
				ThreadUtils.wait(this.sendWindow, Duration.ofSeconds(WND_SIZE_CONTROL_TIMEOUT));
			}
		}
	}
	
	/**
	 * 获取超时（丢包）数据包并重新发送。
	 * 
	 * @return true：有丢包；false：没有丢包。
	 */
	private boolean timeoutRetry() {
		final List<UtpWindowData> windowDatas = this.sendWindow.timeoutWindowData();
		if(CollectionUtils.isNotEmpty(windowDatas)) {
			data(windowDatas);
			LOGGER.debug("数据包超时（丢包）重新发送数据包大小：{}", windowDatas.size());
			return true;
		}
		return false;
	}
	
	/**
	 * 连接
	 */
	public boolean connect() {
		this.connect = false;
		this.syn();
		synchronized (this.connectLock) {
			if(!this.connectLock.get()) {
				ThreadUtils.wait(this.connectLock, Duration.ofSeconds(CONNECT_TIMEOUT));
			}
		}
		return this.connect;
	}

	/**
	 * 接收数据消息
	 */
	private void data(int timestamp, short seqnr, short acknr, ByteBuffer buffer) throws NetException {
		UtpWindowData windowData = null;
		try {
			windowData = this.receiveWindow.receive(timestamp, seqnr, buffer);
		} catch (NetException e) {
			this.resetAndClose();
			throw e;
		} catch (Exception e) {
			this.resetAndClose();
			throw new NetException(e);
		}
		if(windowData == null) {
			return;
		} else {
			this.state(windowData.getTimestamp(), windowData.getSeqnr());
		}
		LOGGER.debug("UTP处理数据：{}", windowData.getSeqnr());
		final ByteBuffer attachment = windowData.buffer();
		this.peerUnpackMessageHandler.onMessage(attachment);
	}
	
	/**
	 * 发送数据消息
	 */
	private void data(List<UtpWindowData> windowDatas) {
		if(CollectionUtils.isEmpty(windowDatas)) {
			return;
		}
		windowDatas.forEach(windowData -> {
			if(windowData.pushTimes() > UtpConfig.MAX_PUSH_TIMES) {
				LOGGER.warn("消息发送失败次数超限：{}", windowData.getSeqnr());
				this.sendWindow.discard(windowData.getSeqnr());
			} else {
				data(windowData);
			}
		});
	}
	
	/**
	 * 发送数据
	 */
	private void data(UtpWindowData windowData) {
		LOGGER.debug("UTP发送数据：{}", windowData.getSeqnr());
		final ByteBuffer buffer = header(UtpConfig.TYPE_DATA, windowData.getLength() + 20);
		buffer.putShort(this.sendId);
		buffer.putInt(windowData.pushUpdateGetTimestamp()); // 更新发送时间
		buffer.putInt(windowData.getTimestamp() - this.receiveWindow.timestamp());
		buffer.putInt(this.receiveWindow.remainWndSize());
		buffer.putShort(windowData.getSeqnr());
		buffer.putShort(this.receiveWindow.seqnr()); // acknr=请求seqnr
		buffer.put(windowData.getData());
		this.pushMessage(buffer);
	}
	
	/**
	 * 接收结束消息
	 */
	private void fin(int timestamp, short seqnr, short acknr) {
		this.connect = false;
		super.close();
		this.state(timestamp, seqnr);
	}
	
	/**
	 * 发送结束消息
	 */
	private void fin() {
		final ByteBuffer buffer = header(UtpConfig.TYPE_FIN, 20);
		buffer.putShort(this.recvId);
		buffer.putInt(DateUtils.timestampUs());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort((short) (this.sendWindow.seqnr() + 1));
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}

	/**
	 * 接收应答消息
	 */
	private void state(int timestamp, short seqnr, short acknr, int wndSize) {
		LOGGER.debug("UTP收到响应：{}", acknr);
		if(!this.connect) { // 没有连接
			this.connect = this.available();
			if(this.connect) {
				// 握手时seqnr为下一个seqnr，所以这里-1设置为当前seqnr。
				this.receiveWindow.connect(timestamp, (short) (seqnr - 1));
			}
			synchronized (this.connectLock) {
				this.connectLock.set(true);
				this.connectLock.notifyAll();
			}
		}
		this.sendWindow.ack(acknr, wndSize);
		synchronized (this.sendWindow) {
			this.sendWindow.notifyAll();
		}
	}
	
	/**
	 * 发送应答消息，发送此消息不增加seqnr。
	 */
	private void state(int timestamp, short seqnr) {
		final int now = DateUtils.timestampUs();
		final ByteBuffer buffer = header(UtpConfig.TYPE_STATE, 20);
		buffer.putShort(this.sendId);
		buffer.putInt(now);
		buffer.putInt(now - timestamp);
		buffer.putInt(this.receiveWindow.remainWndSize());
		buffer.putShort(this.sendWindow.seqnr());
		buffer.putShort(seqnr); // acknr=请求seqnr
		this.pushMessage(buffer);
	}
	
	/**
	 * 接收reset消息
	 */
	private void reset(int timestamp, short seqnr, short acknr) {
		this.connect = false;
		super.close();
		this.state(timestamp, seqnr);
	}
	
	/**
	 * 发送reset消息
	 */
	private void reset() {
		final ByteBuffer buffer = header(UtpConfig.TYPE_RESET, 20);
		buffer.putShort(this.recvId);
		buffer.putInt(DateUtils.timestampUs());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort((short) (this.sendWindow.seqnr() + 1));
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}
	
	/**
	 *接收握手消息
	 */
	private void syn(int timestamp, short seqnr, short acknr) {
		if(!this.connect) {
			this.connect = true;
			this.receiveWindow.connect(timestamp, seqnr);
		}
		this.state(timestamp, seqnr);
	}
	
	/**
	 * 发送握手消息，第一条消息。
	 */
	private void syn() {
		final UtpWindowData windowData = this.sendWindow.send();
		final ByteBuffer buffer = header(UtpConfig.TYPE_SYN, 20);
		buffer.putShort(this.recvId);
		buffer.putInt(windowData.pushUpdateGetTimestamp());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort(windowData.getSeqnr());
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}

	/**
	 * 设置消息头
	 */
	private ByteBuffer header(byte type, int size) {
		final ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.put(type);
		buffer.put(UtpConfig.EXTENSION);
		return buffer;
	}
	
	/**
	 * 发送消息
	 */
	private void pushMessage(ByteBuffer buffer) {
		try {
			super.send(buffer);
		} catch (NetException e) {
			LOGGER.error("UTP发送消息异常", e);
		}
	}
	
	/**
	 * 发送fin消息，标记关闭。
	 */
	@Override
	public void close() {
		LOGGER.debug("UTP关闭");
		this.utpService.remove(this);
		this.fin();
		super.close();
	}
	
	/**
	 * 发送reset消息，标记关闭。
	 */
	private void resetAndClose() {
		LOGGER.debug("UTP重置");
		this.utpService.remove(this);
		this.reset();
		super.close();
	}
	
}
