package com.acgist.snail.config;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.NetUtils;

/**
 * 系统配置
 * 
 * @author acgist
 */
public final class SystemConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemConfig.class);
	
	private static final SystemConfig INSTANCE = new SystemConfig();
	
	public static final SystemConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 配置文件：{@value}
	 */
	private static final String SYSTEM_CONFIG = "/config/system.properties";
	/**
	 * 端口占用字节大小：{@value}
	 */
	public static final int PORT_LENGHT = 2;
	/**
	 * IPv4占用字节大小：{@value}
	 */
	public static final int IPV4_LENGTH = 4;
	/**
	 * IPv6占用字节大小：{@value}
	 */
	public static final int IPV6_LENGTH = 16;
	/**
	 * 强制关闭程序定时任务时间（单位：秒）：{@value}
	 */
	public static final int SHUTDOWN_FORCE_TIME = 30;
	/**
	 * IPv4端口占用字节大小：{@value}
	 */
	public static final int IPV4_PORT_LENGTH = IPV4_LENGTH + PORT_LENGHT;
	/**
	 * IPv6端口占用字节大小：{@value}
	 */
	public static final int IPV6_PORT_LENGTH = IPV6_LENGTH + PORT_LENGHT;
	/**
	 * 数据大小比例：{@value}
	 */
	public static final int DATA_SCALE = 1024;
	/**
	 * 1KB数据大小：{@value}
	 * 1KB = 1024B
	 */
	public static final int ONE_KB = DATA_SCALE;
	/**
	 * 1MB数据大小：{@value}
	 * 1MB = 1024KB = 1024 * 1024B
	 */
	public static final int ONE_MB = DATA_SCALE * ONE_KB;
	/**
	 * TCP消息缓冲大小：{@value}
	 */
	public static final int TCP_BUFFER_LENGTH = 16 * ONE_KB;
	/**
	 * UDP消息缓存大小：{@value}
	 */
	public static final int UDP_BUFFER_LENGTH = 2 * ONE_KB;
	/**
	 * 最大的网络包大小：{@value}
	 * 校验网络数据大小：防止太长导致内存泄漏
	 */
	public static final int MAX_NET_BUFFER_LENGTH = 4 * ONE_MB;
	/**
	 * 最小下载速度：{@value}KB
	 */
	public static final int MIN_DOWNLOAD_BUFFER_KB = 16;
	/**
	 * 默认数据传输大小：{@value}
	 * 
	 * @see #MIN_DOWNLOAD_BUFFER_KB
	 */
	public static final int DEFAULT_EXCHANGE_LENGTH = MIN_DOWNLOAD_BUFFER_KB * ONE_KB;
	/**
	 * 时间比例：{@value}
	 */
	public static final int DATE_SCALE = 1000;
	/**
	 * 一秒钟（毫秒）：{@value}
	 */
	public static final int ONE_SECOND_MILLIS = DATE_SCALE;
	/**
	 * 没有超时时间：{@value}
	 */
	public static final int NONE_TIMEOUT = 0;
	/**
	 * 连接超时时间（秒）：{@value}
	 */
	public static final int CONNECT_TIMEOUT = 5;
	/**
	 * 连接超时时间（毫秒）：{@value}
	 */
	public static final int CONNECT_TIMEOUT_MILLIS = CONNECT_TIMEOUT * ONE_SECOND_MILLIS;
	/**
	 * 接收超时时间（秒）：{@value}
	 */
	public static final int RECEIVE_TIMEOUT = 5;
	/**
	 * 接收超时时间（毫秒）：{@value}
	 */
	public static final int RECEIVE_TIMEOUT_MILLIS = RECEIVE_TIMEOUT * ONE_SECOND_MILLIS;
	/**
	 * 下载超时时间（秒）：{@value}
	 */
	public static final int DOWNLOAD_TIMEOUT = 30;
	/**
	 * 下载超时时间（毫秒）：{@value}
	 */
	public static final int DOWNLOAD_TIMEOUT_MILLIS = DOWNLOAD_TIMEOUT * ONE_SECOND_MILLIS;
	/**
	 * 刷新时间（秒）：{@value}
	 * 任务列表、速度统计
	 */
	public static final int REFRESH_INTERVAL = 5;
	/**
	 * 刷新时间（毫秒）：{@value}
	 */
	public static final int REFRESH_INTERVAL_MILLIS = REFRESH_INTERVAL * ONE_SECOND_MILLIS;
	/**
	 * SHA-1散列值长度：{@value}
	 */
	public static final int SHA1_HASH_LENGTH = 20;
	/**
	 * 编码：{@value}
	 */
	public static final String CHARSET_GBK = "GBK";
	/**
	 * 编码：{@value}
	 */
	public static final String CHARSET_UTF8 = "UTF-8";
	/**
	 * 编码：{@value}
	 */
	public static final String CHARSET_ASCII = "ASCII";
	/**
	 * 编码：{@value}
	 */
	public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";
	/**
	 * 系统默认编码：{@value}
	 * 启动参数：-D file.encoding=UTF-8
	 */
	public static final String DEFAULT_CHARSET = CHARSET_UTF8;
	
	static {
		INSTANCE.init();
		INSTANCE.logger();
		INSTANCE.release();
	}
	
	/**
	 * 软件名称
	 */
	private String name;
	/**
	 * 软件名称（英文）
	 */
	private String nameEn;
	/**
	 * 软件版本
	 */
	private String version;
	/**
	 * FTP匿名用户
	 */
	private String ftpUser;
	/**
	 * FTP匿名密码
	 */
	private String ftpPassword;
	/**
	 * 作者
	 */
	private String author;
	/**
	 * 官网与源码
	 */
	private String source;
	/**
	 * 问题与建议
	 */
	private String support;
	/**
	 * 最新稳定版本
	 */
	private String latestRelease;
	/**
	 * STUN服务器
	 * 地址：host
	 * 地址端口：host:port
	 * 协议类型地址：stun:host
	 * 协议类型地址端口：stun:host:port
	 */
	private String stunServer;
	/**
	 * 系统服务端口（本地服务：启动检测）
	 */
	private int servicePort;
	/**
	 * BT服务端口（本地端口：Peer、DHT、UTP、STUN）
	 */
	private int torrentPort;
	/**
	 * BT服务端口（外网端口：Peer、DHT、UTP、STUN）
	 */
	private int torrentPortExt = 0;
	/**
	 * 单个任务Peer数量（同时下载）
	 */
	private int peerSize;
	/**
	 * 单个任务Tracker数量
	 */
	private int trackerSize;
	/**
	 * 任务即将完成时可以重复下载的Piece数量
	 */
	private int pieceRepeatSize;
	/**
	 * DHT执行周期（秒）
	 */
	private int dhtInterval;
	/**
	 * PEX执行周期（秒）
	 */
	private int pexInterval;
	/**
	 * 本地发现执行周期（秒）
	 */
	private int lsdInterval;
	/**
	 * Have消息执行周期（秒）
	 */
	private int haveInterval;
	/**
	 * Tracker执行周期（秒）
	 */
	private int trackerInterval;
	/**
	 * Peer（连接、接入）优化周期（秒）
	 */
	private int peerOptimizeInterval;
	/**
	 * 软件信息
	 * 
	 * @see #nameEn
	 * @see #version
	 */
	private String nameEnAndVersion;
	/**
	 * 外网IP地址
	 */
	private String externalIPAddress;
	/**
	 * 外网IP地址是否是IPv4
	 */
	private boolean externalIPAddressIPv4 = true;
	
	private SystemConfig() {
		super(SYSTEM_CONFIG);
	}
	
	/**
	 * 初始化配置
	 */
	private void init() {
		this.name = this.getString("acgist.system.name");
		this.nameEn = this.getString("acgist.system.name.en");
		this.version = this.getString("acgist.system.version");
		this.ftpUser = this.getString("acgist.system.ftp.user");
		this.ftpPassword = this.getString("acgist.system.ftp.password");
		this.author = this.getString("acgist.system.author");
		this.source = this.getString("acgist.system.source");
		this.support = this.getString("acgist.system.support");
		this.latestRelease = this.getString("acgist.system.latest.release");
		this.stunServer = this.getString("acgist.system.stun.server");
		this.servicePort = this.getInteger("acgist.service.port", 16888);
		this.torrentPort = this.getInteger("acgist.torrent.port", 18888);
		this.peerSize = this.getInteger("acgist.peer.size", 20);
		this.trackerSize = this.getInteger("acgist.tracker.size", 50);
		this.pieceRepeatSize = this.getInteger("acgist.piece.repeat.size", 8);
		this.dhtInterval = this.getInteger("acgist.dht.interval", 120);
		this.pexInterval = this.getInteger("acgist.pex.interval", 120);
		this.lsdInterval = this.getInteger("acgist.lsd.interval", 120);
		this.haveInterval = this.getInteger("acgist.have.interval", 30);
		this.trackerInterval = this.getInteger("acgist.tracker.interval", 120);
		this.peerOptimizeInterval = this.getInteger("acgist.peer.optimize.interval", 60);
		this.nameEnAndVersion = SymbolConfig.Symbol.SPACE.join(this.nameEn, this.version);
	}

	/**
	 * 记录日志
	 */
	private void logger() {
		LOGGER.debug("软件名称：{}", this.name);
		LOGGER.debug("软件名称（英文）：{}", this.nameEn);
		LOGGER.debug("软件版本：{}", this.version);
		LOGGER.debug("FTP匿名用户：{}", this.ftpUser);
		LOGGER.debug("FTP匿名密码：{}", this.ftpPassword);
		LOGGER.debug("作者：{}", this.author);
		LOGGER.debug("官网与源码：{}", this.source);
		LOGGER.debug("问题与建议：{}", this.support);
		LOGGER.debug("最新稳定版本：{}", this.latestRelease);
		LOGGER.debug("STUN服务器：{}", this.stunServer);
		LOGGER.debug("系统服务端口（本地服务：启动检测）：{}", this.servicePort);
		LOGGER.debug("BT服务端口（本地端口：Peer、DHT、UTP、STUN）：{}", this.torrentPort);
		LOGGER.debug("单个任务Peer数量（同时下载）：{}", this.peerSize);
		LOGGER.debug("单个任务Tracker数量：{}", this.trackerSize);
		LOGGER.debug("任务即将完成时可以重复下载的Piece数量：{}", this.pieceRepeatSize);
		LOGGER.debug("DHT执行周期（秒）：{}", this.dhtInterval);
		LOGGER.debug("PEX执行周期（秒）：{}", this.pexInterval);
		LOGGER.debug("本地发现执行周期（秒）：{}", this.lsdInterval);
		LOGGER.debug("Have消息执行周期（秒）：{}", this.haveInterval);
		LOGGER.debug("Tracker执行周期（秒）：{}", this.trackerInterval);
		LOGGER.debug("Peer（连接、接入）优化周期（秒）：{}", this.peerOptimizeInterval);
		LOGGER.debug("软件信息：{}", this.nameEnAndVersion);
	}
	
	/**
	 * @return 软件名称
	 */
	public static final String getName() {
		return INSTANCE.name;
	}

	/**
	 * @return 软件名称（英文）
	 */
	public static final String getNameEn() {
		return INSTANCE.nameEn;
	}

	/**
	 * @return 软件版本
	 */
	public static final String getVersion() {
		return INSTANCE.version;
	}

	/**
	 * @return FTP匿名用户
	 */
	public static final String getFtpUser() {
		return INSTANCE.ftpUser;
	}

	/**
	 * @return FTP匿名密码
	 */
	public static final String getFtpPassword() {
		return INSTANCE.ftpPassword;
	}
	
	/**
	 * @return 作者
	 */
	public static final String getAuthor() {
		return INSTANCE.author;
	}

	/**
	 * @return 官网与源码
	 */
	public static final String getSource() {
		return INSTANCE.source;
	}

	/**
	 * @return 问题与建议
	 */
	public static final String getSupport() {
		return INSTANCE.support;
	}
	
	/**
	 * @return 最新稳定版本
	 */
	public static final String getLatestRelease() {
		return INSTANCE.latestRelease;
	}

	/**
	 * @return STUN服务器
	 */
	public static final String getStunServer() {
		return INSTANCE.stunServer;
	}
	
	/**
	 * @return 系统服务端口（本地服务：启动检测）
	 */
	public static final int getServicePort() {
		return INSTANCE.servicePort;
	}

	/**
	 * @return BT服务端口（本地端口：Peer、DHT、UTP、STUN）
	 */
	public static final int getTorrentPort() {
		return INSTANCE.torrentPort;
	}
	
	/**
	 * @return BT服务端口（外网端口：Peer、DHT、UTP、STUN）
	 * 
	 * @see #getTorrentPort()
	 */
	public static final int getTorrentPortExt() {
		if(INSTANCE.torrentPortExt == 0) {
			return getTorrentPort();
		}
		return INSTANCE.torrentPortExt;
	}
	
	/**
	 * @param torrentPortExt BT服务端口（外网端口：Peer、DHT、UTP、STUN）
	 */
	public static final void setTorrentPortExt(int torrentPortExt) {
		LOGGER.debug("设置BT服务端口（外网端口：Peer、DHT、UTP、STUN）：{}", torrentPortExt);
		INSTANCE.torrentPortExt = torrentPortExt;
	}
	
	/**
	 * @return BT服务端口（外网端口：Peer、DHT、UTP、STUN）
	 */
	public static final short getTorrentPortExtShort() {
		return NetUtils.portToShort(getTorrentPortExt());
	}
	
	/**
	 * @return 单个任务Peer数量（同时下载）
	 */
	public static final int getPeerSize() {
		return INSTANCE.peerSize;
	}
	
	/**
	 * @return 单个任务Tracker数量
	 */
	public static final int getTrackerSize() {
		return INSTANCE.trackerSize;
	}

	/**
	 * @return 任务即将完成时可以重复下载的Piece数量
	 */
	public static final int getPieceRepeatSize() {
		return INSTANCE.pieceRepeatSize;
	}
	
	/**
	 * @return DHT执行周期（秒）
	 */
	public static final int getDhtInterval() {
		return INSTANCE.dhtInterval;
	}
	
	/**
	 * @return PEX执行周期（秒）
	 */
	public static final int getPexInterval() {
		return INSTANCE.pexInterval;
	}
	
	/**
	 * @return 本地发现执行周期（秒）
	 */
	public static final int getLsdInterval() {
		return INSTANCE.lsdInterval;
	}
	
	/**
	 * @return Have消息执行周期（秒）
	 */
	public static final int getHaveInterval() {
		return INSTANCE.haveInterval;
	}
	
	/**
	 * @return Tracker执行周期（秒）
	 */
	public static final int getTrackerInterval() {
		return INSTANCE.trackerInterval;
	}
	
	/**
	 * @return Peer（连接、接入）优化周期（秒）
	 */
	public static final int getPeerOptimizeInterval() {
		return INSTANCE.peerOptimizeInterval;
	}

	/**
	 * @return 软件信息
	 */
	public static final String getNameEnAndVersion() {
		return INSTANCE.nameEnAndVersion;
	}

	/**
	 * @param externalIPAddress 外网IP地址
	 */
	public static final void setExternalIPAddress(String externalIPAddress) {
		LOGGER.debug("设置外网IP地址：{}", externalIPAddress);
		INSTANCE.externalIPAddress = externalIPAddress;
		INSTANCE.externalIPAddressIPv4 = NetUtils.ipv4(externalIPAddress);
	}
	
	/**
	 * @return 外网IP地址
	 */
	public static final String getExternalIPAddress() {
		return INSTANCE.externalIPAddress;
	}
	
	/**
	 * @return 外网IP地址是否是IPv4
	 */
	public static final boolean externalIPAddressIPv4() {
		return INSTANCE.externalIPAddressIPv4;
	}
	
}
