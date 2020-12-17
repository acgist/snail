package com.acgist.snail.context.exception;

import com.acgist.snail.utils.StringUtils;

/**
 * <p>数据库异常</p>
 * 
 * @author acgist
 */
public class RepositoryException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * <p>验证参数必须为空</p>
	 * 
	 * @param object 参数
	 */
	public static final void requireNull(Object object) {
		if(object != null) {
			throw new RepositoryException("参数必须为空：" + object);
		}
	}
	
	/**
	 * <p>验证参数不能为空</p>
	 * 
	 * @param object 参数
	 */
	public static final void requireNotNull(Object object) {
		if(object == null) {
			throw new RepositoryException("参数不能为空：" + object);
		}
	}
	
	/**
	 * <p>验证参数必须匹配正则表达式</p>
	 * 
	 * @param value 参数
	 * @param regex 正则表达式
	 */
	public static final void requireMatchRegex(String value, String regex) {
		if(!StringUtils.regex(value, regex, true)) {
			throw new RepositoryException(String.format("参数必须匹配正则表达式：%s-%s", value, regex));
		}
	}
	
	public RepositoryException() {
		super("数据库异常");
	}

	/**
	 * @param message 错误信息
	 */
	public RepositoryException(String message) {
		super(message);
	}

	/**
	 * @param cause 原始异常
	 */
	public RepositoryException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public RepositoryException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
