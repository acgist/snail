package com.acgist.snail.net.client.ftp;

import java.io.InputStream;

import com.acgist.snail.net.client.AbstractClient;
import com.acgist.snail.net.message.impl.FtpMessageHandler;

/**
 * FTP
 */
public class FtpClient extends AbstractClient<FtpMessageHandler> {

	public static final String ANONYMOUS = "anonymous"; // 匿名用户名
	
	private String host; // 服务器地址
	private int port; // 服务器端口
	private String user; // FTP用户
	private String password; // FTP密码
	private String filePath; // 文件路径
	
	public FtpClient() {
		super(FtpMessageHandler.SPLIT, new FtpMessageHandler());
		this.user = ANONYMOUS;
		this.password = ANONYMOUS;
	}
	
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
		boolean ok = connect(host, port);
		if(ok) {
			this.login();
			this.changeMode();
			this.download();
		}
		return ok;
	}
	
	/**
	 * 获取输入流，阻塞线程
	 */
	public InputStream inputStream() {
		return messageHandler.inputStream();
	}
	
	/**
	 * 错误信息
	 */
	public String failMessage() {
		return messageHandler.failMessage();
	}
	
	/**
	 * 关闭系统
	 */
	public void close() {
		command("QUIT");
		messageHandler.close();
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
	 * 开始下载
	 */
	private void download() {
		command("TYPE I");
		command("RETR " + this.filePath);
		command("REST 100000");
	}
	
	/**
	 * 发送命令
	 */
	private void command(String command) {
		send(command);
	}
	
}
