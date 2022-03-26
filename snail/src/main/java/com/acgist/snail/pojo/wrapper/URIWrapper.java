package com.acgist.snail.pojo.wrapper;

import java.net.URI;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>URI包装器</p>
 * 
 * <p>URI里面数据已经缓存不需要自己在做缓存，含有RAW方法没有解码，不含RAW方法已经解码。</p>
 * 
 * @author acgist
 */
public final class URIWrapper {

	/**
	 * <p>链接地址</p>
	 */
	private String uri;
	/**
	 * <p>URI</p>
	 */
	private URI nativeUri;
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
	 * <p>新建URI包装器</p>
	 * 
	 * @param uri 链接地址
	 * 
	 * @return {@link URIWrapper}
	 */
	public static final URIWrapper newInstance(String uri) {
		return new URIWrapper(uri);
	}
	
	/**
	 * <p>新建URI包装器</p>
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
	 * <p>新建URI包装器</p>
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
		// 不能解码：空格新建URI抛出异常
		this.nativeUri = URI.create(this.uri);
		// 解析端口
		final int decodePort = this.nativeUri.getPort();
		this.port = decodePort == -1 ? this.defaultPort : decodePort;
		// 解析用户信息
		final String userInfo = this.nativeUri.getUserInfo();
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
		return this;
	}
	
	/**
	 * <p>获取协议</p>
	 * 
	 * @return 协议
	 */
	public String scheme() {
		return this.nativeUri.getScheme();
	}
	
	/**
	 * <p>获取地址和端口</p>
	 * 
	 * @return 地址和端口
	 */
	public String authority() {
		return this.nativeUri.getAuthority();
	}
	
	/**
	 * <p>获取地址</p>
	 * 
	 * @return 地址
	 */
	public String host() {
		return this.nativeUri.getHost();
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
		return this.nativeUri.getPath();
	}
	
	/**
	 * <p>获取参数</p>
	 * 
	 * @return 参数
	 */
	public String query() {
		return this.nativeUri.getQuery();
	}
	
	/**
	 * <p>获取标识</p>
	 * 
	 * @return 标识
	 */
	public String fragment() {
		return this.nativeUri.getFragment();
	}
	
	/**
	 * <p>获取特殊部分</p>
	 * 
	 * @return 特殊部分
	 */
	public String schemeSpecificPart() {
		return this.nativeUri.getSchemeSpecificPart();
	}
	
	/**
	 * <p>解析获取参数</p>
	 * 
	 * @return 参数
	 */
	public String[] querys() {
		if(this.query() != null) {
			return this.query().split(SymbolConfig.Symbol.AND.toString());
		} else {
			final int queryIndex = this.schemeSpecificPart().indexOf(SymbolConfig.Symbol.QUESTION.toChar()) + 1;
			return this.schemeSpecificPart().substring(queryIndex).split(SymbolConfig.Symbol.AND.toString());
		}
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.uri);
	}
	
}