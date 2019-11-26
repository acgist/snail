package com.acgist.snail.net.ftp.bootstrap;

import java.net.URI;

import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>FtpClient Builder</p>
 * <p>使用FTP链接创建FTP客户端</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class FtpClientBuilder {

	/**
	 * FTP默认端口
	 */
	private static final int DEFAULT_PORT = 21;
	
	/**
	 * 下载链接
	 */
	private final String url;
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
	 * 解析URL：地址、端口、用户、文件等信息
	 */
	private void decodeUrl() {
		final URI uri = URI.create(this.url);
		final String userInfo = uri.getUserInfo();
		decodeUserInfo(userInfo);
		this.host = uri.getHost();
		int port = uri.getPort();
		if(port == -1) {
			port = DEFAULT_PORT;
		}
		this.port = port;
		this.filePath = uri.getPath();
	}

	/**
	 * 解析用户授权信息
	 */
	private void decodeUserInfo(String userInfo) {
		if(StringUtils.isEmpty(userInfo)) {
			this.user = SystemConfig.getFtpUser();
			this.password = SystemConfig.getFtpPassword();
		} else {
			final String[] userInfos = userInfo.split(":");
			if(userInfos.length == 1) {
				this.user = userInfos[0];
				this.password = SystemConfig.getFtpPassword();
			} else if(userInfos.length == 2) {
				this.user = userInfos[0];
				this.password = userInfos[1];
			} else {
				this.user = SystemConfig.getFtpUser();
				this.password = SystemConfig.getFtpPassword();
			}
		}
	}

}
