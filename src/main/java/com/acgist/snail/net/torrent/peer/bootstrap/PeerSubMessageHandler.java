package com.acgist.snail.net.torrent.peer.bootstrap;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.torrent.IMessageEncryptHandler;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.bootstrap.PeerConnect;
import com.acgist.snail.net.torrent.bootstrap.PeerLauncher;
import com.acgist.snail.net.torrent.peer.bootstrap.dht.DhtExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.ltep.ExtensionMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerConfig.Type;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.BitfieldUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Peer消息代理（TCP/UDP）</p>
 * <p>The BitTorrent Protocol Specification</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0003.html</p>
 * <p>Private Torrents</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0027.html</p>
 * <pre>
 * D = 目前正在下载（有需要的文件部份且没有禁止连接）
 * d = 客户端请求下载，但用户拒绝传输（有需要的文件部份但连接被禁止）
 * U = 目前正在上传（需要的文件部份且没有禁止连接）
 * u = 用户请求客户端上传，但客户端拒绝（有需要的文件部份但连接被禁止）
 * O = 刷新并接受禁止连接的用户
 * S = 用户被拒（一段时间没有传送任何数据的用户，一般是60秒）
 * I = 用户为传入连接
 * K = 客户端没有用户需要的文件部份
 * ? = 用户没有客户端需要的文件部份
 * X = 通过Peer Exchange（PEX）获取的用户列表所包含的用户或IPv6用户通知客户端其IPv4地址
 * H = 通过DHT连接的用户
 * E = 用户正使用协议加密连接（全部流量）
 * e = 用户正使用协议加密连接（握手）
 * P = 用户正使用uTP连接
 * L = 用户是本地的（通过网络广播或是保留的本地IP范围发现）
 * </pre>
 * <p>消息格式：长度 类型 负载</p>
 * <p>加密：如果Peer没有强制使用加密，优先使用明文。</p>
 * 
 * TODO：流水线
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class PeerSubMessageHandler implements IMessageCodec<ByteBuffer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerSubMessageHandler.class);
	
	/**
	 * 握手超时时间
	 */
	private static final int HANDSHAKE_TIMEOUT = 5;
	
	/**
	 * 发送握手
	 */
	private volatile boolean handshakeSend = false;
	/**
	 * 处理握手
	 */
	private volatile boolean handshakeRecv = false;
	/**
	 * Peer信息
	 */
	private PeerSession peerSession;
	/**
	 * Torrent信息
	 */
	private TorrentSession torrentSession;
	/**
	 * 连入客户端
	 */
	private PeerConnect peerConnect;
	/**
	 * 请求客户端
	 */
	private PeerLauncher peerLauncher;
	/**
	 * 消息代理
	 */
	private IMessageEncryptHandler messageEncryptHandler;
	/**
	 * 扩展消息代理
	 */
	private ExtensionMessageHandler extensionMessageHandler;
	/**
	 * DHT扩展消息代理
	 */
	private DhtExtensionMessageHandler dhtExtensionMessageHandler;
	
	private PeerSubMessageHandler() {
	}

	private PeerSubMessageHandler(PeerSession peerSession, TorrentSession torrentSession) {
		init(peerSession, torrentSession, PeerConfig.HANDSHAKE_RESERVED);
	}
	
	/**
	 * 服务端
	 */
	public static final PeerSubMessageHandler newInstance() {
		return new PeerSubMessageHandler();
	}

	/**
	 * 客户端
	 */
	public static final PeerSubMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession) {
		return new PeerSubMessageHandler(peerSession, torrentSession);
	}

	/**
	 * 初始化
	 */
	private void init(PeerSession peerSession, TorrentSession torrentSession, byte[] reserved) {
		peerSession.reserved(reserved);
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.extensionMessageHandler = ExtensionMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
		this.dhtExtensionMessageHandler = DhtExtensionMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
	}

	/**
	 * 初始化：客户端连接，加入到Peer列表。
	 */
	private boolean init(String infoHashHex, byte[] peerId, byte[] reserved) {
		if(ArrayUtils.equals(PeerService.getInstance().peerId(), peerId)) {
			LOGGER.debug("Peer接入失败：PeerId一致");
			return false;
		}
		final TorrentSession torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		if(torrentSession == null) {
			LOGGER.warn("Peer接入失败：种子信息不存在");
			return false;
		}
		if(!torrentSession.uploadable()) {
			LOGGER.debug("Peer接入时任务不可上传：{}", torrentSession.action());
		}
		final InetSocketAddress socketAddress = this.remoteSocketAddress();
		if(socketAddress == null) {
			LOGGER.warn("Peer接入失败：远程客户端获取失败");
			return false;
		}
		final PeerSession peerSession = PeerManager.getInstance().newPeerSession(
			infoHashHex,
			torrentSession.statistics(),
			socketAddress.getHostString(),
			null,
			PeerConfig.SOURCE_CONNECT);
		final PeerConnect peerConnect = torrentSession.newPeerConnect(peerSession, this);
		if(peerConnect != null) {
			this.peerConnect = peerConnect;
			peerSession.peerConnect(this.peerConnect);
			init(peerSession, torrentSession, reserved);
			return true;
		} else {
			return false;
		}
	}
	
	public TorrentSession torrentSession() {
		return this.torrentSession;
	}
	
	/**
	 * 设置实际的消息代理：TCP、UDP（UTP）
	 */
	public PeerSubMessageHandler messageEncryptHandler(IMessageEncryptHandler messageEncryptHandler) {
		this.messageEncryptHandler = messageEncryptHandler;
		return this;
	}
	
	/**
	 * 是否握手完成
	 */
	public boolean handshake() {
		return this.handshakeRecv;
	}
	
	@Override
	public void onMessage(final ByteBuffer buffer) throws NetException {
		buffer.flip();
		if(!this.handshakeRecv) { // 没有握手
			handshake(buffer);
		} else { // 已经握手
			final byte typeId = buffer.get();
			final PeerConfig.Type type = PeerConfig.Type.valueOf(typeId);
			if(type == null) {
				LOGGER.warn("Peer消息错误（类型不支持）：{}", typeId);
				return;
			}
			LOGGER.debug("Peer消息类型：{}", type);
			switch (type) {
			case CHOKE:
				choke(buffer);
				break;
			case UNCHOKE:
				unchoke(buffer);
				break;
			case INTERESTED:
				interested(buffer);
				break;
			case NOT_INTERESTED:
				notInterested(buffer);
				break;
			case HAVE:
				have(buffer);
				break;
			case BITFIELD:
				bitfield(buffer);
				break;
			case REQUEST:
				request(buffer);
				break;
			case PIECE:
				piece(buffer);
				break;
			case CANCEL:
				cancel(buffer);
				break;
			case DHT:
				dht(buffer);
				break;
			case EXTENSION:
				extension(buffer);
				break;
			default:
				LOGGER.info("Peer消息错误（类型未适配）：{}", type);
				break;
			}
		}
	}

	/**
	 * <p>发送握手消息</p>
	 * <p>注：握手设置超时时间，防止握手一致等待导致一直占用线程。</p>
	 * <p>格式：pstrlen pstr reserved info_hash peer_id</p>
	 * <pre>
	 * pstrlen：协议（pstr）的长度：19
	 * pstr：BitTorrent协议：{@link PeerConfig#HANDSHAKE_NAME}
	 * reserved：8字节，用于扩展BT协议：{@link PeerConfig#HANDSHAKE_RESERVED}
	 * info_hash：info_hash
	 * peer_id：peer_id
	 * </pre>
	 */
	public void handshake(PeerLauncher peerLauncher) {
		LOGGER.debug("发送握手消息");
		this.handshakeSend = true;
		this.peerLauncher = peerLauncher;
		if(this.peerSession != null && this.peerLauncher != null) {
			this.peerSession.peerLauncher(this.peerLauncher);
		}
		final ByteBuffer buffer = ByteBuffer.allocate(PeerConfig.HANDSHAKE_LENGTH);
		buffer.put((byte) PeerConfig.HANDSHAKE_NAME_LENGTH);
		buffer.put(PeerConfig.HANDSHAKE_NAME_BYTES);
		buffer.put(PeerConfig.HANDSHAKE_RESERVED);
		buffer.put(this.torrentSession.infoHash().infoHash());
		buffer.put(PeerService.getInstance().peerId());
		this.sendEncrypt(buffer, HANDSHAKE_TIMEOUT);
	}
	
	/**
	 * <p>处理握手消息</p>
	 * <p>服务端：初始化、握手、解除阻塞。</p>
	 * <p>客户端：设置PeerId。</p>
	 * <p>通用：发送扩展消息、发送DHT消息、交换位图。</p>
	 */
	private void handshake(ByteBuffer buffer) {
		LOGGER.debug("处理握手消息");
		if(buffer.remaining() != PeerConfig.HANDSHAKE_LENGTH) {
			LOGGER.warn("握手消息格式错误（消息长度）：{}", buffer.remaining());
//			this.close(); // 不关闭，选择忽略。
			return;
		}
		final byte length = buffer.get();
		if(length != PeerConfig.HANDSHAKE_NAME_LENGTH) {
			LOGGER.warn("握手消息格式错误（协议长度）：{}", length);
//			this.close(); // 不关闭，选择忽略。
			return;
		}
		final byte[] names = new byte[length];
		buffer.get(names);
		final String name = new String(names);
		if(!PeerConfig.HANDSHAKE_NAME.equals(name)) {
			LOGGER.warn("握手消息格式错误（下载协议错误）：{}", name);
//			this.close(); // 不关闭，选择忽略。
			return;
		}
		this.handshakeRecv = true;
		final boolean server = !this.handshakeSend; // 是否是服务端
		final byte[] reserved = new byte[PeerConfig.RESERVED_LENGTH];
		buffer.get(reserved);
		final byte[] infoHash = new byte[SystemConfig.SHA1_HASH_LENGTH];
		buffer.get(infoHash);
		final String infoHashHex = StringUtils.hex(infoHash);
		final byte[] peerId = new byte[PeerConfig.PEER_ID_LENGTH];
		buffer.get(peerId);
		if(server) {
			final boolean ok = init(infoHashHex, peerId, reserved);
			if(ok) {
				handshake((PeerLauncher) null);
			} else {
				this.close();
				return;
			}
		}
		this.peerSession.id(peerId);
		extension(); // 发送扩展消息
		dht(); // 发送DHT消息
		bitfield(); // 交换位图
		if(server) {
			unchoke(); // 解除阻塞
		}
	}

	/**
	 * <p>发送心跳消息</p>
	 * <p>只有消息长度，没有消息编号和负载。</p>
	 * <p>
	 * 格式：4字节：len=0000
	 * </p>
	 */
	public void keepAlive() {
		LOGGER.debug("发送心跳消息");
		pushMessage(null, null);
	}
	
	/**
	 * <p>发送阻塞消息</p>
	 * <p>
	 * 格式：5字节：len=0001 id=0
	 * </p>
	 */
	public void choke() {
		LOGGER.debug("发送阻塞消息");
		this.peerSession.amChoke();
		pushMessage(PeerConfig.Type.CHOKE, null);
	}

	/**
	 * <p>处理阻塞消息</p>
	 * <p>阻塞后不能再进行下载请求。</p>
	 */
	private void choke(ByteBuffer buffer) {
		LOGGER.debug("处理阻塞消息");
		this.peerSession.peerChoke();
		// 不释放资源，让系统自动优化剔除。
//		if(this.peerLauncher != null) {
//			this.peerLauncher.release();
//		}
	}
	
	/**
	 * <p>发送解除阻塞消息</p>
	 * <p>解除阻塞，客户端可以进行下载。</p>
	 * <p>
	 * 格式：5字节：len=0001 id=1
	 * </p>
	 */
	public void unchoke() {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送解除阻塞消息：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("发送解除阻塞消息：Peer只上传不下载");
			return;
		}
		LOGGER.debug("发送解除阻塞消息");
		this.peerSession.amUnchoke();
		pushMessage(PeerConfig.Type.UNCHOKE, null);
	}
	
	/**
	 * <p>处理解除阻塞消息</p>
	 * <p>被解除阻塞后开始发送下载请求。</p>
	 * <p>注：即使不能下载，也需要解除阻塞状态。</p>
	 */
	private void unchoke(ByteBuffer buffer) {
		LOGGER.debug("处理解除阻塞消息");
		this.peerSession.peerUnchoke();
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理解除阻塞消息：任务不可下载");
			return;
		}
		if(this.peerLauncher != null) {
			this.peerLauncher.download(); // 开始下载
		}
	}
	
	/**
	 * <p>发送感兴趣消息</p>
	 * <p>参考：{@link #have(ByteBuffer)}</p>
	 * <p>
	 * 格式：5字节：len=0001 id=2
	 * </p>
	 */
	public void interested() {
		LOGGER.debug("发送感兴趣消息");
		this.peerSession.amInterested();
		pushMessage(PeerConfig.Type.INTERESTED, null);
	}

	/**
	 * <p>处理感兴趣消息</p>
	 */
	private void interested(ByteBuffer buffer) {
		LOGGER.debug("处理感兴趣消息");
		this.peerSession.peerInterested();
	}

	/**
	 * <p>发送不感兴趣消息</p>
	 * <p>客户端已经拥有Peer所有的Piece时发送不感兴趣。</p>
	 * <p>
	 * 格式：5字节：len=0001 id=3
	 * </p>
	 */
	public void notInterested() {
		LOGGER.debug("发送不感兴趣消息");
		this.peerSession.amNotInterested();
		pushMessage(PeerConfig.Type.NOT_INTERESTED, null);
	}

	/**
	 * <p>处理不感兴趣消息</p>
	 */
	private void notInterested(ByteBuffer buffer) {
		LOGGER.debug("处理不感兴趣消息");
		this.peerSession.peerNotInterested();
	}

	/**
	 * <p>发送have消息</p>
	 * <p>
	 * 格式：5字节：len=0005 id=4 piece_index
	 * </p>
	 * <p>
	 * piece_index：Piece下标：每当客户端下载完Piece，发送have消息告诉所有与客户端连接的Peer。
	 * </p>
	 */
	public void have(int index) {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送have消息：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("发送have消息：Peer只上传不下载");
			return;
		}
		if(this.peerSession.havePiece(index)) { // Peer已经含有该Piece时不发送have通知
			LOGGER.debug("发送have消息：Peer已经含有该Piece");
			return;
		}
		LOGGER.debug("发送have消息：{}", index);
		pushMessage(PeerConfig.Type.HAVE, ByteBuffer.allocate(4).putInt(index).array());
	}

	/**
	 * <p>处理have消息</p>
	 * <p>收到have消息时，客户端没有对应的Piece，发送感兴趣消息，表示客户端对Peer感兴趣。</p>
	 */
	private void have(ByteBuffer buffer) {
		if(this.peerSession == null) {
			LOGGER.debug("处理have消息：PeerSession为空");
			return;
		}
		final int index = buffer.getInt();
		this.peerSession.piece(index);
		LOGGER.debug("处理have消息：{}", index);
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理have消息：任务不可下载");
			return;
		}
		if(this.torrentSession.havePiece(index)) {
			// 已经含有该Piece，不能直接发送不感兴趣消息，Piece下载完成后没有更多的Piece时才发送不感兴趣消息。
//			notInterested();
		} else {
			// 如果没有该Piece，发送感兴趣消息。
			if(!this.peerSession.isAmInterested()) {
				interested();
			}
		}
	}

	/**
	 * <p>发送位图消息</p>
	 * <p>
	 * 格式：长度不固定：len=0001+X id=5 bitfield
	 * </p>
	 * <p>
	 * 交换位图：X=bitfield.length，握手后交换位图，每个Piece占一位。
	 * </p>
	 */
	public void bitfield() {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送位图消息：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("发送位图消息：Peer只上传不下载");
			return;
		}
		final BitSet pieces = this.torrentSession.pieces();
		LOGGER.debug("发送位图消息：{}", pieces);
		final int pieceSize = this.torrentSession.torrent().getInfo().pieceSize();
		pushMessage(PeerConfig.Type.BITFIELD, BitfieldUtils.toBytes(pieceSize, pieces));
	}
	
	/**
	 * <p>处理位图消息</p>
	 */
	private void bitfield(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BitSet pieces = BitfieldUtils.toBitSet(bytes); // Peer位图
		this.peerSession.pieces(pieces);
		LOGGER.debug("处理位图消息：{}", pieces);
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理位图消息：任务不可下载");
			return;
		}
		final BitSet notHave = new BitSet(); // 没有下载的位图
		notHave.or(pieces);
		notHave.andNot(this.torrentSession.pieces());
		LOGGER.debug("感兴趣位图：{}", notHave);
		if(notHave.cardinality() == 0) {
			notInterested();
		} else {
			interested();
		}
	}

	/**
	 * <p>发送request消息</p>
	 * <p>客户端收到Peer的unchoke请求后，开始发送request消息，一般交换数据是以slice（长度16KB的块）为单位的。</p>
	 * <p>
	 * 格式：13字节：len=0013 id=6 index begin length
	 * </p>
	 * <pre>
	 * index：Piece索引
	 * begin：Piece内偏移
	 * length：请求Peer发送的数据的长度
	 * </pre>
	 */
	public void request(int index, int begin, int length) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("发送request消息：任务不可下载");
			return;
		}
		if(this.peerSession.isPeerChocking()) {
			LOGGER.debug("发送request消息：阻塞");
			return;
		}
		LOGGER.debug("发送request消息：{}-{}-{}", index, begin, length);
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		pushMessage(PeerConfig.Type.REQUEST, buffer.array());
	}

	/**
	 * <p>处理request消息</p>
	 */
	private void request(ByteBuffer buffer) {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("处理request消息：任务不可上传");
			return;
		}
		if(this.peerSession.isAmChocking()) {
			LOGGER.debug("处理request消息：阻塞");
			return;
		}
		// 累计上传大小以及超过任务大小：阻塞（不上传）
		if(this.peerSession.statistics().uploadSize() > this.torrentSession.size()) {
			LOGGER.debug("累计上传大小超过任务大小：阻塞");
			this.choke();
			return;
		}
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		final int length = buffer.getInt();
		LOGGER.debug("处理request消息：{}-{}-{}", index, begin, length);
		if(this.torrentSession.havePiece(index)) {
			try {
				final byte[] bytes = this.torrentSession.read(index, begin, length);
				piece(index, begin, bytes);
			} catch (NetException e) {
				LOGGER.error("处理request消息异常", e);
			}
		}
	}

	/**
	 * <p>发送piece消息</p>
	 * <p>收到request消息，如果Peer未被阻塞，且存在slice，则返回数据。</p>
	 * <p>
	 * 格式：长度不固定：len=0009+X id=7 index begin block
	 * </p>
	 * <p>
	 * X=block长度（一般为16KB）
	 * </p>
	 */
	public void piece(int index, int begin, byte[] bytes) {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送piece消息：任务不可上传");
			return;
		}
		if(bytes == null) {
			LOGGER.debug("发送piece消息：数据为空");
			return;
		}
		LOGGER.debug("发送piece消息：{}-{}", index, begin);
		this.peerSession.upload(bytes.length); // 上传数据统计
		final ByteBuffer buffer = ByteBuffer.allocate(8 + bytes.length);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.put(bytes);
		pushMessage(PeerConfig.Type.PIECE, buffer.array());
	}

	/**
	 * <p>处理piece消息</p>
	 */
	private void piece(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理piece消息：任务不可下载");
			return;
		}
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		LOGGER.debug("处理piece消息：{}-{}", index, begin);
		final int remaining = buffer.remaining();
		byte[] bytes = null;
		if(remaining > 0) {
			bytes = new byte[remaining];
			buffer.get(bytes);
		}
		if(this.peerLauncher != null) {
			this.peerLauncher.piece(index, begin, bytes);
		}
	}

	/**
	 * <p>发送cancel消息</p>
	 * </p>与request作用相反，取消下载。</p>
	 * <p>
	 * 格式：13字节：len=0013 id=8 index begin length
	 * <p>
	 */
	public void cancel(int index, int begin, int length) {
		LOGGER.debug("发送cancel消息：{}", index);
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		pushMessage(PeerConfig.Type.CANCEL, buffer.array());
	}
	
	/**
	 * <p>处理cancel消息</p>
	 */
	private void cancel(ByteBuffer buffer) {
		LOGGER.debug("处理cancel消息");
	}
	
	/**
	 * <p>发送DHT消息</p>
	 * <p>支持DHT的客户端使用，指明DHT监听的端口。</p>
	 * <p>
	 * 格式：3字节：len=0003 id=9 listen-port
	 * </p>
	 * <p>
	 * listen-port：两字节，DHT端口。
	 * </p>
	 */
	public void dht() {
		if(this.peerSession.supportDhtProtocol()) {
			LOGGER.debug("发送DHT消息");
			this.dhtExtensionMessageHandler.port();
		}
	}
	
	/**
	 * <p>处理DHT消息</p>
	 */
	private void dht(ByteBuffer buffer) {
		this.dhtExtensionMessageHandler.onMessage(buffer);
	}

	/**
	 * <p>发送扩展消息</p>
	 * <p>
	 * 格式：长度不固定：len=0001+X id=20 ex
	 * </p>
	 * <pre>
	 * X：扩展消息长度
	 * ex：扩展消息
	 * </pre>
	 */
	public void extension() {
		if(this.peerSession.supportExtensionProtocol()) {
			LOGGER.debug("发送扩展消息");
			this.extensionMessageHandler.handshake();
		}
	}
	
	/**
	 * <p>处理扩展消息</p>
	 */
	private void extension(ByteBuffer buffer) throws NetException {
		LOGGER.debug("处理扩展消息");
		this.extensionMessageHandler.onMessage(buffer);
	}
	
	/**
	 * 发送扩展消息：pex
	 */
	public void pex(byte[] bytes) {
		this.extensionMessageHandler.pex(bytes);
	}
	
	/**
	 * 发送扩展消息：holepunch
	 */
	public void holepunch(String host, Integer port) {
		this.extensionMessageHandler.holepunch(host, port);
	}
	
	/**
	 * 发送扩展消息：连接
	 */
	public void holepunchConnect(String host, int port) {
		this.extensionMessageHandler.holepunchConnect(host, port);
	}
	
	/**
	 * 发送扩展消息：uploadOnly
	 */
	public void uploadOnly() {
		this.extensionMessageHandler.uploadOnly();
	}
	
	/**
	 * 发送消息
	 */
	public void pushMessage(PeerConfig.Type type, byte[] payload) {
		this.sendEncrypt(buildMessage(type, payload));
	}
	
	/**
	 * <p>创建消息</p>
	 * <p>
	 * 消息格式：length_prefix message_id payload
	 * </p>
	 * <pre>
	 * length_prefix：4字节：message_id和payload的长度和
	 * message_id：1字节：{@linkplain Type 消息类型编号}
	 * payload：消息内容
	 * </pre>
	 */
	private ByteBuffer buildMessage(PeerConfig.Type type, byte[] payload) {
		final Byte id = type == null ? null : type.id();
		int capacity = 0;
		if(id != null) {
			capacity += 1;
		}
		if(payload != null) {
			capacity += payload.length;
		}
		// 长度：+4 = length prefix
		final ByteBuffer buffer = ByteBuffer.allocate(capacity + 4);
		buffer.putInt(capacity);
		if(id != null) {
			buffer.put(id);
		}
		if(payload != null) {
			buffer.put(payload);
		}
		return buffer;
	}
	
	/**
	 * <p>释放Peer时使用，调用前最好不要发送其他消息，防止等待不能及时释放。</p>
	 * {@link IMessageEncryptHandler#close()}
	 */
	public void close() {
		this.messageEncryptHandler.close();
	}
	
	/**
	 * {@link IMessageEncryptHandler#available()}
	 */
	public boolean available() {
		return this.messageEncryptHandler.available();
	}
	
	/**
	 * {@link IMessageEncryptHandler#send(ByteBuffer)}
	 */
	public void send(ByteBuffer buffer) {
		try {
			this.messageEncryptHandler.send(buffer);
		} catch (NetException e) {
			LOGGER.error("Peer消息发送异常", e);
		}
	}
	
	/**
	 * {@link IMessageEncryptHandler#send(ByteBuffer, int)}
	 */
	public void send(ByteBuffer buffer, int timeout) {
		try {
			this.messageEncryptHandler.send(buffer, timeout);
		} catch (NetException e) {
			LOGGER.error("Peer消息发送异常", e);
		}
	}
	
	/**
	 * {@link IMessageEncryptHandler#sendEncrypt(ByteBuffer)}
	 */
	public void sendEncrypt(ByteBuffer buffer) {
		try {
			this.messageEncryptHandler.sendEncrypt(buffer);
		} catch (NetException e) {
			LOGGER.error("Peer消息发送异常", e);
		}
	}

	/**
	 * {@link IMessageEncryptHandler#sendEncrypt(ByteBuffer, int)}
	 */
	public void sendEncrypt(ByteBuffer buffer, int timeout) {
		try {
			this.messageEncryptHandler.sendEncrypt(buffer, timeout);
		} catch (NetException e) {
			LOGGER.error("Peer消息发送异常", e);
		}
	}
	
	/**
	 * {@link IMessageEncryptHandler#remoteSocketAddress()}
	 */
	private InetSocketAddress remoteSocketAddress() {
		return this.messageEncryptHandler.remoteSocketAddress();
	}

}
