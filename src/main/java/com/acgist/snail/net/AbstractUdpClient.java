package com.acgist.snail.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractUdpMessageHandler;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * UDP客户端
 */
public abstract class AbstractUdpClient<T extends AbstractUdpMessageHandler> {
	
	public static final String UDP_REGEX = "udp://.*";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUdpClient.class);

	private DatagramChannel channel;
	
	/**
	 * 打开客户端
	 */
	public boolean open() {
		boolean ok = true;
		try {
			this.channel = DatagramChannel.open();
			channel.configureBlocking(false); // 不阻塞
		} catch (IOException e) {
			close();
			ok = false;
			LOGGER.error("UDP打开端口异常", e);
		}
		return ok;
	}

	/**
	 * 绑定消息处理器
	 */
	public void bindMessageHandler(T handler) {
		SystemThreadContext.submit(() -> {
			try {
				handler.handle(channel);
			} catch (IOException e) {
				LOGGER.error("消息代理异常", e);
			}
		});
	}
	
	/**
	 * 发送消息
	 */
	public void send(ByteBuffer buffer, SocketAddress address) throws NetException {
		buffer.flip();
		try {
			channel.send(buffer, address);
		} catch (Exception e) {
			throw new NetException(e);
		}
	}

	/**
	 * 关闭channel
	 */
	public void close() {
		LOGGER.info("关闭UDP Client通道");
		IoUtils.close(channel);
	}

	/**
	 * 验证UCP协议
	 */
	public static final boolean verify(String url) {
		return StringUtils.regex(url, UDP_REGEX, true);
	}
	
}
