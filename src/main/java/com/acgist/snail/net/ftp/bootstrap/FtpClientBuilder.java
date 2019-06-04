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
public class FtpClientBuilder {

	private static final int FTP_DEFAULT_PORT = 21;
	
	private final String url; // 下载链接
	
	private String host; // 服务器地址
	private int port; // 服务器端口
	private String filePath; // 文件路径
	private String user; // 用户账号
	private String password; // 用户密码
	
	private FtpClientBuilder(String url) {
		this.url = url;
	}

	public static final FtpClientBuilder newInstance(String url) {
		return new FtpClientBuilder(url);
	}
	
	/**
	 * 创建FtpClient
	 */
	public FtpClient build() {
		this.decodeUrl();
		return FtpClient.newInstance(
			this.host,
			this.port,
			this.user,
			this.password,
			this.filePath
		);
	}
	
	/**
	 * 解析URL，获取地址、端口、用户、文件等信息。
	 */
	private void decodeUrl() {
		final URI uri = URI.create(this.url);
		final String userInfo = uri.getUserInfo();
		decodeUserInfo(userInfo);
		this.filePath = uri.getPath();
		this.host = uri.getHost();
		int port = uri.getPort();
		if(port == -1) {
			port = FTP_DEFAULT_PORT;
		}
		this.port = port;
	}

	/**
	 * 解析用户信息
	 */
	private void decodeUserInfo(String userInfo) {
		if(StringUtils.isEmpty(userInfo)) {
			this.user = FtpClient.ANONYMOUS;
			this.password = FtpClient.ANONYMOUS;
		} else {
			final String[] userInfos = userInfo.split(":");
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
