package com.acgist.snail.pojo.session;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import com.acgist.snail.net.torrent.PeerConnect;
import com.acgist.snail.net.torrent.bootstrap.PeerDownloader;
import com.acgist.snail.net.torrent.bootstrap.PeerUploader;
import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.system.IStatistics;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>Peer信息</p>
 * <p>IP地址、端口、下载统计、上传统计等等</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class PeerSession implements IStatistics {
	
	/**
	 * <p>PeerId</p>
	 */
	private byte[] id;
	/**
	 * <p>Peer客户端名称</p>
	 */
	private String clientName;
	/**
	 * <p>保留位</p>
	 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
	 */
	private byte[] reserved;
	/**
	 * <p>pex flags</p>
	 */
	private volatile byte flags = 0;
	/**
	 * <p>状态：下载中、上传中</p>
	 */
	private volatile byte status = 0;
	/**
	 * <p>来源</p>
	 */
	private volatile byte source = 0;
	/**
	 * <p>失败次数</p>
	 */
	private volatile int failTimes = 0;
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
	 * <p>Peer已下载Piece位图</p>
	 */
	private final BitSet pieces;
	/**
	 * <p>下载错误Piece位图：校验失败Piece位图</p>
	 * <p>选择Piece时需要排除下载错误Piece位图</p>
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
	 * <p>向中继发出rendezvous消息进入等待，收到中继connect消息后设置可以连接释放等待锁。</p>
	 */
	private final Object holepunchLock = new Object();
	/**
	 * <p>PEX来源</p>
	 * <p>直接连接不上时使用holepunch协议连接</p>
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
	 * <p>支持的扩展协议：协议=协议ID</p>
	 */
	private final Map<PeerConfig.ExtensionType, Byte> extension;

	private PeerSession(IStatisticsSession parent, String host, Integer port) {
		this.host = host;
		this.port = port;
		this.pieces = new BitSet();
		this.badPieces = new BitSet();
		this.suggestPieces = new BitSet();
		this.allowedPieces = new BitSet();
		this.extension = new HashMap<>();
		this.statistics = new StatisticsSession(false, false, parent);
	}
	
	public static final PeerSession newInstance(IStatisticsSession parent, String host, Integer port) {
		return new PeerSession(parent, host, port);
	}
	
	/**
	 * <p>判断是否相等：IP地址是否一致（不判断端口）</p>
	 */
	public boolean equalsHost(String host) {
		return StringUtils.equals(this.host, host);
	}
	
	@Override
	public void upload(int buffer) {
		this.statistics.upload(buffer);
	}

	@Override
	public void download(int buffer) {
		this.statistics.download(buffer);
	}

	@Override
	public IStatisticsSession statistics() {
		return this.statistics;
	}
	
	/**
	 * <p>设置PeerId和客户端名称</p>
	 */
	public void id(byte[] id) {
		this.id = id;
		this.clientName = PeerConfig.name(this.id);
	}
	
	/**
	 * @return 客户端名称
	 */
	public String clientName() {
		return this.clientName;
	}
	
	/**
	 * @return Peer IP地址
	 */
	public String host() {
		return this.host;
	}
	
	/**
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
	 * <p>清空Piece位图：已下载Piece位图、下载错误Piece位图、推荐下载Piece位图、快速允许Piece位图</p>
	 */
	public void cleanPieces() {
		this.pieces.clear();
		this.badPieces.clear();
		this.suggestPieces.clear();
		this.allowedPieces.clear();
	}
	
	/**
	 * <p>设置Peer已下载Piece位图</p>
	 * 
	 * @param pieces Peer已下载Piece位图
	 */
	public void pieces(BitSet pieces) {
		this.pieces.or(pieces);
	}

	/**
	 * <p>设置Peer已下载Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void piece(int index) {
		this.pieces.set(index);
	}
	
	/**
	 * <p>取消Peer已下载Piece位图</p>
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
	 */
	public boolean havePiece(int index) {
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
	 * <p>Peer已下载Piece位图排除下载错误Piece位图</p>
	 */
	public BitSet availablePieces() {
		final BitSet bitSet = new BitSet();
		bitSet.or(this.pieces);
		bitSet.andNot(this.badPieces);
		return bitSet;
	}
	
	/**
	 * <p>设置推荐下载Piece位图</p>
	 * <p>同时设置Peer已下载Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void suggestPieces(int index) {
		this.pieces.set(index);
		this.suggestPieces.set(index);
	}

	/**
	 * <p>获取推荐下载Piece位图</p>
	 */
	public BitSet suggestPieces() {
		return this.suggestPieces;
	}
	
	/**
	 * <p>设置快速运行下载Piece位图</p>
	 * <p>同时设置Peer已下载Piece位图</p>
	 * 
	 * @param index Piece索引
	 */
	public void allowedPieces(int index) {
		this.pieces.set(index);
		this.allowedPieces.set(index);
	}

	/**
	 * <p>获取快速允许下载Piece位图</p>
	 */
	public BitSet allowedPieces() {
		return this.allowedPieces;
	}
	
	/**
	 * <p>是否支持快速允许下载</p>
	 */
	public boolean supportAllowedFast() {
		return !this.allowedPieces.isEmpty(); 
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
	 * <p>Peer是否支持扩展协议</p>
	 * 
	 * @param type 扩展协议类型
	 */
	public boolean supportExtensionType(PeerConfig.ExtensionType type) {
		return this.extension.containsKey(type);
	}

	/**
	 * <p>获取扩展协议标识</p>
	 * 
	 * @param type 扩展协议类型
	 */
	public Byte extensionTypeId(PeerConfig.ExtensionType type) {
		return this.extension.get(type);
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
	 * 	<dt>是否可用</dt>
	 * 	<dd>失败次数小于{@linkplain PeerConfig#MAX_FAIL_TIMES 最大失败次数}</dd>
	 * 	<dd>有可用端口（主动连接的客户端可能没有端口号）</dd>
	 * </dl>
	 */
	public boolean available() {
		return
			this.failTimes < PeerConfig.MAX_FAIL_TIMES &&
			this.port != null;
	}
	
	/**
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
	 * <p>设置保留位</p>
	 * 
	 * @param reserved 保留位
	 */
	public void reserved(byte[] reserved) {
		this.reserved = reserved;
	}
	
	/**
	 * <p>是否支持DHT扩展协议</p>
	 */
	public boolean supportDhtProtocol() {
		return this.reserved != null && (this.reserved[7] & PeerConfig.DHT_PROTOCOL) != 0;
	}
	
	/**
	 * <p>是否支持扩展协议</p>
	 */
	public boolean supportExtensionProtocol() {
		return this.reserved != null && (this.reserved[5] & PeerConfig.EXTENSION_PROTOCOL) != 0;
	}
	
	/**
	 * <p>是否支持快速扩展协议</p>
	 */
	public boolean supportFastExtensionProtocol() {
		return this.reserved != null && (this.reserved[7] & PeerConfig.FAST_PROTOCOL) != 0;
	}

	/**
	 * <p>设置来源</p>
	 * <p>每个Peer可能同时属于多个来源</p>
	 */
	public void source(byte source) {
		this.source |= source;
	}

	/**
	 * <p>验证来源</p>
	 */
	private boolean verifySource(byte source) {
		return (this.source & source) != 0;
	}
	
	/**
	 * <p>来源：DHT</p>
	 */
	public boolean fromDht() {
		return verifySource(PeerConfig.SOURCE_DHT);
	}
	
	/**
	 * <p>来源：LSD</p>
	 */
	public boolean fromLsd() {
		return verifySource(PeerConfig.SOURCE_LSD);
	}
	
	/**
	 * <p>来源：PEX</p>
	 */
	public boolean fromPex() {
		return verifySource(PeerConfig.SOURCE_PEX);
	}

	/**
	 * <p>来源：Tracker</p>
	 */
	public boolean fromTacker() {
		return verifySource(PeerConfig.SOURCE_TRACKER);
	}
	
	/**
	 * <p>来源：连接</p>
	 */
	public boolean fromConnect() {
		return verifySource(PeerConfig.SOURCE_CONNECT);
	}
	
	/**
	 * <p>来源：HOLEPUNCH</p>
	 */
	public boolean fromHolepunch() {
		return verifySource(PeerConfig.SOURCE_HOLEPUNCH);
	}
	
	/**
	 * <p>设置状态</p>
	 */
	public void status(byte status) {
		synchronized (this) {
			this.status |= status;
		}
	}
	
	/**
	 * <p>取消状态</p>
	 */
	public void statusOff(byte status) {
		synchronized (this) {
			this.status &= ~status;
		}
	}
	
	/**
	 * <p>验证状态</p>
	 */
	private boolean verifyStatus(byte status) {
		return (this.status & status) != 0;
	}
	
	/**
	 * <p>是否上传中</p>
	 */
	public boolean uploading() {
		return verifyStatus(PeerConfig.STATUS_UPLOAD);
	}
	
	/**
	 * <p>是否下载中</p>
	 */
	public boolean downloading() {
		return verifyStatus(PeerConfig.STATUS_DOWNLOAD);
	}
	
	/**
	 * <p>是否连接中：上传中、下载中</p>
	 */
	public boolean connected() {
		return uploading() || downloading();
	}
	
	/**
	 * <p>获取pex flags</p>
	 */
	public byte flags() {
		return this.flags;
	}
	
	/**
	 * <p>设置pex flags</p>
	 */
	public void flags(byte flags) {
		synchronized (this) {
			this.flags |= flags;
		}
	}
	
	/**
	 * <p>取消pex flags</p>
	 */
	public void flagsOff(byte flags) {
		synchronized (this) {
			this.flags &= ~flags;
		}
	}

	/**
	 * <p>验证pex flags</p>
	 */
	private boolean verifyFlags(byte flags) {
		return (this.flags & flags) != 0;
	}
	
	/**
	 * <p>是否支持UTP</p>
	 */
	public boolean utp() {
		return verifyFlags(PeerConfig.PEX_UTP);
	}
	
	/**
	 * <p>是否可以连接</p>
	 */
	public boolean outgo() {
		return verifyFlags(PeerConfig.PEX_OUTGO);
	}

	/**
	 * <p>是否偏爱加密</p>
	 */
	public boolean encrypt() {
		return verifyFlags(PeerConfig.PEX_PREFER_ENCRYPTION);
	}
	
	/**
	 * <p>是否只上传不下载</p>
	 */
	public boolean uploadOnly() {
		return verifyFlags(PeerConfig.PEX_UPLOAD_ONLY);
	}
	
	/**
	 * <p>是否支持holepunch协议</p>
	 */
	public boolean holepunch() {
		return verifyFlags(PeerConfig.PEX_HOLEPUNCH);
	}
	
	/**
	 * <p>获取Pex来源</p>
	 */
	public PeerSession pexSource() {
		return this.pexSource;
	}
	
	/**
	 * <p>设置Pex来源</p>
	 */
	public void pexSource(PeerSession pexSource) {
		this.pexSource = pexSource;
	}

	/**
	 * <p>holepunch等待锁</p>
	 */
	public void holepunchLock() {
		if(this.holepunchConnect) { // 已经连接
			return;
		}
		synchronized (this.holepunchLock) {
			this.holepunchWait = true;
			ThreadUtils.wait(this.holepunchLock, Duration.ofSeconds(PeerConfig.HOLEPUNCH_LOCK_TIME));
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
	 */
	public boolean holepunchWait() {
		return this.holepunchWait;
	}
	
	/**
	 * <p>holepunch是否连接</p>
	 */
	public boolean holeunchConnect() {
		return this.holepunchConnect;
	}
	
	/**
	 * <p>获取Peer连接</p>
	 * <p>优先顺序：{@link #peerDownloader}、{@link #peerUploader}</p>
	 */
	public PeerConnect peerConnect() {
		return
			this.peerDownloader != null ? this.peerDownloader :
			this.peerUploader != null ? this.peerUploader : null;
	}
	
	public PeerUploader peerUploader() {
		return this.peerUploader;
	}
	
	public void peerUploader(PeerUploader peerUploader) {
		this.peerUploader = peerUploader;
	}
	
	public PeerDownloader peerDownloader() {
		return this.peerDownloader;
	}
	
	public void peerDownloader(PeerDownloader peerDownloader) {
		this.peerDownloader = peerDownloader;
	}
	
	public InetSocketAddress peerSocketAddress() {
		return NetUtils.buildSocketAddress(this.host, this.port);
	}
	
	public InetSocketAddress dhtSocketAddress() {
		return NetUtils.buildSocketAddress(this.host, this.dhtPort);
	}
	
	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(this.host);
	}
	
	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(object instanceof PeerSession) {
			final PeerSession peerSession = (PeerSession) object;
			return StringUtils.equals(this.host, peerSession.host);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.host, this.port, this.dhtPort);
	}

}
