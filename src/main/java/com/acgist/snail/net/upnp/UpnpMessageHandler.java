package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.utils.HeaderUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * UPNP消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UpnpMessageHandler extends UdpMessageHandler {

	/**
	 * 地址
	 */
	private static final String HEADER_LOCATION = "location";
	/**
	 * UPNP控制类型
	 */
	private static final String UPNP_VALUE_MATCH = "urn:schemas-upnp-org";
	
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
		final Map<String, String> headers = HeaderUtils.read(content);
		final boolean support = headers.values().stream().anyMatch(value -> value != null && value.startsWith(UPNP_VALUE_MATCH));
		if(!support) {
			LOGGER.info("UPNP不支持的响应：{}", content);
			return;
		}
		final String location = headers.get(HEADER_LOCATION);
		try {
			if(StringUtils.isNotEmpty(location)) {
				UpnpService.getInstance().load(location).setting();
			}
		} catch (Exception e) {
			LOGGER.error("设置UPNP异常", e);
		}
	}
	
}
