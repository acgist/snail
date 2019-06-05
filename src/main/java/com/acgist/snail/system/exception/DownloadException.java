package com.acgist.snail.system.exception;

/**
 * <p>下载异常</p>
 * <p>用于{@linkplain com.acgist.snail.downloader 下载器}和{@linkplain com.acgist.snail.protocol 下载协议}中出现的异常。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DownloadException extends Exception {

	private static final long serialVersionUID = 1L;

	public DownloadException() {
		super("下载异常");
	}

	public DownloadException(String message) {
		super(message);
	}

	public DownloadException(Throwable cause) {
		super(cause);
	}
	
	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
