package com.acgist.snail.net.http;

import java.io.IOException;
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
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>HTTP客户端</p>
 * <p>使用JDK内置HTTP客户端</p>
 * <p>配置参考：https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class HTTPClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPClient.class);
	
	/**
	 * <p>HTTP状态码</p>
	 * <p>协议链接：https://www.ietf.org/rfc/rfc2616</p>
	 */
	public enum StatusCode {
		
		/** 成功 */
		OK(									200),
		/** 断点续传 */
		PARTIAL_CONTENT(					206),
		/** 请求文件不存在 */
		NOT_FOUND(							404),
		/** 无法满足请求范围 */
		REQUESTED_RANGE_NOT_SATISFIABLE(	416),
		/** 服务器错误 */
		INTERNAL_SERVER_ERROR(				500);
		
		/**
		 * <p>状态码</p>
		 */
		private final int code;
		
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
		 * @return {@code true}-相等；{@code false}-不等；
		 */
		public final boolean equalsCode(int code) {
			return this.code == code;
		}
		
	}
	
	/**
	 * <p>HTTP客户端信息（User-Agent）</p>
	 */
	private static final String USER_AGENT;
	/**
	 * <p>HTTP客户端线程池</p>
	 */
	private static final ExecutorService EXECUTOR = SystemThreadContext.newExecutor(2, 10, 100, 60L, SystemThreadContext.SNAIL_THREAD_HTTP_CLIENT);
	
	static {
		final StringBuilder userAgentBuilder = new StringBuilder();
		userAgentBuilder
			.append("Mozilla/5.0")
			.append(" ")
			.append("(")
			.append(SystemConfig.getNameEn())
			.append("/")
			.append(SystemConfig.getVersion())
			.append("; ")
			.append(SystemConfig.getSupport())
			.append(")");
		USER_AGENT = userAgentBuilder.toString();
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
	
	private HTTPClient(HttpClient client, Builder builder) {
		this.client = client;
		this.builder = builder;
	}
	
	/**
	 * <p>新建客户端</p>
	 * 
	 * @param url 请求地址
	 * 
	 * @return {@link HTTPClient}
	 * 
	 * @see #newInstance(String, int, int)
	 */
	public static final HTTPClient newInstance(String url) {
		return newInstance(url, SystemConfig.CONNECT_TIMEOUT, SystemConfig.RECEIVE_TIMEOUT);
	}
	
	/**
	 * <p>新建客户端</p>
	 * <p>HTTP请求版本{@link Version#HTTP_1_1}</p>
	 * 
	 * @param url 请求地址
	 * @param connectTimeout 超时时间（连接），单位：秒
	 * @param receiveTimeout 超时时间（响应），单位：秒
	 * 
	 * @return HTTP客户端
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
	 * @return 客户端
	 */
	public HTTPClient header(String name, String value) {
		this.builder.header(name, value);
		return this;
	}

	/**
	 * <p>执行GET请求</p>
	 * 
	 * @param <T> 响应体泛型
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
		return request(request, handler);
	}
	
	/**
	 * <p>执行POST请求</p>
	 * 
	 * @param <T> 响应体泛型
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
		return request(request, handler);
	}
	
	/**
	 * <p>执行POST表单请求</p>
	 * 
	 * @param <T> 响应体泛型
	 * @param data 请求表单数据
	 * @param handler 响应体处理器
	 * 
	 * @return 响应
	 * 
	 * @throws NetException 网络异常
	 */
	public <T> HttpResponse<T> postForm(Map<String, String> data, HttpResponse.BodyHandler<T> handler) throws NetException {
		this.builder.header("Content-type", "application/x-www-form-urlencoded;charset=" + SystemConfig.DEFAULT_CHARSET);
		final var request = this.builder
			.POST(newFormBodyPublisher(data))
			.build();
		return request(request, handler);
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
		final var response = request(request, BodyHandlers.discarding());
		HttpHeaders httpHeaders = null;
		if(HTTPClient.ok(response)) {
			httpHeaders = response.headers();
		}
		return HttpHeaderWrapper.newInstance(httpHeaders);
	}
	
	/**
	 * <p>执行请求</p>
	 * 
	 * @param <T> 响应体泛型
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
	 * <p>创建表单提交器</p>
	 * 
	 * @param data 表单数据
	 * 
	 * @return 表单提交器
	 */
	private BodyPublisher newFormBodyPublisher(Map<String, String> data) {
		if(CollectionUtils.isEmpty(data)) {
			return BodyPublishers.noBody();
		}
		final String body = data.entrySet().stream()
			.map(entry -> {
				return entry.getKey() + "=" + UrlUtils.encode(entry.getValue());
			})
			.collect(Collectors.joining("&"));
		return BodyPublishers.ofString(body);
	}
	
	/**
	 * <p>执行GET请求</p>
	 * 
	 * @param <T> 响应体泛型
	 * @param url 请求链接
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
	 * @param url 请求地址
	 * @param handler 响应体处理器
	 * @param connectTimeout 超时时间（连接），单位：秒
	 * @param receiveTimeout 超时时间（响应），单位：秒
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
	 * <p>是否成功：{@link StatusCode#OK}</p>
	 * 
	 * @param <T> 响应体泛型
	 * @param response 响应
	 * 
	 * @return {@code true}-成功；{@code false}-失败；
	 */
	public static final <T> boolean ok(HttpResponse<T> response) {
		return statusCode(response, StatusCode.OK);
	}
	
	/**
	 * <p>是否支持断点续传：{@link StatusCode#PARTIAL_CONTENT}</p>
	 * 
	 * @param <T> 响应体泛型
	 * @param response 响应
	 * 
	 * @return {@code true}-支持；{@code false}-不支持；
	 */
	public static final <T> boolean partialContent(HttpResponse<T> response) {
		return statusCode(response, StatusCode.PARTIAL_CONTENT);
	}

	/**
	 * <p>是否无法满足请求范围：{@link StatusCode#REQUESTED_RANGE_NOT_SATISFIABLE}</p>
	 * 
	 * @param <T> 响应体泛型
	 * @param response 响应
	 * 
	 * @return {@code true}-无法满足；{@code false}-可以满足；
	 */
	public static final <T> boolean requestedRangeNotSatisfiable(HttpResponse<T> response) {
		return statusCode(response, StatusCode.REQUESTED_RANGE_NOT_SATISFIABLE);
	}
	
	/**
	 * <p>是否服务器错误：{@link StatusCode#REQUESTED_RANGE_NOT_SATISFIABLE}</p>
	 * 
	 * @param <T> 响应体泛型
	 * @param response 响应
	 * 
	 * @return {@code true}-是；{@code false}-不是；
	 */
	public static final <T> boolean internalServerError(HttpResponse<T> response) {
		return statusCode(response, StatusCode.INTERNAL_SERVER_ERROR);
	}
	
	/**
	 * <p>判断响应状态码是否匹配</p>
	 * 
	 * @param <T> 响应体泛型
	 * @param response 响应
	 * @param statusCode 状态码
	 * 
	 * @return {@code true}-匹配；{@code false}-不匹配；
	 */
	private static final <T> boolean statusCode(HttpResponse<T> response, StatusCode statusCode) {
		return response != null && statusCode.equalsCode(response.statusCode());
	}
	
	/**
	 * <p>新建原生HTTP客户端</p>
	 * <p>设置{@code sslContext}需要同时设置{@code sslParameters}才有效</p>
	 * 
	 * @param timeout 超时时间（连接），单位：秒
	 * 
	 * @return 原生HTTP客户端
	 */
	public static final HttpClient newClient(int timeout) {
		return HttpClient
			.newBuilder()
			.executor(EXECUTOR) // 线程池
			.version(Version.HTTP_1_1)
			.followRedirects(Redirect.NORMAL) // 重定向：正常
//			.followRedirects(Redirect.ALWAYS) // 重定向：全部
//			.proxy(ProxySelector.getDefault()) // 代理
//			.sslContext(newSSLContext()) // SSL上下文
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
	 * @param timeout 超时时间（响应），单位：秒
	 * 
	 * @return 请求Builder
	 */
	private static final Builder newBuilder(String url, int timeout) {
		return HttpRequest
			.newBuilder()
			.uri(URI.create(url))
			.version(Version.HTTP_1_1) // HTTP协议使用1.1版本：2.0版本没有普及
			.timeout(Duration.ofSeconds(timeout))
			.header("User-Agent", USER_AGENT);
	}
	
	/**
	 * <p>新建{@code SSLParameters}</p>
	 * 
	 * @return {@code SSLParameters}
	 */
	private static final SSLParameters newSSLParameters() {
		final var sslParameters = new SSLParameters();
		// SSL加密套件：RSA和ECDSA签名根据证书类型选择（ECDH不推荐使用）
//		sslParameters.setCipherSuites(new String[] {
//			"TLS_AES_128_GCM_SHA256",
//			"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
//			"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
//			"TLS_RSA_WITH_AES_128_CBC_SHA256",
//			"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
//			"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256"
//		});
		// 不使用TLSv1.3：CPU占用过高
		sslParameters.setProtocols(new String[] {"TLSv1.1", "TLSv1.2"});
		// HTTP协议配置：newBuilder
//		sslParameters.setApplicationProtocols(new String[] {"h2", "http/1.1"});
		return sslParameters;
	}
	
	/**
	 * <p>新建{@code SSLContext}</p>
	 * 
	 * @return {@code SSLContext}
	 */
	public static final SSLContext newSSLContext() {
		SSLContext sslContext = null;
		try {
			// SSL协议：SSL、SSLv2、SSLv3、TLS、TLSv1、TLSv1.1、TLSv1.2、TLSv1.3
			sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, TRUST_ALL_CERT_MANAGER, new SecureRandom());
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
	 * <p>信任所有证书</p>
	 */
	private static final TrustManager[] TRUST_ALL_CERT_MANAGER = new TrustManager[] {
		new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		}
	};

}
