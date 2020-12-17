package com.acgist.snail.context.exception;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class RepositoryExceptionTest extends Performance {

	@Test
	public void testRepositoryException() {
		RepositoryException exception = assertThrows(RepositoryException.class, () -> {throw new RepositoryException();});
		this.log(exception.getMessage());
		exception = assertThrows(RepositoryException.class, () -> {throw new RepositoryException("网络包大小异常");});
		this.log(exception.getMessage());
		exception = assertThrows(RepositoryException.class, () -> {throw new RepositoryException(new NullPointerException());});
		this.log(exception.getMessage());
		exception = assertThrows(RepositoryException.class, () -> {throw new RepositoryException("网络包大小异常", new NullPointerException());});
		this.log(exception.getMessage());
		exception = assertThrows(RepositoryException.class, () -> {RepositoryException.requireNull(new Object());});
		this.log(exception.getMessage());
		exception = assertThrows(RepositoryException.class, () -> {RepositoryException.requireNotNull(null);});
		this.log(exception.getMessage());
		exception = assertThrows(RepositoryException.class, () -> {RepositoryException.requireMatchRegex("acgist", "\\d+");;});
		this.log(exception.getMessage());
		assertDoesNotThrow(() -> {RepositoryException.requireNull(null);});
		assertDoesNotThrow(() -> {RepositoryException.requireNotNull(new Object());});
		assertDoesNotThrow(() -> {RepositoryException.requireMatchRegex("1234", "\\d+");;});
	}
	
}
