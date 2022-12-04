package com.acgist.snail.context.exception;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ScheduledException;
import com.acgist.snail.utils.Performance;

class ScheduledExceptionTest extends Performance {

	@Test
	void testScheduledException() {
		ScheduledException exception = assertThrows(ScheduledException.class, () -> {throw new ScheduledException();});
		this.log(exception.getMessage());
		exception = assertThrows(ScheduledException.class, () -> {throw new ScheduledException("测试网络包大小异常");});
		this.log(exception.getMessage());
		final var nullPointerException = new NullPointerException();
		exception = assertThrows(ScheduledException.class, () -> {throw new ScheduledException(nullPointerException);});
		this.log(exception.getMessage());
		exception = assertThrows(ScheduledException.class, () -> {throw new ScheduledException("测试网络包大小异常", nullPointerException);});
		this.log(exception.getMessage());
		exception = assertThrows(ScheduledException.class, () -> {ScheduledException.verify(-1L);});
		this.log(exception.getMessage());
		assertDoesNotThrow(() -> {ScheduledException.verify(Short.MAX_VALUE);});
	}
	
}
