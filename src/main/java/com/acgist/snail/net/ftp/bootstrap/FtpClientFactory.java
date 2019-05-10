package com.acgist.snail.net.ftp.bootstrap;

import java.net.URI;

import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>FtpClient工厂</p>
 * <p>根据url创建FTP客户端。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FtpClientFactory {

	private static final int FTP_DEFAULT_PORT = 21;
	
	private String url; // 下载链接
	private String host;
	private int port;
	private String filePath;
	private String user;
	private String password;
	
	private FtpClientFactory() {
	}
	
	public static final FtpClient build(String url) {
		final FtpClientFactory builder = new FtpClientFactory();
		builder.url = url;
		builder.decodeUrl();
		return FtpClient.newInstance(
			builder.host,
			builder.port,
			builder.user,
			builder.password,
			builder.filePath
		);
	}
	
	/**
	 * 解码URL，设置地址、端口、用户、文件等信息。
	 */
	private void decodeUrl() {
		URI uri= URI.create(this.url);
		String userInfo = uri.getUserInfo();
		setUserInfo(userInfo);
		this.filePath = uri.getPath();
		this.host = uri.getHost();
		int port = uri.getPort();
		if(port == -1) {
			port = FTP_DEFAULT_PORT;
		}
		this.port = port;
	}

	/**
	 * 设置用户信息
	 */
	private void setUserInfo(String userInfo) {
		if(StringUtils.isEmpty(userInfo)) {
			this.user = FtpClient.ANONYMOUS;
			this.password = FtpClient.ANONYMOUS;
		} else {
			String[] userInfos = userInfo.split(":");
			if(userInfos.length == 1) {
				this.user = userInfos[0];
			} else if(userInfos.length == 2) {
				this.user = userInfos[0];
				this.password = userInfos[1];
			} else {
				this.user = FtpClient.ANONYMOUS;
				this.password = FtpClient.ANONYMOUS;
			}
		}
	}
	
}
