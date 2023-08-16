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
 * Peer信息
 * IP地址、端口、下载统计、上传统计等等
 * 
 * @author acgist
 */
public final class PeerSession extends StatisticsGetter implements IPeerConnect {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PeerSession.class);
    
    /**
     * PeerId
     */
    private byte[] id;
    /**
     * Peer客户端名称
     */
    private String clientName;
    /**
     * pex flags
     */
    private volatile byte flags = 0;
    /**
     * 状态：下载中、上传中
     */
    private volatile byte status = 0;
    /**
     * Peer来源
     * 可以设置多个来源
     */
    private volatile byte source = 0;
    /**
     * 连接失败次数
     */
    private volatile byte failTimes = 0;
    /**
     * Peer地址
     */
    private String host;
    /**
     * Peer端口
     */
    private Integer port;
    /**
     * DHT端口
     */
    private Integer dhtPort;
    /**
     * 保留位
     * 协议链接：http://www.bittorrent.org/beps/bep_0004.html
     */
    private byte[] reserved;
    /**
     * 已经下载Piece位图
     * 
     * TODO：不是线程安全：是否加锁
     */
    private final BitSet pieces;
    /**
     * 下载错误Piece位图
     * 排除下载选择
     */
    private final BitSet badPieces;
    /**
     * 推荐下载Piece位图
     * 优先选择下载
     */
    private final BitSet suggestPieces;
    /**
     * 快速允许下载Piece位图
     * 即使阻塞依然可以选择下载
     */
    private final BitSet allowedPieces;
    /**
     * holepunch是否等待
     */
    private volatile boolean holepunchWait = false;
    /**
     * holepunch是否连接
     * 向中继发出rendezvous消息加锁等待，收到中继connect消息后设置可以连接并释放等待锁。
     */
    private AtomicBoolean holepunchConnect = new AtomicBoolean(false);
    /**
     * PEX来源
     * holepunch协议使用PEX来源作为中继
     */
    private PeerSession pexSource;
    /**
     * Peer上传
     */
    private PeerUploader peerUploader;
    /**
     * Peer下载
     */
    private PeerDownloader peerDownloader;
    /**
     * Peer支持的扩展协议
     * 协议=协议ID
     */
    private final Map<PeerConfig.ExtensionType, Byte> extension;

    /**
     * @param parent 上级统计信息
     * @param host   Peer地址
     * @param port   Peer端口
     */
    private PeerSession(IStatisticsSession parent, String host, Integer port) {
        super(new StatisticsSession(false, false, parent));
        this.host          = host;
        this.port          = port;
        this.pieces        = new BitSet();
        this.badPieces     = new BitSet();
        this.suggestPieces = new BitSet();
        this.allowedPieces = new BitSet();
        this.extension     = new EnumMap<>(PeerConfig.ExtensionType.class);
    }
    
    /**
     * 新建Peer信息
     * 
     * @param parent 上级统计信息
     * @param host   地址
     * @param port   端口
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
     * 设置PeerId和客户端名称
     * 
     * @param id PeerId
     */
    public void id(byte[] id) {
        this.id = id;
        this.clientName = PeerConfig.clientName(this.id);
    }
    
    /**
     * @return PeerId
     */
    public byte[] id() {
        return this.id;
    }
    
    /**
     * @return 客户端名称
     */
    public String clientName() {
        if(this.clientName == null) {
            return PeerConfig.UNKNOWN;
        }
        return this.clientName;
    }
    
    /**
     * 设置客户端名称
     * 
     * @param clientName 客户端名称
     */
    public void clientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * 判断是否未知终端
     * 
     * @return 是否未知终端
     */
    public boolean unknownClientName() {
        return
            this.clientName == null ||
            PeerConfig.UNKNOWN.equals(this.clientName);
    }
    
    /**
     * @return Peer地址
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
     * 设置Peer端口
     * 
     * @param port Peer端口
     */
    public void port(Integer port) {
        this.port = port;
    }
    
    /**
     * @return DHT端口
     */
    public Integer dhtPort() {
        return this.dhtPort;
    }
    
    /**
     * 设置DHT端口
     * 
     * @param dhtPort DHT端口
     */
    public void dhtPort(Integer dhtPort) {
        this.dhtPort = dhtPort;
    }
    
    /**
     * 清空Piece位图
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
     * 设置已经下载Piece位图
     * 
     * @param pieces 已经下载Piece位图
     */
    public void pieces(BitSet pieces) {
        this.pieces.or(pieces);
    }

    /**
     * 设置已经下载Piece位图
     * 
     * @param index Piece索引
     */
    public void piece(int index) {
        if(PeerConfig.checkPiece(index)) {
            this.pieces.set(index);
        }
    }
    
    /**
     * 取消已经下载Piece位图
     * 
     * @param index Piece索引
     */
    public void pieceOff(int index) {
        this.pieces.clear(index);
    }
    
    /**
     * 判断Peer是否含有Piece
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
     * 设置下载错误Piece位图
     * 
     * @param index Piece索引
     */
    public void badPieces(int index) {
        if(PeerConfig.checkPiece(index)) {
            this.badPieces.set(index);
        }
    }
    
    /**
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
     * 设置推荐下载Piece位图
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
     * @return 推荐下载Piece位图
     */
    public BitSet suggestPieces() {
        return this.suggestPieces;
    }
    
    /**
     * 设置快速允许下载Piece位图
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
     * @return 快速允许下载Piece位图
     */
    public BitSet allowedPieces() {
        return this.allowedPieces;
    }

    /**
     * 判断是否支持快速允许下载
     * 
     * @return 是否支持快速允许下载
     */
    public boolean supportAllowedFast() {
        return !this.allowedPieces.isEmpty();
    }
    
    /**
     * 增加失败次数
     */
    public void incrementFailTimes() {
        this.failTimes++;
    }
    
    /**
     * 判断是否可用
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
     * 设置保留位
     * 
     * @param reserved 保留位
     */
    public void reserved(byte[] reserved) {
        this.reserved = reserved;
    }
    
    /**
     * 判断是否支持扩展协议
     * 
     * @param index    保留位索引
     * @param location 扩展协议位置
     * 
     * @return 是否支持扩展协议
     */
    private boolean supportExtension(int index, byte location) {
        return this.reserved != null && (this.reserved[index] & location) == location;
    }
    
    /**
     * 判断是否支持DHT扩展协议
     * 
     * @return 是否支持DHT扩展协议
     */
    public boolean supportDhtProtocol() {
        return this.supportExtension(7, PeerConfig.RESERVED_DHT_PROTOCOL);
    }
    
    /**
     * 判断是否支持扩展协议
     * 
     * @return 是否支持扩展协议
     */
    public boolean supportExtensionProtocol() {
        return this.supportExtension(5, PeerConfig.RESERVED_EXTENSION_PROTOCOL);
    }
    
    /**
     * 判断是否支持快速扩展协议
     * 
     * @return 是否支持快速扩展协议
     */
    public boolean supportFastExtensionProtocol() {
        return this.supportExtension(7, PeerConfig.RESERVED_FAST_PROTOCOL);
    }
    
    /**
     * 添加Peer支持的扩展协议
     * 
     * @param type   扩展协议类型
     * @param typeId 扩展协议标识
     */
    public void supportExtensionType(PeerConfig.ExtensionType type, byte typeId) {
        this.extension.put(type, typeId);
    }
    
    /**
     * 判断Peer是否支持扩展协议
     * 
     * @param type 扩展协议类型
     * 
     * @return 是否支持扩展协议
     */
    public boolean supportExtensionType(PeerConfig.ExtensionType type) {
        return this.extension.containsKey(type);
    }
    
    /**
     * @param type 扩展协议类型
     * 
     * @return 扩展协议标识
     */
    public Byte extensionTypeId(PeerConfig.ExtensionType type) {
        return this.extension.get(type);
    }
    
    /**
     * 设置Peer来源
     * 
     * @param source Peer来源
     */
    public void source(PeerConfig.Source source) {
        synchronized (this) {
            this.source |= source.getValue();
        }
    }

    /**
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
     * 设置状态
     * 
     * @param status 状态
     */
    public void status(byte status) {
        synchronized (this) {
            this.status |= status;
        }
    }
    
    /**
     * 取消状态
     * 
     * @param status 状态
     */
    public void statusOff(byte status) {
        synchronized (this) {
            this.status &= ~status;
        }
    }
    
    /**
     * 判断是否处于状态
     * 
     * @param status 状态
     * 
     * @return 是否处于状态
     */
    private boolean verifyStatus(byte status) {
        return (this.status & status) == status;
    }
    
    /**
     * 判断是否上传中
     * 
     * @return 是否上传中
     */
    public boolean uploading() {
        return this.verifyStatus(PeerConfig.STATUS_UPLOAD);
    }
    
    /**
     * 判断是否下载中
     * 
     * @return 是否下载中
     */
    public boolean downloading() {
        return this.verifyStatus(PeerConfig.STATUS_DOWNLOAD);
    }
    
    /**
     * 判断是否连接中
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
     * @return pex flags
     */
    public byte flags() {
        return this.flags;
    }
    
    /**
     * 设置pex flags
     * 
     * @param flags pex flags
     */
    public void flags(byte flags) {
        synchronized (this) {
            this.flags |= flags;
        }
    }
    
    /**
     * 取消pex flags
     * 
     * @param flags pex flags
     */
    public void flagsOff(byte flags) {
        synchronized (this) {
            this.flags &= ~flags;
        }
    }

    /**
     * 验证pex flags是否含有属性
     * 
     * @param flags pex flags
     * 
     * @return 是否含有属性
     */
    private boolean verifyFlags(byte flags) {
        return (this.flags & flags) == flags;
    }
    
    /**
     * 判断是否支持UTP
     * 
     * @return 是否支持UTP
     */
    public boolean utp() {
        return this.verifyFlags(PeerConfig.PEX_UTP);
    }
    
    /**
     * 判断是否可以连接
     * 
     * @return 是否可以连接
     */
    public boolean outgo() {
        return this.verifyFlags(PeerConfig.PEX_OUTGO);
    }

    /**
     * 判断是否偏爱加密
     * 
     * @return 是否偏爱加密
     */
    public boolean encrypt() {
        return this.verifyFlags(PeerConfig.PEX_PREFER_ENCRYPTION);
    }
    
    /**
     * 判断是否只上传不下载
     * 
     * @return 是否只上传不下载
     */
    public boolean uploadOnly() {
        return this.verifyFlags(PeerConfig.PEX_UPLOAD_ONLY);
    }
    
    /**
     * 判断是否支持holepunch协议
     * 
     * @return 是否支持holepunch协议
     */
    public boolean holepunch() {
        return this.verifyFlags(PeerConfig.PEX_HOLEPUNCH);
    }
    
    /**
     * @return PEX来源
     */
    public PeerSession pexSource() {
        return this.pexSource;
    }
    
    /**
     * 设置PEX来源
     * 
     * @param pexSource PEX来源
     */
    public void pexSource(PeerSession pexSource) {
        this.pexSource = pexSource;
    }

    /**
     * 添加holepunch等待锁
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
     * 释放holepunch等待锁
     */
    public void unlockHolepunch() {
        synchronized (this.holepunchConnect) {
            this.holepunchConnect.set(true);
            this.holepunchConnect.notifyAll();
        }
    }
    
    /**
     * 判断holepunch是否等待
     * 
     * @return 是否等待
     */
    public boolean holepunchWait() {
        return this.holepunchWait;
    }
    
    /**
     * 判断holepunch是否连接
     * 
     * @return 是否连接
     */
    public boolean holeunchConnect() {
        return this.holepunchConnect.get();
    }
    
    /**
     * 获取Peer连接
     * 优先使用下载连接，然后使用上传连接。
     * 
     * @return Peer连接
     */
    public PeerConnect peerConnect() {
        return this.peerDownloader != null ? this.peerDownloader : this.peerUploader;
    }
    
    /**
     * 获取Peer上传
     * 
     * @return Peer上传
     */
    public PeerUploader peerUploader() {
        return this.peerUploader;
    }
    
    /**
     * 设置Peer上传
     * 
     * @param peerUploader Peer上传
     */
    public void peerUploader(PeerUploader peerUploader) {
        this.peerUploader = peerUploader;
    }
    
    /**
     * @return Peer下载
     */
    public PeerDownloader peerDownloader() {
        return this.peerDownloader;
    }
    
    /**
     * 设置Peer下载
     * 
     * @param peerDownloader Peer下载
     */
    public void peerDownloader(PeerDownloader peerDownloader) {
        this.peerDownloader = peerDownloader;
    }
    
    /**
     * @return Peer地址信息
     */
    public InetSocketAddress peerSocketAddress() {
        return NetUtils.buildSocketAddress(this.host, this.port);
    }
    
    /**
     * @return Peer DHT地址信息
     */
    public InetSocketAddress dhtSocketAddress() {
        return NetUtils.buildSocketAddress(this.host, this.dhtPort);
    }
    
    /**
     * 判断是否相等
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
