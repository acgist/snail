package com.acgist.snail.net;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class DownloadExceptionTest extends Performance {

	@Test
	void testDownloadException() {
		DownloadException exception = assertThrows(DownloadException.class, () -> {throw new DownloadException();});
		this.log(exception.getMessage());
		exception = assertThrows(DownloadException.class, () -> {throw new DownloadException("测试下载异常");});
		this.log(exception.getMessage());
		exception = assertThrows(DownloadException.class, () -> {throw new DownloadException(new NullPointerException());});
		this.log(exception.getMessage());
		exception = assertThrows(DownloadException.class, () -> {throw new DownloadException("测试下载异常", new NullPointerException());});
		this.log(exception.getMessage());
	}
	
}
