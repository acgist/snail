package com.acgist.snail.system.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.NetUtils;

/**
 * <p>系统配置</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class SystemConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemConfig.class);
	
	private static final SystemConfig INSTANCE = new SystemConfig();
	
	private static final String SYSTEM_CONFIG = "/config/system.properties";

	/**
	 * 数据比例
	 */
	public static final int DATA_SCALE = 1024;
	/**
	 * 1KB数据大小：1KB = 1024B
	 */
	public static final int ONE_KB = DATA_SCALE;
	/**
	 * 1MB数据大小：1MB = 1024KB = 1024 * 1024B
	 */
	public static final int ONE_MB = 1024 * 1024;
	/**
	 * 最小下载速度：16KB
	 */
	public static final int MIN_BUFFER_KB = 16;
	/**
	 * TCP消息缓冲大小：和Piece交换Slice一样
	 */
	public static final int TCP_BUFFER_LENGTH = 16 * ONE_KB;
	/**
	 * UDP消息缓存大小
	 */
	public static final int UDP_BUFFER_LENGTH = 2 * ONE_KB;
	/**
	 * 连接超时时间（秒）
	 */
	public static final int CONNECT_TIMEOUT = 5;
	/**
	 * 连接超时时间（毫秒）
	 */
	public static final int CONNECT_TIMEOUT_MILLIS = CONNECT_TIMEOUT * 1000;
	/**
	 * 接收超时时间（秒）
	 */
	public static final int RECEIVE_TIMEOUT = 5;
	/**
	 * 接收超时时间（毫秒）
	 */
	public static final int RECEIVE_TIMEOUT_MILLIS = RECEIVE_TIMEOUT * 1000;
	/**
	 * 下载超时时间（秒）
	 */
	public static final int DOWNLOAD_TIMEOUT = 60;
	/**
	 * 下载超时时间（毫秒）
	 */
	public static final int DOWNLOAD_TIMEOUT_MILLIS = DOWNLOAD_TIMEOUT * 1000;
	/**
	 * <p>最大的网络包大小</p>
	 * <p>所有创建ByteBuffer和byte[]对象的长度由外部数据设置时需要验证长度，防止恶意攻击导致内存泄露。</p>
	 */
	public static final int MAX_NET_BUFFER_LENGTH = 4 * ONE_MB;
	/**
	 * SHA1的HASH值长度：20
	 */
	public static final int SHA1_HASH_LENGTH = 20;
	/**
	 * 编码：GBK
	 */
	public static final String CHARSET_GBK = "GBK";
	/**
	 * 编码：UTF-8
	 */
	public static final String CHARSET_UTF8 = "UTF-8";
	/**
	 * 编码：ASCII
	 */
	public static final String CHARSET_ASCII = "ASCII";
	/**
	 * 编码：ISO-8859-1
	 */
	public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";
	/**
	 * 系统默认编码（file.encoding）
	 */
	public static final String DEFAULT_CHARSET = CHARSET_UTF8;
	/**
	 * 无符号BYTE最大值
	 */
	public static final int UNSIGNED_BYTE_MAX = 2 << 7;
	/**
	 * 数字
	 */
	public static final String DIGIT = "0123456789";
	/**
	 * 字符（小写）
	 */
	public static final String LETTER = "abcdefghijklmnopqrstuvwxyz";
	/**
	 * 字符（大写）
	 */
	public static final String LETTER_UPPER = LETTER.toUpperCase();
	/**
	 * 任务列表刷新时间
	 */
	public static final Duration TASK_REFRESH_INTERVAL = Duration.ofSeconds(4);
	/**
	 * <p>用户工作目录</p>
	 * <p>注意顺序：优先初始化，不能使用类变量，本类初始化时会使用。</p>
	 */
	private static final String USER_DIR = System.getProperty("user.dir");
	
	private SystemConfig() {
		super(SYSTEM_CONFIG);
	}

	static {
		LOGGER.info("初始化系统配置");
		INSTANCE.init();
		INSTANCE.logger();
	}

	public static final SystemConfig getInstance() {
		return INSTANCE;
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
	 * <p>STUN服务器</p>
	 * <dl>
	 * 	<dt>格式：</dt>
	 * 	<dd>host</dd>
	 * 	<dd>host:port</dd>
	 * 	<dd>stun:host</dd>
	 * 	<dd>stun:host:port</dd>
	 * </dl>
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
	 * BT服务端口（外网映射：Peer、DHT、UTP、STUN）
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
	 * Tracker执行周期（秒）
	 */
	private int trackerInterval;
	/**
	 * Peer（连接、接入）优化周期（秒）
	 */
	private int peerOptimizeInterval;
	/**
	 * 外网IP地址
	 */
	private String externalIpAddress;
	
	/**
	 * 初始化
	 */
	private void init() {
		this.name = getString("acgist.system.name");
		this.nameEn = getString("acgist.system.name.en");
		this.version = getString("acgist.system.version");
		this.author = getString("acgist.system.author");
		this.source = getString("acgist.system.source");
		this.support = getString("acgist.system.support");
		this.stunServer = getString("acgist.system.stun.server");
		this.servicePort = getInteger("acgist.service.port", 16888);
		this.torrentPort = getInteger("acgist.torrent.port", 18888);
		this.peerSize = getInteger("acgist.peer.size", 20);
		this.trackerSize = getInteger("acgist.tracker.size", 50);
		this.pieceRepeatSize = getInteger("acgist.piece.repeat.size", 4);
		this.dhtInterval = getInteger("acgist.dht.interval", 120);
		this.pexInterval = getInteger("acgist.pex.interval", 120);
		this.lsdInterval = getInteger("acgist.lsd.interval", 120);
		this.trackerInterval = getInteger("acgist.tracker.interval", 120);
		this.peerOptimizeInterval = getInteger("acgist.peer.optimize.interval", 60);
	}

	/**
	 * 日志
	 */
	private void logger() {
		LOGGER.info("软件名称：{}", this.name);
		LOGGER.info("软件名称（英文）：{}", this.nameEn);
		LOGGER.info("软件版本：{}", this.version);
		LOGGER.info("作者：{}", this.author);
		LOGGER.info("官网与源码：{}", this.source);
		LOGGER.info("问题与建议：{}", this.support);
		LOGGER.info("系统服务端口：{}", this.servicePort);
		LOGGER.info("BT服务端口（Peer、DHT、UTP、STUN）：{}", this.torrentPort);
		LOGGER.info("单个任务Peer数量（同时下载）：{}", this.peerSize);
		LOGGER.info("单个任务Tracker数量：{}", this.trackerSize);
		LOGGER.info("任务即将完成时可以重复下载的Piece数量：{}", this.pieceRepeatSize);
		LOGGER.info("DHT执行周期（秒）：{}", this.dhtInterval);
		LOGGER.info("PEX执行周期（秒）：{}", this.pexInterval);
		LOGGER.info("本地发现执行周期（秒）：{}", this.lsdInterval);
		LOGGER.info("Tracker执行周期（秒）：{}", this.trackerInterval);
		LOGGER.info("Peer（连接、接入）优化周期（秒）：{}", this.peerOptimizeInterval);
		LOGGER.info("用户工作目录：{}", SystemConfig.USER_DIR);
	}
	
	/**
	 * 软件名称
	 */
	public static final String getName() {
		return INSTANCE.name;
	}

	/**
	 * 软件名称（英文）
	 */
	public static final String getNameEn() {
		return INSTANCE.nameEn;
	}

	/**
	 * 软件版本
	 */
	public static final String getVersion() {
		return INSTANCE.version;
	}

	/**
	 * 作者
	 */
	public static final String getAuthor() {
		return INSTANCE.author;
	}

	/**
	 * 官网与源码
	 */
	public static final String getSource() {
		return INSTANCE.source;
	}

	/**
	 * 问题与建议
	 */
	public static final String getSupport() {
		return INSTANCE.support;
	}

	/**
	 * STUN服务器
	 */
	public static final String getStunServer() {
		return INSTANCE.stunServer;
	}
	
	/**
	 * 系统服务端口
	 */
	public static final int getServicePort() {
		return INSTANCE.servicePort;
	}

	/**
	 * <p>BT服务端口（本机：Peer、DHT、UTP、STUN）</p>
	 */
	public static final int getTorrentPort() {
		return INSTANCE.torrentPort;
	}
	
	/**
	 * <p>设置BT服务端口（外网：Peer、DHT、UTP、STUN）</p>
	 * <p>UPNP映射时如果端口已经被占用时重新设置的外网端口号</p>
	 */
	public static final void setTorrentPortExt(int torrentPortExt) {
		LOGGER.info("服务端口（外网：Peer、DHT、UTP、STUN）：{}", torrentPortExt);
		INSTANCE.torrentPortExt = torrentPortExt;
	}
	
	/**
	 * <p>BT服务端口（外网：Peer、DHT、UTP、STUN）</p>
	 * <p>如果不存在返回{@linkplain #getTorrentPort() 本机端口}。</p>
	 */
	public static final int getTorrentPortExt() {
		if(INSTANCE.torrentPortExt == 0) {
			return getTorrentPort();
		}
		return INSTANCE.torrentPortExt;
	}
	
	/**
	 * BT服务端口（外网：Peer、DHT、UTP、STUN）：short
	 */
	public static final short getTorrentPortExtShort() {
		return NetUtils.encodePort(getTorrentPortExt());
	}
	
	/**
	 * 单个任务Peer数量（同时下载）
	 */
	public static final int getPeerSize() {
		return INSTANCE.peerSize;
	}
	
	/**
	 * 单个任务Tracker数量
	 */
	public static final int getTrackerSize() {
		return INSTANCE.trackerSize;
	}

	/**
	 * 任务即将完成时可以重复下载的Piece数量
	 */
	public static final int getPieceRepeatSize() {
		return INSTANCE.pieceRepeatSize;
	}

	/**
	 * DHT执行周期（秒）
	 */
	public static final int getDhtInterval() {
		return INSTANCE.dhtInterval;
	}
	
	/**
	 * PEX执行周期（秒）
	 */
	public static final int getPexInterval() {
		return INSTANCE.pexInterval;
	}
	
	/**
	 * 本地发现执行周期（秒）
	 */
	public static final int getLsdInterval() {
		return INSTANCE.lsdInterval;
	}
	
	/**
	 * Tracker执行周期（秒）
	 */
	public static final int getTrackerInterval() {
		return INSTANCE.trackerInterval;
	}
	
	/**
	 * Peer（连接、接入）优化周期（秒）
	 */
	public static final int getPeerOptimizeInterval() {
		return INSTANCE.peerOptimizeInterval;
	}

	/**
	 * 用户工作目录
	 */
	public static final String userDir() {
		return SystemConfig.USER_DIR;
	}
	
	/**
	 * 用户工作目录中的文件路径
	 * 
	 * @param path 文件相对路径：以“/”开头
	 */
	public static final String userDir(String path) {
		return SystemConfig.USER_DIR + path;
	}
	
	/**
	 * 获取软件信息：软件名称（英文） 软件版本
	 */
	public static final String getNameEnAndVersion() {
		return INSTANCE.nameEn + " " + INSTANCE.version;
	}

	/**
	 * 设置外网IP地址
	 */
	public static final void setExternalIpAddress(String externalIpAddress) {
		LOGGER.info("设置外网IP地址：{}", externalIpAddress);
		INSTANCE.externalIpAddress = externalIpAddress;
	}
	
	/**
	 * 获取外网IP地址
	 */
	public static final String getExternalIpAddress() {
		return INSTANCE.externalIpAddress;
	}
	
}
