package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.pojo.wrapper.HeaderWrapper;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>UPNP客户端</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UpnpClient extends UdpClient<UpnpMessageHandler> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpClient.class);

	/**
	 * M-SEARCH协议
	 */
	private static final String PROTOCOL = "M-SEARCH * HTTP/1.1";
	
	private UpnpClient(InetSocketAddress socketAddress) {
		super("UPNP Client", new UpnpMessageHandler(), socketAddress);
	}
	
	public static final UpnpClient newInstance() {
		return new UpnpClient(NetUtils.buildSocketAddress(UpnpServer.UPNP_HOST, UpnpServer.UPNP_PORT));
	}

	@Override
	public boolean open() {
		return this.open(UpnpServer.getInstance().channel());
	}
	
	/**
	 * 发送M-SEARCH消息
	 */
	public void mSearch() {
		LOGGER.debug("发送M-SEARCH消息");
		try {
			send(buildMSearch());
		} catch (NetException e) {
			LOGGER.error("发送M-SEARCH消息异常", e);
		}
	}
	
	/**
	 * 创建M-SEARCH消息
	 */
	private String buildMSearch() {
		final HeaderWrapper builder = HeaderWrapper.newBuilder(PROTOCOL);
		builder
			.header("HOST", UpnpServer.UPNP_HOST + ":" + UpnpServer.UPNP_PORT)
			.header("MX", "3")
			.header("ST", "urn:schemas-upnp-org:device:InternetGatewayDevice:1")
			.header("MAN", "\"ssdp:discover\"");
		return builder.build();
	}

}
