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
import java.util.concurrent.CompletableFuture;
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
	
	/**
	 * 新建客户端
	 */
	public static final HttpClient newClient() {
		return newClient(TIMEOUT);
	}
	
	/**
	 * 新建客户端
	 */
	public static final HttpClient newClient(int timeout) {
		return HttpClient
			.newBuilder()
			.executor(EXECUTOR)
			.followRedirects(Redirect.NORMAL)
//			.followRedirects(Redirect.ALWAYS)
			.connectTimeout(Duration.ofSeconds(timeout))
			.build();
	}
	
	/**
	 * 新建请求
	 */
	public static final Builder newRequest(String url) {
		return newRequest(url, TIMEOUT);
	}
	
	/**
	 * 新建请求，HTTP请求版本{@link Version#HTTP_1_1}。
	 */
	public static final Builder newRequest(String url, int timeout) {
		return HttpRequest
			.newBuilder()
			.uri(URI.create(url))
			.version(Version.HTTP_1_1) // 使用1.1版本协议，2.0版本还没有普及。
			.timeout(Duration.ofSeconds(timeout))
			.header("User-Agent", USER_AGENT);
	}
	
	/**
	 * 表单请求
	 */
	public static final Builder newFormRequest(String url) {
		return newRequest(url)
			.header("Content-type", "application/x-www-form-urlencoded;charset=" + SystemConfig.DEFAULT_CHARSET);
	}
	
	/**
	 * 表单数据
	 */
	public static final BodyPublisher newFormBodyPublisher(Map<String, String> data) {
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

	/**
	 * GET请求
	 */
	public static final <T> HttpResponse<T> get(String requestUrl, HttpResponse.BodyHandler<T> handler) throws NetException {
		return get(requestUrl, handler, TIMEOUT);
	}
	
	/**
	 * GET请求
	 */
	public static final <T> HttpResponse<T> get(String requestUrl, HttpResponse.BodyHandler<T> handler, int timeout) throws NetException {
		final var client = HTTPClient.newClient(timeout);
		final var request = HTTPClient.newRequest(requestUrl, timeout)
			.GET()
			.build();
		return HTTPClient.request(client, request, handler);
	}
	
	/**
	 * POST请求
	 */
	public static final <T> HttpResponse<T> post(String requestUrl, String data, HttpResponse.BodyHandler<T> handler) throws NetException {
		final var client = HTTPClient.newClient();
		final var request = HTTPClient.newRequest(requestUrl)
			.POST(BodyPublishers.ofString(data))
			.build();
		return HTTPClient.request(client, request, handler);
	}
	
	/**
	 * POST表单请求
	 */
	public static final <T> HttpResponse<T> postForm(String requestUrl, Map<String, String> data, HttpResponse.BodyHandler<T> handler) throws NetException {
		final var client = HTTPClient.newClient();
		final var request = HTTPClient.newFormRequest(requestUrl)
			.POST(HTTPClient.newFormBodyPublisher(data))
			.build();
		return HTTPClient.request(client, request, handler);
	}
	
	/**
	 * HEAD请求
	 */
	public static final HttpHeaderWrapper head(String requestUrl) throws NetException {
		final var client = HTTPClient.newClient();
		final var request = HTTPClient.newRequest(requestUrl)
			.method("HEAD", BodyPublishers.noBody())
			.build();
		final var response = HTTPClient.request(client, request, BodyHandlers.ofString());
		if(HTTPClient.ok(response)) {
			return HttpHeaderWrapper.newInstance(response.headers());
		}
		return HttpHeaderWrapper.newInstance(null);
	}
	
	/**
	 * 执行请求
	 */
	public static final <T> HttpResponse<T> request(HttpClient client, HttpRequest request, HttpResponse.BodyHandler<T> handler) throws NetException {
		if(client == null || request == null) {
			return null;
		}
		try {
			return client.send(request, handler);
		} catch (Exception e) {
			throw new NetException(e);
		}
	}
	
	/**
	 * 执行异步请求
	 */
	public static final <T> CompletableFuture<HttpResponse<T>> requestAsync(HttpClient client, HttpRequest request, HttpResponse.BodyHandler<T> handler) {
		if(client == null || request == null) {
			return null;
		}
		return client.sendAsync(request, handler);
	}
	
	/**
	 * <p>判断请求是否成功</p>
	 * <p>响应状态码：{@link #HTTP_OK}、{@link #HTTP_PARTIAL_CONTENT}。</p>
	 */
	public static final <T> boolean ok(HttpResponse<T> response) {
		return response != null &&
			(
				response.statusCode() == HTTP_OK ||
				response.statusCode() == HTTP_PARTIAL_CONTENT
			);
	}

	/**
	 * 无法满足请求范围，响应状态码[{@link #HTTP_REQUESTED_RANGE_NOT_SATISFIABLE}。
	 */
	public static final <T> boolean requestedRangeNotSatisfiable(HttpResponse<T> response) {
		return response != null && response.statusCode() == HTTP_REQUESTED_RANGE_NOT_SATISFIABLE;
	}
	
}
