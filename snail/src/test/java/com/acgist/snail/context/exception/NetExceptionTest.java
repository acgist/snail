package com.acgist.snail.context.exception;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class NetExceptionTest extends Performance {

	@Test
	void testNetException() {
		NetException exception = assertThrows(NetException.class, () -> {throw new NetException();});
		this.log(exception.getMessage());
		exception = assertThrows(NetException.class, () -> {throw new NetException("测试网络异常");});
		this.log(exception.getMessage());
		exception = assertThrows(NetException.class, () -> {throw new NetException(new NullPointerException());});
		this.log(exception.getMessage());
		exception = assertThrows(NetException.class, () -> {throw new NetException("测试网络异常", new NullPointerException());});
		this.log(exception.getMessage());
	}
	
}
