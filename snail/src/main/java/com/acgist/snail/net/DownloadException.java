package com.acgist.snail.net;

/**
 * 下载异常
 * 
 * @author acgist
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
        super(cause);
    }
    
    /**
     * @param message 错误信息
     * @param cause   原始异常
     */
    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
