package com.acgist.snail.net.application;

import com.acgist.snail.net.AbstractTcpServer;
import com.acgist.snail.net.message.impl.ClientMessageHandler;
import com.acgist.snail.system.config.SystemConfig;

/**
 * 系统监听
 */
public class ApplicationServer extends AbstractTcpServer {

	private static final ApplicationServer INSTANCE = new ApplicationServer();
	
	private ApplicationServer() {
		super("系统监听");
	}
	
	public static final ApplicationServer getInstance() {
		return INSTANCE;
	}
	
	public boolean listen() {
		return listen(SystemConfig.getServerHost(), SystemConfig.getServerPort());
	}
	
	public boolean listen(String host, int port) {
		return listen(host, port, ClientMessageHandler.class);
	}
	
	public static final void main(String[] args) throws InterruptedException {
		ApplicationServer.getInstance().listen();
		Thread.sleep(Long.MAX_VALUE);
	}

}