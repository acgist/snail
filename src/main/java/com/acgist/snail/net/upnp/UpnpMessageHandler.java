package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.pojo.bean.Headers;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>UPNP消息</p>
 * <p>协议参考：http://upnp.org/specs/arch/UPnP-arch-DeviceArchitecture-v1.0.pdf</p>
 * TODO：不是非常稳定
 * TODO：协议判断
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
	 * Internet Gateway Device，最后一位类型忽略。
	 */
	private static final String UPNP_DEVICE_IGD = "urn:schemas-upnp-org:device:InternetGatewayDevice:";
	
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
		final Headers headers = Headers.newInstance(content);
		final boolean support = headers.allHeaders().values().stream()
			.anyMatch(value -> StringUtils.startsWith(value, UPNP_DEVICE_IGD));
		if(!support) {
			LOGGER.info("UPNP不支持的响应：{}", content);
			return;
		}
		final String location = headers.header(HEADER_LOCATION);
		try {
			if(StringUtils.isNotEmpty(location)) {
				UpnpService.getInstance().load(location).setting();
			}
		} catch (Exception e) {
			LOGGER.error("端口映射异常", e);
		}
	}
	
}
