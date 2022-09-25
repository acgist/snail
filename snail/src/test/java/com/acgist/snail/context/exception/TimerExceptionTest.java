package com.acgist.snail.context.exception;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class TimerExceptionTest extends Performance {

	@Test
	void testTimerException() {
		TimerException exception = assertThrows(TimerException.class, () -> {throw new TimerException();});
		this.log(exception.getMessage());
		exception = assertThrows(TimerException.class, () -> {throw new TimerException("测试网络包大小异常");});
		this.log(exception.getMessage());
		final var nullPointerException = new NullPointerException();
		exception = assertThrows(TimerException.class, () -> {throw new TimerException(nullPointerException);});
		this.log(exception.getMessage());
		exception = assertThrows(TimerException.class, () -> {throw new TimerException("测试网络包大小异常", nullPointerException);});
		this.log(exception.getMessage());
		exception = assertThrows(TimerException.class, () -> {TimerException.verify(-1L);});
		this.log(exception.getMessage());
		assertDoesNotThrow(() -> {TimerException.verify(Short.MAX_VALUE);});
	}
	
}
