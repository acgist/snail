package com.acgist.snail.net.upnp;

import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpMessageHandler;

/**
 * UPNP消息
 */
public class UpnpMessageHandler extends UdpMessageHandler {

	private static final String HEADER_LOCATION = "location";
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpMessageHandler.class);
	
	@Override
	public void doMessage(ByteBuffer buffer) {
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
				UpnpService.getInstance().load(location);
				break;
			}
		}
	}
	
}
