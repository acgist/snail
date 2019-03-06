package com.acgist.snail.pojo.wrapper;

import java.net.http.HttpHeaders;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * HTTP请求头
 */
public class HttpHeaderWrapper {

	private static final String CONTENT_LENGTH = "Content-Length".toLowerCase();
	private static final String CONTENT_DISPOSITION = "Content-Disposition".toLowerCase();
	
	private Map<String, String> headers;
	
	private HttpHeaderWrapper() {
	}

	public static final HttpHeaderWrapper newInstance(HttpHeaders httpHeaders) {
		HttpHeaderWrapper wrapper = new HttpHeaderWrapper();
		if(httpHeaders != null) {
			wrapper.headers = httpHeaders.map().entrySet()
			.stream()
			.filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()))
			.collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), entry -> entry.getValue().get(0)));
		}
		return wrapper;
	}
	
	public Map<String, String> map() {
		return headers;
	}
	
	/**
	 * 是否未包含数据
	 */
	public boolean isEmpty() {
		return headers == null || headers.isEmpty();
	}
	
	/**
	 * 是否包含数据
	 */
	public boolean isNotEmpty() {
		return !isEmpty();
	}
	
	/**
	 * 文件名称
	 */
	public String fileName(String defaultName) {
		if(headers == null) {
			return defaultName;
		}
		String fileName = headers.get(CONTENT_DISPOSITION);
		if(StringUtils.isEmpty(fileName)) {
			return defaultName;
		}
		final String fileNameLower = fileName.toLowerCase();
		if(fileNameLower.contains("filename")) {
			int index = fileName.indexOf("=");
			if(index != -1) {
				fileName = fileName.substring(index + 1);
				index = fileName.indexOf("?");
				if(index != -1) {
					fileName = fileName.substring(0, index);
				}
			}
			return fileName;
		} else {
			return defaultName;
		}
	}
	
	/**
	 * 文件大小：Content-Length：102400
	 * 
	 */
	public Long fileSize() {
		Long size = 0L;
		if(headers == null) {
			return size;
		}
		if(headers.containsKey(CONTENT_LENGTH)) {
			String value = headers.get(CONTENT_LENGTH).trim();
			if(StringUtils.isNumeric(value)) {
				return Long.valueOf(value);
			}
		}
		return size;
	}
	
}
