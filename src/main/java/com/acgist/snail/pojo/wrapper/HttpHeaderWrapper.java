package com.acgist.snail.pojo.wrapper;

import java.net.http.HttpHeaders;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * HTTP请求头包装器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class HttpHeaderWrapper {

	/**
	 * 端点续传：下载范围
	 */
	private static final String CONTENT_RANGE = "Content-Range".toLowerCase();
	/**
	 * 端点续传
	 */
	private static final String ACCEPT_RANGES = "Accept-Ranges".toLowerCase();
	/**
	 * 下载大小
	 */
	private static final String CONTENT_LENGTH = "Content-Length".toLowerCase();
	/**
	 * 下载描述
	 */
	private static final String CONTENT_DISPOSITION = "Content-Disposition".toLowerCase();
	
	private final Map<String, String> headers;
	
	private HttpHeaderWrapper(Map<String, String> headers) {
		this.headers = headers;
	}

	public static final HttpHeaderWrapper newInstance(HttpHeaders httpHeaders) {
		Map<String, String> headers = null;
		if(httpHeaders != null) {
			headers = httpHeaders.map().entrySet()
			.stream()
			.filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()))
			.collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), entry -> entry.getValue().get(0)));
		}
		return new HttpHeaderWrapper(headers);
	}
	
	/**
	 * 获取所有header数据
	 */
	public Map<String, String> headers() {
		return this.headers;
	}
	
	/**
	 * header数据是否为空
	 */
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(this.headers);
	}
	
	/**
	 * header数据是否不为空
	 */
	public boolean isNotEmpty() {
		return !isEmpty();
	}
	
	/**
	 * 下载文件名称，如果获取不到下载文件名，返回默认的文件名。
	 * 
	 * @param defaultName 默认文件名
	 */
	public String fileName(final String defaultName) {
		if(isEmpty()) {
			return defaultName;
		}
		String fileName = this.headers.get(CONTENT_DISPOSITION);
		if(StringUtils.isEmpty(fileName)) {
			return defaultName;
		}
		final String fileNameLower = fileName.toLowerCase();
		if(fileNameLower.contains("filename")) { // 包含文件名
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
		if(this.headers.containsKey(CONTENT_LENGTH)) {
			String value = this.headers.get(CONTENT_LENGTH).trim();
			if(StringUtils.isNumeric(value)) {
				return Long.valueOf(value);
			}
		}
		return size;
	}
	
	/**
	 * <p>是否支持断点续传</p>
	 * <p>
	 * accept-ranges=bytes<br>
	 * content-range=bytes 0-100/100
	 * </p>
	 */
	public boolean range() {
		boolean range = false;
		if(isEmpty()) {
			return range;
		}
		if(this.headers.containsKey(ACCEPT_RANGES)) {
			range = "bytes".equals(headers.get(ACCEPT_RANGES));
		} else if(this.headers.containsKey(CONTENT_RANGE)) {
			range = true;
		}
		return range;
	}

	/**
	 * 获取开始下载位置
	 */
	public long beginRange() {
		long range = 0L;
		if(this.headers == null) {
			return range;
		}
		if(this.headers.containsKey(CONTENT_RANGE)) {
			String contentRange = this.headers.get(CONTENT_RANGE);
			int endIndex = contentRange.lastIndexOf("-");
			String value = contentRange.substring(5, endIndex).trim();
			if(StringUtils.isNumeric(value)) {
				range = Long.valueOf(value);
			}
		}
		return range;
	}
	
	@Override
	public String toString() {
		if(this.headers != null) {
			return this.headers.toString();
		} else {
			return null;
		}
	}
	
}
