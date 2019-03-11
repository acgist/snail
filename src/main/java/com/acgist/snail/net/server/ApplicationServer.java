package com.acgist.snail.net.server;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.handler.socket.AcceptHandler;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;

/**
 * 系统监听
 */
public class ApplicationServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServer.class);
	
	private static final ApplicationServer INSTANCE = new ApplicationServer();
	
	private ExecutorService executor;
	private AsynchronousChannelGroup group;
	private AsynchronousServerSocketChannel server;
	
	private ApplicationServer() {
	}
	
	public static final ApplicationServer getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 开启监听，线程大小根据客户类型优化
	 */
	public boolean listen() {
		boolean ok = true;
		executor = Executors.newFixedThreadPool(1, SystemThreadContext.newThreadFactory("Server Thread"));
		try {
			group = AsynchronousChannelGroup.withThreadPool(executor);
			server = AsynchronousServerSocketChannel.open(group).bind(new InetSocketAddress(SystemConfig.getServerHost(), SystemConfig.getServerPort()));
			server.accept(server, new AcceptHandler());
		} catch (Exception e) {
			ok = false;
			LOGGER.error("启动系统监听异常", e);
		}
		if(ok) {
			SystemThreadContext.runasyn(() -> {
				try {
					LOGGER.info("启动系统监听线程");
					group.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					LOGGER.error("系统监听异常", e);
				}
			});
		}
		return ok;
	}

	/**
	 * 关闭服务监听
	 */
	public void shutdown() {
		LOGGER.info("关闭系统监听");
		IoUtils.close(group, server, null);
		if(executor != null) {
			executor.shutdown();
		}
	}
	
	public static void main(String[] args) {
		ApplicationServer.getInstance().listen();
	}

}

