package com.acgist.snail.net.torrent.peer.bootstrap;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.torrent.IMessageEncryptHandler;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.bootstrap.PeerDownloader;
import com.acgist.snail.net.torrent.bootstrap.PeerUploader;
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
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Peer消息代理（TCP/UDP）</p>
 * <p>The BitTorrent Protocol Specification</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0003.html</p>
 * <p>Fast Extension</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0006.html</p>
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
	 * <p>握手超时时间</p>
	 */
	public static final int HANDSHAKE_TIMEOUT = SystemConfig.CONNECT_TIMEOUT;
	
	/**
	 * <p>是否已经发送握手</p>
	 */
	private volatile boolean handshakeSend = false;
	/**
	 * <p>是否已经处理握手</p>
	 */
	private volatile boolean handshakeRecv = false;
	/**
	 * <p>是否是客户端</p>
	 */
	private final boolean server;
	/**
	 * <p>Peer信息</p>
	 */
	private PeerSession peerSession;
	/**
	 * <p>Torrent信息</p>
	 */
	private TorrentSession torrentSession;
	/**
	 * <p>Peer上传客户端</p>
	 */
	private PeerUploader peerUploader;
	/**
	 * <p>Peer下载客户端</p>
	 */
	private PeerDownloader peerDownloader;
	/**
	 * <p>消息代理</p>
	 */
	private IMessageEncryptHandler messageEncryptHandler;
	/**
	 * <p>扩展消息代理</p>
	 */
	private ExtensionMessageHandler extensionMessageHandler;
	/**
	 * <p>DHT扩展消息代理</p>
	 */
	private DhtExtensionMessageHandler dhtExtensionMessageHandler;
	
	/**
	 * <p>服务端</p>
	 */
	private PeerSubMessageHandler() {
		this.server = true;
	}

	/**
	 * <p>客户端</p>
	 */
	private PeerSubMessageHandler(PeerSession peerSession, TorrentSession torrentSession) {
		this.server = false;
		init(peerSession, torrentSession, PeerConfig.HANDSHAKE_RESERVED);
	}
	
	/**
	 * <p>服务端</p>
	 */
	public static final PeerSubMessageHandler newInstance() {
		return new PeerSubMessageHandler();
	}

	/**
	 * <p>客户端</p>
	 */
	public static final PeerSubMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession) {
		return new PeerSubMessageHandler(peerSession, torrentSession);
	}

	/**
	 * <p>初始化</p>
	 */
	private void init(PeerSession peerSession, TorrentSession torrentSession, byte[] reserved) {
		peerSession.reserved(reserved);
		peerSession.reset();
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.extensionMessageHandler = ExtensionMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
		this.dhtExtensionMessageHandler = DhtExtensionMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
	}

	/**
	 * <dl>
	 * 	<dt>初始化</dt>
	 * 	<dd>加入Peer列表</dd>
	 * 	<dd>创建客户端连接</dd>
	 * </dl>
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
		if(!torrentSession.ready()) {
			LOGGER.debug("Peer接入失败：任务没有准备完成");
			return false;
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
		final PeerUploader peerUploader = torrentSession.newPeerUploader(peerSession, this);
		if(peerUploader != null) {
			this.peerUploader = peerUploader;
			peerSession.peerUploader(this.peerUploader);
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
	 * <p>是否握手完成</p>
	 */
	public boolean handshake() {
		return this.handshakeRecv;
	}
	
	/**
	 * <p>是否需要加密</p>
	 * <p>验证Peer是否偏爱加密</p>
	 */
	public boolean needEncrypt() {
		if(this.peerSession == null) {
			return false;
		}
		return this.peerSession.encrypt();
	}
	
	/**
	 * <p>设置实际的消息代理：TCP、UDP（UTP）</p>
	 */
	public PeerSubMessageHandler messageEncryptHandler(IMessageEncryptHandler messageEncryptHandler) {
		this.messageEncryptHandler = messageEncryptHandler;
		return this;
	}
	
	@Override
	public void onMessage(final ByteBuffer buffer) throws NetException {
		buffer.flip();
		if(this.handshakeRecv) { // 已经握手
			final byte typeId = buffer.get();
			final PeerConfig.Type type = PeerConfig.Type.valueOf(typeId);
			if(type == null) {
				LOGGER.warn("处理Peer消息错误（类型不支持）：{}", typeId);
				return;
			}
			if(this.peerSession == null) {
				LOGGER.debug("处理Peer消息错误（PeerSession为空）：{}", type);
				return;
			}
			LOGGER.debug("处理Peer消息类型：{}", type);
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
				// DHT扩展
			case DHT:
				dht(buffer);
				break;
				// FAST扩展
			case HAVE_ALL:
				haveAll(buffer);
				break;
			case HAVE_NONE:
				haveNone(buffer);
				break;
			case SUGGEST_PIECE:
				suggestPiece(buffer);
				break;
			case REJECT_REQUEST:
				rejectRequest(buffer);
				break;
			case ALLOWED_FAST:
				allowedFast(buffer);
				break;
				// 扩展协议
			case EXTENSION:
				extension(buffer);
				break;
			default:
				LOGGER.info("处理Peer消息错误（类型未适配）：{}", type);
				break;
			}
		} else { // 没有握手
			handshake(buffer);
		}
	}

	/**
	 * <p>发送握手消息</p>
	 * <p>注：握手设置超时时间，发送方法默认没有设置超时时间，防止握手一直等待导致一直占用线程。</p>
	 * <p>
	 * 格式：pstrlen pstr reserved info_hash peer_id
	 * </p>
	 * <pre>
	 * pstrlen：协议（pstr）的长度：19
	 * pstr：BitTorrent协议：{@link PeerConfig#HANDSHAKE_NAME}
	 * reserved：8字节，用于扩展BT协议：{@link PeerConfig#HANDSHAKE_RESERVED}
	 * info_hash：info_hash
	 * peer_id：peer_id
	 * </pre>
	 */
	public void handshake(PeerDownloader peerDownloader) {
		LOGGER.debug("发送握手消息");
		this.handshakeSend = true;
		this.peerDownloader = peerDownloader;
		if(this.peerSession != null && this.peerDownloader != null) {
			this.peerSession.peerDownloader(this.peerDownloader);
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
	 * <p>服务端：初始化、握手、解除阻塞</p>
	 * <p>客户端：设置PeerId</p>
	 * <p>通用：发送扩展消息、发送DHT消息、交换Piece位图</p>
	 */
	private void handshake(ByteBuffer buffer) {
		LOGGER.debug("处理握手消息");
		if(buffer.remaining() != PeerConfig.HANDSHAKE_LENGTH) {
			LOGGER.warn("处理握手消息格式错误（消息长度）：{}", buffer.remaining());
//			this.close(); // 不关闭：选择忽略
			return;
		}
		final byte length = buffer.get();
		if(length != PeerConfig.HANDSHAKE_NAME_LENGTH) {
			LOGGER.warn("处理握手消息格式错误（协议长度）：{}", length);
//			this.close(); // 不关闭：选择忽略
			return;
		}
		final byte[] names = new byte[length];
		buffer.get(names);
		final String name = new String(names);
		if(!PeerConfig.HANDSHAKE_NAME.equals(name)) {
			LOGGER.warn("处理握手消息格式错误（下载协议错误）：{}", name);
//			this.close(); // 不关闭：选择忽略
			return;
		}
		this.handshakeRecv = true;
		final byte[] reserved = new byte[PeerConfig.RESERVED_LENGTH];
		buffer.get(reserved);
		final byte[] infoHash = new byte[SystemConfig.SHA1_HASH_LENGTH];
		buffer.get(infoHash);
		final String infoHashHex = StringUtils.hex(infoHash);
		final byte[] peerId = new byte[PeerConfig.PEER_ID_LENGTH];
		buffer.get(peerId);
		if(this.server) {
			final boolean ok = init(infoHashHex, peerId, reserved);
			if(ok) {
				if(!this.handshakeSend) {
					handshake((PeerDownloader) null);
				}
			} else {
				this.close();
				return;
			}
		}
		this.peerSession.id(peerId);
		this.extension(); // 发送扩展消息：优先交换扩展
		this.dht(); // 发送DHT消息
		this.exchangeBitfield(); // 交换Piece位图
		if(this.server) {
			this.unchoke(); // 解除阻塞
		}
	}

	/**
	 * <p>发送心跳消息</p>
	 * <p>
	 * 格式：len=0000
	 * </p>
	 */
	public void keepAlive() {
		LOGGER.debug("发送心跳消息");
		pushMessage(null, null);
	}
	
	/**
	 * <p>发送阻塞消息</p>
	 * <p>
	 * 格式：len=0001 id=0x00
	 * </p>
	 * <p>阻塞后Peer不能进行下载</p>
	 */
	public void choke() {
		LOGGER.debug("发送阻塞消息");
		this.peerSession.amChoked();
		pushMessage(PeerConfig.Type.CHOKE, null);
	}

	/**
	 * <p>处理阻塞消息</p>
	 */
	private void choke(ByteBuffer buffer) {
		LOGGER.debug("处理阻塞消息");
		this.peerSession.peerChoked();
		// 不释放资源：让系统自动优化剔除
//		if(this.peerDownloader != null) {
//			this.peerDownloader.release();
//		} else if(this.peerUploader != null) {
//			this.peerUploader.release();
//		}
	}
	
	/**
	 * <p>发送解除阻塞消息</p>
	 * <p>
	 * 格式：len=0001 id=0x01
	 * </p>
	 * <p>解除阻塞后Peer才可以进行下载</p>
	 */
	private void unchoke() {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送解除阻塞消息：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("发送解除阻塞消息：Peer只上传不下载");
			return;
		}
		if(this.peerSession.isAmUnchoked()) {
			LOGGER.debug("发送解除阻塞消息：已经解除");
			return;
		}
		LOGGER.debug("发送解除阻塞消息");
		this.peerSession.amUnchoked();
		pushMessage(PeerConfig.Type.UNCHOKE, null);
	}
	
	/**
	 * <p>处理解除阻塞消息</p>
	 * <p>解除阻塞后开始发送下载请求</p>
	 */
	private void unchoke(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理解除阻塞消息：任务不可下载");
			return;
		}
		LOGGER.debug("处理解除阻塞消息");
		this.peerSession.peerUnchoked();
		this.unchoke();
		this.unchokeDownload();
	}
	
	/**
	 * <p>发送感兴趣消息</p>
	 * <p>
	 * 格式：len=0001 id=0x02
	 * </p>
	 * <p>收到have消息时，如果客户端没有对应的Piece，发送感兴趣消息表示客户端对Peer感兴趣。</p>
	 */
	private void interested() {
		if(this.peerSession.isAmInterested()) {
			LOGGER.debug("发送感兴趣消息：已经感兴趣");
			return;
		}
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
	 * <p>
	 * 格式：len=0001 id=0x03
	 * </p>
	 * <p>客户端已经拥有Peer所有的Piece时发送不感兴趣</p>
	 * <p>在交换Piece位图和Piece下载完成后，如果Peer没有更多的Piece时才发送不感兴趣消息，其他情况不发送此消息。</p>
	 */
	public void notInterested() {
		if(this.peerSession.isAmNotInterested()) {
			LOGGER.debug("发送不感兴趣消息：已经不感兴趣");
			return;
		}
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
	 * 格式：len=0005 id=0x04 index
	 * </p>
	 * <p>
	 * index：Piece索引
	 * </p>
	 * <p>当客户端下载完成一个Piece时，发送have消息告诉与客户端连接的Peer已经拥有该Piece。</p>
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
		if(this.peerSession.havePiece(index)) {
			LOGGER.debug("发送have消息：Peer已经含有该Piece");
			return;
		}
		LOGGER.debug("发送have消息：{}", index);
		pushMessage(PeerConfig.Type.HAVE, NumberUtils.intToBytes(index));
	}

	/**
	 * <p>处理have消息</p>
	 */
	private void have(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理have消息：任务不可下载");
			return;
		}
		final int index = buffer.getInt();
		LOGGER.debug("处理have消息：{}", index);
		this.peerSession.piece(index);
		if(!this.torrentSession.havePiece(index)) {
			interested();
		}
	}

	/**
	 * <p>发送haveAll消息</p>
	 * <p>
	 * 格式：len=0001 id=0x0E
	 * </p>
	 */
	private void haveAll() {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送haveAll消息：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("发送haveAll消息：Peer只上传不下载");
			return;
		}
		LOGGER.debug("发送haveAll消息");
		pushMessage(PeerConfig.Type.HAVE_ALL, null);
	}
	
	/**
	 * <p>处理haveAll消息</p>
	 */
	private void haveAll(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理haveAll消息：任务不可下载");
			return;
		}
		LOGGER.debug("处理haveAll消息");
		final BitSet allPieces = this.torrentSession.allPieces();
		this.peerSession.pieces(allPieces);
		this.torrentSession.fullPieces();
		if(!this.torrentSession.completed()) { // 任务没有完成发送感兴趣消息
			interested();
		}
	}
	
	/**
	 * <p>发送haveNone消息</p>
	 * <p>
	 * 格式：len=0001 id=0x0F
	 * </p>
	 */
	private void haveNone() {
		LOGGER.debug("发送haveNone消息");
		pushMessage(PeerConfig.Type.HAVE_NONE, null);
	}

	/**
	 * <p>处理haveNone消息</p>
	 */
	private void haveNone(ByteBuffer buffer) {
		LOGGER.debug("处理haveAll消息");
		this.peerSession.cleanPieces(); // 清空Peer所有Piece
	}
	
	/**
	 * <p>发送suggestPiece消息</p>
	 * <p>
	 * 格式：len=0005 id=0x0D index
	 * </p>
	 * <p>
	 * index：Piece索引
	 * </p>
	 * <p>Superseeding使用：下载Piece后发送此消息，Piece数据直接在内存中读取，减少读取硬盘。</p>
	 */
	public void suggestPiece(int index) {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送suggestPiece消息：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("发送suggestPiece消息：Peer只上传不下载");
			return;
		}
		if(this.peerSession.havePiece(index)) {
			LOGGER.debug("发送suggestPiece消息：Peer已经含有该Piece");
			return;
		}
		LOGGER.debug("发送suggestPiece消息：{}", index);
		pushMessage(PeerConfig.Type.SUGGEST_PIECE, NumberUtils.intToBytes(index));
	}
	
	/**
	 * <p>处理suggestPiece消息</p>
	 */
	private void suggestPiece(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理suggestPiece消息：任务不可下载");
			return;
		}
		final int index = buffer.getInt();
		LOGGER.debug("处理suggestPiece消息：{}", index);
		this.peerSession.suggestPieces(index);
		if(!this.torrentSession.havePiece(index)) {
			interested();
		}
	}
	
	/**
	 * <p>发送rejectRequest消息</p>
	 * <p>
	 * 格式：len=0013 id=0x10 index begin length
	 * </p>
	 * 
	 * @see {@link #request(int, int, int)}
	 */
	private void rejectRequest(int index, int begin, int length) {
		if(!this.peerSession.supportFastExtensionProtocol()) {
			return;
		}
		LOGGER.debug("发送rejectRequest消息：{}-{}-{}", index, begin, length);
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		pushMessage(PeerConfig.Type.REJECT_REQUEST, buffer.array());
	}
	
	/**
	 * <p>处理rejectRequest消息</p>
	 */
	private void rejectRequest(ByteBuffer buffer) {
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		final int length = buffer.getInt();
		LOGGER.debug("处理rejectRequest消息：{}-{}-{}", index, begin, length);
	}
	
	/**
	 * <p>发送allowedFast消息</p>
	 * <p>
	 * 格式：len=0005 id=0x11 index
	 * </p>
	 * <p>
	 * index：Piece索引
	 * </p>
	 */
	public void allowedFast(int index) {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送allowedFast消息：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("发送allowedFast消息：Peer只上传不下载");
			return;
		}
		if(this.peerSession.havePiece(index)) {
			LOGGER.debug("发送allowedFast消息：Peer已经含有该Piece");
			return;
		}
		LOGGER.debug("发送allowedFast消息：{}", index);
		pushMessage(PeerConfig.Type.ALLOWED_FAST, NumberUtils.intToBytes(index));
	}
	
	/**
	 * <p>处理allowedFast消息</p>
	 */
	private void allowedFast(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理allowedFast消息：任务不可下载");
			return;
		}
		final int index = buffer.getInt();
		LOGGER.debug("处理allowedFast消息：{}", index);
		this.peerSession.allowedPieces(index);
		if(!this.torrentSession.havePiece(index)) {
			interested();
		}
		this.allowedFastDownload();
	}
	
	/**
	 * <dl>
	 * 	<dt>交换Piece位图</dt>
	 * 	<dd>如果任务已经下载所有Piece，Peer支持FAST扩展，发送haveAll代替交换Piece位图。</dd>
	 * 	<dd>如果任务没有下载任何Piece，Peer支持FAST扩展，发送haveNone代替交换Piece位图。</dd>
	 * 	<dd>其他情况发送交换Piece位图</dd>
	 * <dl>
	 */
	private void exchangeBitfield() {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("交换Piece位图：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("交换Piece位图：Peer只上传不下载");
			return;
		}
		if(this.peerSession.supportFastExtensionProtocol()) { // 支持FAST扩展
			final var pieces = this.torrentSession.pieces(); // 已下载Pieces
			if(pieces.isEmpty()) {
				this.haveNone();
				return;
			}
			// 任务已经完成
			if(this.torrentSession.completed()) {
				final var allPieces = this.torrentSession.allPieces(); // 所有Pieces
				allPieces.andNot(pieces);
				if(allPieces.isEmpty()) {
					this.haveAll();
					return;
				}
			}
		}
		this.bitfield();
	}
	
	/**
	 * <p>发送Piece位图消息</p>
	 * <p>
	 * 格式：len=0001+X id=0x05 bitfield
	 * </p>
	 * <pre>
	 * X：bitfield.length
	 * bitfield：Piece位图
	 * </pre>
	 */
	private void bitfield() {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送Piece位图消息：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("发送Piece位图消息：Peer只上传不下载");
			return;
		}
		final BitSet pieces = this.torrentSession.pieces();
		LOGGER.debug("发送Piece位图消息：{}", pieces);
		final int pieceSize = this.torrentSession.torrent().getInfo().pieceSize();
		pushMessage(PeerConfig.Type.BITFIELD, BitfieldUtils.toBytes(pieceSize, pieces));
	}
	
	/**
	 * <p>处理Piece位图消息</p>
	 */
	private void bitfield(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理Piece位图消息：任务不可下载");
			return;
		}
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BitSet pieces = BitfieldUtils.toBitSet(bytes); // Peer已下载Piece位图
		LOGGER.debug("处理Piece位图消息：{}", pieces);
		this.peerSession.pieces(pieces);
		this.torrentSession.fullPieces(pieces);
		final BitSet notHave = new BitSet(); // 没有下载的Piece位图
		notHave.or(pieces);
		notHave.andNot(this.torrentSession.pieces());
		LOGGER.debug("处理Piece位图消息（感兴趣的Piece位图）：{}", notHave);
		if(notHave.isEmpty()) {
			notInterested();
		} else {
			interested();
		}
	}
	
	/**
	 * <p>发送request消息</p>
	 * <p>
	 * 格式：len=0013 id=0x06 index begin length
	 * </p>
	 * <pre>
	 * index：Piece索引
	 * begin：Piece内偏移
	 * length：请求数据长度
	 * </pre>
	 * <p>客户端收到Peer的unchoke消息后，开始发送request消息下载数据，一般交换数据是以slice（默认16KB）为单位。</p>
	 */
	public void request(int index, int begin, int length) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("发送request消息：任务不可下载");
			return;
		}
		if(this.peerSession.isPeerChoked()) {
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
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		final int length = buffer.getInt();
		if(this.peerSession.isAmChoked()) {
			LOGGER.debug("处理request消息：阻塞");
			rejectRequest(index, begin, length);
			return;
		}
		// 累计上传大小超过任务大小：阻塞（不上传）
		if(this.peerSession.statistics().uploadSize() > this.torrentSession.size()) {
			LOGGER.debug("累计上传大小超过任务大小：阻塞");
			this.choke(); // 发送阻塞消息
			rejectRequest(index, begin, length);
			return;
		}
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
	 * <p>
	 * 格式：len=0009+X id=0x07 index begin block
	 * </p>
	 * <pre>
	 * index：Piece索引
	 * begin：Piece内偏移
	 * X：block长度（默认16KB）
	 * </pre>
	 */
	private void piece(int index, int begin, byte[] bytes) {
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
		if(this.peerDownloader != null) {
			this.peerDownloader.piece(index, begin, bytes);
		} else if(this.peerUploader != null) {
			this.peerUploader.piece(index, begin, bytes);
		}
	}

	/**
	 * <p>发送cancel消息</p>
	 * <p>
	 * 格式：len=0013 id=0x08 index begin length
	 * <p>
	 * </p>与request作用相反：取消下载</p>
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
	 * <p>
	 * 格式：len=0003 id=0x09 listen-port
	 * </p>
	 * <p>
	 * listen-port：DHT端口
	 * </p>
	 * <p>支持DHT的客户端使用：指明DHT监听的端口</p>
	 */
	private void dht() {
		if(this.peerSession.supportDhtProtocol()) {
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
	 * 格式：len=0001+X id=0x14 ex
	 * </p>
	 * <pre>
	 * X：ex长度
	 * ex：扩展消息
	 * </pre>
	 */
	private void extension() {
		if(this.peerSession.supportExtensionProtocol()) {
			this.extensionMessageHandler.handshake();
		}
	}
	
	/**
	 * <p>处理扩展消息</p>
	 */
	private void extension(ByteBuffer buffer) throws NetException {
		this.extensionMessageHandler.onMessage(buffer);
	}
	
	/**
	 * <p>发送扩展消息：pex</p>
	 */
	public void pex(byte[] bytes) {
		this.extensionMessageHandler.pex(bytes);
	}
	
	/**
	 * <p>发送扩展消息：uploadOnly</p>
	 */
	public void uploadOnly() {
		this.extensionMessageHandler.uploadOnly();
	}
	
	/**
	 * <p>发送扩展消息：holepunch-rendezvous</p>
	 */
	public void holepunchRendezvous(PeerSession peerSession) {
		this.extensionMessageHandler.holepunchRendezvous(peerSession);
	}
	
	/**
	 * <p>发送扩展消息：holepunch-connect</p>
	 */
	public void holepunchConnect(String host, int port) {
		this.extensionMessageHandler.holepunchConnect(host, port);
	}
	
	/**
	 * <p>发送消息</p>
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
	 * length_prefix（4字节）：message_id长度 + payload长度
	 * message_id（1字节）：{@linkplain Type 消息类型ID}
	 * payload：负载（消息内容）
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
		final ByteBuffer buffer = ByteBuffer.allocate(capacity + 4); // length_prefix：四字节
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
	 * <p>释放Peer</p>
	 * 
	 * @see {@link IMessageEncryptHandler#close()}
	 */
	public void close() {
		this.messageEncryptHandler.close();
	}
	
	/**
	 * <p>是否可用</p>
	 * 
	 * @see {@link IMessageEncryptHandler#available()}
	 */
	public boolean available() {
		return this.messageEncryptHandler.available();
	}
	
	/**
	 * <p>发送消息</p>
	 * 
	 * @see {@link IMessageEncryptHandler#send(ByteBuffer)}
	 */
	public void send(ByteBuffer buffer) {
		try {
			this.messageEncryptHandler.send(buffer);
		} catch (NetException e) {
			LOGGER.error("Peer消息发送异常", e);
		}
	}
	
	/**
	 * <p>发送消息</p>
	 * 
	 * @see {@link IMessageEncryptHandler#send(ByteBuffer, int)}
	 */
	public void send(ByteBuffer buffer, int timeout) {
		try {
			this.messageEncryptHandler.send(buffer, timeout);
		} catch (NetException e) {
			LOGGER.error("Peer消息发送异常", e);
		}
	}
	
	/**
	 * <p>发送加密消息</p>
	 * 
	 * @see {@link IMessageEncryptHandler#sendEncrypt(ByteBuffer)}
	 */
	public void sendEncrypt(ByteBuffer buffer) {
		try {
			this.messageEncryptHandler.sendEncrypt(buffer);
		} catch (NetException e) {
			LOGGER.error("Peer消息发送异常", e);
		}
	}

	/**
	 * <p>发送加密消息</p>
	 * 
	 * @see {@link IMessageEncryptHandler#sendEncrypt(ByteBuffer, int)}
	 */
	public void sendEncrypt(ByteBuffer buffer, int timeout) {
		try {
			this.messageEncryptHandler.sendEncrypt(buffer, timeout);
		} catch (NetException e) {
			LOGGER.error("Peer消息发送异常", e);
		}
	}
	
	/**
	 * <p>获取远程服务地址</p>
	 * 
	 * @see {@link IMessageEncryptHandler#remoteSocketAddress()}
	 */
	private InetSocketAddress remoteSocketAddress() {
		return this.messageEncryptHandler.remoteSocketAddress();
	}

	/**
	 * <p>解除阻塞下载</p>
	 */
	private void unchokeDownload() {
		if(
			this.peerSession != null &&
			this.peerSession.isPeerUnchoked()
		) {
			if(this.peerDownloader != null) {
				this.peerDownloader.download();
			} else if(this.peerUploader != null) {
				this.peerUploader.download();
			}
		}
	}

	/**
	 * <p>快速允许下载</p>
	 */
	private void allowedFastDownload() {
		if(
			this.peerSession != null &&
			this.peerSession.isPeerChoked() &&
			this.peerSession.supportAllowedFast()
		) {
			if(this.peerDownloader != null) {
				this.peerDownloader.download();
			} else if(this.peerUploader != null) {
				this.peerUploader.download();
			}
		}
	}
	
}