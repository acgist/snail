package com.acgist.snail;

import org.junit.jupiter.api.Test;

import com.acgist.snail.Snail.SnailBuilder;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

public class SnailTest extends Performance {

	@Test
	public void testBuilder() throws DownloadException {
		final Snail snail = SnailBuilder.newBuilder().http().build();
//		final Snail snail = SnailBuilder.newBuilder().allProtocol().build();
		final var downloader = snail.download("https://www.acgist.com");
		while(downloader.taskSession().download()) {
			ThreadUtils.sleep(1000);
		}
	}
	
}
