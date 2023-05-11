package com.acgist.snail.net.torrent.peer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.PeerConfig.Source;
import com.acgist.snail.context.IStatisticsSession;
import com.acgist.snail.context.StatisticsGetter;
import com.acgist.snail.context.session.StatisticsSession;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.IPeerConnect;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Peer信息</p>
 * <p>IP地址、端口、下载统计、上传统计等等</p>
 * 
 * @author acgist
 */
public final class PeerSession extends StatisticsGetter implements IPeerConnect {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerSession.class);
	
	/**
	 * <p>PeerId</p>
	 */
	private byte[] id;
	/**
	 * <p>Peer客户端名称</p>
	 */
	private String clientName;
	/**
	 * <p>pex flags</p>
	 */
	private volatile byte flags = 0;
	/**
	 * <p>状态：下载中、上传中</p>
	 */
	private volatile byte status = 0;
	/**
	 * <p>Peer来源</p>
	 * <p>可以设置多个来源</p>
	 */
	private volatile byte source = 0;
	/**
	 * <p>连接失败次数</p>
	 */
	private volatile byte failTimes = 0;
	/**
	 * <p>Peer地址</p>
	 */
	private String host;
	/**
	 * <p>Peer端口</p>
	 */
	private Integer port;
	/**
	 * <p>DHT端口</p>
	 */
	private Integer dhtPort;
	/**
	 * <p>保留位</p>
	 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
	 */
	private byte[] reserved;
	/**
	 * <p>已经下载Piece位图</p>
	 * 
	 * TODO：不是线程安全：是否加锁
	 */
	private final BitSet pieces;
	/**
	 * <p>下载错误Piece位图</p>
	 * <p>排除下载选择</p>
	 */
	private final BitSet badPieces;
	/**
	 * <p>推荐下载Piece位图</p>
	 * <p>优先选择下载</p>
	 */
	private final BitSet suggestPieces;
	/**
	 * <p>快速允许下载Piece位图</p>
	 * <p>即使阻塞依然可以选择下载</p>
	 */
	private final BitSet allowedPieces;
	/**
	 * <p>holepunch是否等待</p>
	 */
	private volatile boolean holepunchWait = false;
	/**
	 * <p>holepunch是否连接</p>
	 * <p>向中继发出rendezvous消息加锁等待，收到中继connect消息后设置可以连接并释放等待锁。</p>
	 */
	private AtomicBoolean holepunchConnect = new AtomicBoolean(false);
	/**
	 * <p>PEX来源</p>
	 * <p>holepunch协议使用PEX来源作为中继</p>
	 */
	private PeerSession pexSource;
	/**
	 * <p>Peer上传</p>
	 */
	private PeerUploader peerUploader;
	/**
	 * <p>Peer下载</p>
	 */
	private PeerDownloader peerDownloader;
	/**
	 * <p>Peer支持的扩展协议</p>
	 * <p>协议=协议ID</p>
	 */
	private final Map<PeerConfig.ExtensionType, Byte> extension;

	/**
	 * @param parent 上级统计信息
	 * @param host Peer地址
	 * @param port Peer端口
	 */
	private PeerSession(IStatisticsSession parent, String host, Integer port) {
		super(new StatisticsSession(false, false, parent));
		this.host = host;
		this.port = port;
		this.pieces = new BitSet();
		this.badPieces = new BitSet();
		this.suggestPieces = new BitSet();
		this.allowedPieces = new BitSet();
		this.extension = new EnumMap<>(PeerConfig.ExtensionType.class);
	}
	
	/**
	 * <p>新建Peer信息</p>
	 * 
	 * @param parent 上级统计信息
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return {@link PeerSession}
	 */
	public static final PeerSession newInstance(IStatisticsSession parent, String host, Integer port) {
		return new PeerSession(parent, host, port);
	}

	@Override
	public IPeerConnect.ConnectType connectType() {
		final PeerConnect connect = this.peerConnect();
		if(connect == null) {
			return null;
		}
		return connect.connectType();
	}
	
	/**
	 * <p>设置PeerId和客户端名称</p>
	 * 
	 * @param id PeerId
	 */
	public void id(byte[] id) {
		this.id = id;
		this.clientName = PeerConfig.clientName(this.id);
	}
	
	/**
	 * <p>获取PeerId</p>
	 * 
	 * @return PeerId
	 */
	public byte[] id() {
		return this.id;
	}
	
	/**
	 * <p>获取客户端名称</p>
	 * 
	 * @return 客户端名称
	 */
	public String clientName() {
		if(this.clientName == null) {
			return PeerConfig.UNKNOWN;
		}
		return this.clientName;
	}
	
	/**
	 * <p>设置客户端名称</p>
	 * 
	 * @param clientName 客户端名称
	 */
	public void clientName(String clientName) {
		this.clientName = clientName;
	}

	/**
	 * <p>判断是否未知终端</p>
	 * 
	 * @return 是否未知终端
	 */
	public boolean unknownClientName() {
		return
			this.clientName == null ||
			PeerConfig.UNKNOWN.equals(this.clientName);
	}
	
	/**
	 * <p>获取Peer地址</p>
	 * 
	 * @return Peer地址
	 */
	public String host() {
		return this.host;
	}
	
	/**
	 * <p>获取Peer端口</p>
	 * 
	 * @return Peer端口
	 */
	public Integer port() {
		return this.port;
	}
	
	/**
	 * <p>设置Peer端口</p>
	 * 
	 * @param port Peer端口
	 */
	public void port(Integer port) {
		this.port = port;
	}
	
	/**
	 * <p>获取DHT端口</p>
	 * 
	 * @return DHT端口
	 */
	public Integer dhtPort() {
		return this.dhtPort;
	}
	
	/**
	 * <p>设置DHT端口</p>
	 * 
	 * @param dhtPort DHT端口
	 */
	public void dhtPort(Integer dhtPort) {
		this.dhtPort = dhtPort;
	}
	
	/**
	 * <p>清空Piece位图</p>
	 * 
	 * @see #pieces
	 * @see #badPieces
	 * @see #suggestPieces
	 * @see #allowedPieces
	 */
	public void cleanPieces() {
		this.pieces.clear();
		this.badPieces.clear();
		this.suggestPieces.clear();
		this.allowedPieces.clear();
	}
	
	/**
	 * <p>设置已经下载Piece位图</p>
	 * 
	 * @param pieces 已经下载Piece位图
	 */
	public void pieces(BitSet pieces) {
		this.pieces.or(pieces);
	}

	/**
	 * <p>设置已经下载Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void piece(int index) {
		if(PeerConfig.checkPiece(index)) {
			this.pieces.set(index);
		}
	}
	
	/**
	 * <p>取消已经下载Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void pieceOff(int index) {
		this.pieces.clear(index);
	}
	
	/**
	 * <p>判断Peer是否含有Piece</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return 是否含有Piece
	 */
	public boolean hasPiece(int index) {
		if(PeerConfig.checkPiece(index)) {
			return this.pieces.get(index);
		}
		return false;
	}
	
	/**
	 * <p>设置下载错误Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void badPieces(int index) {
		if(PeerConfig.checkPiece(index)) {
			this.badPieces.set(index);
		}
	}
	
	/**
	 * <p>获取可用的Piece位图</p>
	 * 
	 * @return 可用的Piece位图
	 * 
	 * @see #pieces
	 * @see #badPieces
	 */
	public BitSet availablePieces() {
		final BitSet bitSet = new BitSet();
		bitSet.or(this.pieces);
		bitSet.andNot(this.badPieces);
		return bitSet;
	}
	
	/**
	 * <p>设置推荐下载Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void suggestPieces(int index) {
		if(PeerConfig.checkPiece(index)) {
			this.pieces.set(index);
			this.suggestPieces.set(index);
		}
	}

	/**
	 * <p>获取推荐下载Piece位图</p>
	 * 
	 * @return 推荐下载Piece位图
	 */
	public BitSet suggestPieces() {
		return this.suggestPieces;
	}
	
	/**
	 * <p>设置快速允许下载Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void allowedPieces(int index) {
		if(PeerConfig.checkPiece(index)) {
			this.pieces.set(index);
			this.allowedPieces.set(index);
		}
	}

	/**
	 * <p>获取快速允许下载Piece位图</p>
	 * 
	 * @return 快速允许下载Piece位图
	 */
	public BitSet allowedPieces() {
		return this.allowedPieces;
	}

	/**
	 * <p>判断是否支持快速允许下载</p>
	 * 
	 * @return 是否支持快速允许下载
	 */
	public boolean supportAllowedFast() {
		return !this.allowedPieces.isEmpty();
	}
	
	/**
	 * <p>增加失败次数</p>
	 */
	public void incrementFailTimes() {
		this.failTimes++;
	}
	
	/**
	 * <p>判断是否可用</p>
	 * 
	 * @return 是否可用
	 * 
	 * @see #port
	 * @see #failTimes
	 * @see PeerConfig#MAX_FAIL_TIMES
	 */
	public boolean available() {
		return
			this.port != null &&
			this.failTimes < PeerConfig.MAX_FAIL_TIMES;
	}
	
	/**
	 * <p>设置保留位</p>
	 * 
	 * @param reserved 保留位
	 */
	public void reserved(byte[] reserved) {
		this.reserved = reserved;
	}
	
	/**
	 * <p>判断是否支持扩展协议</p>
	 * 
	 * @param index 保留位索引
	 * @param location 扩展协议位置
	 * 
	 * @return 是否支持扩展协议
	 */
	private boolean supportExtension(int index, byte location) {
		return this.reserved != null && (this.reserved[index] & location) == location;
	}
	
	/**
	 * <p>判断是否支持DHT扩展协议</p>
	 * 
	 * @return 是否支持DHT扩展协议
	 */
	public boolean supportDhtProtocol() {
		return this.supportExtension(7, PeerConfig.RESERVED_DHT_PROTOCOL);
	}
	
	/**
	 * <p>判断是否支持扩展协议</p>
	 * 
	 * @return 是否支持扩展协议
	 */
	public boolean supportExtensionProtocol() {
		return this.supportExtension(5, PeerConfig.RESERVED_EXTENSION_PROTOCOL);
	}
	
	/**
	 * <p>判断是否支持快速扩展协议</p>
	 * 
	 * @return 是否支持快速扩展协议
	 */
	public boolean supportFastExtensionProtocol() {
		return this.supportExtension(7, PeerConfig.RESERVED_FAST_PROTOCOL);
	}
	
	/**
	 * <p>添加Peer支持的扩展协议</p>
	 * 
	 * @param type 扩展协议类型
	 * @param typeId 扩展协议标识
	 */
	public void supportExtensionType(PeerConfig.ExtensionType type, byte typeId) {
		this.extension.put(type, typeId);
	}
	
	/**
	 * <p>判断Peer是否支持扩展协议</p>
	 * 
	 * @param type 扩展协议类型
	 * 
	 * @return 是否支持扩展协议
	 */
	public boolean supportExtensionType(PeerConfig.ExtensionType type) {
		return this.extension.containsKey(type);
	}
	
	/**
	 * <p>获取扩展协议标识</p>
	 * 
	 * @param type 扩展协议类型
	 * 
	 * @return 扩展协议标识
	 */
	public Byte extensionTypeId(PeerConfig.ExtensionType type) {
		return this.extension.get(type);
	}
	
	/**
	 * <p>设置Peer来源</p>
	 * 
	 * @param source Peer来源
	 */
	public void source(PeerConfig.Source source) {
		synchronized (this) {
			this.source |= source.getValue();
		}
	}

	/**
	 * <p>获取所有来源</p>
	 * 
	 * @return 所有来源
	 */
	public List<PeerConfig.Source> sources() {
		final PeerConfig.Source[] sources = PeerConfig.Source.values();
		final List<PeerConfig.Source> list = new ArrayList<>();
		for (Source value : sources) {
			if((this.source & value.getValue()) == value.getValue()) {
				list.add(value);
			}
		}
		return list;
	}
	
	/**
	 * <p>设置状态</p>
	 * 
	 * @param status 状态
	 */
	public void status(byte status) {
		synchronized (this) {
			this.status |= status;
		}
	}
	
	/**
	 * <p>取消状态</p>
	 * 
	 * @param status 状态
	 */
	public void statusOff(byte status) {
		synchronized (this) {
			this.status &= ~status;
		}
	}
	
	/**
	 * <p>判断是否处于状态</p>
	 * 
	 * @param status 状态
	 * 
	 * @return 是否处于状态
	 */
	private boolean verifyStatus(byte status) {
		return (this.status & status) == status;
	}
	
	/**
	 * <p>判断是否上传中</p>
	 * 
	 * @return 是否上传中
	 */
	public boolean uploading() {
		return this.verifyStatus(PeerConfig.STATUS_UPLOAD);
	}
	
	/**
	 * <p>判断是否下载中</p>
	 * 
	 * @return 是否下载中
	 */
	public boolean downloading() {
		return this.verifyStatus(PeerConfig.STATUS_DOWNLOAD);
	}
	
	/**
	 * <p>判断是否连接中</p>
	 * 
	 * @return 是否连接中
	 * 
	 * @see #uploading()
	 * @see #downloading()
	 */
	public boolean connected() {
		return this.uploading() || this.downloading();
	}
	
	/**
	 * <p>获取pex flags</p>
	 * 
	 * @return pex flags
	 */
	public byte flags() {
		return this.flags;
	}
	
	/**
	 * <p>设置pex flags</p>
	 * 
	 * @param flags pex flags
	 */
	public void flags(byte flags) {
		synchronized (this) {
			this.flags |= flags;
		}
	}
	
	/**
	 * <p>取消pex flags</p>
	 * 
	 * @param flags pex flags
	 */
	public void flagsOff(byte flags) {
		synchronized (this) {
			this.flags &= ~flags;
		}
	}

	/**
	 * <p>验证pex flags是否含有属性</p>
	 * 
	 * @param flags pex flags
	 * 
	 * @return 是否含有属性
	 */
	private boolean verifyFlags(byte flags) {
		return (this.flags & flags) == flags;
	}
	
	/**
	 * <p>判断是否支持UTP</p>
	 * 
	 * @return 是否支持UTP
	 */
	public boolean utp() {
		return this.verifyFlags(PeerConfig.PEX_UTP);
	}
	
	/**
	 * <p>判断是否可以连接</p>
	 * 
	 * @return 是否可以连接
	 */
	public boolean outgo() {
		return this.verifyFlags(PeerConfig.PEX_OUTGO);
	}

	/**
	 * <p>判断是否偏爱加密</p>
	 * 
	 * @return 是否偏爱加密
	 */
	public boolean encrypt() {
		return this.verifyFlags(PeerConfig.PEX_PREFER_ENCRYPTION);
	}
	
	/**
	 * <p>判断是否只上传不下载</p>
	 * 
	 * @return 是否只上传不下载
	 */
	public boolean uploadOnly() {
		return this.verifyFlags(PeerConfig.PEX_UPLOAD_ONLY);
	}
	
	/**
	 * <p>判断是否支持holepunch协议</p>
	 * 
	 * @return 是否支持holepunch协议
	 */
	public boolean holepunch() {
		return this.verifyFlags(PeerConfig.PEX_HOLEPUNCH);
	}
	
	/**
	 * <p>获取PEX来源</p>
	 * 
	 * @return PEX来源
	 */
	public PeerSession pexSource() {
		return this.pexSource;
	}
	
	/**
	 * <p>设置PEX来源</p>
	 * 
	 * @param pexSource PEX来源
	 */
	public void pexSource(PeerSession pexSource) {
		this.pexSource = pexSource;
	}

	/**
	 * <p>添加holepunch等待锁</p>
	 */
	public void lockHolepunch() {
		if(!this.holepunchConnect.get()) {
			synchronized (this.holepunchConnect) {
				if(!this.holepunchConnect.get()) {
					this.holepunchWait = true;
					try {
						this.holepunchConnect.wait(PeerConfig.HOLEPUNCH_TIMEOUT);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						LOGGER.debug("线程等待异常", e);
					}
					this.holepunchWait = false;
				}
			}
		}
	}
	
	/**
	 * <p>释放holepunch等待锁</p>
	 */
	public void unlockHolepunch() {
		synchronized (this.holepunchConnect) {
			this.holepunchConnect.set(true);
			this.holepunchConnect.notifyAll();
		}
	}
	
	/**
	 * <p>判断holepunch是否等待</p>
	 * 
	 * @return 是否等待
	 */
	public boolean holepunchWait() {
		return this.holepunchWait;
	}
	
	/**
	 * <p>判断holepunch是否连接</p>
	 * 
	 * @return 是否连接
	 */
	public boolean holeunchConnect() {
		return this.holepunchConnect.get();
	}
	
	/**
	 * <p>获取Peer连接</p>
	 * <p>优先使用下载连接，然后使用上传连接。</p>
	 * 
	 * @return Peer连接
	 */
	public PeerConnect peerConnect() {
		return this.peerDownloader != null ? this.peerDownloader : this.peerUploader;
	}
	
	/**
	 * <p>获取Peer上传</p>
	 * 
	 * @return Peer上传
	 */
	public PeerUploader peerUploader() {
		return this.peerUploader;
	}
	
	/**
	 * <p>设置Peer上传</p>
	 * 
	 * @param peerUploader Peer上传
	 */
	public void peerUploader(PeerUploader peerUploader) {
		this.peerUploader = peerUploader;
	}
	
	/**
	 * <p>获取Peer下载</p>
	 * 
	 * @return Peer下载
	 */
	public PeerDownloader peerDownloader() {
		return this.peerDownloader;
	}
	
	/**
	 * <p>设置Peer下载</p>
	 * 
	 * @param peerDownloader Peer下载
	 */
	public void peerDownloader(PeerDownloader peerDownloader) {
		this.peerDownloader = peerDownloader;
	}
	
	/**
	 * <p>获取Peer地址信息</p>
	 * 
	 * @return Peer地址信息
	 */
	public InetSocketAddress peerSocketAddress() {
		return NetUtils.buildSocketAddress(this.host, this.port);
	}
	
	/**
	 * <p>获取Peer DHT地址信息</p>
	 * 
	 * @return Peer DHT地址信息
	 */
	public InetSocketAddress dhtSocketAddress() {
		return NetUtils.buildSocketAddress(this.host, this.dhtPort);
	}
	
	/**
	 * <p>判断是否相等</p>
	 * 
	 * @param host Peer地址
	 * @param port Peer端口
	 * 
	 * @return 是否相等
	 */
	public boolean equals(String host, Integer port) {
		return
			StringUtils.equals(this.host, host) &&
			NumberUtils.equals(this.port, port);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.host, this.port);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof PeerSession session) {
			return this.equals(session.host, session.port);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.host, this.port, this.dhtPort);
	}

}
