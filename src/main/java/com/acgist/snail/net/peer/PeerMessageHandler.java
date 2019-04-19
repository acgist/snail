package com.acgist.snail.net.peer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.peer.MessageType.Action;
import com.acgist.snail.net.peer.extension.ExtensionMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.manager.PeerSessionManager;
import com.acgist.snail.system.manager.TorrentSessionManager;
import com.acgist.snail.utils.StringUtils;

/**
 * Peer消息处理
 * http://www.bittorrent.org/beps/bep_0003.html
 * http://www.bittorrent.org/beps/bep_0004.html
 * http://www.bittorrent.org/beps/bep_0009.html
 * http://www.bittorrent.org/beps/bep_0010.html
 * http://www.bittorrent.org/beps/bep_0011.html
 * https://wiki.theory.org/index.php/BitTorrentSpecification
 * https://blog.csdn.net/li6322511/article/details/79002753
 * https://blog.csdn.net/p312011150/article/details/81478237
 * https://blog.csdn.net/weixin_41310209/article/details/87165399
 * TODO：实现Exctended消息
 */
public class PeerMessageHandler extends TcpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerMessageHandler.class);
	
	/**
	 * 协议名称
	 */
	private static final String HANDSHAKE_NAME = "BitTorrent protocol";
	private static final byte[] HANDSHAKE_NAME_BYTES = HANDSHAKE_NAME.getBytes();
	/**
	 * 保留位：http://www.bittorrent.org/beps/bep_0004.html
	 */
	private static final byte[] HANDSHAKE_RESERVED = {0, 0, 0, 0, 0, 0, 0, 0};
	private static final int HANDSHAKE_LENGTH = 68;
	
	private volatile boolean handshake = false; // 是否握手
	
	private static final int DHT_PROTOCOL = 1; // 0x01
	private static final int EXTENSION_PROTOCOL = 1 << 4; // 0x10
	
	static {
		HANDSHAKE_RESERVED[5] |= EXTENSION_PROTOCOL; // Extension Protocol
//		HANDSHAKE_RESERVED[7] |= DHT_PROTOCOL; // DHT Protocol
	}

	/**
	 * 如果消息长度不够一个Integer长度时使用
	 */
	private static final int INTEGER_BYTE_LENGTH = 4;
	private ByteBuffer lengthStick = ByteBuffer.allocate(INTEGER_BYTE_LENGTH);
	
	private byte[] reserved = new byte[8];
	private ByteBuffer buffer;
	private PeerClient peerClient;
	
	private MessageType.Action action; // 客户端动作，默认：下载
	
	private PeerSession peerSession;
	private TorrentSession torrentSession;
	private TorrentStreamGroup torrentStreamGroup;
	
	private ExtensionMessageHandler extensionMessageHandler;
	
	public PeerMessageHandler(PeerSession peerSession, TorrentSession torrentSession) {
		this.action = Action.download;
		init(peerSession, torrentSession);
	}

	/**
	 * 初始化
	 */
	private void init(PeerSession peerSession, TorrentSession torrentSession) {
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.torrentStreamGroup = torrentSession.torrentStreamGroup();
		this.extensionMessageHandler = ExtensionMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
	}

	/**
	 * 初始化
	 */
	private void init(String infoHashHex, String peerId) {
		final TorrentSession torrentSession = TorrentSessionManager.getInstance().torrentSession(infoHashHex);
		if(torrentSession == null) { // 不存在
			return;
		}
		final TaskSession taskSession = torrentSession.taskSession();
		if(taskSession == null) {
			return;
		}
		InetSocketAddress address = null;
		try {
			address = (InetSocketAddress) socket.getRemoteAddress();
		} catch (IOException e) {
			LOGGER.error("获取远程客户端端口异常", e);
		}
		if(address == null) {
			return;
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("远程Peer客户端连接：{}:{}", address.getHostString(), address.getPort());
		}
		final PeerSession peerSession = PeerSessionManager.getInstance().newPeerSession(infoHashHex, taskSession.statistics(), address.getHostString(), address.getPort());
		init(peerSession, torrentSession);
	}
	
	@Override
	public boolean onMessage(ByteBuffer attachment) {
		boolean doNext = true; // 是否继续处理消息
		int length = 0;
		attachment.flip();
		while(true) {
			if(buffer == null) {
				if(handshake) {
					for (int index = 0; index < attachment.limit(); index++) {
						lengthStick.put(attachment.get());
						if(lengthStick.position() == INTEGER_BYTE_LENGTH) {
							break;
						}
					}
					if(lengthStick.position() == INTEGER_BYTE_LENGTH) {
						lengthStick.flip();
						length = lengthStick.getInt();
						lengthStick.compact();
					} else {
						break;
					}
				} else { // 握手
					length = HANDSHAKE_LENGTH;
				}
				if(length == 0) { // 心跳
					keepAlive();
					break;
				}
				buffer = ByteBuffer.allocate(length);
			} else {
				length = buffer.capacity() - buffer.position();
			}
			final int remaining = attachment.remaining();
			if(remaining > length) { // 包含一个完整消息
				byte[] bytes = new byte[length];
				attachment.get(bytes);
				buffer.put(bytes);
				oneMessage(buffer);
				buffer = null;
			} else if(remaining == length) { // 刚好一个完整消息
				byte[] bytes = new byte[length];
				attachment.get(bytes);
				buffer.put(bytes);
				oneMessage(buffer);
				buffer = null;
				break;
			} else if(remaining < length) { // 不是完整消息
				byte[] bytes = new byte[remaining];
				attachment.get(bytes);
				buffer.put(bytes);
				break;
			}
		}
		return doNext;
	}
	
	private boolean oneMessage(final ByteBuffer buffer) {
		buffer.flip();
		if(!handshake) {
			handshake = true;
			handshake(buffer);
		} else {
			final byte typeValue = buffer.get();
			final MessageType.Type type = MessageType.Type.valueOf(typeValue);
			if(type == null) {
				LOGGER.warn("不支持的类型：{}", typeValue);
				return true;
			}
			LOGGER.debug("Peer消息类型：{}", type);
			switch (type) {
			case choke:
				choke(buffer);
				break;
			case unchoke:
				unchoke(buffer);
				break;
			case interested:
				interested(buffer);
				break;
			case notInterested:
				notInterested(buffer);
				break;
			case have:
				have(buffer);
				break;
			case bitfield:
				bitfield(buffer);
				break;
			case request:
				request(buffer);
				break;
			case piece:
				piece(buffer);
				break;
			case cancel:
				cancel(buffer);
				break;
			case port:
				port(buffer);
				break;
			case extension:
				extension(buffer);
				break;
			}
		}
		return true;
	}

	/**
	 * 握手：
	 * 消息格式：pstrlen pstr reserved info_hash peer_id
	 * pstrlen：pstr的长度：19<br>
	 * pstr：BitTorrent协议的关键字：BitTorrent protocol<br>
	 * reserved：8字节，用于扩展BT协议，一般都设置：0<br>
	 * info_hash：info_hash<br>
	 * peer_id：peer_id
	 */
	public void handshake(PeerClient peerClient) {
		LOGGER.debug("握手");
		this.peerClient = peerClient;
		final ByteBuffer buffer = ByteBuffer.allocate(HANDSHAKE_LENGTH);
		buffer.put((byte) HANDSHAKE_NAME_BYTES.length);
		buffer.put(HANDSHAKE_NAME_BYTES);
		buffer.put(HANDSHAKE_RESERVED);
		buffer.put(torrentSession.infoHash().infoHash());
		buffer.put(PeerServer.PEER_ID.getBytes());
		send(buffer);
	}
	
	/**
	 * 处理握手
	 */
	private void handshake(ByteBuffer buffer) {
		LOGGER.debug("被握手");
		final byte length = buffer.get();
		final byte[] names = new byte[length];
		buffer.get(names);
		final String name = new String(names);
		if(!HANDSHAKE_NAME.equals(name)) {
			LOGGER.warn("下载协议错误：{}", name);
		}
		buffer.get(this.reserved);
		final byte[] infoHashs = new byte[20];
		buffer.get(infoHashs);
		final String infoHashHex = StringUtils.hex(infoHashs);
		final byte[] peerIds = new byte[20];
		buffer.get(peerIds);
		final String peerId = new String(peerIds);
		if(server) { // 服务端
			init(infoHashHex, peerId);
			handshake((PeerClient) null); // 握手
		} else { // 客户端
			peerSession.id(peerId);
		}
		extension();
		bitfield(); // 交换位图
		if(server) { // TODO：服务端：判断连接数量，阻塞|不阻塞
			unchoke();
		}
	}

	/**
	 * 4字节：消息持久：len=0000
	 * 只有消息长度，没有消息编号和负载
	 */
	public void keepAlive() {
		pushMessage(null, null);
	}
	
	/**
	 * 5字节：len=0001 id=0
	 * 阻塞
	 */
	public void choke() {
		LOGGER.debug("阻塞");
		peerSession.amChoke();
		pushMessage(MessageType.Type.choke, null);
	}

	private void choke(ByteBuffer buffer) {
		LOGGER.debug("被阻塞");
		peerSession.peerChoke();
		if(peerClient != null) {
			peerClient.release();
		}
	}
	
	/**
	 * 5字节：len=0001 id=1
	 * 解除阻塞
	 */
	public void unchoke() {
		LOGGER.debug("解除阻塞");
		peerSession.amUnchoke();
		pushMessage(MessageType.Type.unchoke, null);
	}
	
	private void unchoke(ByteBuffer buffer) {
		LOGGER.debug("被解除阻塞");
		peerSession.peerUnchoke();
		if(action == Action.download) {
			if(peerClient != null) {
				peerClient.launcher(); // 开始下载
			}
		}
	}
	
	/**
	 * 5字节：len=0001 id=2
	 * 收到have消息时，客户端对Peer感兴趣
	 */
	public void interested() {
		LOGGER.debug("感兴趣");
		peerSession.amInterested();
		pushMessage(MessageType.Type.interested, null);
	}

	private void interested(ByteBuffer buffer) {
		LOGGER.debug("被感兴趣");
		peerSession.peerInterested();
	}

	/**
	 * 5字节：len=0001 id=3
	 * 客户端对Peer不感兴趣
	 */
	public void notInterested() {
		LOGGER.debug("不感兴趣");
		peerSession.amNotInterested();
		pushMessage(MessageType.Type.notInterested, null);
	}

	private void notInterested(ByteBuffer buffer) {
		LOGGER.debug("被不感兴趣");
		peerSession.peerNotInterested();
	}

	/**
	 * 5字节：len=0005 id=4 piece_index
	 * piece index：piece下标，每当客户端下载完piece，发出have消息告诉所有与客户端连接的Peer
	 */
	public void have(int index) {
		LOGGER.debug("发送have消息：{}", index);
		pushMessage(MessageType.Type.have, ByteBuffer.allocate(4).putInt(index).array());
	}

	private void have(ByteBuffer buffer) {
		final int index = buffer.getInt();
		LOGGER.debug("收到have消息：{}", index);
		peerSession.piece(index);
		if(torrentStreamGroup.have(index)) { // 已有=不感兴趣
			notInterested();
		} else { // 没有=感兴趣
			interested();
		}
	}

	/**
	 * 长度不固定：len=0001+X id=5 bitfield
	 * 交换位图：X=bitfield.length，握手后交换位图，每个piece占一位
	 */
	public void bitfield() {
		final BitSet pieces = torrentStreamGroup.pieces();
		LOGGER.debug("发送位图：{}", pieces);
		pushMessage(MessageType.Type.bitfield, pieces.toByteArray());
	}

	/**
	 * 交换位图
	 */
	private void bitfield(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BitSet pieces = BitSet.valueOf(bytes);
		peerSession.pieces(pieces);
		LOGGER.debug("收到位图：{}", pieces);
		final BitSet notHave = new BitSet();
		notHave.or(pieces);
		notHave.andNot(torrentStreamGroup.pieces());
		LOGGER.debug("感兴趣位图：{}", notHave);
		if(notHave.cardinality() == 0) {
			notInterested();
		} else {
			interested();
		}
	}

	/**
	 * 13字节：len=0013 id=6 index begin length
	 * index：piece的索引
	 * begin：piece内的偏移
	 * length：请求Peer发送的数据的长度
	 * 当客户端收到Peer的unchoke请求后即可构建request消息，一般交换数据是以slice（长度16KB的块）为单位的
	 * TODO：流水线处理
	 */
	public void request(int index, int begin, int length) {
		if(peerSession.isPeerChocking()) {
			return; // 被阻塞不发送请求
		}
		LOGGER.debug("发送请求：{}-{}-{}", index, begin, length);
		ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		pushMessage(MessageType.Type.request, buffer.array());
	}

	private void request(ByteBuffer buffer) {
		if(peerSession.isAmChocking()) { // 被阻塞不操作
			return;
		}
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		final int length = buffer.getInt();
		LOGGER.debug("收到请求：{}-{}-{}", index, begin, length);
		if(torrentStreamGroup.have(index)) {
			byte[] bytes = torrentStreamGroup.read(index, begin, length);
			piece(index, begin, bytes); // bytes == null也要发送数据
		}
	}

	/**
	 * 长度不固定：len=0009+X id=7 index begin block
	 * piece消息：X=block长度（一般为16KB），收到request消息，如果没有Peer未被阻塞，且存在slice，则返回数据
	 */
	public void piece(int index, int begin, byte[] bytes) {
		if(bytes == null) {
			return;
		}
		LOGGER.debug("发送响应：{}-{}", index, begin);
		ByteBuffer buffer = ByteBuffer.allocate(8 + bytes.length);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.put(bytes);
		pushMessage(MessageType.Type.piece, buffer.array());
	}

	private void piece(ByteBuffer buffer) {
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		LOGGER.debug("收到响应：{}-{}", index, begin);
		final int remaining = buffer.remaining();
		byte[] bytes = null;
		if(remaining > 0) {
			bytes = new byte[remaining];
			buffer.get(bytes);
		}
		if(peerClient != null) {
			peerClient.piece(index, begin, bytes);
		}
	}

	/**
	 * 13字节：len=0013 id=8 index begin length
	 * 与request作用相反，取消下载
	 */
	public void cancel(int index, int begin, int length) {
		ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		pushMessage(MessageType.Type.cancel, buffer.array());
	}
	
	private void cancel(ByteBuffer buffer) {
		// 不处理
	}
	
	/**
	 * 3字节：len=0003 id=9 listen-port
	 * listen-port：两字节
	 * 支持DHT的客户端使用，指明DHT监听的端口
	 */
	public void port(short port) {
		pushMessage(MessageType.Type.port, ByteBuffer.allocate(4).putShort(port).array());
	}
	
	private void port(ByteBuffer buffer) {
		// TODO：DHT
	}

	/**
	 * 扩展消息：len=unknow id=20 消息
	 */
	public void extension() {
		if(supportExtensionProtocol()) {
			LOGGER.debug("发送扩展消息");
			extensionMessageHandler.handshake();
		}
	}
	
	private void extension(ByteBuffer buffer) {
		LOGGER.debug("收到扩展消息");
		extensionMessageHandler.onMessage(buffer);
	}
	
	public void download() {
		action(Action.download);
	}
	
	public void torrent() {
		action(Action.torrent);
	}
	
	public Action action() {
		return this.action;
	}
	
	public void action(Action action) {
		this.action = action;
	}
	
	/**
	 * 发送消息
	 */
	public void pushMessage(MessageType.Type type, byte[] payload) {
		send(buildMessage(type, payload));
	}
	
	/**
	 * 创建消息：
	 * 消息格式：length_prefix message_ID payload<br>
	 * length prefix：4字节：message id和payload的长度和<br>
	 * message id：1字节：指明消息的编号<br>
	 * payload：消息内容
	 */
	private ByteBuffer buildMessage(MessageType.Type type, byte[] payload) {
		final Byte id = type.value();
		int capacity = 0;
		if(id != null) {
			capacity += 1;
		}
		if(payload != null) {
			capacity += payload.length;
		}
		final ByteBuffer buffer = ByteBuffer.allocate(capacity + 4); // +4=length prefix
		buffer.putInt(capacity);
		if(id != null) {
			buffer.put(id);
		}
		if(payload != null) {
			buffer.put(payload);
		}
		return buffer;
	}
	
	private boolean supportExtensionProtocol() {
		if(this.reserved != null) {
			return (this.reserved[5] & EXTENSION_PROTOCOL) != 0;
		}
		return false;
	}
	
	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		super.failed(exc, attachment);
		if(peerClient != null) {
			peerClient.release();
		}
	}
	
}
