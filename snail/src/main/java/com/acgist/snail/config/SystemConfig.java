package com.acgist.snail.config;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>系统配置</p>
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
	 * <p>配置文件：{@value}</p>
	 */
	private static final String SYSTEM_CONFIG = "/config/system.properties";
	/**
	 * <p>端口占用字节大小：{@value}</p>
	 */
	public static final int PORT_LENGHT = 2;
	/**
	 * <p>IPv4占用字节大小：{@value}</p>
	 */
	public static final int IPV4_LENGTH = 4;
	/**
	 * <p>IPv6占用字节大小：{@value}</p>
	 */
	public static final int IPV6_LENGTH = 16;
	/**
	 * <p>强制关机定时任务时间（单位：秒）：{@value}</p>
	 */
	public static final int SHUTDOWN_FORCE_TIME = 30;
	/**
	 * <p>IPv4端口占用字节大小：{@value}</p>
	 */
	public static final int IPV4_PORT_LENGTH = IPV4_LENGTH + PORT_LENGHT;
	/**
	 * <p>IPv6端口占用字节大小：{@value}</p>
	 */
	public static final int IPV6_PORT_LENGTH = IPV6_LENGTH + PORT_LENGHT;
	/**
	 * <p>数据大小比例：{@value}</p>
	 */
	public static final int DATA_SCALE = 1024;
	/**
	 * <p>1KB数据大小：{@value}</p>
	 * <p>1KB = 1024B</p>
	 */
	public static final int ONE_KB = DATA_SCALE;
	/**
	 * <p>1MB数据大小：{@value}</p>
	 * <p>1MB = 1024KB = 1024 * 1024B</p>
	 */
	public static final int ONE_MB = DATA_SCALE * ONE_KB;
	/**
	 * <p>TCP消息缓冲大小：{@value}</p>
	 */
	public static final int TCP_BUFFER_LENGTH = 16 * ONE_KB;
	/**
	 * <p>UDP消息缓存大小：{@value}</p>
	 */
	public static final int UDP_BUFFER_LENGTH = 2 * ONE_KB;
	/**
	 * <p>最大的网络包大小：{@value}</p>
	 * <p>校验网络数据大小：防止太长导致内存泄漏</p>
	 */
	public static final int MAX_NET_BUFFER_LENGTH = 4 * ONE_MB;
	/**
	 * <p>最小下载速度：{@value}</p>
	 * <p>默认：16KB</p>
	 */
	public static final int MIN_DOWNLOAD_BUFFER_KB = 16;
	/**
	 * <p>默认数据传输大小：{@value}</p>
	 * 
	 * @see #MIN_DOWNLOAD_BUFFER_KB
	 */
	public static final int DEFAULT_EXCHANGE_LENGTH = MIN_DOWNLOAD_BUFFER_KB * ONE_KB;
	/**
	 * <p>时间比例：{@value}</p>
	 */
	public static final int DATE_SCALE = 1000;
	/**
	 * <p>一秒钟（毫秒）：{@value}</p>
	 */
	public static final int ONE_SECOND_MILLIS = DATE_SCALE;
	/**
	 * <p>没有超时时间：{@value}</p>
	 */
	public static final int NONE_TIMEOUT = 0;
	/**
	 * <p>连接超时时间（秒）：{@value}</p>
	 */
	public static final int CONNECT_TIMEOUT = 5;
	/**
	 * <p>连接超时时间（毫秒）：{@value}</p>
	 */
	public static final int CONNECT_TIMEOUT_MILLIS = CONNECT_TIMEOUT * ONE_SECOND_MILLIS;
	/**
	 * <p>接收超时时间（秒）：{@value}</p>
	 */
	public static final int RECEIVE_TIMEOUT = 5;
	/**
	 * <p>接收超时时间（毫秒）：{@value}</p>
	 */
	public static final int RECEIVE_TIMEOUT_MILLIS = RECEIVE_TIMEOUT * ONE_SECOND_MILLIS;
	/**
	 * <p>下载超时时间（秒）：{@value}</p>
	 */
	public static final int DOWNLOAD_TIMEOUT = 30;
	/**
	 * <p>下载超时时间（毫秒）：{@value}</p>
	 */
	public static final int DOWNLOAD_TIMEOUT_MILLIS = DOWNLOAD_TIMEOUT * ONE_SECOND_MILLIS;
	/**
	 * <p>刷新时间（秒）：{@value}</p>
	 * <p>任务列表、速度统计</p>
	 */
	public static final int REFRESH_INTERVAL = 4;
	/**
	 * <p>刷新时间（毫秒）：{@value}</p>
	 */
	public static final int REFRESH_INTERVAL_MILLIS = REFRESH_INTERVAL * ONE_SECOND_MILLIS;
	/**
	 * <p>SHA-1散列值长度：{@value}</p>
	 */
	public static final int SHA1_HASH_LENGTH = 20;
	/**
	 * <p>编码：{@value}</p>
	 */
	public static final String CHARSET_GBK = "GBK";
	/**
	 * <p>编码：{@value}</p>
	 */
	public static final String CHARSET_UTF8 = "UTF-8";
	/**
	 * <p>编码：{@value}</p>
	 */
	public static final String CHARSET_ASCII = "ASCII";
	/**
	 * <p>编码：{@value}</p>
	 */
	public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";
	/**
	 * <p>系统默认编码：{@value}</p>
	 * <p>启动参数：-D file.encoding=UTF-8</p>
	 */
	public static final String DEFAULT_CHARSET = CHARSET_UTF8;
	
	static {
		LOGGER.debug("初始化系统配置：{}", SYSTEM_CONFIG);
		INSTANCE.init();
		INSTANCE.logger();
		INSTANCE.release();
	}
	
	/**
	 * <p>软件名称</p>
	 */
	private String name;
	/**
	 * <p>软件名称（英文）</p>
	 */
	private String nameEn;
	/**
	 * <p>软件版本</p>
	 */
	private String version;
	/**
	 * <p>FTP匿名用户</p>
	 */
	private String ftpUser;
	/**
	 * <p>FTP匿名密码</p>
	 */
	private String ftpPassword;
	/**
	 * <p>作者</p>
	 */
	private String author;
	/**
	 * <p>官网与源码</p>
	 */
	private String source;
	/**
	 * <p>问题与建议</p>
	 */
	private String support;
	/**
	 * <p>最新稳定版本</p>
	 */
	private String latestRelease;
	/**
	 * <p>STUN服务器</p>
	 * <table border="1">
	 * 	<caption>配置格式</caption>
	 * 	<tr>
	 * 		<th>格式</th>
	 * 		<th>描述</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>host</td>
	 * 		<td>地址</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>host:port</td>
	 * 		<td>地址：端口</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>stun:host</td>
	 * 		<td>协议类型：地址</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>stun:host:port</td>
	 * 		<td>协议类型：地址：端口</td>
	 * 	</tr>
	 * </table>
	 */
	private String stunServer;
	/**
	 * <p>系统服务端口（本地服务：启动检测）</p>
	 */
	private int servicePort;
	/**
	 * <p>BT服务端口（本地端口：Peer、DHT、UTP、STUN）</p>
	 */
	private int torrentPort;
	/**
	 * <p>BT服务端口（外网端口：Peer、DHT、UTP、STUN）</p>
	 */
	private int torrentPortExt = 0;
	/**
	 * <p>单个任务Peer数量（同时下载）</p>
	 */
	private int peerSize;
	/**
	 * <p>单个任务Tracker数量</p>
	 */
	private int trackerSize;
	/**
	 * <p>任务即将完成时可以重复下载的Piece数量</p>
	 */
	private int pieceRepeatSize;
	/**
	 * <p>DHT执行周期（秒）</p>
	 */
	private int dhtInterval;
	/**
	 * <p>PEX执行周期（秒）</p>
	 */
	private int pexInterval;
	/**
	 * <p>本地发现执行周期（秒）</p>
	 */
	private int lsdInterval;
	/**
	 * <p>Have消息执行周期（秒）</p>
	 */
	private int haveInterval;
	/**
	 * <p>Tracker执行周期（秒）</p>
	 */
	private int trackerInterval;
	/**
	 * <p>Peer（连接、接入）优化周期（秒）</p>
	 */
	private int peerOptimizeInterval;
	/**
	 * <p>软件信息</p>
	 * 
	 * @see #nameEn
	 * @see #version
	 */
	private String nameEnAndVersion;
	/**
	 * <p>外网IP地址</p>
	 */
	private String externalIPAddress;
	/**
	 * <p>外网IP地址是否是IPv4</p>
	 */
	private boolean externalIPAddressIPv4 = true;
	
	private SystemConfig() {
		super(SYSTEM_CONFIG);
	}
	
	/**
	 * <p>初始化配置</p>
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
	 * <p>记录日志</p>
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
	 * <p>获取软件名称</p>
	 * 
	 * @return 软件名称
	 */
	public static final String getName() {
		return INSTANCE.name;
	}

	/**
	 * <p>获取软件名称（英文）</p>
	 * 
	 * @return 软件名称（英文）
	 */
	public static final String getNameEn() {
		return INSTANCE.nameEn;
	}

	/**
	 * <p>获取软件版本</p>
	 * 
	 * @return 软件版本
	 */
	public static final String getVersion() {
		return INSTANCE.version;
	}

	/**
	 * <p>获取FTP匿名用户</p>
	 * 
	 * @return FTP匿名用户
	 */
	public static final String getFtpUser() {
		return INSTANCE.ftpUser;
	}

	/**
	 * <p>获取FTP匿名密码</p>
	 * 
	 * @return FTP匿名密码
	 */
	public static final String getFtpPassword() {
		return INSTANCE.ftpPassword;
	}
	
	/**
	 * <p>获取作者</p>
	 * 
	 * @return 作者
	 */
	public static final String getAuthor() {
		return INSTANCE.author;
	}

	/**
	 * <p>获取官网与源码</p>
	 * 
	 * @return 官网与源码
	 */
	public static final String getSource() {
		return INSTANCE.source;
	}

	/**
	 * <p>获取问题与建议</p>
	 * 
	 * @return 问题与建议
	 */
	public static final String getSupport() {
		return INSTANCE.support;
	}
	
	/**
	 * <p>获取最新稳定版本</p>
	 * 
	 * @return 最新稳定版本
	 */
	public static final String getLatestRelease() {
		return INSTANCE.latestRelease;
	}

	/**
	 * <p>获取STUN服务器</p>
	 * 
	 * @return STUN服务器
	 */
	public static final String getStunServer() {
		return INSTANCE.stunServer;
	}
	
	/**
	 * <p>获取系统服务端口（本地服务：启动检测）</p>
	 * 
	 * @return 系统服务端口（本地服务：启动检测）
	 */
	public static final int getServicePort() {
		return INSTANCE.servicePort;
	}

	/**
	 * <p>获取BT服务端口（本地端口：Peer、DHT、UTP、STUN）</p>
	 * 
	 * @return BT服务端口（本地端口：Peer、DHT、UTP、STUN）
	 */
	public static final int getTorrentPort() {
		return INSTANCE.torrentPort;
	}
	
	/**
	 * <p>获取BT服务端口（外网端口：Peer、DHT、UTP、STUN）</p>
	 * 
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
	 * <p>设置BT服务端口（外网端口：Peer、DHT、UTP、STUN）</p>
	 * 
	 * @param torrentPortExt BT服务端口（外网端口：Peer、DHT、UTP、STUN）
	 */
	public static final void setTorrentPortExt(int torrentPortExt) {
		LOGGER.debug("设置BT服务端口（外网端口：Peer、DHT、UTP、STUN）：{}", torrentPortExt);
		INSTANCE.torrentPortExt = torrentPortExt;
	}
	
	/**
	 * <p>获取BT服务端口（外网端口：Peer、DHT、UTP、STUN）</p>
	 * 
	 * @return BT服务端口（外网端口：Peer、DHT、UTP、STUN）
	 */
	public static final short getTorrentPortExtShort() {
		return NetUtils.portToShort(getTorrentPortExt());
	}
	
	/**
	 * <p>获取单个任务Peer数量（同时下载）</p>
	 * 
	 * @return 单个任务Peer数量（同时下载）
	 */
	public static final int getPeerSize() {
		return INSTANCE.peerSize;
	}
	
	/**
	 * <p>获取单个任务Tracker数量</p>
	 * 
	 * @return 单个任务Tracker数量
	 */
	public static final int getTrackerSize() {
		return INSTANCE.trackerSize;
	}

	/**
	 * <p>获取任务即将完成时可以重复下载的Piece数量</p>
	 * 
	 * @return 任务即将完成时可以重复下载的Piece数量
	 */
	public static final int getPieceRepeatSize() {
		return INSTANCE.pieceRepeatSize;
	}
	
	/**
	 * <p>获取DHT执行周期（秒）</p>
	 * 
	 * @return DHT执行周期（秒）
	 */
	public static final int getDhtInterval() {
		return INSTANCE.dhtInterval;
	}
	
	/**
	 * <p>获取PEX执行周期（秒）</p>
	 * 
	 * @return PEX执行周期（秒）
	 */
	public static final int getPexInterval() {
		return INSTANCE.pexInterval;
	}
	
	/**
	 * <p>获取本地发现执行周期（秒）</p>
	 * 
	 * @return 本地发现执行周期（秒）
	 */
	public static final int getLsdInterval() {
		return INSTANCE.lsdInterval;
	}
	
	/**
	 * <p>获取Have消息执行周期（秒）</p>
	 * 
	 * @return Have消息执行周期（秒）
	 */
	public static final int getHaveInterval() {
		return INSTANCE.haveInterval;
	}
	
	/**
	 * <p>获取Tracker执行周期（秒）</p>
	 * 
	 * @return Tracker执行周期（秒）
	 */
	public static final int getTrackerInterval() {
		return INSTANCE.trackerInterval;
	}
	
	/**
	 * <p>获取Peer（连接、接入）优化周期（秒）</p>
	 * 
	 * @return Peer（连接、接入）优化周期（秒）
	 */
	public static final int getPeerOptimizeInterval() {
		return INSTANCE.peerOptimizeInterval;
	}

	/**
	 * <p>获取软件信息</p>
	 * 
	 * @return 软件信息
	 */
	public static final String getNameEnAndVersion() {
		return INSTANCE.nameEnAndVersion;
	}

	/**
	 * <p>设置外网IP地址</p>
	 * 
	 * @param externalIPAddress 外网IP地址
	 */
	public static final void setExternalIPAddress(String externalIPAddress) {
		LOGGER.debug("设置外网IP地址：{}", externalIPAddress);
		INSTANCE.externalIPAddress = externalIPAddress;
		INSTANCE.externalIPAddressIPv4 = NetUtils.ipv4(externalIPAddress);
	}
	
	/**
	 * <p>获取外网IP地址</p>
	 * 
	 * @return 外网IP地址
	 */
	public static final String getExternalIPAddress() {
		return INSTANCE.externalIPAddress;
	}
	
	/**
	 * <p>判断外网IP地址是否是IPv4</p>
	 * 
	 * @return 外网IP地址是否是IPv4
	 */
	public static final boolean externalIPAddressIPv4() {
		return INSTANCE.externalIPAddressIPv4;
	}
	
}
