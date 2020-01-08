package com.acgist.snail.pojo.wrapper;

import java.util.List;
import java.util.Map;

/**
 * <p>SDP头部信息封装器</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class SdpHeaderWrapper extends HeaderWrapper {

	/**
	 * <p>头部信息分隔符：{@value}</p>
	 */
	private static final String DEFAULT_HEADER_KV = "=";
	/**
	 * <p>头部信息填充符：{@value}</p>
	 */
	private static final String DEFAULT_HEADER_PADDING = "";
	
	private SdpHeaderWrapper(String content) {
		super(DEFAULT_HEADER_KV, DEFAULT_HEADER_PADDING, content);
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
