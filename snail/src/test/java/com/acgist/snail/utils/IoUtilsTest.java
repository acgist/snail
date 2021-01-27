package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class IoUtilsTest extends Performance {

	@Test
	public void testClose() throws IOException {
		final InputStream inputStream = InputStream.nullInputStream();
		IoUtils.close(inputStream);
		assertThrows(IOException.class, () -> inputStream.available());
	}
	
}
