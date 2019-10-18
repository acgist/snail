package com.acgist.snail.net.torrent.utp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.IMessageEncryptHandler;
import com.acgist.snail.net.torrent.peer.PeerCryptMessageCodec;
import com.acgist.snail.net.torrent.peer.PeerUnpackMessageCodec;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpService;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpWindow;
import com.acgist.snail.net.torrent.utp.bootstrap.UtpWindowData;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.UtpConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>uTP消息</p>
 * <p>uTorrent transport protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0029.html</p>
 * <p>uTP头：</p>
 * <pre>
0       4       8               16              24              32
+-------+-------+---------------+---------------+---------------+
| type  | ver   | extension     | connection_id                 |
+-------+-------+---------------+---------------+---------------+
| timestamp_microseconds                                        |
+---------------+---------------+---------------+---------------+
| timestamp_difference_microseconds                             |
+---------------+---------------+---------------+---------------+
| wnd_size                                                      |
+---------------+---------------+---------------+---------------+
| seq_nr                        | ack_nr                        |
+---------------+---------------+---------------+---------------+
 * </pre>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class UtpMessageHandler extends UdpMessageHandler implements IMessageEncryptHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtpMessageHandler.class);
	
	/**
	 * UTP消息请求头字节长度
	 */
	private static final int UTP_HEADER_SIZE = 20;
	/**
	 * UTP消息最小字节长度
	 */
	private static final int UTP_MIN_SIZE = 20;
	/**
	 * 扩展消息最小字节长度
	 */
	private static final int UTP_EXT_MIN_SIZE = 2;
	
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
	 * 消息处理现场
	 */
	private Future<?> future;
	/**
	 * UTP Service
	 */
	private final UtpService utpService;
	/**
	 * 发送窗口
	 */
	private final UtpWindow sendWindow;
	/**
	 * 接收窗口
	 */
	private final UtpWindow recvWindow;
	/**
	 * 连接锁
	 */
	private final AtomicBoolean connectLock;
	/**
	 * Peer代理
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	/**
	 * <p>UTP窗口请求数据队列</p>
	 * <p>UTP请求数据异步执行，防止阻塞导致不能及时处理响应信息。</p>
	 */
	private final BlockingQueue<UtpWindowData> requests = new LinkedBlockingQueue<>();
	
	/**
	 * 服务端
	 */
	public UtpMessageHandler(final short connectionId, InetSocketAddress socketAddress) {
		this(PeerSubMessageHandler.newInstance(), socketAddress, connectionId, true);
	}

	/**
	 * 客户端
	 */
	public UtpMessageHandler(PeerSubMessageHandler peerSubMessageHandler, InetSocketAddress socketAddress) {
		this(peerSubMessageHandler, socketAddress, (short) 0, false);
	}
	
	private UtpMessageHandler(PeerSubMessageHandler peerSubMessageHandler, InetSocketAddress socketAddress, short connectionId, boolean recv) {
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.peerSubMessageHandler.messageEncryptHandler(this);
		final var peerUnpackMessageCodec = new PeerUnpackMessageCodec(this.peerSubMessageHandler);
		final var peerCryptMessageCodec = new PeerCryptMessageCodec(peerUnpackMessageCodec, this.peerSubMessageHandler);
		this.messageCodec = peerCryptMessageCodec;
		this.utpService = UtpService.getInstance();
		this.sendWindow = UtpWindow.newInstance();
		this.recvWindow = UtpWindow.newInstance();
		this.connectLock = new AtomicBoolean(false);
		this.socketAddress = socketAddress;
		if(recv) { // 服务端
			this.sendId = connectionId;
			this.recvId = (short) (this.sendId + 1);
		} else { // 客户端
			this.recvId = this.utpService.connectionId();
			this.sendId = (short) (this.recvId + 1);
		}
		this.utpService.put(this);
	}
	
	public String key() {
		return this.utpService.buildKey(this.recvId, this.socketAddress);
	}
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		buffer.flip();
		if(buffer.remaining() < UTP_MIN_SIZE) {
			throw new NetException("UTP消息格式错误（长度）：" + buffer.remaining());
		}
		final byte typeVersion = buffer.get(); // type|version
		final byte type = (byte) (typeVersion >> 4); // type
		final byte extension = buffer.get(); // 扩展
		final short connectionId = buffer.getShort(); // 连接ID
		final int timestamp = buffer.getInt(); // 时间戳
		final int timestampDifference = buffer.getInt(); // 时间戳对比
		final int wndSize = buffer.getInt(); // 窗口大小
		final short seqnr = buffer.getShort(); // 请求序号
		final short acknr = buffer.getShort(); // 响应序号
		if(extension != 0 && buffer.remaining() >= UTP_EXT_MIN_SIZE) { // 扩展数据
			final short extLength = buffer.getShort();
			if(extLength <= 0 || buffer.remaining() < extLength) {
				throw new NetException("UTP信息格式错误（扩展消息长度）：" + extLength);
			}
			final byte[] extData = new byte[extLength];
			buffer.get(extData);
		}
