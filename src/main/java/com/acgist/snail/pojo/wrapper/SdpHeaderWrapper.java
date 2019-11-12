package com.acgist.snail.pojo.wrapper;

import java.util.List;
import java.util.Map;

/**
 * <p>SDP头信息</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class SdpHeaderWrapper extends HeaderWrapper {

	/**
	 * 头信息分隔符
	 */
	private static final String DEFAULT_HEADER_KV = "=";
	/**
	 * 头信息填充符
	 */
	private static final String DEFAULT_HEADER_PADDING = "";
	
	private SdpHeaderWrapper(String content) {
		super(DEFAULT_HEADER_KV, content);
	}
	
	private SdpHeaderWrapper(Map<String, List<String>> headers) {
		super(DEFAULT_HEADER_KV, DEFAULT_HEADER_PADDING, headers);
	}
	
	public static final SdpHeaderWrapper newInstance(String content) {
		return new SdpHeaderWrapper(content);
	}
	
	public static final SdpHeaderWrapper newBuilder(Map<String, List<String>> headers) {
		return new SdpHeaderWrapper(headers);
	}

}
