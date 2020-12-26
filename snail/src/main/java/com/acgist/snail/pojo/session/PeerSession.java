package com.acgist.snail.pojo.session;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.PeerConfig.Source;
import com.acgist.snail.net.torrent.peer.PeerConnect;
import com.acgist.snail.net.torrent.peer.PeerDownloader;
import com.acgist.snail.net.torrent.peer.PeerUploader;
import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.IStatisticsSessionGetter;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Peer信息</p>
 * <p>IP地址、端口、下载统计、上传统计等等</p>
 * 
 * @author acgist
 */
public final class PeerSession implements IStatisticsSessionGetter {
	
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
	 * <p>Peer可以设置多个来源</p>
	 */
	private volatile byte source = 0;
	/**
	 * <p>失败次数</p>
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
	 * 
	 * TODO：独立封装
	 */
	private byte[] reserved;
	/**
	 * <p>已下载Piece位图</p>
	 */
	private final BitSet pieces;
	/**
	 * <p>下载错误Piece位图</p>
	 * <p>Piece下载完成后校验失败时设置下载错误Piece位图，选择Piece时需要排除下载错误Piece位图。</p>
	 */
	private final BitSet badPieces;
	/**
	 * <p>推荐下载Piece位图</p>
	 * <p>优先下载</p>
	 */
	private final BitSet suggestPieces;
	/**
	 * <p>快速允许下载Piece位图</p>
	 * <p>即是被Peer阻塞依然可以下载的Piece</p>
	 */
	private final BitSet allowedPieces;
	/**
	 * <p>holepunch是否等待</p>
	 */
	private volatile boolean holepunchWait = false;
	/**
	 * <p>holepunch是否连接</p>
	 */
	private volatile boolean holepunchConnect = false;
	/**
	 * <p>holepunch等待锁</p>
	 * <p>向中继发出rendezvous消息进入等待，收到中继connect消息后设置可以连接并释放等待锁。</p>
	 */
	private final Object holepunchLock = new Object();
	/**
	 * <p>PEX来源</p>
	 * <p>直接连接不上时使用holepunch协议连接，PEX来源作为中继。</p>
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
	 * <p>统计信息</p>
	 */
	private final IStatisticsSession statistics;
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
		this.host = host;
		this.port = port;
		this.pieces = new BitSet();
		this.badPieces = new BitSet();
		this.suggestPieces = new BitSet();
		this.allowedPieces = new BitSet();
		this.extension = new EnumMap<>(PeerConfig.ExtensionType.class);
		this.statistics = new StatisticsSession(false, false, parent);
	}
	
	/**
	 * <p>新建Peer信息</p>
	 * 
	 * @param parent 上级统计信息
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return Peer信息
	 */
	public static final PeerSession newInstance(IStatisticsSession parent, String host, Integer port) {
		return new PeerSession(parent, host, port);
	}

	@Override
	public IStatisticsSession statistics() {
		return this.statistics;
	}
	
	/**
	 * <p>获取累计上传大小</p>
	 * 
	 * @return 累计上传大小
	 * 
	 * @see IStatisticsSession#uploadSize()
	 */
	public long uploadSize() {
		return this.statistics.uploadSize();
	}
	
	/**
	 * <p>获取累计下载大小</p>
	 * 
	 * @return 累计下载大小
	 * 
	 * @see IStatisticsSession#downloadSize()
	 */
	public long downloadSize() {
		return this.statistics.downloadSize();
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
		return this.clientName;
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
	 * <p>清空：已下载Piece位图、下载错误Piece位图、推荐下载Piece位图、快速允许下载Piece位图</p>
	 */
	public void cleanPieces() {
		this.pieces.clear();
		this.badPieces.clear();
		this.suggestPieces.clear();
		this.allowedPieces.clear();
	}
	
	/**
	 * <p>设置已下载Piece位图</p>
	 * 
	 * @param pieces 已下载Piece位图
	 */
	public void pieces(BitSet pieces) {
		this.pieces.or(pieces);
	}

	/**
	 * <p>设置已下载Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void piece(int index) {
		this.pieces.set(index);
	}
	
	/**
	 * <p>取消已下载Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void pieceOff(int index) {
		this.pieces.clear(index);
	}
	
	/**
	 * <p>Peer是否含有Piece</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return 是否含有
	 */
	public boolean hasPiece(int index) {
		if(index < 0) {
			return false;
		}
		return this.pieces.get(index);
	}
	
	/**
	 * <p>设置下载错误Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void badPieces(int index) {
		this.badPieces.set(index);
	}
	
	/**
	 * <p>获取可用的Piece位图</p>
	 * <p>已下载Piece位图排除下载错误Piece位图</p>
	 * 
	 * @return 可用的Piece位图
	 */
	public BitSet availablePieces() {
		final BitSet bitSet = new BitSet();
		bitSet.or(this.pieces);
		bitSet.andNot(this.badPieces);
		return bitSet;
	}
	
	/**
	 * <p>设置推荐下载Piece位图</p>
	 * <p>同时设置已下载Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void suggestPieces(int index) {
		this.pieces.set(index);
		this.suggestPieces.set(index);
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
	 * <p>同时设置已下载Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void allowedPieces(int index) {
		this.pieces.set(index);
		this.allowedPieces.set(index);
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
	 * <p>握手失败增加失败次数，超过{@linkplain PeerConfig#MAX_FAIL_TIMES 最大失败次数}后不可用。</p>
	 */
	public void fail() {
		this.failTimes++;
	}
	
	/**
	 * <dl>
	 * 	<dt>判断是否可用</dt>
	 * 	<dd>失败次数小于{@linkplain PeerConfig#MAX_FAIL_TIMES 最大失败次数}</dd>
	 * 	<dd>端口可用（主动连接的客户端可能没有设置端口）</dd>
	 * </dl>
	 * 
	 * @return 是否可用
	 */
	public boolean available() {
		return
			this.failTimes < PeerConfig.MAX_FAIL_TIMES &&
			this.port != null;
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
	 * @return 是否支持
	 */
	private boolean supportExtension(int index, int location) {
		return this.reserved != null && (this.reserved[index] & location) != 0;
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
	public void addExtensionType(PeerConfig.ExtensionType type, byte typeId) {
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
			this.source |= source.value();
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
		for (Source source : sources) {
			if((this.source & source.value()) != 0) {
				list.add(source);
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
	 * <p>验证状态</p>
	 * 
	 * @param status 状态
	 * 
	 * @return 是否处于状态
	 */
	private boolean verifyStatus(byte status) {
		return (this.status & status) != 0;
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
	 * <p>判断是否连接中（上传中、下载中）</p>
	 * 
	 * @see #uploading()
	 * @see #downloading()
	 * 
	 * @return 是否连接中
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
	 * <p>验证pex flags</p>
	 * 
	 * @param flags pex flags
	 * 
	 * @return 是否含有属性
	 */
	private boolean verifyFlags(byte flags) {
		return (this.flags & flags) != 0;
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
	 * <p>获取Pex来源</p>
	 * 
	 * @return Pex来源
	 */
	public PeerSession pexSource() {
		return this.pexSource;
	}
	
	/**
	 * <p>设置Pex来源</p>
	 * 
	 * @param pexSource Pex来源
	 */
	public void pexSource(PeerSession pexSource) {
		this.pexSource = pexSource;
	}

	/**
	 * <p>添加holepunch等待锁</p>
	 */
	public void lockHolepunch() {
		if(this.holepunchConnect) { // 已经连接
			return;
		}
		synchronized (this.holepunchLock) {
			this.holepunchWait = true;
			try {
				// TODO：优化：状态锁一个原子变量
				this.holepunchLock.wait(PeerConfig.HOLEPUNCH_TIMEOUT);
			} catch (InterruptedException e) {
				LOGGER.debug("线程等待异常", e);
				Thread.currentThread().interrupt();
			}
			this.holepunchWait = false;
		}
	}
	
	/**
	 * <p>释放holepunch等待锁</p>
	 */
	public void unlockHolepunch() {
		synchronized (this.holepunchLock) {
			this.holepunchConnect = true;
			this.holepunchLock.notifyAll();
		}
	}
	
	/**
	 * <p>holepunch是否等待</p>
	 * 
	 * @return 是否等待
	 */
	public boolean holepunchWait() {
		return this.holepunchWait;
	}
	
	/**
	 * <p>holepunch是否连接</p>
	 * 
	 * @return 是否连接
	 */
	public boolean holeunchConnect() {
		return this.holepunchConnect;
	}
	
	/**
	 * <p>获取Peer连接</p>
	 * <p>优先顺序：{@link #peerDownloader}、{@link #peerUploader}</p>
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
	
	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(this.host);
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
		return StringUtils.equals(this.host, host) && NumberUtils.equals(this.port, port);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof PeerSession) {
			final PeerSession peerSession = (PeerSession) object;
			return peerSession.equals(this.host, this.port);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.host, this.port, this.dhtPort);
	}

}
