package com.acgist.snail.context.wrapper;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>HTTP头部信息包装器</p>
 * 
 * @author acgist
 */
public final class HttpHeaderWrapper extends HeaderWrapper {
	
	/**
	 * <p>HTTP客户端信息：{@value}</p>
	 */
	public static final String HEADER_USER_AGENT = "User-Agent";
	/**
	 * <p>MIME类型：{@value}</p>
	 */
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	/**
	 * <p>下载大小：{@value}</p>
	 */
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	/**
	 * <p>下载描述：{@value}</p>
	 */
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	/**
	 * <p>请求下载范围：{@value}</p>
	 * <p>返回数据范围：Content-Range=bytes 0-100/100</p>
	 * 
	 * @see #HEADER_RANGE
	 */
	public static final String HEADER_CONTENT_RANGE = "Content-Range";
	/**
	 * <p>接收范围请求：{@value}</p>
	 * <p>返回全部数据：Accept-Ranges=bytes</p>
	 * 
	 * @see #HEADER_RANGE
	 */
	public static final String HEADER_ACCEPT_RANGES = "Accept-Ranges";
	/**
	 * <p>范围请求：{@value}</p>
	 * <table border="1">
	 * 	<caption>HTTP协议断点续传设置</caption>
	 * 	<tr>
	 * 		<td>Range: bytes=0-499</td>
	 * 		<td>范围：0-499</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Range: bytes=500-999</td>
	 * 		<td>范围：500-999</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Range: bytes=-500</td>
	 * 		<td>最后500字节</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Range: bytes=500-</td>
	 * 		<td>500字节开始到结束</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Range: bytes=0-0,-1</td>
	 * 		<td>第一个字节和最后一个字节</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Range: bytes=500-600,601-999</td>
	 * 		<td>同时指定多个范围</td>
	 * 	</tr>
	 * </table>
	 */
	public static final String HEADER_RANGE = "Range";
	/**
	 * <p>接收范围请求：{@value}</p>
	 * 
	 * @see #HEADER_CONTENT_RANGE
	 * @see #HEADER_ACCEPT_RANGES
	 */
	public static final String HEADER_VALUE_BYTES = "bytes";
	/**
	 * <p>文件名称：{@value}</p>
	 * 
	 * @see #HEADER_CONTENT_DISPOSITION
	 */
	public static final String HEADER_VALUE_FILENAME = "filename";
	
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
	 * <p>下载文件名称：如果不存在返回默认文件名称</p>
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
		final int index = fileNameLower.indexOf(HEADER_VALUE_FILENAME);
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
	 * <p>文件名称提取</p>
	 * 
	 * @param index 开始位置
	 * @param fileName 文件名称
	 * 
	 * @return 文件名称
	 */
	private String extractFileName(int index, String fileName) {
		// 删除：filename前面内容
		fileName = fileName.substring(index + HEADER_VALUE_FILENAME.length());
		// URL解码
		fileName = UrlUtils.decode(fileName);
		// 删除：等号前面内容
		index = fileName.indexOf(SymbolConfig.Symbol.EQUALS.toChar());
		if(index != -1) {
			fileName = fileName.substring(index + 1);
		}
		// 删除：分号后面内容
		index = fileName.indexOf(SymbolConfig.Symbol.SEMICOLON.toChar());
		if(index != -1) {
			fileName = fileName.substring(0, index);
		}
		// 删除：空格
		fileName = fileName.strip();
		// 删除：单引号
		final String singleQuote = SymbolConfig.Symbol.SINGLE_QUOTE.toString();
		if(fileName.startsWith(singleQuote) && fileName.endsWith(singleQuote)) {
			fileName = fileName.substring(1, fileName.length() - 1);
		}
		// 删除：双引号
		final String doubleQuote = SymbolConfig.Symbol.DOUBLE_QUOTE.toString();
		if(fileName.startsWith(doubleQuote) && fileName.endsWith(doubleQuote)) {
			fileName = fileName.substring(1, fileName.length() - 1);
		}
		// 删除：参数
		index = fileName.indexOf(SymbolConfig.Symbol.QUESTION.toChar());
		if(index != -1) {
			fileName = fileName.substring(0, index);
		}
		// 删除：空格
		return fileName.strip();
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
		// JDK内置HttpClient工具汉字ISO-8859-1字符转为char没有去掉符号（& 0xFF）
		final char[] chars = fileName.toCharArray();
		for (int index = 0; index < chars.length; index++) {
			// 去掉符号
			chars[index] = (char) (chars[index] & 0x00FF);
		}
		fileName = new String(chars);
		// UTF-8：GBK可以编码
		final String fileNameUtf8 = StringUtils.charsetFrom(fileName, SystemConfig.CHARSET_ISO_8859_1);
		if(gbkEncoder.canEncode(fileNameUtf8)) {
			return fileNameUtf8;
		}
		// GBK：GBK可以编码
		final String fileNameGbk = StringUtils.charset(fileName, SystemConfig.CHARSET_ISO_8859_1, SystemConfig.CHARSET_GBK);
		if(gbkEncoder.canEncode(fileNameGbk)) {
			return fileNameGbk;
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
	 * 
	 * @return 是否支持断点续传
	 * 
	 * @see #HEADER_CONTENT_RANGE
	 * @see #HEADER_ACCEPT_RANGES
	 */
	public boolean range() {
		if(HEADER_VALUE_BYTES.equalsIgnoreCase(this.header(HEADER_ACCEPT_RANGES))) {
			return true;
		}
		return this.header(HEADER_CONTENT_RANGE) != null;
	}

}
