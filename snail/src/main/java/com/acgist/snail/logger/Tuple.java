package com.acgist.snail.logger;

import java.util.Objects;

/**
 * <p>日志单元</p>
 * <p>每条日志对应一个日志单元</p>
 * 
 * @author acgist
 */
public final class Tuple {

	/**
	 * <p>格式化符号</p>
	 */
	public static final String FORMAT_CODE = "{}";
	/**
	 * <p>格式化符号长度</p>
	 */
	public static final int FORMAT_CODE_LENGTH = FORMAT_CODE.length();
	
	/**
	 * <p>原始信息</p>
	 */
	private final String message;
	/**
	 * <p>格式化信息</p>
	 */
	private final String[] format;
	/**
	 * <p>后缀文本</p>
	 */
	private final String suffix;
	
	/**
	 * @param message 原始日志
	 */
	public Tuple(String message) {
		this.message = message;
		int pos = 0;
		int last = 0;
		int index = 0;
		String[] format = new String[Byte.SIZE];
		final int messageLength = message.length();
		while ((pos = message.indexOf(FORMAT_CODE, last)) >= 0) {
			format[index++] = message.substring(last, pos);
			last = pos + FORMAT_CODE_LENGTH;
			if(index >= format.length) {
				final String[] resizeFormat = new String[format.length + Byte.SIZE];
				System.arraycopy(format, 0, resizeFormat, 0, format.length);
				format = resizeFormat;
			}
		}
		if(last != 0 && last < messageLength) {
			this.suffix = message.substring(last);
		} else {
			this.suffix = null;
		}
		this.format = new String[index];
		System.arraycopy(format, 0, this.format, 0, index);
	}
	
	/**
	 * <p>格式化日志</p>
	 * 
	 * @param objects 参数列表
	 * 
	 * @return 日志信息
	 */
	public final String format(Object ... objects) {
		final StringBuilder builder = new StringBuilder();
		return this.format(builder, objects).toString();
	}
	
	/**
	 * <p>格式化日志</p>
	 * 
	 * @param builder 日志Builder
	 * @param objects 参数列表
	 * 
	 * @return 日志信息
	 */
	public final StringBuilder format(StringBuilder builder, Object ... objects) {
		Objects.requireNonNull(builder, "没有日志Builder");
		if(objects == null || objects.length == 0 || this.format.length == 0) {
			builder.append(this.message);
			return builder;
		}
		final int objectLength = objects.length;
		final int formatLength = this.format.length;
		for (int index = 0; index < formatLength; index++) {
			builder.append(this.format[index]);
			if(index < objectLength) {
				builder.append(objects[index]);
			} else {
				builder.append(FORMAT_CODE);
			}
		}
		if(this.suffix != null) {
			builder.append(this.suffix);
		}
		// 注意：直接忽略后面多余参数
		return builder;
	}
	
	/**
	 * <p>获取异常参数</p>
	 * 
	 * @param objects 参数列表
	 * 
	 * @return 异常
	 */
	public final Throwable throwable(Object ... objects) {
		if(objects == null || objects.length == 0) {
			return null;
		}
		final Object object = objects[objects.length - 1];
		if(object instanceof Throwable t) {
			return t;
		}
		return null;
	}
	
}
