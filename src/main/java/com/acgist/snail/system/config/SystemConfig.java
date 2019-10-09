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
public class SystemConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemConfig.class);
	
	private static final SystemConfig INSTANCE = new SystemConfig();
	
	private static final String SYSTEM_CONFIG = "/config/system.properties";

	/**
	 * 消息缓冲大小：Piece大小一样
	 */
	public static final int BUFFER_SIZE = 16 * 1024;
	/**
	 * 连接超时时间
	 */
	public static final int CONNECT_TIMEOUT = 5;
	/**
	 * 发送超时时间
	 */
	public static final int SEND_TIMEOUT = 5;
	/**
	 * 接收超时时间
	 */
	public static final int RECEIVE_TIMEOUT = 5;
	/**
	 * <p>最大的网络包大小</p>
	 * <p>所有创建ByteBuffer和byte[]对象的长度由外部数据设置时需要验证长度，防止恶意攻击导致内存泄露。</p>
	 */
	public static final int MAX_NET_BUFFER_SIZE = 4 * 1024 * 1024;
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
	 * 系统服务端口（本地服务：启动检测）
	 */
	private Integer servicePort;
	/**
	 * BT服务端口（本地端口：Peer、DHT、UTP）
	 */
	private Integer torrentPort;
	/**
	 * BT服务端口（外网映射：Peer、DHT、UTP）
	 */
	private Integer torrentPortExt;
	/**
	 * 单个任务Peer数量（同时下载）
	 */
	private Integer peerSize;
	/**
	 * 单个任务Tracker数量
	 */
	private Integer trackerSize;
	/**
	 * 任务即将完成时可以重复下载的Piece数量
	 */
	private Integer pieceRepeatSize;
	/**
	 * DHT执行周期（秒）
	 */
	private Integer dhtInterval;
	/**
	 * PEX执行周期（秒）
	 */
	private Integer pexInterval;
	/**
	 * 本地发现执行周期（秒）
	 */
	private Integer lsdInterval;
	/**
	 * Tracker执行周期（秒）
	 */
	private Integer trackerInterval;
	/**
	 * Peer（连接、接入）优化周期（秒）
	 */
	private Integer peerOptimizeInterval;
	
	/**
	 * 初始化
	 */
	private void init() {
		INSTANCE.name = getString("acgist.system.name");
		INSTANCE.nameEn = getString("acgist.system.name.en");
		INSTANCE.version = getString("acgist.system.version");
		INSTANCE.author = getString("acgist.system.author");
		INSTANCE.source = getString("acgist.system.source");
		INSTANCE.support = getString("acgist.system.support");
		INSTANCE.servicePort = getInteger("acgist.service.port");
		INSTANCE.torrentPort = getInteger("acgist.torrent.port");
		INSTANCE.peerSize = getInteger("acgist.peer.size");
		INSTANCE.trackerSize = getInteger("acgist.tracker.size");
		INSTANCE.pieceRepeatSize = getInteger("acgist.piece.repeat.size");
		INSTANCE.dhtInterval = getInteger("acgist.dht.interval");
		INSTANCE.pexInterval = getInteger("acgist.pex.interval");
		INSTANCE.lsdInterval = getInteger("acgist.lsd.interval");
		INSTANCE.trackerInterval = getInteger("acgist.tracker.interval");
		INSTANCE.peerOptimizeInterval = getInteger("acgist.peer.optimize.interval");
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
		LOGGER.info("BT服务端口（Peer、DHT、UTP）：{}", this.torrentPort);
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
	 * 系统服务端口
	 */
	public static final Integer getServicePort() {
		return INSTANCE.servicePort;
	}

	/**
	 * <p>BT服务端口（本机：Peer、DHT、UTP）</p>
	 */
	public static final Integer getTorrentPort() {
		return INSTANCE.torrentPort;
	}
	
	/**
	 * <p>设置BT服务端口（外网：Peer、DHT、UTP）</p>
	 * <p>UPNP映射时如果端口已经被占用时重新设置的外网端口号</p>
	 */
	public static final void setTorrentPortExt(Integer torrentPortExt) {
		LOGGER.info("服务端口（外网：Peer、DHT、UTP）：{}", torrentPortExt);
		INSTANCE.torrentPortExt = torrentPortExt;
	}
	
	/**
	 * <p>BT服务端口（外网：Peer、DHT、UTP）</p>
	 * <p>如果不存在返回{@linkplain #getTorrentPort() 本机端口}。</p>
	 */
	public static final Integer getTorrentPortExt() {
		if(INSTANCE.torrentPortExt == null) {
			return getTorrentPort();
		}
		return INSTANCE.torrentPortExt;
	}
	
	/**
	 * BT服务端口（外网：Peer、DHT、UTP）：short
	 */
	public static final Short getTorrentPortExtShort() {
		return NetUtils.encodePort(getTorrentPortExt());
	}
	
	/**
	 * 单个任务Peer数量（同时下载）
	 */
	public static final Integer getPeerSize() {
		return INSTANCE.peerSize;
	}
	
	/**
	 * 单个任务Tracker数量
	 */
	public static final Integer getTrackerSize() {
		return INSTANCE.trackerSize;
	}

	/**
	 * 任务即将完成时可以重复下载的Piece数量
	 */
	public static final Integer getPieceRepeatSize() {
		return INSTANCE.pieceRepeatSize;
	}

	/**
	 * DHT执行周期（秒）
	 */
	public static final Integer getDhtInterval() {
		return INSTANCE.dhtInterval;
	}
	
	/**
	 * PEX执行周期（秒）
	 */
	public static final Integer getPexInterval() {
		return INSTANCE.pexInterval;
	}
	
	/**
	 * 本地发现执行周期（秒）
	 */
	public static final Integer getLsdInterval() {
		return INSTANCE.lsdInterval;
	}
	
	/**
	 * Tracker执行周期（秒）
	 */
	public static final Integer getTrackerInterval() {
		return INSTANCE.trackerInterval;
	}
	
	/**
	 * Peer（连接、接入）优化周期（秒）
	 */
	public static final Integer getPeerOptimizeInterval() {
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

}
