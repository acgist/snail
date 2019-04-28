package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.system.exception.NetException;

/**
 * UPNP消息
 */
public class UpnpMessageHandler extends UdpMessageHandler {

	private static final String HEADER_LOCATION = "location";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpMessageHandler.class);
	
	@Override
	public void onMessage(InetSocketAddress address, ByteBuffer buffer) {
		final String content = new String(buffer.array());
		this.initUpnpService(content);
	}
	
	/**
	 * 初始化UpnpService
	 */
	private void initUpnpService(String content) {
		final String[] headers = content.split("\n");
		for (String header : headers) {
			if(header.toLowerCase().startsWith(HEADER_LOCATION)) {
				final int index = header.indexOf(":") + 1;
				final String location = header.substring(index).trim();
				try {
					UpnpService.getInstance().load(location).setting();
				} catch (NetException e) {
					LOGGER.error("设置UPNP异常", e);
				}
				break;
			}
		}
	}
	
}
