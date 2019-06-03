package com.acgist.snail.pojo.wrapper;

import java.net.http.HttpHeaders;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * wrapper - HTTP请求头
 */
public class HttpHeaderWrapper {

	/**
	 * 下载大小
	 */
	private static final String CONTENT_LENGTH = "Content-Length".toLowerCase();
	/**
	 * 下载描述
	 */
	private static final String CONTENT_DISPOSITION = "Content-Disposition".toLowerCase();
	/**
	 * 端点续传：下载范围
	 */
	private static final String CONTENT_RANGE = "Content-Range".toLowerCase();
	/**
	 * 端点续传
	 */
	private static final String ACCEPT_RANGES = "Accept-Ranges".toLowerCase();
	
	private Map<String, String> headers;
	
	private HttpHeaderWrapper() {
	}

	public static final HttpHeaderWrapper newInstance(HttpHeaders httpHeaders) {
		final HttpHeaderWrapper wrapper = new HttpHeaderWrapper();
		if(httpHeaders != null) {
			wrapper.headers = httpHeaders.map().entrySet()
			.stream()
			.filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()))
			.collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), entry -> entry.getValue().get(0)));
		}
		return wrapper;
	}
	
	/**
	 * 获取所有header数据
	 */
	public Map<String, String> headers() {
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
	 * 下载文件名称
	 */
	public String fileName(final String defaultName) {
		if(isEmpty()) {
			return defaultName;
		}
		String fileName = headers.get(CONTENT_DISPOSITION);
		if(StringUtils.isEmpty(fileName)) {
			return defaultName;
		}
		final String fileNameLower = fileName.toLowerCase();
		if(fileNameLower.contains("filename")) {
			fileName = UrlUtils.decode(fileName);
			int index = fileName.indexOf("=");
			if(index != -1) {
				fileName = fileName.substring(index + 1);
				index = fileName.indexOf("?");
				if(index != -1) {
					fileName = fileName.substring(0, index);
				}
			}
			fileName = fileName.trim();
			if(StringUtils.isEmpty(fileName)) {
				return defaultName;
			}
			return fileName;
		} else {
			return defaultName;
		}
	}
	
	/**
	 * 下载文件大小：Content-Length：102400
	 */
	public Long fileSize() {
		Long size = 0L;
		if(isEmpty()) {
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
	
	/**
	 * 是否支持断点续传
	 * accept-ranges=bytes
	 * content-range=bytes 0-100/100
	 */
	public boolean range() {
		boolean range = false;
		if(isEmpty()) {
			return range;
		}
		if(headers.containsKey(ACCEPT_RANGES)) {
			range = "bytes".equals(headers.get(ACCEPT_RANGES));
		} else if(headers.containsKey(CONTENT_RANGE)) {
			range = true;
		}
		return range;
	}

	/**
	 * 获取开始下载位置
	 */
	public long beginRange() {
		long range = 0L;
		if(headers == null) {
			return range;
		}
		if(headers.containsKey(CONTENT_RANGE)) {
			String contentRange = headers.get(CONTENT_RANGE);
			int endIndex = contentRange.lastIndexOf("-");
			String value = contentRange.substring(5, endIndex).trim();
			if(StringUtils.isNumeric(value)) {
				range = Long.valueOf(value);
			}
		}
		return range;
	}
	
}
