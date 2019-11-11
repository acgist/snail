package com.acgist.snail.pojo.wrapper;

import java.util.List;
import java.util.Map;

/**
 * <p>SDP头信息</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public class SdpHeaderWrapper extends HeaderWrapper {

	private SdpHeaderWrapper(Map<String, List<String>> headers) {
		super(headers);
	}

}
