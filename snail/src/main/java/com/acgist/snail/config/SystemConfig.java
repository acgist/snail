package com.acgist.snail.config;

import java.nio.file.Paths;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * <p>最小下载速度：{@value}</p>
	 * <p>16KB</p>
	 */
	public static final int MIN_BUFFER_KB = 16;
	/**
	 * <p>IP和端口占用字节大小：{@value}</p>
	 */
	public static final int IP_PORT_LENGTH = 6;
	/**
	 * <p>TCP消息缓冲大小：{@value}</p>
	 * <p>大小和Piece交换Slice大小一样</p>
	 */
	public static final int TCP_BUFFER_LENGTH = 16 * ONE_KB;
	/**
	 * <p>UDP消息缓存大小：{@value}</p>
	 */
	public static final int UDP_BUFFER_LENGTH = 2 * ONE_KB;
	/**
	 * <p>数据传输默认大小：{@value}</p>
	 * <p>一般IO读写缓冲数据大小</p>
	 */
	public static final int DEFAULT_EXCHANGE_BYTES_LENGTH = 16 * ONE_KB;
	/**
	 * <p>连接超时时间（秒）：{@value}</p>
	 */
	public static final int CONNECT_TIMEOUT = 5;
	/**
	 * <p>连接超时时间（毫秒）：{@value}</p>
	 */
	public static final int CONNECT_TIMEOUT_MILLIS = CONNECT_TIMEOUT * 1000;
	/**
	 * <p>接收超时时间（秒）：{@value}</p>
	 */
	public static final int RECEIVE_TIMEOUT = 5;
	/**
	 * <p>接收超时时间（毫秒）：{@value}</p>
	 */
	public static final int RECEIVE_TIMEOUT_MILLIS = RECEIVE_TIMEOUT * 1000;
	/**
	 * <p>下载超时时间（秒）：{@value}</p>
	 */
	public static final int DOWNLOAD_TIMEOUT = 30;
	/**
	 * <p>下载超时时间（毫秒）：{@value}</p>
	 */
	public static final int DOWNLOAD_TIMEOUT_MILLIS = DOWNLOAD_TIMEOUT * 1000;
	/**
	 * <p>最大的网络包大小：{@value}</p>
	 * <p>如果创建ByteBuffer和byte[]对象的长度是由外部数据决定时需要验证长度：防止恶意攻击导致内存泄露</p>
	 */
	public static final int MAX_NET_BUFFER_LENGTH = 4 * ONE_MB;
	/**
	 * <p>SHA-1的Hash值长度：{@value}</p>
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
	 * <p>系统默认编码（file.encoding）：{@value}</p>
	 */
	public static final String DEFAULT_CHARSET = CHARSET_UTF8;
	/**
	 * <p>无符号BYTE最大值：{@value}</p>
	 */
	public static final int UNSIGNED_BYTE_MAX = 2 << 7;
	/**
	 * <p>数字：{@value}</p>
	 */
	public static final String DIGIT = "0123456789";
	/**
	 * <p>字符（小写）：{@value}</p>
	 */
	public static final String LETTER = "abcdefghijklmnopqrstuvwxyz";
	/**
	 * <p>字符（大写）</p>
	 */
	public static final String LETTER_UPPER = LETTER.toUpperCase();
	/**
	 * <p>任务列表刷新时间</p>
	 */
	public static final Duration TASK_REFRESH_INTERVAL = Duration.ofSeconds(4);
	/**
	 * <p>换行分隔符</p>
	 */
	public static final String LINE_SEPARATOR = "\n";
	/**
	 * <p>换行分隔符（兼容）</p>
	 */
	public static final String LINE_COMPAT_SEPARATOR = "\r\n";
	/**
	 * <p>用户工作目录</p>
	 * <p>注意：初始化为常量（不能使用类变量：本类初始化时会使用）</p>
	 */
	private static final String USER_DIR = System.getProperty("user.dir");
	
	static {
		LOGGER.info("初始化系统配置：{}", SYSTEM_CONFIG);
		INSTANCE.init();
		INSTANCE.logger();
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
	 * <p>删除任务是否删除文件</p>
	 */
	private boolean taskFileDelete;
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
	 * <p>HLS下载线程数量</p>
	 */
	private int hlsThreadSize;
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
	 * <p>Tracker执行周期（秒）</p>
	 */
	private int trackerInterval;
	/**
	 * <p>Peer（连接、接入）优化周期（秒）</p>
	 */
	private int peerOptimizeInterval;
	/**
	 * <p>外网IP地址</p>
	 */
	private String externalIpAddress;
	
	/**
	 * <p>禁止创建实例</p>
	 */
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
		this.taskFileDelete = this.getBoolean("acgist.task.file.delete", false);
		this.servicePort = this.getInteger("acgist.service.port", 16888);
		this.torrentPort = this.getInteger("acgist.torrent.port", 18888);
		this.peerSize = this.getInteger("acgist.peer.size", 20);
		this.trackerSize = this.getInteger("acgist.tracker.size", 50);
		this.pieceRepeatSize = this.getInteger("acgist.piece.repeat.size", 4);
		this.hlsThreadSize = this.getInteger("acgist.hls.thread.size", 10);
		this.dhtInterval = this.getInteger("acgist.dht.interval", 120);
		this.pexInterval = this.getInteger("acgist.pex.interval", 120);
		this.lsdInterval = this.getInteger("acgist.lsd.interval", 120);
		this.trackerInterval = this.getInteger("acgist.tracker.interval", 120);
		this.peerOptimizeInterval = this.getInteger("acgist.peer.optimize.interval", 60);
	}

	/**
	 * <p>日志记录</p>
	 */
	private void logger() {
		LOGGER.info("软件名称：{}", this.name);
		LOGGER.info("软件名称（英文）：{}", this.nameEn);
		LOGGER.info("软件版本：{}", this.version);
		LOGGER.info("FTP匿名用户：{}", this.ftpUser);
		LOGGER.info("FTP匿名密码：{}", this.ftpPassword);
		LOGGER.info("作者：{}", this.author);
		LOGGER.info("官网与源码：{}", this.source);
		LOGGER.info("问题与建议：{}", this.support);
		LOGGER.info("最新稳定版本：{}", this.latestRelease);
		LOGGER.info("STUN服务器：{}", this.stunServer);
		LOGGER.info("删除任务是否删除文件：{}", this.taskFileDelete);
		LOGGER.info("系统服务端口：{}", this.servicePort);
		LOGGER.info("BT服务端口（Peer、DHT、UTP、STUN）：{}", this.torrentPort);
		LOGGER.info("单个任务Peer数量（同时下载）：{}", this.peerSize);
		LOGGER.info("单个任务Tracker数量：{}", this.trackerSize);
		LOGGER.info("任务即将完成时可以重复下载的Piece数量：{}", this.pieceRepeatSize);
		LOGGER.info("HLS下载线程数量：{}", this.hlsThreadSize);
		LOGGER.info("DHT执行周期（秒）：{}", this.dhtInterval);
		LOGGER.info("PEX执行周期（秒）：{}", this.pexInterval);
		LOGGER.info("本地发现执行周期（秒）：{}", this.lsdInterval);
		LOGGER.info("Tracker执行周期（秒）：{}", this.trackerInterval);
		LOGGER.info("Peer（连接、接入）优化周期（秒）：{}", this.peerOptimizeInterval);
		LOGGER.info("用户工作目录：{}", SystemConfig.USER_DIR);
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
	 * <p>获取删除任务是否删除文件</p>
	 * 
	 * @return 删除任务是否删除文件
	 */
	public static final boolean getTaskFileDelete() {
		return INSTANCE.taskFileDelete;
	}
	
	/**
	 * <p>获取系统服务端口</p>
	 * 
	 * @return 系统服务端口
	 */
	public static final int getServicePort() {
		return INSTANCE.servicePort;
	}

	/**
	 * <p>获取BT服务端口</p>
	 * <p>本地端口：Peer、DHT、UTP、STUN</p>
	 * 
	 * @return BT服务端口
	 */
	public static final int getTorrentPort() {
		return INSTANCE.torrentPort;
	}
	
	/**
	 * <p>获取BT服务端口</p>
	 * <p>外网端口：Peer、DHT、UTP、STUN</p>
	 * <p>如果不存在返回{@linkplain #getTorrentPort() 本地端口}</p>
	 * 
	 * @return BT服务端口
	 */
	public static final int getTorrentPortExt() {
		if(INSTANCE.torrentPortExt == 0) {
			return getTorrentPort();
		}
		return INSTANCE.torrentPortExt;
	}
	
	/**
	 * <p>设置BT服务端口</p>
	 * <p>外网端口：Peer、DHT、UTP、STUN</p>
	 * <p>本地端口和外网端口可能不一致</p>
	 * 
	 * @param torrentPortExt BT服务端口
	 */
	public static final void setTorrentPortExt(int torrentPortExt) {
		LOGGER.info("BT服务端口（外网端口：Peer、DHT、UTP、STUN）：{}", torrentPortExt);
		INSTANCE.torrentPortExt = torrentPortExt;
	}
	
	/**
	 * <p>获取BT服务端口（short）</p>
	 * <p>外网端口：Peer、DHT、UTP、STUN</p>
	 * 
	 * @return BT服务端口
	 */
	public static final short getTorrentPortExtShort() {
		return NetUtils.encodePort(getTorrentPortExt());
	}
	
	/**
	 * <p>获取单个任务Peer数量（同时下载）</p>
	 * 
	 * @return 单个任务Peer数量
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
	 * <p>获取HLS下载线程数量</p>
	 * 
	 * @return HLS下载线程数量
	 */
	public static final int getHlsThreadSize() {
		return INSTANCE.hlsThreadSize;
	}

	/**
	 * <p>获取DHT执行周期（秒）</p>
	 * 
	 * @return DHT执行周期
	 */
	public static final int getDhtInterval() {
		return INSTANCE.dhtInterval;
	}
	
	/**
	 * <p>获取PEX执行周期（秒）</p>
	 * 
	 * @return PEX执行周期
	 */
	public static final int getPexInterval() {
		return INSTANCE.pexInterval;
	}
	
	/**
	 * <p>获取本地发现执行周期（秒）</p>
	 * 
	 * @return 本地发现执行周期
	 */
	public static final int getLsdInterval() {
		return INSTANCE.lsdInterval;
	}
	
	/**
	 * <p>获取Tracker执行周期（秒）</p>
	 * 
	 * @return Tracker执行周期
	 */
	public static final int getTrackerInterval() {
		return INSTANCE.trackerInterval;
	}
	
	/**
	 * <p>获取Peer（连接、接入）优化周期（秒）</p>
	 * 
	 * @return Peer优化周期
	 */
	public static final int getPeerOptimizeInterval() {
		return INSTANCE.peerOptimizeInterval;
	}

	/**
	 * <p>获取用户工作目录</p>
	 * 
	 * @return 用户工作目录
	 */
	public static final String userDir() {
		return SystemConfig.USER_DIR;
	}
	
	/**
	 * <p>获取用户工作目录中的文件路径</p>
	 * 
	 * @param path 文件相对路径
	 * 
	 * @return 文件路径
	 */
	public static final String userDir(String path) {
		return Paths.get(userDir(), path).toString();
	}
	
	/**
	 * <p>获取软件信息</p>
	 * <p>软件信息：软件名称（英文） + " " + 软件版本</p>
	 * 
	 * @return 软件信息
	 */
	public static final String getNameEnAndVersion() {
		return INSTANCE.nameEn + " " + INSTANCE.version;
	}

	/**
	 * <p>设置外网IP地址</p>
	 * 
	 * @param externalIpAddress 外网IP地址
	 */
	public static final void setExternalIpAddress(String externalIpAddress) {
		LOGGER.info("设置外网IP地址：{}", externalIpAddress);
		INSTANCE.externalIpAddress = externalIpAddress;
	}
	
	/**
	 * <p>获取外网IP地址</p>
	 * 
	 * @return 外网IP地址
	 */
	public static final String getExternalIpAddress() {
		return INSTANCE.externalIpAddress;
	}
	
}
