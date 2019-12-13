package com.acgist.snail.player.web;

import java.io.IOException;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.NetException;
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
		} catch (NetException e) {
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
	private void execute(String path, HttpExchange exchange) throws NetException {
		final StringTokenizer tokenizer = new StringTokenizer(path, "/");
		final int count = tokenizer.countTokens();
		switch (count) {
		case 0:
			this.tasks(exchange);
			break;
		case 1:
			this.files(tokenizer.nextToken(), exchange);
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
	 */
	private void tasks(HttpExchange exchange) {
	}
	
	/**
	 * <p>显示任务所有文件</p>
	 * 
	 * @param id 任务ID
	 * @param exchange 交换机
	 */
	private void files(String id, HttpExchange exchange) {
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

}
