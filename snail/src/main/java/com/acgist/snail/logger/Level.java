package com.acgist.snail.logger;

/**
 * <p>日志级别</p>
 * 
 * @author acgist
 */
public enum Level {

	/**
	 * <p>DEBUG</p>
	 */
	DEBUG(100, "DEBUG"),
	/**
	 * <p>INFO</p>
	 */
	INFO(200, "INFO"),
	/**
	 * <p>WARN</p>
	 */
	WARN(300, "WARN"),
	/**
	 * <p>ERROR</p>
	 */
	ERROR(400, "ERROR"),
	/**
	 * <p>OFF</p>
	 */
	OFF(999, "OFF");
	
	/**
	 * <p>级别</p>
	 */
	private final int value;
	/**
	 * <p>名称</p>
	 */
	private final String name;
	
	private Level(int value, String name) {
		this.value = value;
		this.name = name;
	}
	
	/**
	 * <p>获取日志级别</p>
	 * 
	 * @return 级别
	 */
	public final int value() {
		return this.value;
	}

	/**
	 * <p>通过名称获取日志级别</p>
	 * 
	 * @param name 名称
	 * 
	 * @return 级别
	 */
	public static final Level of(String name) {
		final Level[] values = Level.values();
		for (Level level : values) {
			if(level.name.equalsIgnoreCase(name)) {
				return level;
			}
		}
		return Level.INFO;
	}
	
}
