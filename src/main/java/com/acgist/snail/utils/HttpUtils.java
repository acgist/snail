package com.acgist.snail.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;

import com.acgist.snail.module.config.SystemConfig;

/**
 * http工具
 */
public class HttpUtils {

//	private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

	private static final String USER_AGENT;
	
	static {
		StringBuilder builder = new StringBuilder();
		builder.append("Mozilla/5.0").append(" ")
		.append("(compatible; ").append(SystemConfig.getNameEn()).append("/").append(SystemConfig.getVersion()).append("; ").append(SystemConfig.getSupport()).append(")");
		USER_AGENT = builder.toString();
	}
	
	/**
	 * HTTPClient
	 */
	public static final HttpClient newClient() {
		return HttpClient
			.newBuilder()
			.followRedirects(Redirect.NORMAL)
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
	
}
