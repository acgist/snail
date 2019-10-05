package com.acgist.snail.system.exception;

/**
 * GUI异常
 * 
 * @author acgist
 * @since 1.0.0
 */
public class GuiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GuiException() {
		super("GUI异常");
	}

	public GuiException(String message) {
		super(message);
	}

	public GuiException(Throwable cause) {
		super(cause);
	}

	public GuiException(String message, Throwable cause) {
		super(message, cause);
	}

}
