package com.acgist.snail.system.config;

import com.acgist.snail.utils.StringUtils;

/**
 * <p>协议配置</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ProtocolConfig {

	public static final String UDP_REGEX = "udp://.+";
	
	/**
	 * 协议
	 */
	public enum Protocol {
		
		/** udp */
		udp,
		/** tcp */
		tcp,
		/** wss:websocket */
		wss,
		/** http */
		http,
		/** 磁力链接 */
		magnet,
		/** 迅雷链接 */
		thunder,
		/** BT */
		torrent;

	}
	
	/**
	 * 验证UDP协议
	 */
	public static final boolean verifyUdp(String url) {
		return StringUtils.regex(url, UDP_REGEX, false);
	}
	
}
