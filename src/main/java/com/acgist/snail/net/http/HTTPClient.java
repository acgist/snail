package com.acgist.snail.net.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * HTTP客户端
 * 
 * @author acgist
 * @since 1.0.0
 */
public class HTTPClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPClient.class);
	
	public static final int TIMEOUT = 5;
	
	public static final int HTTP_OK = 200; // OK
	public static final int HTTP_PARTIAL_CONTENT = 206; // 端点续传
	public static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE= 416; // 无法满足请求范围
	public static final int HTTP_INTERNAL_SERVER_ERROR = 500; // 服务器错误
	
	/**
	 * 浏览器信息
	 */
	private static final String USER_AGENT;
	
	/**
	 * HTTP线程池
	 */
	private static final ExecutorService EXECUTOR = SystemThreadContext.newExecutor(2, 10, 100, 60L, SystemThreadContext.SNAIL_THREAD_HTTP);
	
	static {
		final StringBuilder userAgentBuilder = new StringBuilder(); // 客户端信息
		userAgentBuilder
			.append("Mozilla/5.0")
			.append(" ")
			.append("(compatible; ")
			.append(SystemConfig.getNameEn())
			.append("/")
			.append(SystemConfig.getVersion())
			.append("; ")
			.append(SystemConfig.getSupport())
			.append(")");
		USER_AGENT = userAgentBuilder.toString();
		LOGGER.debug("User-Agent：{}", USER_AGENT);
	}
	
	private final HttpClient client;
	private final Builder builder; // request builder
	
	private HTTPClient(HttpClient client, Builder builder) {
		this.client = client;
		this.builder = builder;
	}
	
	/**
	 * 新建客户端
	 * 
	 * @param url 请求地址
	 * 
	 * @return HTTP客户端
	 */
	public static final HTTPClient newInstance(String url) {
		return newInstance(url, TIMEOUT);
	}
	
	/**
	 * <p>新建客户端</p>
	 * <p>HTTP请求版本{@link Version#HTTP_1_1}</p>
	 * 
	 * @param url 请求地址
	 * @param timeout 超时时间，单位：秒
	 * 
	 * @return HTTP客户端
	 */
	public static final HTTPClient newInstance(String url, int timeout) {
		final HttpClient client = newClient(timeout);
		final Builder builder = newBuilder(url, timeout);
		return new HTTPClient(client, builder);
	}
	
	/**
	 * 获取HttpClient
	 */
	public HttpClient client() {
		return this.client;
	}
	
	/**
	 * 设置请求头
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
	 * GET请求
	 */
	public <T> HttpResponse<T> get(HttpResponse.BodyHandler<T> handler) throws NetException {
		final var request = this.builder
			.GET()
			.build();
		return request(request, handler);
	}
	
	/**
	 * POST请求
	 */
	public <T> HttpResponse<T> post(String data, HttpResponse.BodyHandler<T> handler) throws NetException {
		if(data == null || data.isEmpty()) {
			this.builder.POST(BodyPublishers.noBody());
		} else {
			this.builder.POST(BodyPublishers.ofString(data));
		}
		final var request = this.builder
			.build();
		return request(request, handler);
	}
	
	/**
	 * POST表单请求
	 */
	public <T> HttpResponse<T> postForm(Map<String, String> data, HttpResponse.BodyHandler<T> handler) throws NetException {
		this.builder.header("Content-type", "application/x-www-form-urlencoded;charset=" + SystemConfig.DEFAULT_CHARSET);
		if(data == null || data.isEmpty()) {
			this.builder.POST(BodyPublishers.noBody());
		} else {
			this.builder.POST(newFormBodyPublisher(data));
		}
		final var request = this.builder
			.build();
		return request(request, handler);
	}
	
	/**
	 * HEAD请求
	 */
	public HttpHeaderWrapper head() throws NetException {
		final var request = this.builder
			.method("HEAD", BodyPublishers.noBody())
			.build();
		final var response = request(request, BodyHandlers.ofString());
		if(HTTPClient.ok(response)) {
			return HttpHeaderWrapper.newInstance(response.headers());
		}
		return HttpHeaderWrapper.newInstance(null);
	}
	
	/**
	 * 执行请求
	 */
	private <T> HttpResponse<T> request(HttpRequest request, HttpResponse.BodyHandler<T> handler) throws NetException {
		if(this.client == null || request == null) {
			return null;
		}
		try {
			return this.client.send(request, handler);
		} catch (Exception e) {
			throw new NetException(e);
		}
	}
	
