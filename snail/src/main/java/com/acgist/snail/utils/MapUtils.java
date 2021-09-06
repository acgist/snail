package com.acgist.snail.utils;

import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.config.SymbolConfig;

/**
 * <p>Map工具</p>
 * 
 * @author acgist
 */
public class MapUtils {

	private MapUtils() {
	}
	
	/**
	 * <p>判断是否为空</p>
	 * 
	 * @param map Map
	 * 
	 * @return 是否为空
	 */
	public static final boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * <p>判断是否非空</p>
	 * 
	 * @param map Map
	 * 
	 * @return 是否非空
	 */
	public static final boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}
	
	/**
	 * <p>Map转为URL参数</p>
	 * 
	 * @param map Map
	 * 
	 * @return URL参数
	 */
	public static final String toUrlQuery(Map<String, String> map) {
		if(MapUtils.isEmpty(map)) {
			return null;
		}
		return map.entrySet().stream()
			.map(entry -> SymbolConfig.Symbol.EQUALS.join(entry.getKey(), UrlUtils.encode(entry.getValue())))
			.collect(Collectors.joining(SymbolConfig.Symbol.AND.toString()));
	}
	
}
