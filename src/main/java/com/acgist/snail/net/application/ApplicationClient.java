package com.acgist.snail.net.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.TcpClient;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>系统客户端</p>
 * <p>启动检测：如果已经启动实例，唤醒已启动的窗口</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ApplicationClient extends TcpClient<ApplicationMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationClient.class);
	
	private ApplicationClient() {
		super("Application Client", 2, new ApplicationMessageHandler());
	}
	
	public static final ApplicationClient newInstance() {
		return new ApplicationClient();
	}
	
	@Override
	public boolean connect() {
		return connect(NetUtils.LOCAL_IP, SystemConfig.getServicePort());
	}
	
	/**
	 * 发送客户端消息
	 */
	public void send(ApplicationMessage message) {
		try {
			send(message.toString());
		} catch (NetException e) {
			LOGGER.error("Application消息发送异常", e);
		}
	}
	
	/**
	 * 唤起主窗口
	 */
	public static final void notifyWindow() {
		final ApplicationClient client = ApplicationClient.newInstance();
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
	
}
