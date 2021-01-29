package com.acgist.snail.protocol.hls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.bean.M3u8.Type;
import com.acgist.snail.utils.Performance;

public class M3u8BuilderTest extends Performance {

	@Test
	public void testFile() throws DownloadException, IOException, NetException {
		final var m3u8 = M3u8Builder.newInstance(new File("E://snail/m3u8/file.m3u8"), "https://www.acgist.com/file/acgist").build();
		assertEquals(Type.FILE, m3u8.getType());
		m3u8.getLinks().forEach(this::log);
	}
	
	@Test
	public void testM3U8() throws NetException, DownloadException {
		final var m3u8 = M3u8Builder.newInstance(new File("E://snail/m3u8/m3u8.m3u8"), "https://www.acgist.com/m3u8/acgist").build();
		assertEquals(Type.M3U8, m3u8.getType());
		this.log(m3u8.maxRateLink());
		m3u8.getLinks().forEach(this::log);
	}
	
	@Test
	public void testStream() throws NetException, DownloadException {
		final var m3u8 = M3u8Builder.newInstance(new File("E://snail/m3u8/stream.m3u8"), "https://www.acgist.com/stream/acgist").build();
		assertEquals(Type.STREAM, m3u8.getType());
		m3u8.getLinks().forEach(this::log);
	}
	
	@Test
	public void testCipher() throws NetException, DownloadException {
		assertThrows(NetException.class, () -> {
//			M3u8Builder.newInstance(new File("E://snail/m3u8/cipher.m3u8"), "https://www.acgist.com/cipher/acgist").build();
			M3u8Builder.newInstance(new File("E://snail/m3u8/sequence.m3u8"), "https://www.acgist.com/cipher/acgist").build();
		});
	}
	
}
