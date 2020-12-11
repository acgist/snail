package com.acgist.snail.pojo.wrapper;

import java.net.URI;

import com.acgist.snail.utils.StringUtils;

/**
 * <p>URI包装器</p>
 * 
 * @author acgist
 * 
 * TODO：协议、Authority
 */
public final class URIWrapper {

	/**
	 * <p>链接地址</p>
	 */
	private String uri;
	/**
	 * <p>默认端口</p>
	 */
	private int defaultPort;
	/**
	 * <p>默认用户</p>
	 */
	private String defaultUser;
	/**
	 * <p>默认密码</p>
	 */
	private String defaultPassword;
	/**
	 * <p>地址</p>
	 */
	private String host;
	/**
	 * <p>端口</p>
	 */
	private int port;
	/**
	 * <p>账号</p>
	 */
	private String user;
	/**
	 * <p>密码</p>
	 */
	private String password;
	/**
	 * <p>路径</p>
	 */
	private String path;
	/**
	 * <p>参数</p>
	 */
	private String query;
	
	/**
	 * @param uri 链接地址
	 */
	private URIWrapper(String uri) {
		this.uri = uri;
	}
	
	/**
	 * @param uri 链接地址
	 * @param defaultPort 默认端口
	 */
	private URIWrapper(String uri, int defaultPort) {
		this.uri = uri;
		this.defaultPort = defaultPort;
	}
	
	/**
	 * @param uri 链接地址
	 * @param defaultPort 默认端口
	 * @param defaultUser 默认用户
	 * @param defaultPassword 默认密码
	 */
	private URIWrapper(String uri, int defaultPort, String defaultUser, String defaultPassword) {
		this.uri = uri;
		this.defaultPort = defaultPort;
		this.defaultUser = defaultUser;
		this.defaultPassword = defaultPassword;
	}
	
	/**
	 * <p>创建URI包装器</p>
	 * 
	 * @param uri 链接地址
	 * 
	 * @return URIWrapper
	 */
	public static final URIWrapper newInstance(String uri) {
		return new URIWrapper(uri);
	}
	
	/**
	 * <p>创建URI包装器</p>
	 * 
	 * @param uri 链接地址
	 * @param defaultPort 默认端口
	 * 
	 * @return URIWrapper
	 */
	public static final URIWrapper newInstance(String uri, int defaultPort) {
		return new URIWrapper(uri, defaultPort);
	}
	
	/**
	 * <p>创建URI包装器</p>
	 * 
	 * @param uri 链接地址
	 * @param defaultPort 默认端口
	 * @param defaultUser 默认用户
	 * @param defaultPassword 默认密码
	 * 
	 * @return URIWrapper
	 */
	public static final URIWrapper newInstance(String uri, int defaultPort, String defaultUser, String defaultPassword) {
		return new URIWrapper(uri, defaultPort, defaultUser, defaultPassword);
	}
	
	/**
	 * <p>解析链接地址</p>
	 * 
	 * @return URIWrapper
	 */
	public URIWrapper decode() {
		final URI uri = URI.create(this.uri);
		// 解析用户信息
		final String userInfo = uri.getUserInfo();
		if(StringUtils.isEmpty(userInfo)) {
			this.user = this.defaultUser;
			this.password = this.defaultPassword;
		} else {
			final String[] userInfos = userInfo.split(":");
			if(userInfos.length == 1) {
				this.user = userInfos[0];
				this.password = this.defaultPassword;
			} else if(userInfos.length == 2) {
				this.user = userInfos[0];
				this.password = userInfos[1];
			} else {
				this.user = this.defaultUser;
				this.password = this.defaultPassword;
			}
		}
		// 解析地址
		this.host = uri.getHost();
		// 解析端口
		final int port = uri.getPort();
		if(port == -1) {
			this.port = this.defaultPort;
		} else {
			this.port = port;
		}
		// 解析路径
		this.path = uri.getPath();
		// 解析参数
		this.query = uri.getQuery();
		return this;
	}
	
	/**
	 * <p>获取地址</p>
	 * 
	 * @return 地址
	 */
	public String host() {
		return this.host;
	}
	
	/**
	 * <p>获取端口</p>
	 * 
	 * @return 端口
	 */
	public int port() {
		return this.port;
	}
	
	/**
	 * <p>获取用户</p>
	 * 
	 * @return 用户
	 */
	public String user() {
		return this.user;
	}
	
	/**
	 * <p>获取密码</p>
	 * 
	 * @return 密码
	 */
	public String password() {
		return this.password;
	}
	
	/**
	 * <p>获取路径</p>
	 * 
	 * @return 路径
	 */
	public String path() {
		return this.path;
	}
	
	/**
	 * <p>获取参数</p>
	 * 
	 * @return 参数
	 */
	public String query() {
		return this.query;
	}
	
}