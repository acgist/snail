package com.acgist.snail.net.torrent.peer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.PeerConfig.Type;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.codec.IMessageDecoder;
import com.acgist.snail.net.torrent.IEncryptMessageSender;
import com.acgist.snail.net.torrent.IPeerConnect;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.BitfieldUtils;
import com.acgist.snail.utils.ByteUtils;
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
 * <p>Peer状态标识：https://baike.baidu.com/item/uTorrent/8195186</p>
 * 
 * @author acgist
 */
public final class PeerSubMessageHandler implements IMessageDecoder<ByteBuffer>, IPeerConnect {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerSubMessageHandler.class);
	
	/**
	 * <p>检查是否使用最大次数：{@value}</p>
	 */
	private static final int MAX_USELESS_CHECK = 3;
	
	/**
	 * <p>检查是否使用次数</p>
	 */
	private int uselessCheck = 0;
	/**
	 * <p>是否可用</p>
	 */
	private volatile boolean available = false;
	/**
	 * <p>是否已经发送握手</p>
	 */
	private volatile boolean handshakeSend = false;
	/**
	 * <p>是否已经处理握手</p>
	 */
	private volatile boolean handshakeRecv = false;
	/**
	 * <p>是否是服务端</p>
	 */
	private final boolean server;
	/**
	 * <p>Peer连接</p>
	 * 
	 * @see PeerUploader
	 * @see PeerDownloader
	 */
	private PeerConnect peerConnect;
	/**
	 * <p>Peer信息</p>
	 */
	private PeerSession peerSession;
	/**
	 * <p>BT任务信息</p>
	 */
	private TorrentSession torrentSession;
	/**
	 * <p>Peer连接信息</p>
	 */
	private PeerConnectSession peerConnectSession;
	/**
	 * <p>加密消息代理</p>
	 */
	private IEncryptMessageSender messageEncryptSender;
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
	 * 
	 * @param peerSession Peer信息
	 * @param torrentSession BT任务信息
	 */
	private PeerSubMessageHandler(PeerSession peerSession, TorrentSession torrentSession) {
		this.server = false;
		this.init(peerSession, torrentSession);
	}
	
	/**
	 * <p>服务端</p>
	 * 
	 * @return PeerSubMessageHandler
	 */
	public static final PeerSubMessageHandler newInstance() {
		return new PeerSubMessageHandler();
	}

	/**
	 * 客户端
	 * 
	 * @param peerSession    Peer信息
	 * @param torrentSession BT任务信息
	 * 
	 * @return PeerSubMessageHandler
	 */
	public static final PeerSubMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession) {
		return new PeerSubMessageHandler(peerSession, torrentSession);
	}

	/**
	 * 初始消息代理
	 * 
	 * @param peerSession    Peer信息
	 * @param torrentSession BT任务信息
	 */
	private void init(PeerSession peerSession, TorrentSession torrentSession) {
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
		this.extensionMessageHandler = ExtensionMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
		this.dhtExtensionMessageHandler = DhtExtensionMessageHandler.newInstance(this.peerSession, this.torrentSession, this);
	}

	/**
	 * <p>初始化服务端</p>
	 * 
	 * @param infoHashHex InfoHashHex
	 * @param peerId      PeerId
	 * 
	 * @return 是否成功
	 */
	private boolean initServer(String infoHashHex, byte[] peerId) {
		if(Arrays.equals(PeerConfig.getInstance().getPeerId(), peerId)) {
			LOGGER.debug("Peer接入失败（PeerId一致）");
			return false;
		}
		final TorrentSession torrentSession = TorrentContext.getInstance().torrentSession(infoHashHex);
		if(torrentSession == null) {
			LOGGER.debug("Peer接入失败（种子信息不存在）：{}", infoHashHex);
			return false;
		}
		if(!torrentSession.useable()) {
			LOGGER.debug("Peer接入失败（任务没有准备完成）：{}", infoHashHex);
			return false;
		}
		final InetSocketAddress socketAddress = this.remoteSocketAddress();
		if(socketAddress == null) {
			LOGGER.debug("Peer接入失败（远程客户端获取失败）：{}", infoHashHex);
			return false;
		}
		// 禁止自动获取端口：通过PEX消息获取端口
		final PeerSession peerSession = PeerContext.getInstance().newPeerSession(
			infoHashHex,
			torrentSession.statistics(),
			socketAddress.getHostString(),
			null,
			PeerConfig.Source.CONNECT
		);
		final PeerUploader peerUploader = torrentSession.newPeerUploader(peerSession, this);
		if(peerUploader == null) {
			return false;
		} else {
			this.init(peerSession, torrentSession);
			this.peerConnect = peerUploader;
			this.peerConnectSession = peerUploader.peerConnectSession();
			this.peerSession.peerUploader(peerUploader);
			return true;
		}
	}
	
	/**
	 * <p>初始化客户端</p>
	 * 
	 * @param peerDownloader Peer下载
	 * 
	 * @return Peer消息代理
	 */
	public PeerSubMessageHandler initClient(PeerDownloader peerDownloader) {
		this.peerConnect = peerDownloader;
		this.peerConnectSession = peerDownloader.peerConnectSession();
		this.peerSession.peerDownloader(peerDownloader);
		return this;
	}
	
	/**
	 * <p>判断是否没有使用</p>
	 * 
	 * @return 是否没有使用
	 */
	public boolean useless() {
		if(this.handshakeRecv) {
			return false;
		}
		return ++this.uselessCheck > MAX_USELESS_CHECK;
	}
	
	/**
	 * <p>判断是否处理握手消息</p>
	 * 
	 * @return 是否处理握手消息
	 */
	public boolean handshakeRecv() {
		return this.handshakeRecv;
	}
	
	/**
	 * <p>获取BT任务信息</p>
	 * 
	 * @return BT任务信息
	 */
	public TorrentSession torrentSession() {
		return this.torrentSession;
	}
	
	/**
	 * <p>判断是否需要加密</p>
	 * 
	 * @return 是否需要加密
	 * 
	 * @see PeerSession#encrypt()
	 */
	public boolean needEncrypt() {
		if(this.peerSession == null) {
			// 默认使用明文
			return false;
		}
		return this.peerSession.encrypt();
	}
	
	/**
	 * <p>设置实际消息代理：TCP、UDP（UTP）</p>
	 * 
	 * @param messageEncryptSender 实际消息代理
	 * 
	 * @return Peer消息代理
	 */
	public PeerSubMessageHandler messageEncryptSender(IEncryptMessageSender messageEncryptSender) {
		this.messageEncryptSender = messageEncryptSender;
		return this;
	}
	
	@Override
	public final IPeerConnect.ConnectType connectType() {
		return this.messageEncryptSender.connectType();
	}
	
	@Override
	public void onMessage(final ByteBuffer buffer) throws NetException {
		if(this.handshakeRecv) {
			final byte typeId = buffer.get();
			final PeerConfig.Type type = PeerConfig.Type.of(typeId);
			if(type == null) {
				LOGGER.warn("处理Peer消息错误（未知类型）：{}", typeId);
				return;
			}
			if(!this.available) {
				LOGGER.debug("处理Peer消息错误（状态错误）：{}-{}-{}-{}", this.server, this.available, this.handshakeSend, this.handshakeRecv);
				return;
			}
			LOGGER.debug("处理Peer消息类型：{}", type);
			switch (type) {
				case CHOKE -> this.choke(buffer);
				case UNCHOKE -> this.unchoke(buffer);
				case INTERESTED -> this.interested(buffer);
				case NOT_INTERESTED -> this.notInterested(buffer);
				case HAVE -> this.have(buffer);
				case BITFIELD -> this.bitfield(buffer);
				case REQUEST -> this.request(buffer);
				case PIECE -> this.piece(buffer);
				case CANCEL -> this.cancel(buffer);
				// DHT扩展
				case DHT -> this.dht(buffer);
				// 扩展协议
				case EXTENSION -> this.extension(buffer);
				// FAST扩展
				case HAVE_ALL -> this.haveAll(buffer);
				case HAVE_NONE -> this.haveNone(buffer);
				case SUGGEST_PIECE -> this.suggestPiece(buffer);
				case REJECT_REQUEST -> this.rejectRequest(buffer);
				case ALLOWED_FAST -> this.allowedFast(buffer);
				default -> LOGGER.warn("处理Peer消息错误（类型未适配）：{}", type);
			}
		} else {
			this.handshakeRecv = true;
			final boolean success = this.handshake(buffer);
			if(success) {
				LOGGER.debug("Peer握手成功：{}", this.peerSession);
			} else {
				LOGGER.debug("Peer握手失败：{}", this.peerSession);
				this.close();
			}
		}
	}

	/**
	 * <p>发送握手消息</p>
	 * <p>注意：握手设置超时时间防止一直等待阻塞线程</p>
	 * <p>格式：pstrlen pstr reserved info_hash peer_id</p>
	 * <p>pstrlen：pstr.length</p>
	 * <p>pstr：{@linkplain PeerConfig#PROTOCOL_NAME BitTorrent协议}</p>
	 * <p>reserved：{@linkplain PeerConfig#RESERVED 扩展BT协议}</p>
	 * <p>info_hash：info_hash</p>
	 * <p>peer_id：peer_id</p>
	 */
	public void handshake() {
		if(this.handshakeSend) {
			LOGGER.debug("握手消息已经发送");
			return;
		}
		LOGGER.debug("发送握手消息");
		this.handshakeSend = true;
		final ByteBuffer buffer = ByteBuffer.allocate(PeerConfig.HANDSHAKE_LENGTH);
		buffer.put((byte) PeerConfig.PROTOCOL_NAME_LENGTH);
		buffer.put(PeerConfig.PROTOCOL_NAME_BYTES);
		buffer.put(PeerConfig.RESERVED);
		buffer.put(this.torrentSession.infoHash().infoHash());
		buffer.put(PeerConfig.getInstance().getPeerId());
		this.sendEncrypt(buffer, SystemConfig.CONNECT_TIMEOUT);
	}
	
	/**
	 * <p>处理握手消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @return 是否握手成功
	 */
	private boolean handshake(ByteBuffer buffer) {
		LOGGER.debug("处理握手消息");
		final int remaining = buffer.remaining();
		if(remaining != PeerConfig.HANDSHAKE_LENGTH) {
			LOGGER.warn("处理握手消息格式错误（消息长度）：{}", remaining);
			return false;
		}
		final byte length = buffer.get();
		if(length != PeerConfig.PROTOCOL_NAME_LENGTH) {
			LOGGER.warn("处理握手消息格式错误（协议长度）：{}", length);
			return false;
		}
		final byte[] name = new byte[length];
		buffer.get(name);
		if(!Arrays.equals(name, PeerConfig.PROTOCOL_NAME_BYTES)) {
			final String nameValue = new String(name);
			LOGGER.warn("处理握手消息格式错误（下载协议错误）：{}", nameValue);
			return false;
		}
		final byte[] reserved = new byte[PeerConfig.RESERVED_LENGTH];
		buffer.get(reserved);
		final byte[] infoHash = new byte[SystemConfig.SHA1_HASH_LENGTH];
		buffer.get(infoHash);
		final String infoHashHex = StringUtils.hex(infoHash);
		final byte[] peerId = new byte[PeerConfig.PEER_ID_LENGTH];
		buffer.get(peerId);
		if(this.server) {
			if(this.initServer(infoHashHex, peerId)) {
				this.available = true;
			} else {
				return false;
			}
		} else {
			this.available = true;
		}
		this.handshake();
		this.peerSession.id(peerId);
		this.peerSession.reserved(reserved);
		// 位图交换优先发送
		this.fastBitfield();
		this.extension();
		this.dht();
		this.unchoke();
		return true;
	}

	/**
	 * <p>发送心跳消息</p>
	 * <p>格式：len=0000</p>
	 */
	public void keepAlive() {
		LOGGER.debug("发送心跳消息");
		this.pushMessage(null, null);
	}
	
	/**
	 * <p>发送阻塞消息</p>
	 * <p>格式：len=0001 id=0x00</p>
	 */
	public void choke() {
		LOGGER.debug("发送阻塞消息");
		this.peerConnectSession.amChoked();
		this.pushMessage(PeerConfig.Type.CHOKE);
	}

	/**
	 * <p>处理阻塞消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void choke(ByteBuffer buffer) {
		LOGGER.debug("处理阻塞消息");
		this.peerConnectSession.peerChoked();
		// 不用释放资源：系统自动优化剔除
//		this.peerConnect.release();
	}
	
	/**
	 * <p>发送解除阻塞消息</p>
	 * <p>格式：len=0001 id=0x01</p>
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
		if(this.peerConnectSession.isAmUnchoked()) {
			LOGGER.debug("发送解除阻塞消息：已经解除");
			return;
		}
		LOGGER.debug("发送解除阻塞消息");
		this.peerConnectSession.amUnchoked();
		this.pushMessage(PeerConfig.Type.UNCHOKE);
	}
	
	/**
	 * <p>处理解除阻塞消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void unchoke(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理解除阻塞消息：任务不可下载");
			return;
		}
		LOGGER.debug("处理解除阻塞消息");
		this.peerConnectSession.peerUnchoked();
		// 解除阻塞：开始发送下载请求
		this.unchokeDownload();
	}
	
	/**
	 * <p>发送感兴趣消息</p>
	 * <p>格式：len=0001 id=0x02</p>
	 * <p>客户端没有对应的Piece：发送感兴趣消息</p>
	 */
	private void interested() {
		if(this.peerConnectSession.isAmInterested()) {
			LOGGER.debug("发送感兴趣消息：已经感兴趣");
			return;
		}
		LOGGER.debug("发送感兴趣消息");
		this.peerConnectSession.amInterested();
		this.pushMessage(PeerConfig.Type.INTERESTED);
	}

	/**
	 * <p>处理感兴趣消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void interested(ByteBuffer buffer) {
		LOGGER.debug("处理感兴趣消息");
		this.peerConnectSession.peerInterested();
	}

	/**
	 * <p>发送不感兴趣消息</p>
	 * <p>格式：len=0001 id=0x03</p>
	 * <p>客户端拥有所有的Piece：发送不感兴趣消息</p>
	 */
	public void notInterested() {
		if(this.peerConnectSession.isAmNotInterested()) {
			LOGGER.debug("发送不感兴趣消息：已经不感兴趣");
			return;
		}
		LOGGER.debug("发送不感兴趣消息");
		this.peerConnectSession.amNotInterested();
		this.pushMessage(PeerConfig.Type.NOT_INTERESTED);
	}

	/**
	 * <p>处理不感兴趣消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void notInterested(ByteBuffer buffer) {
		LOGGER.debug("处理不感兴趣消息");
		this.peerConnectSession.peerNotInterested();
	}

	/**
	 * <p>发送have消息</p>
	 * <p>格式：len=0005 id=0x04 index</p>
	 * <p>index：Piece索引</p>
	 * <p>Piece下载完成：发送have消息通知与客户端连接的Peer已经拥有该Piece</p>
	 * 
	 * @param indexArray Piece索引
	 */
	public void have(Integer ... indexArray) {
		if(ArrayUtils.isEmpty(indexArray)) {
			LOGGER.debug("发送have消息：没有可用索引");
			return;
		}
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送have消息：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("发送have消息：Peer只上传不下载");
			return;
		}
		int pos = 0;
		int length = 0;
		final byte[] bytes = new byte[9 * indexArray.length];
		for (Integer index : indexArray) {
			if(this.peerSession.hasPiece(index)) {
				LOGGER.debug("发送have消息：Peer已经含有");
			} else {
				LOGGER.debug("发送have消息：{}", index);
				final byte[] message = this.buildMessage(PeerConfig.Type.HAVE, NumberUtils.intToBytes(index)).array();
				length = message.length;
				System.arraycopy(message, 0, bytes, pos, length);
				pos += length;
			}
		}
		if(pos > 0) {
			final ByteBuffer buffer = ByteBuffer.allocate(pos);
			buffer.put(bytes, 0, pos);
			this.sendEncrypt(buffer);
		}
	}

	/**
	 * <p>处理have消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void have(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理have消息：任务不可下载");
			return;
		}
		final int index = buffer.getInt();
		LOGGER.debug("处理have消息：{}", index);
		this.peerSession.piece(index);
		if(!this.torrentSession.hasPiece(index)) {
			this.interested();
		}
	}

	/**
	 * <p>发送haveAll消息</p>
	 * <p>格式：len=0001 id=0x0E</p>
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
		this.pushMessage(PeerConfig.Type.HAVE_ALL);
	}
	
	/**
	 * <p>处理haveAll消息</p>
	 * 
	 * @param buffer 消息
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
		if(!this.torrentSession.completed()) {
			this.interested();
		}
	}
	
	/**
	 * <p>发送haveNone消息</p>
	 * <p>格式：len=0001 id=0x0F</p>
	 */
	private void haveNone() {
		LOGGER.debug("发送haveNone消息");
		this.pushMessage(PeerConfig.Type.HAVE_NONE);
	}

	/**
	 * <p>处理haveNone消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void haveNone(ByteBuffer buffer) {
		LOGGER.debug("处理haveAll消息");
		this.peerSession.cleanPieces();
		this.notInterested();
	}
	
	/**
	 * <p>发送suggestPiece消息</p>
	 * <p>格式：len=0005 id=0x0D index</p>
	 * <p>index：Piece索引</p>
	 * <p>Piece下载完成：直接读取内存Piece数据（减少读取硬盘）</p>
	 * 
	 * @param index Piece索引
	 */
	public void suggestPiece(int index) {
		if(!this.peerSession.supportFastExtensionProtocol()) {
			LOGGER.debug("发送suggestPiece消息：Peer不支持Fast扩展");
			return;
		}
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送suggestPiece消息：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("发送suggestPiece消息：Peer只上传不下载");
			return;
		}
		if(this.peerSession.hasPiece(index)) {
			LOGGER.debug("发送suggestPiece消息：Peer已经含有");
			return;
		}
		LOGGER.debug("发送suggestPiece消息：{}", index);
		this.pushMessage(PeerConfig.Type.SUGGEST_PIECE, NumberUtils.intToBytes(index));
	}
	
	/**
	 * <p>处理suggestPiece消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void suggestPiece(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理suggestPiece消息：任务不可下载");
			return;
		}
		final int index = buffer.getInt();
		LOGGER.debug("处理suggestPiece消息：{}", index);
		this.peerSession.suggestPieces(index);
		if(!this.torrentSession.hasPiece(index)) {
			this.interested();
		}
	}
	
	/**
	 * <p>发送rejectRequest消息</p>
	 * <p>格式：len=0013 id=0x10 index begin length</p>
	 * <p>index：Piece索引</p>
	 * <p>begin：Piece内偏移</p>
	 * <p>length：请求数据长度</p>
	 * 
	 * @param index Piece索引
	 * @param begin Piece内偏移
	 * @param length 请求数据长度
	 * 
	 * @see #request(int, int, int)
	 */
	private void rejectRequest(int index, int begin, int length) {
		if(!this.peerSession.supportFastExtensionProtocol()) {
			LOGGER.debug("发送rejectRequest消息：Peer不支持Fast扩展");
			return;
		}
		LOGGER.debug("发送rejectRequest消息：{}-{}-{}", index, begin, length);
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		this.pushMessage(PeerConfig.Type.REJECT_REQUEST, buffer.array());
	}
	
	/**
	 * <p>处理rejectRequest消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void rejectRequest(ByteBuffer buffer) {
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		final int length = buffer.getInt();
		LOGGER.debug("处理rejectRequest消息：{}-{}-{}", index, begin, length);
	}
	
	/**
	 * <p>发送allowedFast消息</p>
	 * <p>格式：len=0005 id=0x11 index</p>
	 * <p>index：Piece索引</p>
	 * 
	 * @param index Piece索引
	 */
	public void allowedFast(int index) {
		if(!this.peerSession.supportFastExtensionProtocol()) {
			LOGGER.debug("发送allowedFast消息：Peer不支持Fast扩展");
			return;
		}
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送allowedFast消息：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("发送allowedFast消息：Peer只上传不下载");
			return;
		}
		if(this.peerSession.hasPiece(index)) {
			LOGGER.debug("发送allowedFast消息：Peer已经含有");
			return;
		}
		LOGGER.debug("发送allowedFast消息：{}", index);
		this.pushMessage(PeerConfig.Type.ALLOWED_FAST, NumberUtils.intToBytes(index));
	}
	
	/**
	 * <p>处理allowedFast消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void allowedFast(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理allowedFast消息：任务不可下载");
			return;
		}
		final int index = buffer.getInt();
		LOGGER.debug("处理allowedFast消息：{}", index);
		this.peerSession.allowedPieces(index);
		if(!this.torrentSession.hasPiece(index)) {
			this.interested();
		}
		this.allowedFastDownload();
	}
	
	/**
	 * <p>快速交换Piece位图</p>
	 * 
	 * @see #bitfield()
	 */
	private void fastBitfield() {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("快速交换Piece位图：任务不可上传");
			return;
		}
		if(this.peerSession.uploadOnly()) {
			LOGGER.debug("快速交换Piece位图：Peer只上传不下载");
			return;
		}
		if(this.peerSession.supportFastExtensionProtocol()) {
			// 支持FAST扩展
			final var pieces = this.torrentSession.pieces();
			if(pieces.isEmpty()) {
				this.haveNone();
				return;
			}
			if(this.torrentSession.completed()) {
				final var allPieces = this.torrentSession.allPieces();
				allPieces.andNot(pieces);
				// 任务必须下载完成所有Piece：如果只是部分下载不用发送
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
	 * <p>格式：len=0001+X id=0x05 bitfield</p>
	 * <p>X：bitfield.length</p>
	 * <p>bitfield：Piece位图</p>
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
		this.pushMessage(PeerConfig.Type.BITFIELD, BitfieldUtils.toBytes(pieceSize, pieces));
	}
	
	/**
	 * <p>处理Piece位图消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void bitfield(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理Piece位图消息：任务不可下载");
			return;
		}
		final byte[] bytes = ByteUtils.remainingToBytes(buffer);
		// Peer已经下载Piece位图
		final BitSet pieces = BitfieldUtils.toBitSet(bytes);
		this.peerSession.pieces(pieces);
		this.torrentSession.fullPieces(pieces);
		// 客户端没有下载Piece位图
		final BitSet notHave = new BitSet();
		notHave.or(pieces);
		notHave.andNot(this.torrentSession.pieces());
		LOGGER.debug("""
			处理Piece位图消息
			Peer已经下载Piece位图：{}
			客户端没有下载Piece位图：{}""",
			pieces,
			notHave
		);
		if(notHave.isEmpty()) {
			this.notInterested();
		} else {
			this.interested();
		}
	}
	
	/**
	 * <p>发送request消息</p>
	 * <p>格式：len=0013 id=0x06 index begin length</p>
	 * <p>index：Piece索引</p>
	 * <p>begin：Piece内偏移</p>
	 * <p>length：请求数据长度</p>
	 * 
	 * @param index Piece索引
	 * @param begin Piece内偏移
	 * @param length 请求数据长度
	 */
	public void request(int index, int begin, int length) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("发送request消息：任务不可下载");
			return;
		}
		if(this.peerConnectSession.isPeerChoked()) {
			LOGGER.debug("发送request消息：阻塞");
			return;
		}
		LOGGER.debug("发送request消息：{}-{}-{}", index, begin, length);
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		this.pushMessage(PeerConfig.Type.REQUEST, buffer.array());
	}

	/**
	 * <p>处理request消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void request(ByteBuffer buffer) {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("处理request消息：任务不可上传");
			return;
		}
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		final int length = buffer.getInt();
		if(this.peerConnectSession.isAmChoked()) {
			LOGGER.debug("处理request消息：阻塞");
			this.rejectRequest(index, begin, length);
			return;
		}
		if(this.peerSession.uploadSize() > this.torrentSession.size()) {
			LOGGER.debug("处理request消息：累计上传大小超过任务大小");
			this.choke();
			this.rejectRequest(index, begin, length);
			return;
		}
		if(this.torrentSession.hasPiece(index)) {
			LOGGER.debug("处理request消息：{}-{}-{}", index, begin, length);
			try {
				this.piece(index, begin, this.torrentSession.read(index, begin, length));
			} catch (NetException e) {
				LOGGER.error("处理request消息异常", e);
			}
		} else {
			LOGGER.debug("处理request消息：Piece没有下载");
		}
	}

	/**
	 * <p>发送piece消息</p>
	 * <p>格式：len=0009+X id=0x07 index begin block</p>
	 * <p>X：block.length（默认：16KB）</p>
	 * <p>index：Piece索引</p>
	 * <p>begin：Piece内偏移</p>
	 * <p>block：Piece请求数据</p>
	 * 
	 * @param index Piece索引
	 * @param begin Piece内偏移
	 * @param bytes Piece请求数据
	 */
	private void piece(int index, int begin, byte[] bytes) {
		if(!this.torrentSession.uploadable()) {
			LOGGER.debug("发送piece消息：任务不可上传");
			return;
		}
		LOGGER.debug("发送piece消息：{}-{}", index, begin);
		this.peerConnect.uploadMark(bytes.length);
		final ByteBuffer buffer = ByteBuffer.allocate(8 + bytes.length);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.put(bytes);
		this.pushMessage(PeerConfig.Type.PIECE, buffer.array());
	}

	/**
	 * <p>处理piece消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void piece(ByteBuffer buffer) {
		if(!this.torrentSession.downloadable()) {
			LOGGER.debug("处理piece消息：任务不可下载");
			return;
		}
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		LOGGER.debug("处理piece消息：{}-{}", index, begin);
		final byte[] bytes = ByteUtils.remainingToBytes(buffer);
		if(this.peerConnect != null) {
			this.peerConnect.downloadMark(bytes.length);
			this.peerConnect.piece(index, begin, bytes);
		}
	}

	/**
	 * <p>发送cancel消息</p>
	 * <p>格式：len=0013 id=0x08 index begin length</p>
	 * <p>index：Piece索引</p>
	 * <p>begin：Piece内偏移</p>
	 * <p>length：请求数据长度</p>
	 * 
	 * @param index Piece索引
	 * @param begin Piece内偏移
	 * @param length 请求数据长度
	 */
	public void cancel(int index, int begin, int length) {
		LOGGER.debug("发送cancel消息：{}", index);
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.putInt(index);
		buffer.putInt(begin);
		buffer.putInt(length);
		this.pushMessage(PeerConfig.Type.CANCEL, buffer.array());
	}
	
	/**
	 * <p>处理cancel消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void cancel(ByteBuffer buffer) {
		final int index = buffer.getInt();
		final int begin = buffer.getInt();
		final int length = buffer.getInt();
		LOGGER.debug("处理cancel消息：{}-{}-{}", index, begin, length);
	}
	
	/**
	 * <p>发送DHT消息</p>
	 * <p>格式：len=0003 id=0x09 listen-port</p>
	 * <p>listen-port：DHT端口</p>
	 * <p>支持DHT的客户端使用：指明DHT监听端口</p>
	 * 
	 * @see DhtExtensionMessageHandler#port()
	 */
	private void dht() {
		if(this.peerSession.supportDhtProtocol()) {
			this.dhtExtensionMessageHandler.port();
		}
	}
	
	/**
	 * <p>处理DHT消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @see DhtExtensionMessageHandler#onMessage(ByteBuffer)
	 */
	private void dht(ByteBuffer buffer) {
		this.dhtExtensionMessageHandler.onMessage(buffer);
	}

	/**
	 * <p>发送扩展消息</p>
	 * <p>格式：len=0001+X id=0x14 ex</p>
	 * <p>X：ex.length</p>
	 * <p>ex：扩展消息</p>
	 * 
	 * @see ExtensionMessageHandler#handshake()
	 */
	private void extension() {
		if(this.peerSession.supportExtensionProtocol()) {
			this.extensionMessageHandler.handshake();
		}
	}
	
	/**
	 * <p>处理扩展消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see ExtensionMessageHandler#onMessage(ByteBuffer)
	 */
	private void extension(ByteBuffer buffer) throws NetException {
		this.extensionMessageHandler.onMessage(buffer);
	}
	
	/**
	 * <p>发送扩展消息：PEX</p>
	 * 
	 * @param bytes 消息
	 * 
	 * @see ExtensionMessageHandler#pex(byte[])
	 */
	public void pex(byte[] bytes) {
		this.extensionMessageHandler.pex(bytes);
	}
	
	/**
	 * <p>发送扩展消息：uploadOnly</p>
	 * 
	 * @see ExtensionMessageHandler#uploadOnly()
	 */
	public void uploadOnly() {
		this.extensionMessageHandler.uploadOnly();
	}
	
	/**
	 * <p>发送扩展消息：holepunch-rendezvous</p>
	 * 
	 * @param peerSession Peer信息
	 * 
	 * @see ExtensionMessageHandler#holepunchRendezvous(PeerSession)
	 */
	public void holepunchRendezvous(PeerSession peerSession) {
		this.extensionMessageHandler.holepunchRendezvous(peerSession);
	}
	
	/**
	 * <p>发送扩展消息：holepunch-connect</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 * 
	 * @see ExtensionMessageHandler#holepunchConnect(String, Integer)
	 */
	public void holepunchConnect(String host, int port) {
		this.extensionMessageHandler.holepunchConnect(host, port);
	}
	
	/**
	 * <p>发送消息</p>
	 * 
	 * @param type 类型
	 */
	public void pushMessage(PeerConfig.Type type) {
		this.pushMessage(type, null);
	}
	
	/**
	 * <p>发送消息</p>
	 * 
	 * @param type 类型
	 * @param payload 负载
	 */
	public void pushMessage(PeerConfig.Type type, byte[] payload) {
		this.sendEncrypt(this.buildMessage(type, payload));
	}
	
	/**
	 * <p>新建消息</p>
	 * <p>消息格式：length_prefix message_id payload</p>
	 * <p>length_prefix：message_id.length + payload.length</p>
	 * <p>message_id：{@linkplain Type 消息类型ID}</p>
	 * <p>payload：负载</p>
	 * 
	 * @param type 类型
	 * @param payload 负载
	 * 
	 * @return 消息
	 */
	private ByteBuffer buildMessage(PeerConfig.Type type, byte[] payload) {
		final Byte id = type == null ? null : type.getId();
		int capacity = 0;
		if(id != null) {
			capacity += 1;
		}
		if(payload != null) {
			capacity += payload.length;
		}
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
	 * <p>释放Peer</p>
	 * 
	 * @see IEncryptMessageSender#close()
	 */
	public void close() {
		this.messageEncryptSender.close();
	}
	
	/**
	 * <p>是否可用</p>
	 * 
	 * @return 是否可用
	 * 
	 * @see IEncryptMessageSender#available()
	 */
	public boolean available() {
		return this.messageEncryptSender.available();
	}
	
	/**
	 * <p>发送Peer消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @see IEncryptMessageSender#send(ByteBuffer)
	 */
	public void send(ByteBuffer buffer) {
		try {
			this.messageEncryptSender.send(buffer);
		} catch (NetException e) {
			LOGGER.error("发送Peer消息异常：{}", this.peerSession, e);
		}
	}
	
	/**
	 * <p>发送Peer消息</p>
	 * 
	 * @param buffer 消息
	 * @param timeout 超时时间（秒）
	 * 
	 * @see IEncryptMessageSender#send(ByteBuffer, int)
	 */
	public void send(ByteBuffer buffer, int timeout) {
		try {
			this.messageEncryptSender.send(buffer, timeout);
		} catch (NetException e) {
			LOGGER.error("发送Peer消息异常：{}", this.peerSession, e);
		}
	}
	
	/**
	 * <p>发送加密Peer消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @see IEncryptMessageSender#sendEncrypt(ByteBuffer)
	 */
	public void sendEncrypt(ByteBuffer buffer) {
		try {
			this.messageEncryptSender.sendEncrypt(buffer);
		} catch (NetException e) {
			LOGGER.error("发送加密Peer消息异常：{}", this.peerSession, e);
		}
	}

	/**
	 * <p>发送加密Peer消息</p>
	 * 
	 * @param buffer 消息
	 * @param timeout 超时时间（秒）
	 * 
	 * @see IEncryptMessageSender#sendEncrypt(ByteBuffer, int)
	 */
	public void sendEncrypt(ByteBuffer buffer, int timeout) {
		try {
			this.messageEncryptSender.sendEncrypt(buffer, timeout);
		} catch (NetException e) {
			LOGGER.error("发送加密Peer消息异常：{}", this.peerSession, e);
		}
	}
	
	/**
	 * <p>获取远程服务地址</p>
	 * 
	 * @see IEncryptMessageSender#remoteSocketAddress()
	 */
	private InetSocketAddress remoteSocketAddress() {
		return this.messageEncryptSender.remoteSocketAddress();
	}

	/**
	 * <p>解除阻塞下载</p>
	 */
	private void unchokeDownload() {
		if(
			this.peerConnectSession != null &&
			this.peerConnectSession.isPeerUnchoked()
		) {
			if(this.peerConnect != null) {
				this.peerConnect.download();
			}
		}
	}

	/**
	 * <p>快速允许下载</p>
	 */
	private void allowedFastDownload() {
		if(
			this.peerSession != null &&
			this.peerSession.supportAllowedFast() &&
			this.peerConnectSession != null &&
			this.peerConnectSession.isPeerChoked()
		) {
			if(this.peerConnect != null) {
				this.peerConnect.download();
			}
		}
	}
	
}