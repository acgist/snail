package com.acgist.snail.system.exception;

/**
 * <p>定时任务时间异常</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public class TimerException extends ArgumentException {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>验证时间周期</p>
	 * <p>如果时间周期格式错误抛出异常：{@link TimerException}</p>
	 * 
	 * @param time 时间周期
	 */
	public static final void verify(long time) {
		if(time < 0) {
			throw new TimerException(time);
		}
	}
	
	public TimerException() {
		super("定时任务时间异常");
	}
	
	/**
	 * @param time 时间（周期）
	 */
	public TimerException(long time) {
		super("定时任务时间错误：" + time);
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
		super(cause.getMessage(), cause);
	}

	/**
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public TimerException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
