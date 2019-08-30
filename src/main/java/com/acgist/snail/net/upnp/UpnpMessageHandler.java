package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.utils.StringUtils;

/**
 * UPNP消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UpnpMessageHandler extends UdpMessageHandler {

	/**
	 * 类型
	 */
	private static final String HEADER_NT = "nt";
	/**
	 * 地址
	 */
	private static final String HEADER_LOCATION = "location";
	/**
	 * UPNP控制类型
	 */
	private static final String UPNP_SERVICE = "urn:schemas-upnp-org:service:WANIPConnection:1";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpMessageHandler.class);
	
	@Override
	public void onMessage(ByteBuffer buffer, InetSocketAddress socketAddress) {
		final String content = new String(buffer.array());
		this.config(content);
	}
	
	/**
	 * 配置UPNP
	 */
	private void config(String content) {
		System.out.println(content);
		final String[] headers = content.split("\n");
		boolean upnp = false; // 支持UPNP
		String location = null; // 控制地址
		for (String header : headers) {
			if(header.toLowerCase().startsWith(HEADER_NT)) {
				final int index = header.indexOf(":") + 1;
				upnp = UPNP_SERVICE.equalsIgnoreCase(header.substring(index).trim());
			}
			if(header.toLowerCase().startsWith(HEADER_LOCATION)) {
				final int index = header.indexOf(":") + 1;
				location = header.substring(index).trim();
			}
		}
		try {
			if(upnp && StringUtils.isNotEmpty(location)) {
				UpnpService.getInstance().load(location).setting();
			}
		} catch (Exception e) {
			LOGGER.error("设置UPNP异常", e);
		}
	}
	
}