//		LOGGER.debug(
//			"收到UTP消息，类型：{}，扩展：{}，连接ID：{}，时间戳：{}，时间戳对比：{}，窗口大小：{}，请求序号：{}，应答序号：{}",
//			type, extension, connectionId, timestamp, timestampDifference, wndSize, seqnr, acknr
//		);
		switch (type) {
		case UtpConfig.ST_DATA:
			data(timestamp, seqnr, acknr, buffer);
			break;
		case UtpConfig.ST_STATE:
			state(timestamp, seqnr, acknr, wndSize);
			break;
		case UtpConfig.ST_FIN:
			fin(timestamp, seqnr, acknr);
			break;
		case UtpConfig.ST_RESET:
			reset(timestamp, seqnr, acknr);
			break;
		case UtpConfig.ST_SYN:
			syn(timestamp, seqnr, acknr);
			break;
		default:
			LOGGER.warn(
				"不支持的UTP消息类型，类型：{}，扩展：{}，连接ID：{}，时间戳：{}，时间戳对比：{}，窗口大小：{}，请求序号：{}，应答序号：{}",
				type, extension, connectionId, timestamp, timestampDifference, wndSize, seqnr, acknr
			);
			break;
		}
	}

	@Override
	public void send(ByteBuffer buffer, int timeout) throws NetException {
		this.sendPacket(buffer);
	}
	
	@Override
	public void sendEncrypt(ByteBuffer buffer, int timeout) throws NetException {
		this.messageCodec.encode(buffer);
		this.sendPacket(buffer);
	}

	/**
	 * <p>UDP拆包</p>
	 */
	private void sendPacket(ByteBuffer buffer) throws NetException {
		if(!available()) {
			LOGGER.debug("UTP消息发送失败：通道不可用");
			return;
		}
		if(buffer.position() != 0) {
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("UTP消息发送失败：{}", buffer);
			return;
		}
		byte[] bytes;
		int remaining;
		while((remaining = buffer.remaining()) > 0) { // UDP拆包
			if(remaining > UtpConfig.UTP_PACKET_MAX_LENGTH) {
				bytes = new byte[UtpConfig.UTP_PACKET_MAX_LENGTH];
			} else {
				bytes = new byte[remaining];
			}
			buffer.get(bytes);
			final UtpWindowData windowData = this.sendWindow.build(bytes);
			this.data(windowData);
		}
	}
	
	/**
	 * 连接
	 */
	public boolean connect() {
		this.connect = false;
		this.syn();
		if(!this.connectLock.get()) {
			synchronized (this.connectLock) {
				if(!this.connectLock.get()) {
					ThreadUtils.wait(this.connectLock, Duration.ofSeconds(PeerConfig.CONNECT_TIMEOUT));
				}
			}
		}
		// 连接失败移除
		if(!this.connect) {
			this.closeAll();
		}
		return this.connect;
	}
	
	/**
	 * 获取超时（丢包）数据包并重新发送
	 * 
	 * @return true：有丢包；false：没有丢包；
	 */
	public boolean timeoutRetry() {
		final List<UtpWindowData> windowDatas = this.sendWindow.timeoutWindowData();
		if(CollectionUtils.isNotEmpty(windowDatas)) {
			data(windowDatas);
			LOGGER.debug("数据包超时（丢包）重新发送数据包大小：{}", windowDatas.size());
			return true;
		}
		return false;
	}
	
	/**
	 * 发送数据消息
	 */
	private void data(List<UtpWindowData> windowDatas) {
		if(CollectionUtils.isEmpty(windowDatas)) {
			return;
		}
		windowDatas.forEach(windowData -> {
			if(windowData.getPushTimes() > UtpConfig.MAX_PUSH_TIMES) {
				LOGGER.warn("发送UTP数据失败（次数超限）：{}-{}", windowData.getSeqnr(), windowData.getPushTimes());
				this.sendWindow.discard(windowData.getSeqnr());
			} else {
				data(windowData);
			}
		});
	}
	
	/**
	 * 接收数据消息
	 */
	private void data(int timestamp, short seqnr, short acknr, ByteBuffer buffer) throws NetException {
		UtpWindowData windowData = null;
		try {
			windowData = this.recvWindow.receive(timestamp, seqnr, buffer);
		} catch (IOException e) {
			throw new NetException(e);
		}
		if(windowData != null) {
			this.buildFuture();
			LOGGER.debug("处理UTP数据：{}", windowData.getSeqnr());
			this.state(windowData.getTimestamp(), windowData.getSeqnr());
			// 同步处理
//			this.messageCodec.decode(windowData.buffer());
			// 异步处理
			if(!this.requests.offer(windowData)) {
				LOGGER.warn("UTP消息插入队列失败");
			}
		}
	}
	
	/**
	 * 发送数据消息
	 */
	private void data(UtpWindowData windowData) {
		if(windowData.hasData()) {
			LOGGER.debug("发送UTP数据：{}", windowData.getSeqnr());
			final ByteBuffer buffer = header(UtpConfig.TYPE_DATA, windowData.getLength() + UTP_HEADER_SIZE);
			buffer.putShort(this.sendId);
			buffer.putInt(windowData.pushUpdateGetTimestamp()); // 更新发送时间
			buffer.putInt(windowData.getTimestamp() - this.recvWindow.timestamp());
			buffer.putInt(this.recvWindow.remainWndSize());
			buffer.putShort(windowData.getSeqnr());
			buffer.putShort(this.recvWindow.seqnr()); // acknr=请求seqnr
			buffer.put(windowData.getData());
			this.pushMessage(buffer);
		}
	}

	/**
	 * 接收应答消息
	 */
	private void state(int timestamp, short seqnr, short acknr, int wndSize) {
		LOGGER.debug("收到UTP响应：{}", acknr);
		if(!this.connect) { // 没有连接
			this.connect = this.available();
			if(this.connect) {
				// 注意：seqnr-1
				this.recvWindow.connect(timestamp, (short) (seqnr - 1));
			}
			synchronized (this.connectLock) {
				this.connectLock.set(true);
				this.connectLock.notifyAll();
			}
		}
		this.sendWindow.ack(acknr, wndSize);
	}
	
	/**
	 * 发送应答消息：发送此消息不增加seqnr
	 */
	private void state(int timestamp, short seqnr) {
		LOGGER.debug("发送UTP响应：{}", seqnr);
		final int now = DateUtils.timestampUs();
		final ByteBuffer buffer = header(UtpConfig.TYPE_STATE, UTP_HEADER_SIZE);
		buffer.putShort(this.sendId);
		buffer.putInt(now);
		buffer.putInt(now - timestamp);
		buffer.putInt(this.recvWindow.remainWndSize());
		buffer.putShort(this.sendWindow.seqnr());
		buffer.putShort(seqnr); // acknr=请求seqnr
		this.pushMessage(buffer);
	}

	/**
	 * 接收结束消息
	 */
	private void fin(int timestamp, short seqnr, short acknr) {
		LOGGER.debug("收到UTP消息（fin）");
		this.connect = false;
		this.state(timestamp, seqnr);
		this.closeAll();
	}
	
	/**
	 * 发送结束消息
	 */
	private void fin() {
		LOGGER.debug("发送UTP消息（fin）");
		final ByteBuffer buffer = header(UtpConfig.TYPE_FIN, UTP_HEADER_SIZE);
		buffer.putShort(this.sendId);
		buffer.putInt(DateUtils.timestampUs());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort((short) (this.sendWindow.seqnr() + 1));
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}
	
	/**
	 * 接收reset消息
	 */
	private void reset(int timestamp, short seqnr, short acknr) {
		LOGGER.debug("收到UTP消息（reset）");
		this.connect = false;
		this.state(timestamp, seqnr);
		this.closeAll();
	}
	
	/**
	 * 发送reset消息
	 */
	private void reset() {
		LOGGER.debug("发送UTP消息（reset）");
		final ByteBuffer buffer = header(UtpConfig.TYPE_RESET, UTP_HEADER_SIZE);
		buffer.putShort(this.sendId);
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
			this.recvWindow.connect(timestamp, seqnr);
		}
		this.state(timestamp, seqnr);
	}
	
	/**
	 * 发送握手消息
	 */
	private void syn() {
		final UtpWindowData windowData = this.sendWindow.build();
		final ByteBuffer buffer = header(UtpConfig.TYPE_SYN, UTP_HEADER_SIZE);
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
		buffer.put(type); // 消息类型
		buffer.put(UtpConfig.EXTENSION); // 扩展扩展
		return buffer;
	}
	
	/**
	 * 发送消息
	 */
	private void pushMessage(ByteBuffer buffer) {
		try {
			this.send(buffer, this.remoteSocketAddress());
		} catch (NetException e) {
			LOGGER.error("发送UTP消息异常", e);
		}
	}
	
	/**
	 * <p>创建异步处理线程</p>
	 */
	private void buildFuture() {
		if(this.future != null) {
			return;
		}
		this.future = this.utpService.submit(() -> {
			while(!this.close) {
				try {
					final var windowData = this.requests.take();
					this.messageCodec.decode(windowData.buffer());
				} catch (NetException e) {
					LOGGER.error("UTP请求执行异常", e);
				} catch (InterruptedException e) {
					LOGGER.debug("UTP请求执行异常", e);
					Thread.currentThread().interrupt();
				}
			}
		});
	}
	
	/**
	 * 关闭窗口
	 */
	private void closeWindow() {
		this.sendWindow.close();
		this.recvWindow.close();
	}
	
	/**
	 * 关闭所有信息
	 */
	private void closeAll() {
		super.close();
		this.connect = false;
		this.utpService.remove(this);
		if(this.future != null) {
			this.future.cancel(true);
			this.future = null;
		}
	}
	
	/**
	 * 发送fin消息，标记关闭。
	 */
	@Override
	public void close() {
		LOGGER.debug("关闭UTP");
		this.closeWindow();
		this.fin();
		this.closeAll();
	}
	
	/**
	 * 发送reset消息，标记关闭。
	 */
	public void resetAndClose() {
		LOGGER.debug("重置UTP");
		this.closeWindow();
		this.reset();
		this.closeAll();
	}
	
}
