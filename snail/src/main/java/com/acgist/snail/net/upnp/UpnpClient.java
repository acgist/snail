package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.context.wrapper.HeaderWrapper;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.UdpClient;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>UPNP客户端</p>
 * 
 * @author acgist
 */
public final class UpnpClient extends UdpClient<UpnpMessageHandler> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpClient.class);

	/**
	 * <p>M-SEARCH协议：{@value}</p>
	 */
	private static final String PROTOCOL = "M-SEARCH * HTTP/1.1";
	
	/**
	 * @param socketAddress 地址
	 */
	private UpnpClient(InetSocketAddress socketAddress) {
		super("UPNP Client", new UpnpMessageHandler(socketAddress));
	}
	
	public static final UpnpClient newInstance() {
		return new UpnpClient(NetUtils.buildSocketAddress(UpnpServer.upnpHost(), UpnpServer.UPNP_PORT));
	}
	
	@Override
	public boolean open() {
		return this.open(UpnpServer.getInstance().channel());
	}

	/**
	 * <p>发送M-SEARCH消息</p>
	 */
	public void mSearch() {
		LOGGER.debug("发送M-SEARCH消息");
		try {
			this.send(this.buildMSearch());
		} catch (NetException e) {
			LOGGER.error("发送M-SEARCH消息异常", e);
		}
	}
	
	/**
	 * <p>新建M-SEARCH消息</p>
	 * 
	 * @return 消息
	 */
	private String buildMSearch() {
		final HeaderWrapper builder = HeaderWrapper.newBuilder(PROTOCOL);
		builder
			.header("HOST", SymbolConfig.Symbol.COLON.join(UpnpServer.upnpHost(), UpnpServer.UPNP_PORT))
			.header("ST", UpnpServer.UPNP_ROOT_DEVICE)
			.header("MAN", "\"ssdp:discover\"")
			.header("MX", "3");
		return builder.build();
	}

}
