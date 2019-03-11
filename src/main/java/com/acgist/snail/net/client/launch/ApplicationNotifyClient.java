package com.acgist.snail.net.client.launch;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.client.AbstractClient;
import com.acgist.snail.pojo.message.ClientMessage;
import com.acgist.snail.pojo.message.ClientMessage.Type;

/**
 * 启动检测：如果已经启动实例，通过这个方法唤醒已启动的窗口
 */
public class ApplicationNotifyClient extends AbstractClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationNotifyClient.class);
	
	protected ApplicationNotifyClient() {
		super(1, "Notify Client Thread");
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
				for (int i = 0; i < 10; i++) {
					send(ClientMessage.text(message));
				}
			}
		}
		scanner.close();
	}
	
	/**
	 * 唤起主窗口
	 */
	public static final void notifyWindow() {
		ApplicationNotifyClient client = new ApplicationNotifyClient();
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
	
	public static void main(String[] args) {
		ApplicationNotifyClient client = new ApplicationNotifyClient();
		client.connect();
		client.readin();
	}

}
