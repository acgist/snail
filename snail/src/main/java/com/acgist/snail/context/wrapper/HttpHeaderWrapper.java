package com.acgist.snail.context.wrapper;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * HTTP头部信息包装器
 * 
 * @author acgist
 */
public final class HttpHeaderWrapper extends HeaderWrapper {
	
	/**
	 * 范围请求
	 * HTTP协议断点续传设置
	 * 
	 * Range: bytes=0-499              范围：0-499
	 * Range: bytes=500-999            范围：500-999
	 * Range: bytes=-500               最后500字节
	 * Range: bytes=500-               500字节开始到结束
	 * Range: bytes=0-0,-1             第一个字节和最后一个字节
	 * Range: bytes=500-600,601-999    同时指定多个范围
	 */
	public static final String HEADER_RANGE = "Range";
	/**
	 * HTTP客户端信息
	 */
	public static final String HEADER_USER_AGENT = "User-Agent";
	/**
	 * 接收范围请求
	 * 返回全部数据：Accept-Ranges=bytes
	 * 
	 * @see #HEADER_RANGE
	 */
	public static final String HEADER_ACCEPT_RANGES = "Accept-Ranges";
	/**
	 * MIME类型
	 */
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	/**
	 * 请求下载范围
	 * 返回数据范围：Content-Range=bytes 0-100/100
	 * 
	 * @see #HEADER_RANGE
	 */
	public static final String HEADER_CONTENT_RANGE = "Content-Range";
	/**
	 * 下载大小
	 */
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	/**
	 * 下载描述
	 */
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	/**
	 * 接收范围请求
	 * 
	 * @see #HEADER_ACCEPT_RANGES
	 * @see #HEADER_CONTENT_RANGE
	 */
	public static final String HEADER_VALUE_BYTES = "bytes";
	/**
	 * 文件名称
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
	 * @param headers 头部信息
	 * 
	 * @return {@link HttpHeaderWrapper}
	 */
	public static final HttpHeaderWrapper newInstance(Map<String, List<String>> headers) {
		return new HttpHeaderWrapper(headers);
	}

	/**
	 * 下载文件名称：如果不存在返回默认文件名称
	 * 
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
	 * 提取文件名称
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
		if(index >= 0) {
			fileName = fileName.substring(index + 1);
		}
		// 删除：分号后面内容
		index = fileName.indexOf(SymbolConfig.Symbol.SEMICOLON.toChar());
		if(index >= 0) {
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
		if(index >= 0) {
			fileName = fileName.substring(0, index);
		}
		// 删除：空格
		return fileName.strip();
	}
	
	/**
	 * 文件名称解码
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
	 * Content-Length：102400
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
	 * 判断是否支持断点续传
	 * 
	 * @return 是否支持断点续传
	 * 
	 * @see #HEADER_ACCEPT_RANGES
	 * @see #HEADER_CONTENT_RANGE
	 */
	public boolean range() {
		if(HEADER_VALUE_BYTES.equalsIgnoreCase(this.header(HEADER_ACCEPT_RANGES))) {
			return true;
		}
		return this.header(HEADER_CONTENT_RANGE) != null;
	}

}
