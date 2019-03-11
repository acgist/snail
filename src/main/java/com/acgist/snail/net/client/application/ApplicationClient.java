package com.acgist.snail.net.client.application;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.client.AbstractClient;
import com.acgist.snail.net.message.impl.ClientMessageHandler;
import com.acgist.snail.pojo.message.ClientMessage;
import com.acgist.snail.pojo.message.ClientMessage.Type;
import com.acgist.snail.system.config.SystemConfig;

/**
 * 启动检测：如果已经启动实例，通过这个方法唤醒已启动的窗口
 */
public class ApplicationClient extends AbstractClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationClient.class);
	
	@Override
	public void connect() {
		this.connect(SystemConfig.getServerHost(), SystemConfig.getServerPort());
	}
	
	@Override
	public void connect(String host, int port) {
		this.connect(host, port, new ClientMessageHandler());
	}
	
	/**
	 * 用户输入传输
	 */
	public void readin() {
		Scanner scanner = new Scanner(System.in);
		String message = null;
		while ((message = scanner.next()) != null) {
			if(message.equals("close")) {
				send(ClientMessage.message(Type.close, message));
				close();
				break;
			} else {
				send(ClientMessage.text(message));
			}
		}
		scanner.close();
	}
	
	/**
	 * 唤起主窗口
	 */
	public static final void notifyWindow() {
		ApplicationClient client = new ApplicationClient();
		try {
			client.connect();
			client.send(ClientMessage.message(ClientMessage.Type.notify));
			client.send(ClientMessage.message(ClientMessage.Type.close));
		} catch (Exception e) {
			LOGGER.error("通知主窗口异常", e);
		} finally {
			client.close();
		}
	}
	
	public static final void main(String[] args) {
		ApplicationClient client = new ApplicationClient();
		client.connect();
		client.readin();
	}

}
