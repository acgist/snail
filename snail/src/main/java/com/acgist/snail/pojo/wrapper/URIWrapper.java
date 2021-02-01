package com.acgist.snail.pojo.wrapper;

import java.net.URI;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>URI包装器</p>
 * 
 * @author acgist
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
	 * <p>协议（Scheme）</p>
	 */
	private String scheme;
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
	 * <p>标识</p>
	 */
	private String fragment;
	
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
	 * @return {@link URIWrapper}
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
	 * @return {@link URIWrapper}
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
	 * @return {@link URIWrapper}
	 */
	public static final URIWrapper newInstance(String uri, int defaultPort, String defaultUser, String defaultPassword) {
		return new URIWrapper(uri, defaultPort, defaultUser, defaultPassword);
	}
	
	/**
	 * <p>解析链接地址</p>
	 * 
	 * @return {@link URIWrapper}
	 */
	public URIWrapper decode() {
		final URI decodeUri = URI.create(this.uri);
		// 解析协议
		this.scheme = decodeUri.getScheme();
		// 解析用户信息
		final String userInfo = decodeUri.getUserInfo();
		if(StringUtils.isEmpty(userInfo)) {
			this.user = this.defaultUser;
			this.password = this.defaultPassword;
		} else {
			final String[] userInfos = userInfo.split(SymbolConfig.Symbol.COLON.toString());
			if(userInfos.length == 1) {
				this.user = userInfos[0];
				this.password = this.defaultPassword;
			} else if(userInfos.length > 1) {
				this.user = userInfos[0];
				this.password = userInfos[1];
			} else {
				this.user = this.defaultUser;
				this.password = this.defaultPassword;
			}
		}
		// 解析地址
		this.host = decodeUri.getHost();
		// 解析端口
		final int decodePort = decodeUri.getPort();
		if(decodePort == -1) {
			this.port = this.defaultPort;
		} else {
			this.port = decodePort;
		}
		// 解析路径
		this.path = decodeUri.getPath();
		// 解析参数
		this.query = decodeUri.getQuery();
		// 解析标识
		this.fragment = decodeUri.getFragment();
		return this;
	}
	
	/**
	 * <p>获取协议</p>
	 * 
	 * @return 协议
	 */
	public String scheme() {
		return this.scheme;
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
	
	/**
	 * <p>获取标识</p>
	 * 
	 * @return 标识
	 */
	public String fragment() {
		return this.fragment;
	}
	
}