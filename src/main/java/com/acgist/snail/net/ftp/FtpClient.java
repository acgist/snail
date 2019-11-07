package com.acgist.snail.net.ftp;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.TcpClient;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>FTP客户端</p>
 * <p>默认编码：GBK</p>
 * <p>连接登陆成功后会发送FEAT指令，如果支持UTF8指令，切换UTF-8编码。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FtpClient extends TcpClient<FtpMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpClient.class);
	
	/**
	 * 连接状态
	 */
	private boolean connect = false;
	/**
	 * 服务器地址
	 */
	private String host;
	/**
	 * 服务器端口
	 */
	private int port;
	/**
	 * 用户账号
	 */
	private String user;
	/**
	 * 用户密码
	 */
	private String password;
	/**
	 * 文件路径
	 */
	private String filePath;
	/**
	 * 编码：默认GBK
	 */
	private String charset = SystemConfig.CHARSET_GBK;

	private FtpClient(String host, int port, String user, String password, String filePath) {
		super("FTP Client", SystemConfig.CONNECT_TIMEOUT, new FtpMessageHandler());
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.filePath = filePath;
	}
	
	public static final FtpClient newInstance(String host, int port, String user, String password, String filePath) {
		return new FtpClient(host, port, user, password, filePath);
	}

	@Override
	public boolean connect() {
		this.connect = connect(this.host, this.port);
		this.handler.lock(); // 锁定：等待FTP欢迎消息
		if(this.connect) {
			this.login();
			if(this.handler.login()) {
				this.charset();
			} else {
				this.connect = false;
			}
		}
		return this.connect;
	}
	
	/**
	 * <p>开始下载</p>
	 * <p>默认从头开始下载</p>
	 * 
	 * @return 文件流
	 */
	public InputStream download() throws NetException {
		return this.download(null);
	}
	
	/**
	 * <p>开始下载</p>
	 * 
	 * @param downloadSize 断点续传位置
	 * 
	 * @return 文件流
	 */
	public InputStream download(Long downloadSize) throws NetException {
		if(!this.connect) {
			throw new NetException(this.failMessage("FTP服务器连接失败"));
		}
		synchronized (this) {
			changeMode();
			command("TYPE I"); // 设置数据模式：二进制
			if(downloadSize != null && downloadSize > 0L) {
				command("REST " + downloadSize);
			}
			command("RETR " + this.filePath);
			final var input = this.handler.inputStream();
			if(input == null) {
				throw new NetException(this.failMessage("获取FTP文件流失败"));
			}
			return input;
		}
	}
	
	/**
	 * @return 文件大小
	 */
	public Long size() throws NetException {
		if(!this.connect) {
			throw new NetException(this.failMessage("FTP服务器连接失败"));
		}
		synchronized (this) {
			this.changeMode();
			command("TYPE A"); // 切换数据模式：ASCII
			command("LIST " + this.filePath);
			final InputStream inputStream = this.handler.inputStream();
			final String data = StringUtils.ofInputStream(inputStream, this.charset);
			if(data == null) {
				throw new NetException(this.failMessage("未知错误"));
			}
			final Optional<String> optional = Stream.of(data.split(" "))
				.map(String::trim)
				.filter(StringUtils::isNotEmpty)
				.skip(4)
				.findFirst();
			if(optional.isPresent()) {
				return Long.valueOf(optional.get());
			}
			throw new NetException("获取FTP文件大小失败");
		}
	}
	
	/**
	 * 关闭资源
	 */
	@Override
	public void close() {
		if(!this.connect) {
			return;
		}
		command("QUIT", false); // 退出命令
		super.close();
	}
	
	/**
	 * @return 是否支持断点续传
	 */
	public boolean range() {
		return this.handler.range();
	}
	
	/**
	 * @return 错误信息
	 */
	public String failMessage(String defaultMessage) {
		return this.handler.failMessage(defaultMessage);
	}
	
	/**
	 * 登陆服务器
	 */
	private void login() {
		command("USER " + this.user);
		command("PASS " + this.password);
	}
	
	/**
	 * 设置编码
	 */
	private void charset() {
		command("FEAT"); // 列出扩展命令
		this.charset = this.handler.charset();
		if(SystemConfig.CHARSET_UTF8.equals(this.charset)) {
			command("OPTS UTF8 ON"); // 设置UTF8
		}
	}
	
	/**
	 * 切换被动模式
	 */
	private void changeMode() {
		command("PASV");
	}
	
	/**
	 * <p>发送FTP命令</p>
	 * <p>默认加锁</p>
	 * 
	 * @param command 命令
	 */
	private void command(String command) {
		this.command(command, true);
	}
	
	/**
	 * <p>发送FTP命令</p>
	 * 
	 * @param command 命令
	 * @param lock 是否加锁
	 */
	private void command(String command, boolean lock) {
		try {
			LOGGER.debug("发送FTP命令：{}", command);
			if(lock) {
				this.handler.resetLock();
				send(command, this.charset);
				this.handler.lock();
			} else {
				send(command, this.charset);
			}
		} catch (NetException e) {
			LOGGER.error("发送FTP命令异常：{}", command, e);
		}
	}

}
