package com.acgist.snail.pojo.wrapper;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>HTTP头部信息包装器</p>
 * 
 * @author acgist
 */
public final class HttpHeaderWrapper extends HeaderWrapper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpHeaderWrapper.class);

	/**
	 * <p>HTTP客户端信息：{@value}</p>
	 */
	public static final String HEADER_USER_AGENT = "User-Agent";
	/**
	 * <p>MIME类型：{@value}</p>
	 */
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	/**
	 * <p>请求下载范围：{@value}</p>
	 * <p>接收{@link #HEADER_RANGE}时返回范围数据</p>
	 */
	public static final String HEADER_CONTENT_RANGE = "Content-Range";
	/**
	 * <p>下载大小：{@value}</p>
	 */
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	/**
	 * <p>下载描述：{@value}</p>
	 */
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	/**
	 * <p>接收范围请求：{@value}</p>
	 * <p>没有接收{@link #HEADER_RANGE}时返回全部数据</p>
	 */
	public static final String HEADER_ACCEPT_RANGES = "Accept-Ranges";
	/**
	 * <p>范围请求：{@value}</p>
	 * <table border="1">
	 * 	<caption>HTTP协议断点续传设置</caption>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=0-499}</td>
	 * 		<td>{@code 0}-{@code 499}字节范围</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=500-999}</td>
	 * 		<td>{@code 500}-{@code 999}字节范围</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=-500}</td>
	 * 		<td>最后{@code 500}字节</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=500-}</td>
	 * 		<td>{@code 500}字节开始到结束</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=0-0,-1}</td>
	 * 		<td>第一个字节和最后一个字节</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>{@code Range: bytes=500-600,601-999}</td>
	 * 		<td>同时指定多个范围</td>
	 * 	</tr>
	 * </table>
	 */
	public static final String HEADER_RANGE = "Range";
	/**
	 * <p>接收范围请求：{@value}</p>
	 * 
	 * @see #HEADER_ACCEPT_RANGES
	 */
	public static final String HEADER_RANGE_BYTES = "bytes";
	/**
	 * <p>文件名称：{@value}</p>
	 * 
	 * @see #HEADER_CONTENT_DISPOSITION
	 */
	public static final String HEADER_CONTENT_DISPOSITION_FILENAME = "filename";
	
	/**
	 * @param headers 头部信息
	 */
	private HttpHeaderWrapper(Map<String, List<String>> headers) {
		super(headers);
	}
	
	/**
	 * @param httpHeaders HTTP头部信息
	 * 
	 * @return HttpHeaderWrapper
	 */
	public static final HttpHeaderWrapper newInstance(Map<String, List<String>> httpHeaders) {
		return new HttpHeaderWrapper(httpHeaders);
	}

	/**
	 * <p>获取文件名称</p>
	 * <p>下载文件名称：如果不存在返回默认的文件名称</p>
	 * <pre>
	 * Content-Disposition:attachment;filename="snail.jar"
	 * Content-Disposition:attachment;filename=snail.jar?version=1.0.0
	 * Content-Disposition:inline;filename="snail.jar";filename*=utf-8''snail.jar
	 * </pre>
	 * 
	 * @param defaultName 默认文件名称
	 * 
	 * @return 文件名称
	 */
	public String fileName(final String defaultName) {
		String fileName = this.header(HEADER_CONTENT_DISPOSITION);
		if(StringUtils.isEmpty(fileName)) {
			return defaultName;
		}
		// 转为小写获取截取位置
		final String fileNameLower = fileName.toLowerCase();
		final int index = fileNameLower.indexOf(HEADER_CONTENT_DISPOSITION_FILENAME);
		if(index >= 0) {
			// 包含文件名称
			fileName = this.extractFileName(index, fileName);
			fileName = this.charsetFileName(fileName);
			if(StringUtils.isEmpty(fileName)) {
				return defaultName;
			}
			return fileName;
		} else {
			return defaultName;
		}
	}

	/**
	 * <p>头部信息名称提取</p>
	 * 
	 * @param index 开始位置
	 * @param fileName 文件名称
	 * 
	 * @return 文件名称
	 */
	private String extractFileName(int index, String fileName) {
		// 删除：filename前面内容
		fileName = fileName.substring(index + HEADER_CONTENT_DISPOSITION_FILENAME.length());
		// URL解码
		fileName = UrlUtils.decode(fileName);
		// 删除：等号前面内容
		index = fileName.indexOf('=');
		if(index != -1) {
			fileName = fileName.substring(index + 1);
		}
		// 删除：分号后面内容
		index = fileName.indexOf(';');
		if(index != -1) {
			fileName = fileName.substring(0, index);
		}
		// 删除：空格
		fileName = fileName.trim();
		// 删除：引号前后内容
		if(fileName.startsWith("\'") && fileName.endsWith("\'")) {
			fileName = fileName.substring(1, fileName.length() - 1);
		}
		if(fileName.startsWith("\"") && fileName.endsWith("\"")) {
			fileName = fileName.substring(1, fileName.length() - 1);
		}
		// 删除：参数
		index = fileName.indexOf('?');
		if(index != -1) {
			fileName = fileName.substring(0, index);
		}
		// 删除：空格
		return fileName.trim();
	}
	
	/**
	 * <p>文件名称解码</p>
	 * 
	 * @param fileName 文件名称
	 * 
	 * @return 文件名称
	 */
	private String charsetFileName(String fileName) {
		if(StringUtils.isEmpty(fileName)) {
			return fileName;
		}
		final var gbkEncoder = Charset.forName(SystemConfig.CHARSET_GBK).newEncoder();
		// 只是进行URL编码
		if(gbkEncoder.canEncode(fileName)) {
			return fileName;
		}
		// HttpClient工具汉字ISO-8859-1字符转为char没有去掉符号（& 0xFF）
		final char[] chars = fileName.toCharArray();
		for (int index = 0; index < chars.length; index++) {
			// 去掉符号
			chars[index] = (char) (chars[index] & 0x00FF);
		}
		fileName = new String(chars);
		// 处理ISO-8859-1编码：优先验证UTF-8，因为UTF-8字符不完全属于GBK。
		// UTF-8：GBK可以编码
		final String fileNameUTF8 = StringUtils.charsetFrom(fileName, SystemConfig.CHARSET_ISO_8859_1);
		if(gbkEncoder.canEncode(fileNameUTF8)) {
			return fileNameUTF8;
		}
		// GBK：GBK可以编码
		final String fileNameGBK = StringUtils.charset(fileName, SystemConfig.CHARSET_ISO_8859_1, SystemConfig.CHARSET_GBK);
		if(gbkEncoder.canEncode(fileNameGBK)) {
			return fileNameGBK;
		}
		// 其他编码直接返回
		return fileName;
	}
	
	/**
	 * <p>获取文件大小</p>
	 * <p>Content-Length：102400</p>
	 * 
	 * @return 文件大小
	 */
	public long fileSize() {
		long size = 0L;
		final String value = this.header(HEADER_CONTENT_LENGTH);
		if(StringUtils.isNumeric(value)) {
			size = Long.parseLong(value);
		}
		return size;
	}
	
	/**
	 * <p>判断是否支持断点续传</p>
	 * <p>Accept-Ranges=bytes</p>
	 * <p>Content-Range=bytes 0-100/100</p>
	 * 
	 * @return 是否支持断点续传
	 */
	public boolean range() {
		boolean range = false;
		final String acceptRanges = this.header(HEADER_ACCEPT_RANGES);
		final String contentRange = this.header(HEADER_CONTENT_RANGE);
		if(acceptRanges != null) {
			range = HEADER_RANGE_BYTES.equalsIgnoreCase(acceptRanges);
		}
		if(contentRange != null) {
			range = true;
		}
		return range;
	}

	/**
	 * <p>获取开始下载位置</p>
	 * <p>Content-Range=bytes 0-100/100</p>
	 * 
	 * @return 开始下载位置
	 */
	public long beginRange() {
		long range = 0L;
		final String contentRange = this.header(HEADER_CONTENT_RANGE);
		if(contentRange != null) {
			final int endIndex = contentRange.lastIndexOf('-');
			final String value = contentRange.substring(HEADER_RANGE_BYTES.length(), endIndex).trim();
			if(StringUtils.isNumeric(value)) {
				range = Long.parseLong(value);
			}
		}
		return range;
	}
	
	/**
	 * <p>验证开始下载位置是否正确</p>
	 * 
	 * @param size 已下载大小
	 * 
	 * @return 是否正确
	 */
	public boolean verifyBeginRange(long size) {
		final long begin = this.beginRange();
		if(begin == size) {
			return true;
		}
		// TODO：多行文本
		LOGGER.warn(
			"HTTP下载错误（已下载大小和开始下载位置不符），开始位置：{}，响应位置：{}，HTTP响应头部：{}",
			size, begin, this.headers
		);
		return false;
	}

}
