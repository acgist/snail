package com.acgist.snail.net.application;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.TcpClient;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>系统客户端</p>
 * 
 * @author acgist
 */
public final class ApplicationClient extends TcpClient<ApplicationMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationClient.class);
	
	private ApplicationClient() {
		super("Application Client", SystemConfig.CONNECT_TIMEOUT, new ApplicationMessageHandler());
	}
	
	public static final ApplicationClient newInstance() {
		return new ApplicationClient();
	}
	
	@Override
	public boolean connect() {
		return this.connect(NetUtils.LOOPBACK_HOST_ADDRESS, SystemConfig.getServicePort());
	}
	
	/**
	 * <p>发送系统消息</p>
	 * 
	 * @param message 系统消息
	 */
	public void send(ApplicationMessage message) {
		this.handler.send(message);
	}
	
	/**
	 * <p>唤醒主窗口</p>
	 * <p>向已经启动的系统实例发送唤醒消息</p>
	 */
	public static final void notifyWindow() {
		final ApplicationClient client = ApplicationClient.newInstance();
		try {
			final boolean success = client.connect();
			if(success) {
				client.send(ApplicationMessage.Type.NOTIFY.build());
				client.send(ApplicationMessage.Type.CLOSE.build());
			}
		} catch (Exception e) {
			LOGGER.error("唤醒主窗口异常", e);
		} finally {
			client.close();
		}
	}
	
}
