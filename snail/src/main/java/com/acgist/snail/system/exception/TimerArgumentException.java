package com.acgist.snail.system.exception;

/**
 * <p>定时任务时间异常</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public class TimerArgumentException extends ArgumentException {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>验证时间周期</p>
	 * <p>如果时间周期格式错误抛出异常：{@link TimerArgumentException}</p>
	 * 
	 * @param time 时间周期
	 */
	public static final void verify(long time) {
		if(time < 0) {
			throw new TimerArgumentException(time);
		}
	}
	
	public TimerArgumentException() {
		super("定时任务时间异常");
	}
	
	/**
	 * @param time 时间（周期）
	 */
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
