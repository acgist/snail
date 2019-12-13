package com.acgist.snail.net.web;

import java.io.IOException;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
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

	private WebHandler() {
	}

	public static final WebHandler newInstance() {
		return new WebHandler();
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		final var uri = exchange.getRequestURI();
		final String path = uri.getPath();
		try {
			this.execute(path, exchange);
		} catch (IOException e) {
			LOGGER.error("处理请求异常：{}", path, e);
			this.tasks(exchange); // 发生异常返回任务列表
		}
	}

	/**
	 * <p>执行请求</p>
	 * 
	 * @param path 请求路径
	 * @param exchange 交换机
	 * 
	 * @throws NetException 网络异常
	 */
	private void execute(String path, HttpExchange exchange) throws IOException {
		final StringTokenizer tokenizer = new StringTokenizer(path, "/");
		final int count = tokenizer.countTokens();
		switch (count) {
		case 0:
			this.tasks(exchange);
			break;
		case 1:
			this.files(tokenizer.nextToken(), exchange);
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
	 * 
	 * @throws NetException 网络异常 
	 */
	private void tasks(HttpExchange exchange) throws IOException {
		this.response(HtmlBuilder.getInstance().buildTasks(), exchange);
	}
	
	/**
	 * <p>显示任务所有文件</p>
	 * 
	 * @param id 任务ID
	 * @param exchange 交换机
	 * 
	 * @throws NetException 网络异常
	 */
	private void files(String id, HttpExchange exchange) throws IOException {
		this.response(HtmlBuilder.getInstance().buildFiles(id), exchange);
	}
	
	/**
	 * <p>响应HTML</p>
	 * 
	 * @param html HTML
	 * 
	 * @throws NetException 网络异常
	 */
	private void response(String html, HttpExchange exchange) throws IOException {
		final var output = exchange.getResponseBody();
		final var headers = exchange.getResponseHeaders();
		headers.add(HttpHeaderWrapper.CONTENT_TYPE, "text/html"); // HTML
		this.header(headers);
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
		headers.add("Server", SystemConfig.getNameEn());
	}
	
}
