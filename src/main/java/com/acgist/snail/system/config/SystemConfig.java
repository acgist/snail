package com.acgist.snail.system.config;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
	
	private static final String SYSTEM_CONFIG = "/config/system.properties";
	
	/**
	 * <p>最大的网络包大小。</p>
	 * <p>所有的需要创建ByteBuffer的长度由外部Peer设置时需要验证长度，防止恶意攻击导致内存泄露。</p>
	 */
	public static final int MAX_NET_BUFFER_SIZE = 10 * 1024 * 1024;
	/**
	 * 系统默认编码（file.encoding）
	 */
	public static final String DEFAULT_CHARSET = "utf-8";
	/**
	 * 无符号BYTE的大小
	 */
	public static final int UNSIGNED_BYTE_SIZE = 2 << 7;
	
	public static final String DIGIT = "0123456789";
	public static final String LETTER = "abcdefghijklmnopqrstuvwxyz";
	public static final String LETTER_UPPER = LETTER.toUpperCase();

	private static final SystemConfig INSTANCE = new SystemConfig();
	
	private SystemConfig() {
		super(SYSTEM_CONFIG);
	}

	static {
		LOGGER.info("初始化数据库配置");
		INSTANCE.init();
		INSTANCE.logger();
	}

	public static final SystemConfig getInstance() {
		return INSTANCE;
	}
	
	private String name; // 名称
	private String nameEn; // 英文名称
	private String version; // 版本
	
	private String author; // 作者
	private String source; // 源码
	private String support; // 支持
	
	private Integer serverPort; // 服务端口
	private Integer btPort; // 服务端口（Peer、DHT、UTP）
	private Integer btPortExt; // 服务端口（外网：Peer、DHT、UTP）
	
	private Integer peerSize; // 单个任务Peer数量
	private Integer trackerSize; // 单个任务Tracker数量
	private Integer pieceRepeatSize; // 任务即将完成时可以重复选择下载的剩下Piece数量
	
	private Integer dhtInterval; // DHT执行周期（秒）
	private Integer pexInterval; // PEX执行周期（秒）
	private Integer peerOptimizeInterval; // 单个任务Peer优化周期（秒）

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
		INSTANCE.serverPort = getInteger("acgist.server.port");
		INSTANCE.btPort = getInteger("acgist.bt.port");
		INSTANCE.peerSize = getInteger("acgist.peer.size");
		INSTANCE.trackerSize = getInteger("acgist.tracker.size");
		INSTANCE.pieceRepeatSize = getInteger("acgist.piece.repeat.size");
		INSTANCE.dhtInterval = getInteger("acgist.dht.interval");
		INSTANCE.pexInterval = getInteger("acgist.pex.interval");
		INSTANCE.peerOptimizeInterval = getInteger("acgist.peer.optimize.interval");
	}

	/**
	 * 日志
	 */
	private void logger() {
		LOGGER.info("名称：{}", this.name);
		LOGGER.info("英文名称：{}", this.nameEn);
		LOGGER.info("版本：{}", this.version);
		LOGGER.info("作者：{}", this.author);
		LOGGER.info("源码：{}", this.source);
		LOGGER.info("支持：{}", this.support);
		LOGGER.info("系统端口：{}", this.serverPort);
		LOGGER.info("服务端口（Peer、DHT、UTP）：{}", this.btPort);
		LOGGER.info("单个任务Peer数量：{}", this.peerSize);
		LOGGER.info("单个任务Tracker数量：{}", this.trackerSize);
		LOGGER.info("任务即将完成时可以重复选择下载的剩下Piece数量：{}", this.pieceRepeatSize);
		LOGGER.info("DHT执行周期（秒）：{}", this.dhtInterval);
		LOGGER.info("PEX执行周期（秒）：{}", this.pexInterval);
		LOGGER.info("单个任务Peer优化周期（秒）：{}", this.peerOptimizeInterval);
	}
	
	/**
	 * 名称
	 */
	public static final String getName() {
		return INSTANCE.name;
	}

	/**
	 * 英文名称
	 */
	public static final String getNameEn() {
		return INSTANCE.nameEn;
	}

	/**
	 * 版本
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
	 * 源码
	 */
	public static final String getSource() {
		return INSTANCE.source;
	}

	/**
	 * 支持
	 */
	public static final String getSupport() {
		return INSTANCE.support;
	}

	/**
	 * 服务端口
	 */
	public static final Integer getServerPort() {
		return INSTANCE.serverPort;
	}

	/**
	 * <p>服务端口（Peer、DHT、UTP）</p>
	 * <p>本机注册使用</p>
	 */
	public static final Integer getBtPort() {
		return INSTANCE.btPort;
	}
	
	/**
	 * 设置服务端口（外网：Peer、DHT、UTP），映射时如果端口已经被占用时重新设置的外网端口号。
	 */
	public static final void setBtPortExt(Integer btPortExt) {
		LOGGER.info("服务端口（外网：Peer、DHT、UTP）：{}", btPortExt);
		INSTANCE.btPortExt = btPortExt;
	}
	
	/**
	 * <p>服务端口（外网：Peer、DHT、UTP）</p>
	 * <p>外网使用，外网的Peer连接此端口。</p>
	 * <p>如果不存在返回{@linkplain #getBtPort() 本机端口}。</p>
	 */
	public static final Integer getBtPortExt() {
		if(INSTANCE.btPortExt == null) {
			return getBtPort();
		}
		return INSTANCE.btPortExt;
	}
	
	/**
	 * 服务端口（外网：Peer、DHT、UTP）：short
	 */
	public static final Short getBtPortExtShort() {
		return NetUtils.encodePort(getBtPortExt());
	}
	
	/**
	 * 单个任务Peer数量
	 */
	public static final Integer getPeerSize() {
		return INSTANCE.peerSize;
	}
	
	/**
	 * 单个任务tracker数量
	 */
	public static final Integer getTrackerSize() {
		return INSTANCE.trackerSize;
	}

	/**
	 * 任务即将完成时可以重复选择下载的剩下Piece数量
	 */
	public static final Integer getPieceRepeatSize() {
		return INSTANCE.pieceRepeatSize;
	}

	/**
	 * PEX执行周期（秒）
	 */
	public static final Integer getPexInterval() {
		return INSTANCE.pexInterval;
	}
	
	/**
	 * 单个任务Peer优化周期
	 */
	public static final Integer getPeerOptimizeInterval() {
		return INSTANCE.peerOptimizeInterval;
	}
	
	/**
	 * DHT执行周期（秒）
	 */
	public static final Integer getDhtInterval() {
		return INSTANCE.dhtInterval;
	}

	/**
	 * 获取名称和版本信息："名称 版本"
	 */
	public static final String getNameEnAndVersion() {
		return INSTANCE.nameEn + " " + INSTANCE.version;
	}

}
