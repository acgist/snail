package com.acgist.snail.system.header;

import java.util.HashMap;
import java.util.Map;

import com.acgist.snail.utils.StringUtils;

/**
 * <p>头处理</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class HeaderUtils {

	/**
	 * <p>读取头</p>
	 * <p>键转为小写</p>
	 * 
	 * @param content 头文本信息
	 * 
	 * @return 头键值对
	 */
	public static final Map<String, String> read(String content) {
		final String[] pairs = content.split("\n");
		final Map<String, String> map = new HashMap<>();
		int index;
		String key, value;
		for (String pair : pairs) {
			pair = pair.trim();
			if(StringUtils.isNotEmpty(pair)) {
				index = pair.indexOf(":");
				if(index == -1) {
					key = pair.trim();
					value = null;
				} else if(index < pair.length()) {
					key = pair.substring(0, index).trim();
					value = pair.substring(index + 1).trim();
				} else {
					key = pair.substring(0, index).trim();
					value = "";
				}
				map.put(key.toLowerCase(), value);
			}
		}
		return map;
	}
	
}
