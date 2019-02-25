package com.acgist.snail.module.exception;

/**
 * 下载异常
 */
public class DownloadException extends Exception {

	private static final long serialVersionUID = 1L;

	public DownloadException() {
		super();
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
