package com.acgist.snail;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.Snail.SnailBuilder;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

public class SnailTest extends Performance {

	@Test
	public void testSnail() {
		final var exception = assertThrows(DownloadException.class, () -> SnailBuilder.getInstance().buildSync().download("https://www.acgist.com"));
		this.log(exception);
	}
	
	@Test
	public void testBuilder() throws DownloadException {
		final Snail snail = SnailBuilder.getInstance()
			.enableHttp()
			.buildSync();
		final var downloader = snail.download("https://www.acgist.com");
		while(downloader.taskSession().download()) {
			ThreadUtils.sleep(1000);
		}
	}
	
}
