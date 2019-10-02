package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.codec.impl.StringMessageCodec;
import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.pojo.wrapper.HeaderWrapper;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>UPNP消息代理</p>
 * <p>参考链接：http://upnp.org/specs/arch/UPnP-arch-DeviceArchitecture-v1.0.pdf</p>
 * <p>注：固定IP有时不能正确获取UPNP设置，请使用自动获取IP地址。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UpnpMessageHandler extends UdpMessageHandler implements IMessageCodec<String> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpMessageHandler.class);

	/**
	 * 地址头名称
	 */
	private static final String HEADER_LOCATION = "location";
	/**
	 * <p>Internet Gateway Device</p>
	 * <p>最后一位类型忽略</p>
	 */
	private static final String UPNP_DEVICE_IGD = "urn:schemas-upnp-org:device:InternetGatewayDevice:";
	
	public UpnpMessageHandler() {
		this.messageCodec = new StringMessageCodec(this);
	}
	
	@Override
	public void onMessage(String message, InetSocketAddress address) {
		final HeaderWrapper headers = HeaderWrapper.newInstance(message);
		// 判断是否支持UPNP设置
		final boolean support = headers.allHeaders().values().stream()
			.anyMatch(list -> list.stream()
				.anyMatch(value -> StringUtils.startsWith(value, UPNP_DEVICE_IGD))
			);
		if(!support) {
			LOGGER.info("UPNP设置失败（驱动）：{}", message);
			return;
		}
		final String location = headers.header(HEADER_LOCATION);
		try {
			if(StringUtils.isNotEmpty(location)) {
				UpnpService.getInstance().load(location).mapping();
			}
		} catch (NetException e) {
			LOGGER.error("UPNP端口映射异常", e);
		}
	}
	
}