//	/**
//	 * 执行异步请求
//	 */
//	private <T> CompletableFuture<HttpResponse<T>> requestAsync(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
//		if(this.client == null || request == null) {
//			return null;
//		}
//		return this.client.sendAsync(request, handler);
//	}

	/**
	 * 表单数据
	 */
	private BodyPublisher newFormBodyPublisher(Map<String, String> data) {
		if(CollectionUtils.isEmpty(data)) {
			return BodyPublishers.noBody();
		}
		final String body = data.entrySet()
			.stream()
			.map(entry -> {
				return entry.getKey() + "=" + UrlUtils.encode(entry.getValue());
			})
			.collect(Collectors.joining("&"));
		return BodyPublishers.ofString(body);
	}
	
	public static final <T> HttpResponse<T> get(String requestUrl, HttpResponse.BodyHandler<T> handler) throws NetException {
		return get(requestUrl, handler, TIMEOUT);
	}
	
	/**
	 * 执行GET请求
	 * 
	 * @param requestUrl 请求地址
	 * @param handler 响应处理器
	 * @param timeout 超时时间
	 * 
	 * @return 响应
	 */
	public static final <T> HttpResponse<T> get(String requestUrl, HttpResponse.BodyHandler<T> handler, int timeout) throws NetException {
		final HTTPClient client = newInstance(requestUrl, timeout);
		return client.get(handler);
	}
	
	/**
	 * <p>成功：{@link #HTTP_OK}</p>
	 */
	public static final <T> boolean ok(HttpResponse<T> response) {
		return response != null && response.statusCode() == HTTP_OK;
	}
	
	/**
	 * <p>端点续传：{@link #HTTP_PARTIAL_CONTENT}</p>
	 */
	public static final <T> boolean partialContent(HttpResponse<T> response) {
		return response != null && response.statusCode() == HTTP_PARTIAL_CONTENT;
	}

	/**
	 * 无法满足请求范围：{@link #HTTP_REQUESTED_RANGE_NOT_SATISFIABLE}
	 */
	public static final <T> boolean requestedRangeNotSatisfiable(HttpResponse<T> response) {
		return response != null && response.statusCode() == HTTP_REQUESTED_RANGE_NOT_SATISFIABLE;
	}
	
	/**
	 * 服务器错误：{@link #HTTP_INTERNAL_SERVER_ERROR}
	 */
	public static final <T> boolean internalServerError(HttpResponse<T> response) {
		return response != null && response.statusCode() == HTTP_INTERNAL_SERVER_ERROR;
	}
	
	/**
	 * 新建原生HTTP客户端
	 */
	public static final HttpClient newClient(int timeout) {
		return HttpClient
			.newBuilder()
			.executor(EXECUTOR) // 线程池
			.followRedirects(Redirect.NORMAL) // 重定向：正常
//			.followRedirects(Redirect.ALWAYS) // 重定向：全部
//			.proxy(ProxySelector.getDefault()) // 代理
//			.sslContext(newSSLContext()) // SSL
//			.sslContext(SSLContext.getDefault()) // SSL
//			.authenticator(Authenticator.getDefault()) // 认证
//			.cookieHandler(CookieHandler.getDefault()) // Cookie
			.connectTimeout(Duration.ofSeconds(timeout)) // 超时
			.build();
	}
	
//	/**
//	 * 信任所有证书
//	 */
//	private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[] {
//		new X509TrustManager() {
//			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//				return null;
//			}
//			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
//			}
//			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
//			}
//		}
//	};
//	
//	/**
//	 * 新建SSLContext
//	 */
//	private static final SSLContext newSSLContext() {
//		SSLContext sslContext = null;
//		try {
//			sslContext = SSLContext.getInstance("TLS");
//			sslContext = SSLContext.getInstance("TLSv1.2");
//			sslContext.init(null, TRUST_ALL_CERTS, new SecureRandom());
//		} catch (Exception e) {
//			LOGGER.error("新建SSLContext异常", e);
//			try {
//				sslContext = SSLContext.getDefault();
//			} catch (Exception ex) {
//				LOGGER.error("新建默认SSLContext异常", ex);
//			}
//		}
//		return sslContext;
//	}
	
	/**
	 * 新建请求Builder
	 */
	private static final Builder newBuilder(String url, int timeout) {
		return HttpRequest
			.newBuilder()
			.uri(URI.create(url))
			.version(Version.HTTP_1_1) // 使用1.1版本协议，2.0版本还没有普及。
			.timeout(Duration.ofSeconds(timeout))
			.header("User-Agent", USER_AGENT);
	}

}
