package com.acgist.snail.net.application;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.TcpClient;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.pojo.message.ApplicationMessage.Type;
import com.acgist.snail.system.config.SystemConfig;

/**
 * 启动检测：如果已经启动实例，通过这个方法唤醒已启动的窗口
 */
public class ApplicationClient extends TcpClient<ApplicationMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationClient.class);
	
	private ApplicationClient() {
		super("Application", ApplicationMessageHandler.SPLIT, new ApplicationMessageHandler());
	}
	
	public static final ApplicationClient newInstance() {
		return new ApplicationClient();
	}
	
	@Override
	public boolean connect() {
		return connect(SystemConfig.getServerHost(), SystemConfig.getServerPort());
	}
	
	/**
	 * 发送客户端消息
	 */
	private void send(ApplicationMessage message) {
		send(message.toJson());
	}
	
	/**
	 * 用户输入传输
	 */
	private void readin() {
		Scanner scanner = new Scanner(System.in);
		String message = null;
		while ((message = scanner.next()) != null) {
			if(message.equals("close")) {
				send(ApplicationMessage.message(Type.close, message));
				close();
				break;
			} else {
				send(ApplicationMessage.text(message));
			}
		}
		scanner.close();
	}
	
	/**
	 * 唤起主窗口
	 */
	public static final void notifyWindow() {
		ApplicationClient client = ApplicationClient.newInstance();
		try {
			client.connect();
			client.send(ApplicationMessage.message(ApplicationMessage.Type.notify));
			client.send(ApplicationMessage.message(ApplicationMessage.Type.close));
		} catch (Exception e) {
			LOGGER.error("通知主窗口异常", e);
		} finally {
			client.close();
		}
	}
	
	public static final void main(String[] args) {
		ApplicationClient client = ApplicationClient.newInstance();
		client.connect();
		client.readin();
	}

}
