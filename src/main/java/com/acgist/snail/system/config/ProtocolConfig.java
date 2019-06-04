package com.acgist.snail.system.config;

import com.acgist.snail.utils.StringUtils;

/**
 * 协议配置
 */
public class ProtocolConfig {

	public static final String UDP_REGEX = "udp://.+";
	
	public enum Protocol {
		
		bt,
		udp,
		tcp,
		http,
		magnet,
		thunder;

	}
	
	/**
	 * 验证UDP
	 */
	public static final boolean verifyUdp(String url) {
		return StringUtils.regex(url, UDP_REGEX, false);
	}
	
}
