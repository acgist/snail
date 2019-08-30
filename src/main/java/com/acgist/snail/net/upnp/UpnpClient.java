package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>UPNP客户端</p>
 * <p>UDP协议、随机端口。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UpnpClient extends UdpClient<UpnpMessageHandler> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpClient.class);

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
	 * 配置UPNP
	 */
	public void config() {
		LOGGER.info("配置UPNP");
		try {
			send(mSearch());
		} catch (NetException e) {
			LOGGER.error("发送UPNP消息异常", e);
		}
	}
	
	/**
	 * M-SEARCH
	 */
	private String mSearch() {
		final String newLine = "\r\n";
		final StringBuilder builder = new StringBuilder();
		builder
			.append("M-SEARCH * HTTP/1.1").append(newLine)
			.append("HOST: 239.255.255.250:1900").append(newLine)
			.append("MX: 3").append(newLine)
			.append("ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1").append(newLine)
			.append("MAN: \"ssdp:discover\"").append(newLine);
		return builder.toString();
	}

}
