package com.acgist.snail.net.ftp;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

import com.acgist.snail.net.AbstractTcpClient;
import com.acgist.snail.net.message.impl.FtpMessageHandler;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * FTP
 */
public class FtpClient extends AbstractTcpClient<FtpMessageHandler> {

	public static final String ANONYMOUS = "anonymous"; // 匿名用户名
	
	private boolean ok = false; // 连接成功
	private String host; // 服务器地址
	private int port; // 服务器端口
	private String user; // FTP用户
	private String password; // FTP密码
	private String filePath; // 文件路径

	public FtpClient(String host, int port, String user, String password, String filePath) {
		super(FtpMessageHandler.SPLIT, new FtpMessageHandler());
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.filePath = filePath;
	}

	@Override
	public boolean connect() {
		this.ok = connect(host, port);
		if(ok) {
			this.login();
		}
		return ok;
	}
	
	/**
	 * 开始下载
	 */
	public InputStream download() {
		return this.download(null);
	}
	
	/**
	 * 开始下载
	 * @param downloadSize 已下载大小
	 */
	public InputStream download(Long downloadSize) {
		if(!ok) {
			return null;
		}
		this.changeMode();
		command("TYPE I");
		if(downloadSize != null && downloadSize != 0L) {
			command("REST " + downloadSize);
		}
		command("RETR " + this.filePath);
		return messageHandler.inputStream();
	}
	
	/**
	 * 获取大小
	 */
	public Long size() throws NetException {
		if(!ok) {
			return 0L;
		}
		this.changeMode();
		command("TYPE A");
		command("LIST " + this.filePath);
		InputStream inputStream = messageHandler.inputStream();
		String data = StringUtils.ofInputStream(inputStream);
		if(data == null) {
			throw new NetException(failMessage());
		}
		Optional<String> optional = Stream.of(data.split(" "))
			.map(String::trim)
			.filter(StringUtils::isNotEmpty)
			.skip(4).findFirst();
		if(optional.isPresent()) {
			return Long.valueOf(optional.get());
		}
		return 0L;
	}
	
	/**
	 * 关闭系统
	 */
	@Override
	public void close() {
		if(!ok) {
			return;
		}
		command("QUIT"); // 退出命令
		messageHandler.close(); // 关闭
		super.close();
	}
	
	/**
	 * 是否支持断点续传
	 */
	public boolean append() {
		return messageHandler.append();
	}
	
	/**
	 * 错误信息
	 */
	public String failMessage() {
		return messageHandler.failMessage();
	}
	
	/**
	 * 登陆服务器
	 */
	private void login() {
		command("USER " + this.user);
		command("PASS " + this.password);
	}
	
	/**
	 * 切换模式：<br>
	 * 切换被动模式<br>
	 * 切换到二进制输出
	 */
	private void changeMode() {
		command("PASV");
	}
	
	/**
	 * 发送命令
	 */
	private void command(String command) {
		send(command);
	}

}
