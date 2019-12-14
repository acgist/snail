package com.acgist.snail.net.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.net.web.bootstrap.HtmlBuilder;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.system.config.MimeConfig;
import com.acgist.snail.system.config.MimeConfig.MimeType;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * <p>Web请求处理器</p>
 * <p>请求地址：{@code [/TaskId][/FilePath]}</p>
 * 
 * @author acgist
 * @since 1.3.0
 */
public final class WebHandler implements HttpHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WebHandler.class);
	
	/**
	 * <p>网站图标</p>
	 */
	private static final String FAVICON_ICO = "favicon.ico";

	private WebHandler() {
	}

	public static final WebHandler newInstance() {
		return new WebHandler();
	}
	
	@Override
	public void handle(HttpExchange exchange) {
		InputStream input = null;
		OutputStream output = null;
		Headers requestHeaders = null;
		Headers responseHeaders = null;
		final var path = exchange.getRequestURI().getPath();
		try {
			input = exchange.getRequestBody();
			output = exchange.getResponseBody();
			requestHeaders = exchange.getRequestHeaders();
			responseHeaders = exchange.getResponseHeaders();
			this.header(responseHeaders); // 设置通用响应头
			this.execute(path, exchange, output, requestHeaders, responseHeaders);
		} catch (Exception e) {
			LOGGER.error("处理请求异常：{}", path, e);
			try {
				// 发生异常返回任务列表
				this.tasks(exchange, output, requestHeaders, responseHeaders);
			} catch (Exception ex) {
				LOGGER.error("响应任务列表异常", ex);
			}
		} finally {
			IoUtils.close(input);
			IoUtils.close(output);
		}
	}

	/**
	 * <p>执行请求</p>
	 * 
	 * @param path 请求路径
	 * @param exchange 交换机
	 * @param output 输出流
	 * @param requestHeaders 请求头
	 * @param responseHeaders 响应头
	 * 
	 * @throws IOException IO异常
	 */
	private void execute(String path, HttpExchange exchange, OutputStream output, Headers requestHeaders, Headers responseHeaders) throws IOException {
		final StringTokenizer tokenizer = new StringTokenizer(path, "/");
		final int count = tokenizer.countTokens();
		switch (count) {
		case 0:
			this.tasks(exchange, output, requestHeaders, responseHeaders);
			break;
		case 1:
			this.files(tokenizer.nextToken(), exchange, output, requestHeaders, responseHeaders);
			break;
		case 2:
		default:
			this.play(tokenizer.nextToken(), tokenizer.nextToken(), exchange);
			break;
		}
	}
	
	/**
	 * <p>显示所有任务</p>
	 * 
	 * @param exchange 交换机
	 * @param output 输出流
	 * @param requestHeaders 请求头
	 * @param responseHeaders 响应头
	 * 
	 * @throws IOException IO异常
	 */
	private void tasks(HttpExchange exchange, OutputStream output, Headers requestHeaders, Headers responseHeaders) throws IOException {
		this.writeHtml(HtmlBuilder.getInstance().buildTasks(), exchange, output, requestHeaders, responseHeaders);
	}
	
	/**
	 * <p>显示任务所有文件</p>
	 * 
	 * @param id 任务ID
	 * @param exchange 交换机
	 * @param output 输出流
	 * @param requestHeaders 请求头
	 * @param responseHeaders 响应头
	 * 
	 * @throws IOException IO异常
	 */
	private void files(String id, HttpExchange exchange, OutputStream output, Headers requestHeaders, Headers responseHeaders) throws IOException {
		if(StringUtils.equals(id, FAVICON_ICO)) {
			this.writeFile("/image/logo.ico", exchange, output, requestHeaders, responseHeaders);
		} else {
			this.writeHtml(HtmlBuilder.getInstance().buildFiles(id), exchange, output, requestHeaders, responseHeaders);
		}
	}
	
	/**
	 * <p>响应文件</p>
	 * 
	 * @param file 文件路径：resources
	 * @param exchange 交换机
	 * @param output 输出流
	 * @param requestHeaders 请求头
	 * @param responseHeaders 响应头
	 */
	private void writeFile(String file, HttpExchange exchange, OutputStream output, Headers requestHeaders, Headers responseHeaders) {
		this.contentType(MimeConfig.mimeType(file), responseHeaders);
		try (final var input = this.getClass().getResourceAsStream(file)) {
			final var bytes = input.readAllBytes();
			// 中文文件名称问题
			// 使用URL编码解决
//			responseHeaders.add("Content-Disposition", "attachment; filename=" + UrlUtils.encode("蜗牛.txt"));
			// 设置ISO-8859-1编码解决
//			responseHeaders.add("Content-Disposition", "attachment; filename=" + new String("蜗牛.txt".getBytes(), SystemConfig.CHARSET_ISO_8859_1));
			exchange.sendResponseHeaders(HTTPClient.StatusCode.OK.code(), bytes.length);
			output.write(bytes);
		} catch (Exception e) {
			LOGGER.error("响应网站图标异常", e);
		}
	}
	
	/**
	 * <p>响应HTML</p>
	 * 
	 * @param html HTML
	 * @param exchange 交换机
	 * @param output 输出流
	 * @param requestHeaders 请求头
	 * @param responseHeaders 响应头
	 * 
	 * @throws IOException IO异常
	 */
	private void writeHtml(String html, HttpExchange exchange, OutputStream output, Headers requestHeaders, Headers responseHeaders) throws IOException {
		this.contentType(MimeType.TEXT_HTML, responseHeaders);
		if(StringUtils.isEmpty(html)) {
			exchange.sendResponseHeaders(HTTPClient.StatusCode.NOT_FOUND.code(), 0);
			return;
		}
		final var bytes = html.getBytes();
		exchange.sendResponseHeaders(HTTPClient.StatusCode.OK.code(), bytes.length);
		output.write(bytes);
	}
	
	/**
	 * <p>播放文件</p>
	 * 
	 * @param id 任务ID
	 * @param path 文件路径
	 * @param exchange 交换机
	 */
	private void play(String id, String path, HttpExchange exchange) {
	}

	/**
	 * <p>设置通用头部信息</p>
	 * 
	 * @param headers 头部信息
	 */
	private void header(Headers headers) {
		// 服务器名称
		headers.add(HttpHeaderWrapper.SERVER, SystemConfig.getNameEn());
	}
	
	/**
	 * <p>设置响应类型</p>
	 * 
	 * @param mimeType MIME类型
	 * @param headers 头部信息
	 */
	private void contentType(MimeType mimeType, Headers headers) {
		headers.add(HttpHeaderWrapper.CONTENT_TYPE, mimeType.value());
	}
	
}
