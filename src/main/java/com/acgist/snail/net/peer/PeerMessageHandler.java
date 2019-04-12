package com.acgist.snail.net.peer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.manager.PeerSessionManager;
import com.acgist.snail.system.manager.TorrentSessionManager;
import com.acgist.snail.utils.StringUtils;

/**
 * Peer消息处理
 * https://blog.csdn.net/li6322511/article/details/79002753
 * https://blog.csdn.net/p312011150/article/details/81478237
 */
public class PeerMessageHandler extends TcpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerMessageHandler.class);
	
	/**
	 * 协议名称
	 */
	private static final String HANDSHAKE_NAME = "BitTorrent protocol";
	private static final byte[] HANDSHAKE_NAME_BYTES = HANDSHAKE_NAME.getBytes();
	private static final byte[] HANDSHAKE_RESERVED = {0, 0, 0, 0, 0, 0, 0, 0};
	private static final int HANDSHAKE_LENGTH = 68;
	
	private volatile boolean init = false; // 初始化
	private volatile boolean handshake = false; // 是否握手
	private ByteBuffer buffer;
	private PeerClient peerClient;
	
	private PeerSession peerSession;
	private TorrentSession torrentSession;
	private TorrentStreamGroup torrentStreamGroup;
	
	public PeerMessageHandler() {
	}

	public PeerMessageHandler(PeerSession peerSession, TorrentSession torrentSession) {
		init(peerSession, torrentSession);
	}

	/**
	 * 初始化
	 */
	private void init(PeerSession peerSession, TorrentSession torrentSession) {
		init = true;
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.torrentStreamGroup = torrentSession.torrentStreamGroup();
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
		final PeerSession peerSession = PeerSessionManager.getInstance().newPeer(infoHashHex, taskSession.statistics(), address.getHostString(), address.getPort());
		init(peerSession, torrentSession);
	}
	
	@Override
	public boolean doMessage(Integer result, ByteBuffer attachment) {
		boolean doNext = true; // 是否继续处理消息
		if (result == 0) {
			LOGGER.info("读取空消息");
		} else {
			int length = 0;
			attachment.flip();
			while(true) {
				if(buffer == null) {
					if(handshake) {
						length = attachment.getInt();
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
		}
		return doNext;
	}
	
	private boolean oneMessage(final ByteBuffer buffer) {
		buffer.flip();
		if(!handshake) {
			handshake = true;
			handshake(buffer);
		} else {
			final byte type = buffer.get();
			LOGGER.debug("消息类型：{}", type);
			switch (type) {
			case 0:
				choke(buffer);
				break;
			case 1:
				unchoke(buffer);
				break;
			case 2:
				interested(buffer);
				break;
			case 3:
				notInterested(buffer);
				break;
			case 4:
				have(buffer);
				break;
			case 5:
				bitfield(buffer);
				break;
			case 6:
				request(buffer);
				break;
			case 7:
				piece(buffer);
				break;
			case 8:
				cancel(buffer);
				break;
			case 9:
				port(buffer);
				break;
			default:
				LOGGER.warn("不支持的类型：{}", type);
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
		this.peerClient = peerClient;
		ByteBuffer buffer = ByteBuffer.allocate(HANDSHAKE_LENGTH);
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
		final byte length = buffer.get();
		final byte[] names = new byte[length];
		buffer.get(names);
		final String name = new String(names);
		if(!HANDSHAKE_NAME.equals(name)) {
			LOGGER.warn("下载协议错误：{}", name);
		}
		final byte[] infoHashs = new byte[20];
		buffer.get(infoHashs);
		final String infoHashHex = StringUtils.hex(infoHashs);
		final byte[] peerIds = new byte[20];
		buffer.get(peerIds);
		final String peerId = new String(peerIds);
		if(!init) {
			init(infoHashHex, peerId);
		} else {
			peerSession.id(peerId);
		}
		bitfield(); // 交换位图
		if(server) { // TODO：服务端：判断连接数量，阻塞|不阻塞
			if(init) {
			} else { // 没有初始化
				choke();
			}
		}
	}
	
	/**
	 * 4字节：消息持久：len=0000
	 * 只有消息长度，没有消息编号和负载
	 */
	public void keepAlive() {
		send(buildMessage(null, null));
	}
	
	/**
	 * 5字节：len=0001 id=0
	 * 阻塞
	 */
	public void choke() {
		peerSession.amChoke();
		send(buildMessage(Byte.decode("0"), null));
	}

	private void choke(ByteBuffer buffer) {
		peerSession.peerChoke();
		peerClient.release();
	}
	
	/**
	 * 5字节：len=0001 id=1
	 * 解除阻塞
	 */
	public void unchoke() {
		peerSession.amUnchoke();
		send(buildMessage(Byte.decode("1"), null));
	}
	
	private void unchoke(ByteBuffer buffer) {
		peerSession.peerUnchoke();
		peerClient.launcher(); // 开始下载
	}
	
	/**
	 * 5字节：len=0001 id=2
	 * 收到have消息时，客户端对Peer感兴趣
	 */
	public void interested() {
		peerSession.amInterested();
		send(buildMessage(Byte.decode("2"), null));
	}

	private void interested(ByteBuffer buffer) {
		peerSession.peerInterested();
	}

	/**
	 * 5字节：len=0001 id=3
	 * 客户端对Peer不感兴趣
	 */
	public void notInterested() {
		peerSession.amNotInterested();
		send(buildMessage(Byte.decode("3"), null));
	}

	private void notInterested(ByteBuffer buffer) {
		peerSession.peerNotInterested();
	}

	/**
	 * 5字节：len=0005 id=4 piece_index
	 * piece index：piece下标，每当客户端下载完piece，发出have消息告诉所有与客户端连接的Peer
	 */
	public void have(int index) {
		send(buildMessage(Byte.decode("4"), ByteBuffer.allocate(4).putInt(index).array()));
	}

	private void have(ByteBuffer buffer) {
		int index = buffer.getInt();
		peerSession.bitSet(index);
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
		send(buildMessage(Byte.decode("5"), pieces.toByteArray()));
	}

	/**
	 * 交换位图
	 */
	private void bitfield(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BitSet bitSet = BitSet.valueOf(bytes);
		peerSession.bitSet(bitSet);
		LOGGER.debug("交换位图：{}", bitSet);
	}

	/**
	 * 13字节：len=0013 id=6 index begin length
	 * index：piece的索引
	 * begin：piece内的偏移
	 * length：请求Peer发送的数据的长度
	 * 当客户端收到Peer的unchoke请求后即可构建request消息，一般交换数据是以slice（长度16KB的块）为单位的
	 */
	public void request(int index, int begin, int length) {
		if(peerSession.isPeerChocking()) {
			return; // 被阻塞不发送请求
		}
		ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		send(buildMessage(Byte.decode("6"), buffer.array()));
	}

	private void request(ByteBuffer buffer) {
		if(peerSession.isAmChocking()) { // 被阻塞不操作
			return;
		}
		int index = buffer.getInt();
		int begin = buffer.getInt();
		int length = buffer.getInt();
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
		ByteBuffer buffer = ByteBuffer.allocate(8 + bytes.length);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.put(bytes);
		send(buildMessage(Byte.decode("7"), buffer.array()));
	}

	private void piece(ByteBuffer buffer) {
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		final int remaining = buffer.remaining();
		byte[] bytes = null;
		if(remaining > 0) {
			bytes = new byte[remaining];
			buffer.get(bytes);
		}
		peerClient.piece(index, begin, bytes);
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
		send(buildMessage(Byte.decode("8"), buffer.array()));
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
		send(buildMessage(Byte.decode("9"), ByteBuffer.allocate(4).putShort(port).array()));
	}
	
	private void port(ByteBuffer buffer) {
		// TODO：DHT
	}
	
	/**
	 * 消息：
	 * 消息格式：length_prefix message_ID payload<br>
	 * length prefix：4字节：message id和payload的长度和<br>
	 * message id：1字节：指明消息的编号<br>
	 * payload：消息内容
	 */
	public ByteBuffer buildMessage(Byte id, byte[] payload) {
		int capacity = 0;
		if(id != null) {
			capacity += 1;
		}
		if(payload != null) {
			capacity += payload.length;
		}
		ByteBuffer buffer = ByteBuffer.allocate(capacity + 4); // +4=length prefix
		buffer.putInt(capacity);
		if(id != null) {
			buffer.put(id);
		}
		if(payload != null) {
			buffer.put(payload);
		}
		return buffer;
	}

}
