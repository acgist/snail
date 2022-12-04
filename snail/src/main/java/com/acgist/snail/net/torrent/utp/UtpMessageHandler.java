package com.acgist.snail.net.torrent.utp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.config.UtpConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.codec.IMessageEncoder;
import com.acgist.snail.net.torrent.IEncryptMessageSender;
import com.acgist.snail.net.torrent.IPeerConnect;
import com.acgist.snail.net.torrent.codec.PeerCryptMessageCodec;
import com.acgist.snail.net.torrent.codec.PeerUnpackMessageCodec;
import com.acgist.snail.net.torrent.peer.PeerSubMessageHandler;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.DateUtils;

/**
 * <p>UTP消息代理</p>
 * <p>uTorrent transport protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0029.html</p>
 * <p>UTP头格式</p>
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
public final class UtpMessageHandler extends UdpMessageHandler implements IEncryptMessageSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtpMessageHandler.class);
	
	/**
	 * <p>是否连接</p>
	 * <p>不能重写方法{@link #available()}判断是否可用：发送方法判断这个状态导致发送连接消息失败</p>
	 */
	private volatile boolean connect;
	/**
	 * <p>接收连接ID</p>
	 */
	private final short recvId;
	/**
	 * <p>发送连接ID</p>
	 */
	private final short sendId;
	/**
	 * <p>连接Key</p>
	 */
	private final String key;
	/**
	 * <p>UTP上下文</p>
	 */
	private final UtpContext utpContext;
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
	 * <p>Peer消息代理</p>
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	
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
	 * @param server 是否是服务端
	 */
	private UtpMessageHandler(PeerSubMessageHandler peerSubMessageHandler, InetSocketAddress socketAddress, short connectionId, boolean server) {
		super(socketAddress);
		peerSubMessageHandler.messageEncryptSender(this);
		final var peerUnpackMessageCodec = new PeerUnpackMessageCodec(peerSubMessageHandler);
		final var peerCryptMessageCodec = new PeerCryptMessageCodec(peerUnpackMessageCodec, peerSubMessageHandler);
		this.messageDecoder = peerCryptMessageCodec;
		this.messageEncoder = peerCryptMessageCodec;
		this.peerSubMessageHandler = peerSubMessageHandler;
		this.utpContext = UtpContext.getInstance();
		this.sendWindow = UtpWindow.newSendInstance();
		this.recvWindow = UtpWindow.newRecvInstance(this.messageDecoder);
		this.ackLossTimes = new AtomicInteger(0);
		this.connectLock = new AtomicBoolean(false);
		if(server) {
			this.sendId = connectionId;
			this.recvId = (short) (this.sendId + 1);
		} else {
			this.recvId = this.utpContext.connectionId();
			this.sendId = (short) (this.recvId + 1);
		}
		this.key = this.utpContext.buildKey(this.recvId, this.socketAddress);
		this.utpContext.put(this);
	}
	
	/**
	 * <p>获取连接Key</p>
	 * 
	 * @return 连接Key
	 */
	public String key() {
		return this.key;
	}
	
	@Override
	public boolean useless() {
		return this.peerSubMessageHandler.useless();
	}
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		if(buffer.remaining() < UtpConfig.HEADER_MIN_LENGTH) {
			throw new NetException("处理UTP消息错误（长度）：" + buffer.remaining());
		}
		// 类型版本
		final byte typeVersion = buffer.get();
		// 消息类型
		final UtpConfig.Type type = UtpConfig.Type.of(typeVersion);
		if(type == null) {
			throw new NetException("未知UTP消息类型：" + typeVersion);
		}
		// 扩展
		final byte extension = buffer.get();
		// 连接ID
		final short connectionId = buffer.getShort();
		// 时间戳
		final int timestamp = buffer.getInt();
		// 时间差
		final int timestampDifference = buffer.getInt();
		// 窗口大小
		final int wndSize = buffer.getInt();
		// 请求编号
		final short seqnr = buffer.getShort();
		// 响应编号
		final short acknr = buffer.getShort();
		// 扩展消息
		if(extension != 0 && buffer.remaining() >= UtpConfig.EXTENSION_MIN_LENGTH) {
			final short extLength = buffer.getShort();
			if(extLength <= 0 || buffer.remaining() < extLength) {
				throw new NetException("处理UTP消息错误（扩展长度）：" + extLength);
			}
			final byte[] extData = new byte[extLength];
			buffer.get(extData);
		}
		// 注意顺序（性能）：按照消息数量排序
		switch (type) {
			case DATA -> this.data(timestamp, seqnr, acknr, buffer);
			case STATE -> this.state(timestamp, seqnr, acknr, wndSize);
			case FIN -> this.fin(timestamp, seqnr, acknr);
			case RESET -> this.reset(timestamp, seqnr, acknr);
			case SYN -> this.syn(timestamp, seqnr, acknr);
			default -> LOGGER.warn("""
				处理UTP消息错误（未知类型）
				类型：{}
				扩展：{}
				连接ID：{}
				时间戳：{}
				时间差：{}
				窗口大小：{}
				请求编号：{}
				应答编号：{}""",
				type,
				extension,
				connectionId,
				timestamp,
				timestampDifference,
				wndSize,
				seqnr,
				acknr
			);
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
		return IPeerConnect.ConnectType.UTP;
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
			if(remaining > UtpConfig.PACKET_MAX_LENGTH) {
				bytes = new byte[UtpConfig.PACKET_MAX_LENGTH];
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
		this.connectLock.set(false);
		this.syn();
		this.lockConnect();
		if(!this.connect) {
			// 连接失败移除
			this.close();
		}
		return this.connect;
	}
	
	/**
	 * <p>超时数据包重新发送</p>
	 * 
	 * @return 连接是否已经关闭
	 */
	public boolean timeoutRetry() {
		if(this.available()) {
			final List<UtpWindowData> windowDatas = this.sendWindow.timeoutWindowData();
			if(CollectionUtils.isNotEmpty(windowDatas)) {
				LOGGER.debug("超时数据包重新发送：{}-{}", this.sendId, windowDatas.size());
				windowDatas.forEach(windowData -> {
					if(windowData.discard()) {
						LOGGER.debug("超时数据包重新发送失败（次数超限）：{}", windowData);
						this.sendWindow.discard(windowData.getSeqnr());
					} else {
						this.data(windowData);
					}
				});
			}
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * <p>处理数据消息</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param acknr 响应编号
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 */
	private void data(int timestamp, short seqnr, short acknr, ByteBuffer buffer) throws NetException {
		if(!this.connect) {
			LOGGER.debug("UTP通道没有连接：{}-{}", seqnr, acknr);
			this.close();
			return;
		}
		LOGGER.debug("处理数据消息：{}-{}", seqnr, acknr);
		try {
			this.recvWindow.receive(timestamp, seqnr, buffer);
		} catch (IOException e) {
			throw new NetException(e);
		} finally {
			// 响应消息响应编号：最后一次接收请求编号
			this.state(timestamp, this.recvWindow.seqnr());
		}
	}
	
	/**
	 * <p>发送数据消息</p>
	 * 
	 * @param windowData 数据消息
	 */
	private void data(UtpWindowData windowData) {
		LOGGER.debug("发送数据消息：{}", windowData);
		final int now = windowData.updateGetTimestamp();
		final ByteBuffer buffer = this.buildMessage(UtpConfig.Type.DATA, windowData.getLength() + UtpConfig.HEADER_LENGTH);
		buffer.putShort(this.sendId);
		buffer.putInt(now);
		buffer.putInt(now - this.recvWindow.timestamp());
		buffer.putInt(this.recvWindow.wndSize());
		buffer.putShort(windowData.getSeqnr());
		buffer.putShort(this.recvWindow.seqnr());
		buffer.put(windowData.getData());
		this.pushMessage(buffer);
	}

	/**
	 * <p>处理响应消息</p>
	 * 
	 * @param timestamp 时间戳
	 * @param seqnr 请求编号
	 * @param acknr 响应编号
	 * @param wndSize 窗口大小
	 */
	private void state(int timestamp, short seqnr, short acknr, int wndSize) {
		LOGGER.debug("处理响应消息：{}-{}", seqnr, acknr);
		if(!this.connect) {
			// 没有连接
			this.connect = this.available();
			if(this.connect) {
				// 注意：seqnr - 1
				this.recvWindow.connect(timestamp, (short) (seqnr - 1));
			}
			this.unlockConnect();
		}
		// 是否丢包
		final boolean loss = this.sendWindow.ack(acknr, wndSize);
		if(loss) {
			// 快速重传：多次返回已处理的数据编号视为丢包
			if(this.ackLossTimes.incrementAndGet() > UtpConfig.FAST_ACK_RETRY_TIMES) {
				// 重新发送最后一个未确认数据包
				final var packet = this.sendWindow.lastUnack();
				if(packet != null) {
					LOGGER.debug("快速重传：{}-{}", acknr, packet);
					this.data(packet);
				}
			}
		} else {
			this.ackLossTimes.set(0);
		}
	}
	
	/**
	 * <p>发送响应消息</p>
	 * <p>响应消息不用增加seqnr</p>
	 * 
	 * @param timestamp 时间戳
	 * @param acknr 响应编号
	 */
	private void state(int timestamp, short acknr) {
		LOGGER.debug("发送响应消息：{}", acknr);
		final int now = DateUtils.timestampUs();
		final ByteBuffer buffer = this.buildMessage(UtpConfig.Type.STATE, UtpConfig.HEADER_LENGTH);
		buffer.putShort(this.sendId);
		buffer.putInt(now);
		buffer.putInt(now - timestamp);
		buffer.putInt(this.recvWindow.wndSize());
		buffer.putShort(this.sendWindow.seqnr());
		buffer.putShort(acknr);
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
		LOGGER.debug("处理结束消息：{}-{}-{}", seqnr, acknr, this.socketAddress);
		// 如果没有连接不用发送响应消息：防止一直往返确认造成死循环
		if(this.connect) {
			this.state(timestamp, seqnr);
		}
		this.closeRemote();
	}
	
	/**
	 * <p>发送结束消息</p>
	 */
	private void fin() {
		LOGGER.debug("发送结束消息：{}", this.socketAddress);
		final ByteBuffer buffer = this.buildMessage(UtpConfig.Type.FIN, UtpConfig.HEADER_LENGTH);
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
		LOGGER.debug("处理重置消息：{}-{}-{}", seqnr, acknr, this.socketAddress);
		// 如果没有连接不用发送响应消息：防止一直往返确认造成死循环
		if(this.connect) {
			this.state(timestamp, seqnr);
		}
		this.closeRemote();
	}
	
	/**
	 * <p>发送重置消息</p>
	 */
	private void reset() {
		LOGGER.debug("发送重置消息：{}", this.socketAddress);
		final ByteBuffer buffer = this.buildMessage(UtpConfig.Type.RESET, UtpConfig.HEADER_LENGTH);
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
		LOGGER.debug("处理握手消息：{}-{}-{}", seqnr, acknr, this.socketAddress);
		if(!this.connect) {
			this.connect = true;
			// seqnr可以设置为随机值：响应需要默认请求编号（acknr）
			this.recvWindow.connect(timestamp, seqnr);
		}
		this.state(timestamp, seqnr);
	}
	
	/**
	 * <p>发送握手消息</p>
	 */
	private void syn() {
		LOGGER.debug("发送握手消息：{}", this.socketAddress);
		final UtpWindowData windowData = this.sendWindow.build();
		final ByteBuffer buffer = this.buildMessage(UtpConfig.Type.SYN, UtpConfig.HEADER_LENGTH);
		buffer.putShort(this.recvId);
		buffer.putInt(windowData.updateGetTimestamp());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort(windowData.getSeqnr());
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}

	/**
	 * <p>新建消息</p>
	 * 
	 * @param type 消息类型
	 * @param size 消息长度
	 * 
	 * @return 消息
	 */
	private ByteBuffer buildMessage(UtpConfig.Type type, int size) {
		final ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.put(type.typeVersion());
		buffer.put(UtpConfig.EXTENSION);
		return buffer;
	}
	
	/**
	 * <p>发送UTP消息</p>
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
	 * <p>关闭窗口</p>
	 * <p>关闭窗口释放信号量才能发送关闭和重置消息（否者可能一直等待）</p>
	 */
	private void closeWindow() {
		this.sendWindow.close();
		this.recvWindow.close();
	}
	
	/**
	 * <p>关闭资源</p>
	 */
	private void closeConnect() {
		super.close();
		this.connect = false;
		this.utpContext.remove(this);
	}

	/**
	 * <p>关闭远程</p>
	 */
	private void closeRemote() {
		this.closeWindow();
		this.closeConnect();
	}
	
	@Override
	public void close() {
		if(this.close) {
			return;
		}
		this.closeWindow();
		if(this.connect) {
			this.fin();
		} else {
			this.reset();
		}
		this.closeConnect();
	}
	
}
