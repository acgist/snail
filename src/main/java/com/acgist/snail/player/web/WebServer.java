package com.acgist.snail.player.web;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NetUtils;
import com.sun.net.httpserver.HttpServer;

/**
 * <p>Web服务端</p>
 * 
 * @author acgist
 * @since 1.3.0
 */
public final class WebServer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);

	private static final WebServer INSTANCE = new WebServer();
	
	/**
	 * <p>连接队列长度</p>
	 */
	private static final int BACKLOG = 4;
	/**
	 * <p>HTTP服务器线程池</p>
	 */
	private static final ExecutorService EXECUTOR = SystemThreadContext.newExecutor(1, BACKLOG, 100, 60L, SystemThreadContext.SNAIL_THREAD_HTTP_SERVER);
	
	private final HttpServer server;
	
	private WebServer() {
		this.server = buildServer();
	}
	
	public static final WebServer getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>创建WebServer</p>
	 * 
	 * @return WebServer
	 */
	private HttpServer buildServer() {
		LOGGER.debug("创建WebServer");
		HttpServer server = null;
		try {
			server = HttpServer.create(NetUtils.buildSocketAddress(SystemConfig.getWebServerPort()), BACKLOG);
		} catch (IOException e) {
			LOGGER.error("创建WebServer异常", e);
		}
		if(server == null) {
			return server;
		}
		server.setExecutor(EXECUTOR); // 设置线程池
		server.createContext("/", WebHandler.newInstance()); // 请求处理器
		return server;
	}
	
	/**
	 * <p>启动服务</p>
	 * 
	 * @throws NetException 网络异常
	 */
	public void launch() throws NetException {
		if(this.server == null) {
			throw new NetException("启动WebServer服务失败");
		}
		this.server.start();
	}
	
	/**
	 * <p>关闭WebServer</p>
	 */
	public void shutdown() {
		LOGGER.debug("关闭WebServer");
		this.server.stop(0);
	}
	
}
