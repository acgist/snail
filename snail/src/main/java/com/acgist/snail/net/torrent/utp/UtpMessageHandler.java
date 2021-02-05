package com.acgist.snail.net.torrent.utp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.config.UtpConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.codec.IMessageEncoder;
import com.acgist.snail.net.torrent.IMessageEncryptSender;
import com.acgist.snail.net.torrent.IPeerConnect;
import com.acgist.snail.net.torrent.peer.PeerCryptMessageCodec;
import com.acgist.snail.net.torrent.peer.PeerSubMessageHandler;
import com.acgist.snail.net.torrent.peer.PeerUnpackMessageCodec;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.DateUtils;

/**
 * <p>uTP消息代理</p>
 * <p>uTorrent transport protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0029.html</p>
 * <p>uTP头格式</p>
 * <pre>
 * 0       4       8               16              24              32
 * +-------+-------+---------------+---------------+---------------+
 * | type  | ver   | extension     | connection_id                 |
 * +-------+-------+---------------+---------------+---------------+
 * | timestamp_microseconds                                        |
 * +---------------+---------------+---------------+---------------+
 * | timestamp_difference_microseconds                             |
 * +---------------+---------------+---------------+---------------+
 * | wnd_size                                                      |
 * +---------------+---------------+---------------+---------------+
 * | seq_nr                        | ack_nr                        |
 * +---------------+---------------+---------------+---------------+
 * </pre>
 * 
 * @author acgist
 */
