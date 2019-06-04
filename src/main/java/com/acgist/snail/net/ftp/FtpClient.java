package com.acgist.snail.net.ftp;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.TcpClient;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * FTP客户端
 * TODO：中文路径
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FtpClient extends TcpClient<FtpMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpClient.class);
	
	public static final String ANONYMOUS = "anonymous"; // 匿名用户名
	
	private boolean ok = false; // 连接成功
	private String host; // 服务器地址
	private int port; // 服务器端口
	private String user; // FTP用户
	private String password; // FTP密码
	private String filePath; // 文件路径

	private FtpClient(String host, int port, String user, String password, String filePath) {
		super("FTP Client", 2, new FtpMessageHandler());
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
		this.ok = connect(this.host, this.port);
		if(this.ok) {
			this.login();
		}
		return this.ok;
	}
	
	/**
	 * 开始下载
	 */
	public InputStream download() {
		return this.download(null);
	}
	
	/**
	 * 开始下载
	 * 
	 * @param downloadSize 已下载大小
	 */
	public InputStream download(Long downloadSize) {
		if(!this.ok) {
			return null;
		}
		synchronized (this) {
			changeMode();
			command("TYPE I");
			if(downloadSize != null && downloadSize != 0L) {
				command("REST " + downloadSize);
			}
			command("RETR " + this.filePath);
			return this.handler.inputStream();
		}
	}
	
	/**
	 * 获取文件大小
	 */
	public Long size() throws NetException {
		if(!this.ok) {
			throw new NetException("服务器连接失败");
		}
		synchronized (this) {
			this.changeMode();
			command("TYPE A");
			command("LIST " + this.filePath);
			final InputStream inputStream = this.handler.inputStream();
			final String data = StringUtils.ofInputStream(inputStream);
			if(data == null) {
				throw new NetException(failMessage());
			}
			final Optional<String> optional = Stream.of(data.split(" "))
				.map(String::trim)
				.filter(StringUtils::isNotEmpty)
				.skip(4)
				.findFirst();
			if(optional.isPresent()) {
				return Long.valueOf(optional.get());
			}
			throw new NetException("获取下载大小失败");
		}
	}
	
	/**
	 * 关闭资源
	 */
	@Override
	public void close() {
		if(!this.ok) {
			return;
		}
		command("QUIT"); // 退出命令
		super.close();
	}
	
	/**
	 * 是否支持断点续传
	 */
	public boolean append() {
		return this.handler.append();
	}
	
	/**
	 * 错误信息
	 */
	public String failMessage() {
		return this.handler.failMessage();
	}
	
	/**
	 * 登陆服务器
	 */
	private void login() {
		command("USER " + this.user);
		command("PASS " + this.password);
//		command("QUOTE OPTS UTF8 ON"); // 设置编码：无效
	}
	
	/**
	 * 切换被动模式
	 */
	private void changeMode() {
		command("PASV");
	}
	
	/**
	 * 发送命令
	 */
	private void command(String command) {
		try {
			send(command);
		} catch (NetException e) {
			LOGGER.error("Ftp命令发送异常", e);
		}
	}

}
