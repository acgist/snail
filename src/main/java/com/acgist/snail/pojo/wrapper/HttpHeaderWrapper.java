package com.acgist.snail.pojo.wrapper;

import java.net.http.HttpHeaders;
import java.util.List;
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
	private static final String CONTENT_RANGE = "Content-Range";
	/**
	 * 端点续传：范围请求
	 */
	private static final String ACCEPT_RANGES = "Accept-Ranges";
	/**
	 * 下载大小
	 */
	private static final String CONTENT_LENGTH = "Content-Length";
	/**
	 * 下载描述
	 */
	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	/**
	 * 范围请求：支持断点续传
	 */
	private static final String BYTES = "bytes";
	/**
	 * 文件名
	 */
	private static final String FILENAME = "filename";
	
	private final Map<String, List<String>> headers;
	
	private HttpHeaderWrapper(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	public static final HttpHeaderWrapper newInstance(HttpHeaders httpHeaders) {
		Map<String, List<String>> headers = null;
		if(httpHeaders != null) {
			headers = httpHeaders.map().entrySet().stream()
				.filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()))
				.collect(Collectors.toMap(
					entry -> entry.getKey(),
					entry -> entry.getValue()
				));
		}
		return new HttpHeaderWrapper(headers);
	}

	/**
	 * 获取所有header数据
	 */
	public Map<String, List<String>> allHeaders() {
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
	 * 获取第一个头信息
	 * 
	 * @param key 头信息名称，忽略大小写。
	 * 
	 * @return 头信息值
	 */
	public String header(String key) {
		final var list = headerList(key);
		if(CollectionUtils.isEmpty(list)) {
			return null;
		}
		final String value = list.get(0);
		return value == null ? null : value.trim();
	}
	
	/**
	 * 获取头信息
	 * 
	 * @param key 头信息名称，忽略大小写。
	 * 
	 * @return 头信息值
	 */
	public List<String> headerList(String key) {
		if(isEmpty()) {
			return null;
		}
		final var optional = this.headers.entrySet().stream()
			.filter(entry -> {
				return StringUtils.equalsIgnoreCase(key, entry.getKey());
			})
			.map(entry -> entry.getValue())
			.findFirst();
		if(optional.isEmpty()) {
			return null;
		}
		return optional.get();
	}
	
	/**
	 * <p>下载文件名称，如果获取不到下载文件名，返回默认的文件名。</p>
	 * <p>Content-Disposition:attachment;filename=snail.jar?version=1.0.0</p>
	 * 
	 * @param defaultName 默认文件名
	 */
	public String fileName(final String defaultName) {
		String fileName = header(CONTENT_DISPOSITION);
		if(StringUtils.isEmpty(fileName)) {
			return defaultName;
		}
		final String fileNameLower = fileName.toLowerCase();
		if(fileNameLower.contains(FILENAME)) { // 包含文件名
			fileName = UrlUtils.decode(fileName); // URL解码
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
	public long fileSize() {
		long size = 0L;
		String value = header(CONTENT_LENGTH);
		if(value != null) {
			if(StringUtils.isNumeric(value)) {
				size = Long.parseLong(value);
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
		final String acceptRanges = header(ACCEPT_RANGES);
		final String contentRange = header(CONTENT_RANGE);
		if(acceptRanges != null) {
			range = BYTES.equals(acceptRanges);
		}
		if(contentRange != null) {
			range = true;
		}
		return range;
	}

	/**
	 * <p>获取开始下载位置</p>
	 * <p>content-range=bytes 0-100/100</p>
	 */
	public long beginRange() {
		long range = 0L;
		String contentRange = header(CONTENT_RANGE);
		if(contentRange != null) {
			int endIndex = contentRange.lastIndexOf("-");
			String value = contentRange.substring(5, endIndex).trim();
			if(StringUtils.isNumeric(value)) {
				range = Long.parseLong(value);
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
