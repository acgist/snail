package com.acgist.snail.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>HTTP客户端</p>
 * <p>配置参考：https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html</p>
 * <p>推荐直接使用HTTP协议下载：HTTPS下载CPU占用较高</p>
 * 
 * @author acgist
 */
public final class HTTPClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPClient.class);
	
	/**
	 * <p>HTTP状态码</p>
	 * <p>协议链接：https://www.ietf.org/rfc/rfc2616</p>
	 * 
	 * @author acgist
	 */
	public enum StatusCode {
		
		/**
		 * <p>成功</p>
		 */
		OK(200),
		/**
		 * <p>断点续传</p>
		 */
		PARTIAL_CONTENT(206),
		/**
		 * <p>永久重定向</p>
		 */
		MOVED_PERMANENTLY(301),
		/**
		 * <p>临时重定向</p>
		 * 
		 * @see #SEE_OTHER
		 * @see #TEMPORARY_REDIRECT
		 */
		FOUND(302),
		/**
		 * <p>临时重定向</p>
		 * <p>请求已被处理：POST不能获取参数</p>
		 */
		SEE_OTHER(303),
		/**
		 * <p>临时重定向</p>
		 * <p>请求没有处理：POST可以获取参数</p>
		 */
		TEMPORARY_REDIRECT(307),
		/**
		 * <p>请求文件不存在</p>
		 */
		NOT_FOUND(404),
		/**
		 * <p>无法满足请求范围</p>
		 */
		REQUESTED_RANGE_NOT_SATISFIABLE(416),
		/**
		 * <p>服务器错误</p>
		 */
		INTERNAL_SERVER_ERROR(500);
		
		/**
		 * <p>状态码</p>
		 */
		private final int code;
		
		/**
		 * @param code 状态码
		 */
		private StatusCode(int code) {
			this.code = code;
		}
		
		/**
		 * <p>获取状态码</p>
		 * 
		 * @return 状态码
		 */
		public final int code() {
			return this.code;
		}
		
		/**
		 * <p>判断状态码是否相等</p>
		 * 
		 * @param code 状态码
		 * 
		 * @return 是否相等
		 */
		public final boolean equalsCode(int code) {
			return this.code == code;
		}
		
		/**
		 * <p>判断响应状态码是否匹配</p>
		 * 
		 * @param <T> 响应体泛型
		 * 
		 * @param response 响应
		 * 
		 * @return 是否匹配
		 */
		public final <T> boolean verifyCode(HttpResponse<T> response) {
			return response != null && this.equalsCode(response.statusCode());
		}
		
	}

	/**
	 * <p>HTTP协议版本</p>
	 * <p>默认不用HTTP2（没有普及）</p>
	 */
	private static final Version VERSION;
	/**
	 * <p>HTTP客户端信息（User-Agent）</p>
	 */
	private static final String USER_AGENT;
	/**
	 * <p>HTTP客户端线程池</p>
	 */
	private static final Executor EXECUTOR;
	/**
	 * <p>是否支持HTTP2</p>
	 */
	private static final boolean ENABLE_H2 = false;
	/**
	 * <p>是否指定加密套件</p>
	 */
	private static final boolean CONFIRM_CIPHER = false;
	
	static {
		VERSION = Version.HTTP_1_1;
		final StringBuilder userAgentBuilder = new StringBuilder();
		userAgentBuilder
			.append("Mozilla/5.0")
			.append(" ")
			.append("(compatible; ")
			.append(SystemConfig.getNameEn())
			.append("/")
			.append(SystemConfig.getVersion())
			.append("; +")
			.append(SystemConfig.getSupport())
			.append(")");
		USER_AGENT = userAgentBuilder.toString();
		// 使用缓存线程池
		EXECUTOR = SystemThreadContext.newCacheExecutor(2, 60L, SystemThreadContext.SNAIL_THREAD_HTTP_CLIENT);
		LOGGER.debug("HTTP客户端信息（User-Agent）：{}", USER_AGENT);
	}
	
	/**
	 * <p>原生HTTP客户端</p>
	 */
	private final HttpClient client;
	/**
	 * <p>请求Builder</p>
	 */
	private final Builder builder;
	
	/**
	 * @param client 原生HTTP客户端
	 * @param builder 请求Builder
	 */
	private HTTPClient(HttpClient client, Builder builder) {
		this.client = client;
		this.builder = builder;
	}
	
	/**
	 * <p>新建HTTPClient</p>
	 * 
	 * @param url 请求地址
	 * 
	 * @return HTTPClient
	 * 
	 * @see #newInstance(String, int, int)
	 */
	public static final HTTPClient newInstance(String url) {
		return newInstance(url, SystemConfig.CONNECT_TIMEOUT, SystemConfig.RECEIVE_TIMEOUT);
	}
	
	/**
	 * <p>新建HTTPClient</p>
	 * <p>HTTP请求协议版本：{@link Version#HTTP_1_1}</p>
	 * 
	 * @param url 请求地址
	 * @param connectTimeout 连接超时时间（单位：秒）
	 * @param receiveTimeout 响应超时时间（单位：秒）
	 * 
	 * @return HTTPClient
	 */
	public static final HTTPClient newInstance(String url, int connectTimeout, int receiveTimeout) {
		final HttpClient client = newClient(connectTimeout);
		final Builder builder = newBuilder(url, receiveTimeout);
		return new HTTPClient(client, builder);
	}
	
	/**
	 * <p>获取原生HTTP客户端</p>
	 * 
	 * @return 原生HTTP客户端
	 */
	public HttpClient client() {
		return this.client;
	}
	
	/**
	 * <p>设置请求头</p>
	 * 
	 * @param name 名称
	 * @param value 值
	 * 
	 * @return HTTPClient
	 */
	public HTTPClient header(String name, String value) {
		this.builder.header(name, value);
		return this;
	}

	/**
	 * <p>设置请求范围</p>
	 * 
	 * @param pos 开始位置
	 * 
	 * @return HTTPClient
	 */
	public HTTPClient range(long pos) {
		return this.header(HttpHeaderWrapper.HEADER_RANGE, "bytes=" + pos + "-");
	}
	
	/**
	 * <p>执行GET请求</p>
	 * 
	 * @param <T> 响应体泛型
	 * 
	 * @param handler 响应体处理器
	 * 
	 * @return 响应
	 * 
	 * @throws NetException 网络异常
	 */
	public <T> HttpResponse<T> get(HttpResponse.BodyHandler<T> handler) throws NetException {
		final var request = this.builder
			.GET()
			.build();
		return this.request(request, handler);
	}
	
	/**
	 * <p>执行POST请求</p>
	 * 
	 * @param <T> 响应体泛型
	 * 
	 * @param data 请求数据
	 * @param handler 响应体处理器
	 * 
	 * @return 响应
	 * 
	 * @throws NetException 网络异常
	 */
	public <T> HttpResponse<T> post(String data, HttpResponse.BodyHandler<T> handler) throws NetException {
		if(StringUtils.isEmpty(data)) {
			this.builder.POST(BodyPublishers.noBody());
		} else {
			this.builder.POST(BodyPublishers.ofString(data));
		}
		final var request = this.builder
			.build();
		return this.request(request, handler);
	}
	
	/**
	 * <p>执行表单POST请求</p>
	 * 
	 * @param <T> 响应体泛型
	 * 
	 * @param data 表单请求数据
	 * @param handler 响应体处理器
	 * 
	 * @return 响应
	 * 
	 * @throws NetException 网络异常
	 */
	public <T> HttpResponse<T> postForm(Map<String, String> data, HttpResponse.BodyHandler<T> handler) throws NetException {
		// 设置表单请求
		this.builder.header(HttpHeaderWrapper.HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=" + SystemConfig.DEFAULT_CHARSET);
		final var request = this.builder
			.POST(buildFormBodyPublisher(data))
			.build();
		return this.request(request, handler);
	}
	
	/**
	 * <p>执行HEAD请求</p>
	 * 
	 * @return 响应头
	 * 
	 * @throws NetException 网络异常
	 */
	public HttpHeaderWrapper head() throws NetException {
		final var request = this.builder
			.method("HEAD", BodyPublishers.noBody())
			.build();
		final var response = this.request(request, BodyHandlers.discarding());
		HttpHeaders httpHeaders = null;
		if(response != null) {
			httpHeaders = response.headers();
		}
		return HttpHeaderWrapper.newInstance(httpHeaders);
	}
	
	/**
	 * <p>执行请求</p>
	 * 
	 * @param <T> 响应体泛型
	 * 
	 * @param request 请求
	 * @param handler 响应体处理器
	 * 
	 * @return 响应
	 * 
	 * @throws NetException 网络异常
	 */
	public <T> HttpResponse<T> request(HttpRequest request, HttpResponse.BodyHandler<T> handler) throws NetException {
		if(this.client == null || request == null) {
			return null;
		}
		try {
			return this.client.send(request, handler);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new NetException("HTTP执行请求失败", e);
		} catch (IOException e) {
			throw new NetException("HTTP执行请求失败", e);
		}
	}
	
	/**
	 * <p>执行异步请求</p>
	 * 
	 * @param <T> 响应体泛型
	 * 
	 * @param request 请求
	 * @param handler 响应体处理器
	 * 
	 * @return 响应异步线程
	 */
	public <T> CompletableFuture<HttpResponse<T>> requestAsync(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
		if(this.client == null || request == null) {
			return null;
		}
		return this.client.sendAsync(request, handler);
	}

	/**
	 * <p>创建表单数据</p>
	 * 
	 * @param data 表单数据
	 * 
	 * @return 表单数据
	 */
	private BodyPublisher buildFormBodyPublisher(Map<String, String> data) {
		if(MapUtils.isEmpty(data)) {
			return BodyPublishers.noBody();
		}
		final String body = data.entrySet().stream()
			.map(entry -> entry.getKey() + "=" + UrlUtils.encode(entry.getValue()))
			.collect(Collectors.joining("&"));
		return BodyPublishers.ofString(body);
	}
	
	/**
	 * <p>执行GET请求</p>
	 * 
	 * @param <T> 响应体泛型
	 * 
	 * @param url 请求地址
	 * @param handler 响应体处理器
	 * 
	 * @return 响应
	 * 
	 * @throws NetException 网络异常
	 * 
	 * @see #get(String, java.net.http.HttpResponse.BodyHandler, int, int)
	 */
	public static final <T> HttpResponse<T> get(String url, HttpResponse.BodyHandler<T> handler) throws NetException {
		return get(url, handler, SystemConfig.CONNECT_TIMEOUT, SystemConfig.RECEIVE_TIMEOUT);
	}
	
	/**
	 * <p>执行GET请求</p>
	 * 
	 * @param <T> 响应体泛型
	 * 
	 * @param url 请求地址
	 * @param handler 响应体处理器
	 * @param connectTimeout 连接超时时间（单位：秒）
	 * @param receiveTimeout 响应超时时间（单位：秒）
	 * 
	 * @return 响应
	 * 
	 * @throws NetException 网络异常
	 */
	public static final <T> HttpResponse<T> get(String url, HttpResponse.BodyHandler<T> handler, int connectTimeout, int receiveTimeout) throws NetException {
		final HTTPClient client = newInstance(url, connectTimeout, receiveTimeout);
		return client.get(handler);
	}
	
	/**
	 * <p>新建原生HTTP客户端</p>
	 * <p>设置{@link SSLContext}需要同时设置{@link SSLParameters}</p>
	 * 
	 * @param timeout 连接超时时间（单位：秒）
	 * 
	 * @return 原生HTTP客户端
	 */
	public static final HttpClient newClient(int timeout) {
		return HttpClient
			.newBuilder()
			.executor(EXECUTOR) // 线程池
			.version(VERSION) // 协议版本
			.followRedirects(Redirect.NORMAL) // 重定向：正常
//			.followRedirects(Redirect.ALWAYS) // 重定向：全部
//			.proxy(ProxySelector.getDefault()) // 代理
			.sslContext(newSSLContext()) // SSL上下文
			.sslParameters(newSSLParameters()) // SSL参数
//			.authenticator(Authenticator.getDefault()) // 认证
//			.cookieHandler(CookieHandler.getDefault()) // Cookie
			.connectTimeout(Duration.ofSeconds(timeout)) // 超时
			.build();
	}

	/**
	 * <p>新建请求Builder</p>
	 * 
	 * @param url 请求地址
	 * @param timeout 响应超时时间（单位：秒）
	 * 
	 * @return 请求Builder
	 */
	public static final Builder newBuilder(String url, int timeout) {
		return HttpRequest
			.newBuilder()
			.uri(URI.create(url))
			.version(VERSION)
			.timeout(Duration.ofSeconds(timeout))
			.header("User-Agent", USER_AGENT);
	}
	
	/**
	 * <p>判断是否可以下载</p>
	 * 
	 * @param response 响应
	 * 
	 * @return 是否可以下载
	 */
	public static final boolean downloadable(HttpResponse<InputStream> response) {
		return
			HTTPClient.StatusCode.OK.verifyCode(response) ||
			HTTPClient.StatusCode.PARTIAL_CONTENT.verifyCode(response);
	}
	
	/**
	 * <p>新建SSLContext</p>
	 * 
	 * @return SSLContext
	 */
	private static final SSLContext newSSLContext() {
		SSLContext sslContext = null;
		try {
			// SSL协议：SSL、SSLv2、SSLv3、TLS、TLSv1、TLSv1.1、TLSv1.2、TLSv1.3
			sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, ALLOWED_ALL_TRUST_MANAGER, SecureRandom.getInstanceStrong());
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			LOGGER.error("新建SSLContext异常", e);
			try {
				sslContext = SSLContext.getDefault();
			} catch (NoSuchAlgorithmException ex) {
				LOGGER.error("新建默认SSLContext异常", ex);
			}
		}
		return sslContext;
	}
	
	/**
	 * <p>新建SSLParameters</p>
	 * 
	 * @return SSLParameters
	 */
	private static final SSLParameters newSSLParameters() {
		final var sslParameters = new SSLParameters();
		// 配置HTTP协议优先级
		if(ENABLE_H2) {
			sslParameters.setApplicationProtocols(new String[] {"http/1.1", "h2"});
		}
		// 指定加密套件：RSA和ECDSA签名根据证书类型选择（ECDH不推荐使用）
		if(CONFIRM_CIPHER) {
			sslParameters.setCipherSuites(new String[] {
				"TLS_AES_128_GCM_SHA256",
				"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
				"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
				"TLS_RSA_WITH_AES_128_CBC_SHA256",
				"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
				"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256"
			});
		}
		// 指定加密版本
		sslParameters.setProtocols(new String[] {"TLSv1.1", "TLSv1.2", "TLSv1.3"});
		return sslParameters;
	}
	
	/**
	 * <p>信任所有证书</p>
	 */
	private static final TrustManager[] ALLOWED_ALL_TRUST_MANAGER = new TrustManager[] {
		new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				if(ArrayUtils.isEmpty(chain)) {
					throw new CertificateException("证书加载失败");
				}
			}
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				if(ArrayUtils.isEmpty(chain)) {
					throw new CertificateException("证书加载失败");
				}
			}
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		}
	};

}
