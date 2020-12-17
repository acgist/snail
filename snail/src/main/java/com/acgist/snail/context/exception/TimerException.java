package com.acgist.snail.context.exception;

/**
 * <p>定时任务时间周期异常</p>
 * 
 * @author acgist
 */
public final class TimerException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>验证定时任务时间周期</p>
	 * 
	 * @param time 时间周期
	 */
	public static final void verify(long time) {
		if(time < 0) {
			throw new TimerException(time);
		}
	}
	
	public TimerException() {
		super("定时任务时间周期异常");
	}
	
	/**
	 * @param time 时间周期
	 */
	public TimerException(long time) {
		super("定时任务时间周期错误：" + time);
	}

	/**
	 * @param message 错误信息
	 */
	public TimerException(String message) {
		super(message);
	}

	/**
	 * @param cause 原始异常
	 */
	public TimerException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public TimerException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
