package com.acgist.snail.module.server;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.config.SystemConfig;
import com.acgist.snail.module.handler.AcceptHandler;

/**
 * 服务监听
 */
public class ApplicationServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServer.class);
	
	private static final ApplicationServer INSTANCE = new ApplicationServer();
	
	private AsynchronousChannelGroup group;
	private AsynchronousServerSocketChannel server;
	
	private ApplicationServer() {
	}
	
	public static final ApplicationServer getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 开启监听
	 */
	public boolean listen() {
		boolean ok = true;
		ExecutorService executor = Executors.newFixedThreadPool(10);
		try {
			group = AsynchronousChannelGroup.withThreadPool(executor);
			server = AsynchronousServerSocketChannel.open(group).bind(new InetSocketAddress(SystemConfig.getServerHost(), SystemConfig.getServerPort()));
			server.accept(server, new AcceptHandler());
		} catch (Exception e) {
			ok = false;
			LOGGER.error("开启服务监听异常", e);
		}
		if(ok) {
			Thread thread = new Thread(() -> {
				try {
					LOGGER.info("开启监听服务线程");
					group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					LOGGER.error("监听服务等待异常", e);
				}
			});
			thread.setName(SystemConfig.getName() + "监听服务");
			thread.setDaemon(true); // 防止阻止系统关闭
			thread.start();
		}
		return ok;
	}

	public static void main(String[] args) {
		getInstance().listen();
	}
	
}

