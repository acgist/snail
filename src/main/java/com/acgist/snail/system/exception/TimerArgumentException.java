package com.acgist.snail.system.exception;

/**
 * <p>定时任务时间异常</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public class TimerArgumentException extends ArgumentException {

	private static final long serialVersionUID = 1L;

	public TimerArgumentException() {
		super("定时任务时间异常");
	}
	
	public TimerArgumentException(long time) {
		super("定时任务时间错误：" + time);
	}

	public TimerArgumentException(String message) {
		super(message);
	}

	public TimerArgumentException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	public TimerArgumentException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