public final class UtpMessageHandler extends UdpMessageHandler implements IMessageEncryptSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtpMessageHandler.class);
	
	/**
	 * <p>UTP消息请求头长度：{@value}</p>
	 */
	private static final int UTP_HEADER_LENGTH = 20;
	/**
	 * <p>UTP消息请求头最小长度：{@value}</p>
	 */
	private static final int UTP_HEADER_MIN_LENGTH = 20;
	/**
	 * <p>UTP扩展消息最小长度：{@value}</p>
	 */
	private static final int UTP_EXT_MIN_LENGTH = 2;
	
	/**
	 * <p>是否连接</p>
	 * <p>不能使用这个状态{@linkplain #available() 判断是否可用}，发送方法判断这个状态，导致发送连接消息失败。</p>
	 */
	private boolean connect;
	/**
	 * <p>接收连接ID</p>
	 */
	private final short recvId;
	/**
	 * <p>发送连接ID</p>
	 */
	private final short sendId;
	/**
	 * <p>UTP Service</p>
	 */
	private final UtpService utpService;
	/**
	 * <p>发送窗口</p>
	 */
	private final UtpWindow sendWindow;
	/**
	 * <p>接收窗口</p>
	 */
	private final UtpWindow recvWindow;
	/**
	 * <p>收到ack消息重复次数</p>
	 */
	private final AtomicInteger ackLossTimes;
	/**
	 * <p>连接锁</p>
	 */
	private final AtomicBoolean connectLock;
	/**
	 * <p>消息编码器</p>
	 */
	private final IMessageEncoder<ByteBuffer> messageEncoder;
	
	/**
	 * <p>服务端</p>
	 * 
	 * @param connectionId 连接ID
	 * @param socketAddress 地址
	 */
	public UtpMessageHandler(short connectionId, InetSocketAddress socketAddress) {
		this(PeerSubMessageHandler.newInstance(), socketAddress, connectionId, true);
	}

	/**
	 * <p>客户端</p>
	 * 
	 * @param peerSubMessageHandler Peer消息代理
	 * @param socketAddress 地址
	 */
	public UtpMessageHandler(PeerSubMessageHandler peerSubMessageHandler, InetSocketAddress socketAddress) {
		this(peerSubMessageHandler, socketAddress, (short) 0, false);
	}
	
	/**
	 * @param peerSubMessageHandler Peer消息代理
	 * @param socketAddress 地址
	 * @param connectionId 连接ID
	 * @param server 是否服务端
	 */
	private UtpMessageHandler(PeerSubMessageHandler peerSubMessageHandler, InetSocketAddress socketAddress, short connectionId, boolean server) {
		super(socketAddress);
		peerSubMessageHandler.messageEncryptSender(this);
		final var peerUnpackMessageCodec = new PeerUnpackMessageCodec(peerSubMessageHandler);
		final var peerCryptMessageCodec = new PeerCryptMessageCodec(peerUnpackMessageCodec, peerSubMessageHandler);
		this.messageDecoder = peerCryptMessageCodec;
		this.messageEncoder = peerCryptMessageCodec;
		this.utpService = UtpService.getInstance();
		this.sendWindow = UtpWindow.newSendInstance();
		this.recvWindow = UtpWindow.newRecvInstance(this.messageDecoder);
		this.ackLossTimes = new AtomicInteger(0);
		this.connectLock = new AtomicBoolean(false);
		if(server) {
			this.sendId = connectionId;
			this.recvId = (short) (this.sendId + 1);
		} else {
			this.recvId = this.utpService.connectionId();
			this.sendId = (short) (this.recvId + 1);
		}
		this.utpService.put(this);
	}
	
	/**
	 * <p>获取Key</p>
	 * 
	 * @return key
	 */
	public String key() {
		return this.utpService.buildKey(this.recvId, this.socketAddress);
	}
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		if(buffer.remaining() < UTP_HEADER_MIN_LENGTH) {
			throw new NetException("处理UTP消息错误（长度）：" + buffer.remaining());
		}
		final byte typeVersion = buffer.get(); // 消息类型
		final UtpConfig.Type type = UtpConfig.Type.of(typeVersion); // UTP消息类型
		if(type == null) {
			throw new NetException("未知UTP消息类型：" + typeVersion);
		}
		final byte extension = buffer.get(); // 扩展
		final short connectionId = buffer.getShort(); // 连接ID
		final int timestamp = buffer.getInt(); // 时间戳
		final int timestampDifference = buffer.getInt(); // 时间差
		final int wndSize = buffer.getInt(); // 窗口大小
		final short seqnr = buffer.getShort(); // 请求编号
		final short acknr = buffer.getShort(); // 响应编号
		if(extension != 0 && buffer.remaining() >= UTP_EXT_MIN_LENGTH) { // 扩展数据
			final short extLength = buffer.getShort();
			if(extLength <= 0 || buffer.remaining() < extLength) {
				throw new NetException("处理UTP消息错误（扩展长度）：" + extLength);
			}
			// 扩展信息
			final byte[] extData = new byte[extLength];
			buffer.get(extData);
		}
		switch (type) {
		case DATA:
			if(this.connect) {
				this.data(timestamp, seqnr, acknr, buffer);
			} else {
				this.close();
			}
			break;
		case STATE:
			this.state(timestamp, seqnr, acknr, wndSize);
			break;
		case FIN:
			this.fin(timestamp, seqnr, acknr);
			break;
		case RESET:
			this.reset(timestamp, seqnr, acknr);
			break;
		case SYN:
			this.syn(timestamp, seqnr, acknr);
			break;
		default:
			// TODO：多行日志
			LOGGER.warn(
				"处理UTP消息错误（未知类型），类型：{}，扩展：{}，连接ID：{}，时间戳：{}，时间差：{}，窗口大小：{}，请求编号：{}，应答编号：{}",
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
		this.messageEncoder.encode(buffer);
		this.sendPacket(buffer);
	}
	
	@Override
	public IPeerConnect.ConnectType connectType() {
		return IMessageEncryptSender.ConnectType.UTP;
	}

	/**
	 * <p>UDP拆包</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 */
	private void sendPacket(ByteBuffer buffer) throws NetException {
		this.check(buffer);
		byte[] bytes;
		int remaining;
		while((remaining = buffer.remaining()) > 0) {
			// UDP拆包
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
	 * <p>连接</p>
	 * 
	 * @return 是否连接成功
	 */
	public boolean connect() {
		this.connect = false;
		this.syn();
		// 添加连接锁
		this.lockConnect();
		// 连接失败移除
		if(!this.connect) {
			this.closeWindow();
			this.closeAll();
		}
		return this.connect;
	}
	
	/**
	 * <p>超时数据包重新发送</p>
	 * 
	 * @return true-丢包；false-正常；
	 */
	public boolean timeoutRetry() {
		final List<UtpWindowData> windowDatas = this.sendWindow.timeoutWindowData();
		if(CollectionUtils.isNotEmpty(windowDatas)) {
			this.data(windowDatas);
			LOGGER.debug("超时数据包重新发送：{}-{}", this.sendId, windowDatas.size());
			return true;
		}
		return false;
	}
	
	/**
	 * <p>发送数据包</p>
	 * 
	 * @param windowDatas 数据包集合
	 */
	private void data(List<UtpWindowData> windowDatas) {
		if(CollectionUtils.isEmpty(windowDatas)) {
			return;
		}
		windowDatas.forEach(windowData -> {
			if(windowData.getPushTimes() > UtpConfig.MAX_PUSH_TIMES) {
				LOGGER.warn("发送数据包失败（次数超限）：{}-{}", windowData.getSeqnr(), windowData.getPushTimes());
				this.sendWindow.discard(windowData.getSeqnr());
			} else {
				this.data(windowData);
			}
		});
	}
	
	/**
	 * <p>处理数据消息</p>
	 * <p>发送响应消息响应编号等于最后一次处理的接收请求编号</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param acknr 响应编号
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 */
	private void data(int timestamp, short seqnr, short acknr, ByteBuffer buffer) throws NetException {
		// TODO：处理acknr
		LOGGER.debug("处理数据消息：{}", seqnr);
		try {
			this.recvWindow.receive(timestamp, seqnr, buffer);
		} catch (IOException e) {
			throw new NetException(e);
		} finally {
			this.state(timestamp, this.recvWindow.seqnr()); // 最后一次处理的接收请求编号
		}
	}
	
	/**
	 * <p>发送数据消息</p>
	 * 
	 * @param windowData 数据消息
	 */
	private void data(UtpWindowData windowData) {
		LOGGER.debug("发送数据消息：{}", windowData.getSeqnr());
		final ByteBuffer buffer = this.buildHeader(UtpConfig.Type.DATA, windowData.getLength() + UTP_HEADER_LENGTH);
		final int now = windowData.pushUpdateGetTimestamp();
		buffer.putShort(this.sendId);
		buffer.putInt(now); // 更新发送时间
		buffer.putInt(now - this.recvWindow.timestamp());
		buffer.putInt(this.recvWindow.wndSize());
		buffer.putShort(windowData.getSeqnr()); // seqnr
		buffer.putShort(this.recvWindow.seqnr()); // acknr
		buffer.put(windowData.getData());
		this.pushMessage(buffer);
	}

	/**
	 * <p>处理响应消息</p>
	 * <p>如果多次返回已处理的数据编号，则视为丢包重新发送最后一个未确认数据包。</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param acknr 响应编号
	 * @param wndSize 窗口大小
	 */
	private void state(int timestamp, short seqnr, short acknr, int wndSize) {
		LOGGER.debug("处理响应消息：{}", acknr);
		if(!this.connect) { // 没有连接
			this.connect = this.available();
			if(this.connect) {
				this.recvWindow.connect(timestamp, (short) (seqnr - 1)); // 注意：seqnr-1
			}
			// 释放连接锁
			this.unlockConnect();
		}
		// 快速重传
		final boolean loss = this.sendWindow.ack(acknr, wndSize); // 是否可能丢包
		if(loss) {
			if(this.ackLossTimes.incrementAndGet() > UtpConfig.FAST_ACK_RETRY_TIMES) {
				final var packet = this.sendWindow.lastUnack();
				if(packet != null) {
					LOGGER.debug("UTP消息快速重传：{}-{}", acknr, packet.getSeqnr());
					this.data(packet);
				}
			}
		} else {
			this.ackLossTimes.set(0);
		}
	}
	
	/**
	 * <p>发送响应消息</p>
	 * <p>发送此消息不增加seqnr</p>
	 * 
	 * @param timestamp 时间戳
	 * @param acknr 响应编号
	 */
	private void state(int timestamp, short acknr) {
		LOGGER.debug("发送响应消息：{}", acknr);
		final int now = DateUtils.timestampUs();
		final ByteBuffer buffer = this.buildHeader(UtpConfig.Type.STATE, UTP_HEADER_LENGTH);
		buffer.putShort(this.sendId);
		buffer.putInt(now);
		buffer.putInt(now - timestamp);
		buffer.putInt(this.recvWindow.wndSize());
		buffer.putShort(this.sendWindow.seqnr()); // seqnr
		buffer.putShort(acknr); // acknr
		this.pushMessage(buffer);
	}

	/**
	 * <p>处理结束消息</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param acknr 响应编号
	 */
	private void fin(int timestamp, short seqnr, short acknr) {
		LOGGER.debug("处理结束消息：{}", this.socketAddress);
		// 如果没有连接不用发送响应消息：防止一直往返确认造成死循环
		if(this.connect) {
			this.state(timestamp, seqnr);
		}
		this.closeWindow();
		this.closeAll();
	}
	
	/**
	 * <p>发送结束消息</p>
	 */
	private void fin() {
		LOGGER.debug("发送结束消息：{}", this.socketAddress);
		final ByteBuffer buffer = this.buildHeader(UtpConfig.Type.FIN, UTP_HEADER_LENGTH);
		buffer.putShort(this.sendId);
		buffer.putInt(DateUtils.timestampUs());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort((short) (this.sendWindow.seqnr() + 1));
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}
	
	/**
	 * <p>处理重置消息</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param acknr 响应编号
	 */
	private void reset(int timestamp, short seqnr, short acknr) {
		LOGGER.debug("处理重置消息：{}", this.socketAddress);
		// 如果没有连接不用发送响应消息：防止一直往返确认造成死循环
		if(this.connect) {
			this.state(timestamp, seqnr);
		}
		this.closeWindow();
		this.closeAll();
	}
	
	/**
	 * <p>发送重置消息</p>
	 */
	private void reset() {
		LOGGER.debug("发送重置消息：{}", this.socketAddress);
		final ByteBuffer buffer = this.buildHeader(UtpConfig.Type.RESET, UTP_HEADER_LENGTH);
		buffer.putShort(this.sendId);
		buffer.putInt(DateUtils.timestampUs());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort((short) (this.sendWindow.seqnr() + 1));
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}
	
	/**
	 * <p>处理握手消息</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param acknr 响应编号
	 */
	private void syn(int timestamp, short seqnr, short acknr) {
		LOGGER.debug("处理握手消息");
		if(!this.connect) {
			this.connect = true;
			this.recvWindow.connect(timestamp, seqnr);
		}
		this.state(timestamp, seqnr);
	}
	
	/**
	 * <p>发送握手消息</p>
	 */
	private void syn() {
		LOGGER.debug("发送握手消息");
		final UtpWindowData windowData = this.sendWindow.build();
		final ByteBuffer buffer = buildHeader(UtpConfig.Type.SYN, UTP_HEADER_LENGTH);
		buffer.putShort(this.recvId);
		buffer.putInt(windowData.pushUpdateGetTimestamp());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort(windowData.getSeqnr());
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}

	/**
	 * <p>设置消息头</p>
	 * 
	 * @param type 消息类型
	 * @param size 消息长度
	 * 
	 * @return 消息
	 */
	private ByteBuffer buildHeader(UtpConfig.Type type, int size) {
		final ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.put(type.typeVersion()); // 消息类型
		buffer.put(UtpConfig.EXTENSION); // 扩展扩展
		return buffer;
	}
	
	/**
	 * <p>发送消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void pushMessage(ByteBuffer buffer) {
		try {
			this.send(buffer, this.remoteSocketAddress());
		} catch (NetException e) {
			LOGGER.error("发送UTP消息异常", e);
		}
	}
	
	/**
	 * <p>添加连接锁</p>
	 */
	private void lockConnect() {
		if(!this.connectLock.get()) {
			synchronized (this.connectLock) {
				if(!this.connectLock.get()) {
					try {
						this.connectLock.wait(SystemConfig.CONNECT_TIMEOUT_MILLIS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						LOGGER.debug("线程等待异常", e);
					}
				}
			}
		}
	}
	
	/**
	 * <p>释放连接锁</p>
	 */
	private void unlockConnect() {
		synchronized (this.connectLock) {
			this.connectLock.set(true);
			this.connectLock.notifyAll();
		}
	}
	
	/**
	 * <p>关闭窗口：释放信号量</p>
	 * <p>释放信号量才能发送关闭和重置消息，否者可能存在一直等待情况。</p>
	 */
	private void closeWindow() {
		this.sendWindow.close();
		this.recvWindow.close();
	}
	
	/**
	 * <p>关闭所有资源：关闭窗口、设置连接关闭、移除消息代理</p>
	 */
	private void closeAll() {
		super.close();
		this.connect = false;
		this.utpService.remove(this);
	}
	
	@Override
	public void close() {
		LOGGER.debug("关闭UTP");
		this.closeWindow();
		if(this.connect) {
			this.fin();
		} else {
			this.reset();
		}
		this.closeAll();
	}
	
}
