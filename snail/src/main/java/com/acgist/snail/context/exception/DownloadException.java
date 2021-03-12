package com.acgist.snail.context.exception;

/**
 * <p>下载异常</p>
 * 
 * @author acgist
 */
public class DownloadException extends Exception {

	private static final long serialVersionUID = 1L;

	public DownloadException() {
		super("下载异常");
	}

	/**
	 * <p>下载异常</p>
	 * 
	 * @param message 错误信息
	 */
	public DownloadException(String message) {
		super(message);
	}

	/**
	 * <p>下载异常</p>
	 * 
	 * @param cause 原始异常
	 */
	public DownloadException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * <p>下载异常</p>
	 * 
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
