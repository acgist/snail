package com.acgist.snail.protocol.udp;

import com.acgist.snail.utils.StringUtils;

/**
 * UDP协议
 */
public class UdpProtocol {

	public static final String UDP_REGEX = "udp://.*";
	
	/**
	 * 验证UDP协议
	 */
	public static final boolean verify(String url) {
		return StringUtils.regex(url, UDP_REGEX, true);
	}
	
}
