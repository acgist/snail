package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class EntityExceptionTest extends Performance {

	@Test
	void testEntityException() {
		EntityException exception = assertThrows(EntityException.class, () -> {throw new EntityException();});
		this.log(exception.getMessage());
		exception = assertThrows(EntityException.class, () -> {throw new EntityException("测试实体异常");});
		this.log(exception.getMessage());
		final var nullPointerException = new NullPointerException();
		exception = assertThrows(EntityException.class, () -> {throw new EntityException(nullPointerException);});
		this.log(exception.getMessage());
		exception = assertThrows(EntityException.class, () -> {throw new EntityException("测试实体异常", nullPointerException);});
		this.log(exception.getMessage());
		exception = assertThrows(EntityException.class, () -> {EntityException.requireNull(nullPointerException);});
		this.log(exception.getMessage());
		exception = assertThrows(EntityException.class, () -> {EntityException.requireNotNull(null);});
		this.log(exception.getMessage());
		assertDoesNotThrow(() -> {EntityException.requireNull(null);});
		assertDoesNotThrow(() -> {EntityException.requireNotNull(new Object());});
	}
	
}
