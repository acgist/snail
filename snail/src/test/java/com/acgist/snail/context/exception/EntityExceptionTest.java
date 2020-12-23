package com.acgist.snail.context.exception;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class EntityExceptionTest extends Performance {

	@Test
	public void testEntityException() {
		EntityException exception = assertThrows(EntityException.class, () -> {throw new EntityException();});
		this.log(exception.getMessage());
		exception = assertThrows(EntityException.class, () -> {throw new EntityException("网络包大小异常");});
		this.log(exception.getMessage());
		exception = assertThrows(EntityException.class, () -> {throw new EntityException(new NullPointerException());});
		this.log(exception.getMessage());
		exception = assertThrows(EntityException.class, () -> {throw new EntityException("网络包大小异常", new NullPointerException());});
		this.log(exception.getMessage());
		exception = assertThrows(EntityException.class, () -> {EntityException.requireNull(new Object());});
		this.log(exception.getMessage());
		exception = assertThrows(EntityException.class, () -> {EntityException.requireNotNull(null);});
		this.log(exception.getMessage());
		assertDoesNotThrow(() -> {EntityException.requireNull(null);});
		assertDoesNotThrow(() -> {EntityException.requireNotNull(new Object());});
	}
	
}
