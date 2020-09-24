package com.acgist.snail.exception;

/**
 * <p>下载异常</p>
 * <p>用途：{@linkplain com.acgist.snail.downloader 下载器}、{@linkplain com.acgist.snail.protocol 下载协议}</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DownloadException extends Exception {

	private static final long serialVersionUID = 1L;

	public DownloadException() {
		super("下载异常");
	}

	/**
	 * @param message 错误信息
	 */
	public DownloadException(String message) {
		super(message);
	}

	/**
	 * @param cause 原始异常
	 */
	public DownloadException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
	
	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
