package com.acgist.snail.exception;

import com.acgist.snail.utils.StringUtils;

/**
 * <p>数据库异常</p>
 * <p>用途：{@linkplain com.acgist.snail.repository 数据库}</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class RepositoryException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * <p>判断参数是否为空</p>
	 * <p>如果不为空抛出{@linkplain RepositoryException 异常}</p>
	 * 
	 * @param object 参数
	 */
	public static final void requireNull(Object object) {
		if(object != null) {
			throw new RepositoryException("参数错误：" + object);
		}
	}
	
	/**
	 * <p>判断参数是否不为空</p>
	 * <p>如果为空抛出{@linkplain RepositoryException 异常}</p>
	 * 
	 * @param object 参数
	 */
	public static final void requireNotNull(Object object) {
		if(object == null) {
			throw new RepositoryException("参数错误：" + object);
		}
	}
	
	/**
	 * <p>判断参数是否匹配正则表达式</p>
	 * <p>如果不匹配抛出{@linkplain RepositoryException 异常}</p>
	 * 
	 * @param value 参数
	 * @param regex 正则表达式
	 */
	public static final void requireMatch(String value, String regex) {
		if(!StringUtils.regex(value, regex, true)) {
			throw new RepositoryException("参数错误：" + value);
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
		super(cause.getMessage(), cause);
	}
	
	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public RepositoryException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
