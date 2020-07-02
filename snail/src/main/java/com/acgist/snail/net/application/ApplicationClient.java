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
 * 
 * @author acgist
 * @since 1.0.0
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
		return connect(NetUtils.LOCAL_IP, SystemConfig.getServicePort());
	}
	
	/**
	 * <p>发送系统消息</p>
	 * 
	 * @param message 系统消息
	 */
	public void send(ApplicationMessage message) {
		try {
			this.send(message.toString());
		} catch (NetException e) {
			LOGGER.error("发送系统消息异常", e);
		}
	}
	
	/**
	 * <p>唤醒主窗口</p>
	 * <p>向已经启动的系统实例发送唤醒消息</p>
	 */
	public static final void notifyWindow() {
		final ApplicationClient client = ApplicationClient.newInstance();
		try {
			final boolean ok = client.connect();
			if(ok) {
				client.send(ApplicationMessage.message(ApplicationMessage.Type.NOTIFY));
				client.send(ApplicationMessage.message(ApplicationMessage.Type.CLOSE));
			}
		} catch (Exception e) {
			LOGGER.error("唤醒主窗口异常", e);
		} finally {
			client.close();
		}
	}
	
}
