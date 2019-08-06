package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

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

	// UPNP端口
	private static final int UPNP_PORT = 1900;
	// UPNP地址
	private static final String UPNP_HOST = "239.255.255.250";
	
	private UpnpClient(InetSocketAddress socketAddress) {
		super("UPNP Client", new UpnpMessageHandler(), socketAddress);
		join(UPNP_HOST);
	}
	
	public static final UpnpClient newInstance() {
		return new UpnpClient(NetUtils.buildSocketAddress(UPNP_HOST, UPNP_PORT));
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
	private ByteBuffer mSearch() {
		final StringBuilder builder = new StringBuilder();
		builder.append("M-SEARCH * HTTP/1.1").append("\r\n");
		builder.append("HOST: 239.255.255.250:1900").append("\r\n");
		builder.append("MX: 2").append("\r\n");
		builder.append("ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1").append("\r\n");
		builder.append("MAN: \"ssdp:discover\"").append("\r\n");
		builder.append("\r\n");
		return ByteBuffer.wrap(builder.toString().getBytes());
	}

}
