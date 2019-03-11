package com.acgist.snail.net.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.UrlUtils;

/**
 * utils - http
 */
public class HttpUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

	public static final int HTTP_OK = 200; // OK
	public static final int HTTP_PARTIAL_CONTENT = 206; // 端点续传
	public static final int HTTP_RANGE_NOT_SATISFIABLE= 416; // 无法满足请求范围
	
	private static final String USER_AGENT;
	
	static {
		StringBuilder builder = new StringBuilder();
		builder
			.append("Mozilla/5.0")
			.append(" ")
			.append("(compatible; ")
			.append(SystemConfig.getNameEn())
			.append("/")
			.append(SystemConfig.getVersion())
			.append("; ")
			.append(SystemConfig.getSupport())
			.append(")");
		USER_AGENT = builder.toString();
	}
	
	/**
	 * HTTPClient
	 */
	public static final HttpClient newClient() {
		return HttpClient
			.newBuilder()
//			.followRedirects(Redirect.NORMAL)
			.followRedirects(Redirect.ALWAYS)
			.connectTimeout(Duration.ofSeconds(5))
			.build();
	}
	
	/**
	 * request
	 */
	public static final Builder newRequest(String url) {
		return HttpRequest
			.newBuilder()
			.uri(URI.create(url))
			.timeout(Duration.ofSeconds(10))
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
	 * 执行请求
	 */
	public static final <T> HttpResponse<T> request(HttpClient client, HttpRequest request, HttpResponse.BodyHandler<T> handler) {
		if(client == null || request == null) {
			return null;
		}
		try {
			return client.send(request, handler);
		} catch (IOException | InterruptedException e) {
			LOGGER.error("执行请求异常", e);
		}
		return null;
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
	 * 判断请求是否成功
	 */
	public static final <T> boolean ok(HttpResponse<T> response) {
		return response != null && (
				response.statusCode() == HTTP_OK ||
				response.statusCode() == HTTP_PARTIAL_CONTENT);
	}

	/**
	 * 无法满足请求范围
	 */
	public static final <T> boolean rangeNotSatisfiable(HttpResponse<T> response) {
		return response != null && response.statusCode() == HTTP_RANGE_NOT_SATISFIABLE;
	}
	
	/**
	 * 获取post数据
	 */
	public static final BodyPublisher formBodyPublisher(Map<String, String> data) {
		if(data == null || data.isEmpty()) {
			return BodyPublishers.noBody();
		}
		String body = data.entrySet()
			.stream()
			.map(entry -> {
				return entry.getKey() + "=" + UrlUtils.encode(entry.getValue());
			})
			.collect(Collectors.joining("&"));
		return BodyPublishers.ofString(body);
	}
	
	/**
	 * 获取header信息
	 */
	public static final HttpHeaderWrapper httpHeader(String url) {
		HttpClient client = HttpUtils.newClient();
		HttpRequest request = HttpUtils.newRequest(url)
			.method("HEAD", BodyPublishers.noBody())
			.build();
		HttpResponse<String> response = HttpUtils.request(client, request, BodyHandlers.ofString());
		if(HttpUtils.ok(response)) {
			return HttpHeaderWrapper.newInstance(response.headers());
		}
		return HttpHeaderWrapper.newInstance(null);
	}

}
